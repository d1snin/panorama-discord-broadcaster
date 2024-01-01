/*
 * Copyright 2023-2024 Mikhail Titov
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

import dev.d1s.panoramadiscordbroadcaster.util.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging

class PostFetcher : Fetcher<FetchedPost>, KoinComponent {

    private val log = logging()

    override suspend fun fetch(url: Url?): FetchedPost {
        requireNotNull(url) {
            "Post fetcher requires the post URL"
        }

        log.d {
            "Fetching post contents from $url"
        }

        val document = withContext(Dispatchers.IO) {
            try {
                Jsoup.connect(url).get()
            } catch (error: Throwable) {
                log.e(error) {
                    "Error fetching post at $url"
                }

                throw error
            }
        }

        log.d {
            "Loaded post document: ${document.title()}"
        }

        return FetchedPost(url, document)
    }
}