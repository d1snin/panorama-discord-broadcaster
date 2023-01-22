/*
 * Copyright 2023 Mikhail Titov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.d1s.neuralmeduzadiscordbroadcaster

import dev.d1s.neuralmeduzadiscordbroadcaster.config.ApplicationConfig
import dev.d1s.neuralmeduzadiscordbroadcaster.database.Key
import dev.d1s.neuralmeduzadiscordbroadcaster.database.RedisClientFactory
import dev.d1s.neuralmeduzadiscordbroadcaster.di.Qualifier
import dev.d1s.neuralmeduzadiscordbroadcaster.util.Url
import dev.d1s.neuralmeduzadiscordbroadcaster.util.setAndPersist
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

typealias PostUrlChangeHandler = suspend (Url) -> Unit

interface LatestPostUrlChangeListener {

    suspend fun onPostUrlChange(handler: PostUrlChangeHandler): Job
}

class DefaultLatestPostUrlChangeListener : LatestPostUrlChangeListener, KoinComponent {

    private val redisClientFactory by inject<RedisClientFactory>()

    private val mainPageFetcher by inject<Fetcher<FetchedMainPage>>(Qualifier.MainPageFetcher)

    private val mainPageParser by inject<Parser<FetchedMainPage, MainPage>>(Qualifier.MainPageParser)

    private val config by inject<ApplicationConfig>()

    private val redis by lazy {
        redisClientFactory.redis
    }

    private var job: Job? = null

    private val listeningScope = CoroutineScope(Dispatchers.Default)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        log.e {
            "Failure while listening for changes: ${throwable.message}"
        }

        log.d {
            throwable.stackTraceToString()
        }
    }

    private val log = logging()

    override suspend fun onPostUrlChange(handler: PostUrlChangeHandler): Job {
        job?.let {
            error("Job has already been started.")
        }

        log.i {
            "Launching listener job..."
        }

        val launchedJob = listeningScope.launch(exceptionHandler) {
            listenToChanges(handler)
        }

        job = launchedJob

        return launchedJob
    }

    private suspend fun listenToChanges(handler: PostUrlChangeHandler) {
        while (true) {
            log.d {
                "Checking for changes..."
            }

            val fetchedMainPage = mainPageFetcher.fetch()
            val mainPage = mainPageParser.parse(fetchedMainPage)

            val currentPostUrl = mainPage.latestPostUrl
            val savedPostUrl = getLatestPostUrl()

            if (currentPostUrl != savedPostUrl) {
                log.i {
                    "Change detected..."
                }

                setLatestPostUrl(currentPostUrl)

                handler(currentPostUrl)

                log.i {
                    "Handler proceeded."
                }
            } else {
                log.d {
                    "No changes."
                }
            }

            delay(config.fetcher.delay)
        }
    }

    private suspend fun getLatestPostUrl() = redis.get(Key.LATEST_POST_URL)

    private suspend fun setLatestPostUrl(url: String) = redis.setAndPersist(Key.LATEST_POST_URL, url)
}