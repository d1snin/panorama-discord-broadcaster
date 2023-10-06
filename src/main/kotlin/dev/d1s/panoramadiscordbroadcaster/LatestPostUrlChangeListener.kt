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

package dev.d1s.panoramadiscordbroadcaster

import dev.d1s.panoramadiscordbroadcaster.config.ApplicationConfig
import dev.d1s.panoramadiscordbroadcaster.database.Key
import dev.d1s.panoramadiscordbroadcaster.database.RedisClientFactory
import dev.d1s.panoramadiscordbroadcaster.di.Qualifier
import dev.d1s.panoramadiscordbroadcaster.util.Url
import dev.d1s.panoramadiscordbroadcaster.util.setAndPersist
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

    private val log = logging()

    override suspend fun onPostUrlChange(handler: PostUrlChangeHandler): Job {
        job?.let {
            error("Job has already been started.")
        }

        log.i {
            "Launching listener job..."
        }

        val launchedJob = listeningScope.launch {
            try {
                listenForChangesCatching(handler)
            } catch (error: Throwable) {
                log.e(error) {
                    "An error occurred."
                }
            }
        }

        job = launchedJob

        return launchedJob
    }

    private suspend fun listenForChangesCatching(handler: PostUrlChangeHandler) {
        try {
            listenForChanges(handler)
        } catch (throwable: Throwable) {
            log.e(throwable) {
                "Failure while listening for changes: ${throwable.message}. Entering the loop again..."

                listenForChanges(handler)
            }
        }
    }

    private suspend fun listenForChanges(handler: PostUrlChangeHandler) {
        while (true) {
            log.d {
                "Checking for changes..."
            }

            val fetchedMainPage = mainPageFetcher.fetchCatching()
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