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

import dev.d1s.panoramadiscordbroadcaster.util.Url
import dev.d1s.panoramadiscordbroadcaster.util.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging

class MainPageFetcher : Fetcher<FetchedMainPage>, KoinComponent {

    private val log = logging()

    override suspend fun fetch(url: Url?): FetchedMainPage {
        val finalUrl = url ?: Urls.PANORAMA_BASE_URL

        log.d {
            "Fetching main page contents from $finalUrl"
        }

        val document = withContext(Dispatchers.IO) {
            Jsoup.connect(finalUrl).get()
        }

        return FetchedMainPage(document)
    }
}