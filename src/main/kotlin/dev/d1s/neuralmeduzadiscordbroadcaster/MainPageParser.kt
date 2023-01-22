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

import dev.d1s.neuralmeduzadiscordbroadcaster.util.CssName
import dev.d1s.neuralmeduzadiscordbroadcaster.util.getFirstByClass
import dev.d1s.neuralmeduzadiscordbroadcaster.util.trySelect
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging

class MainPageParser : Parser<FetchedMainPage, MainPage>, KoinComponent {

    private val log = logging()

    override suspend fun parse(obj: FetchedMainPage): MainPage {
        log.d {
            "Parsing main page contents..."
        }

        val document = obj.document

        val latestPostUrl = document.extractLatestPostUrl()

        return MainPage(latestPostUrl)
    }

    private fun Document.extractLatestPostUrl(): String {
        val link = findLink()

        return link.attr(CssName.HREF_ATTRIBUTE)
    }

    private fun Document.findLink() = trySelect("post link (by .${CssName.POST_IMAGE_CLASS})") {
        val thumbnail = getFirstByClass(CssName.POST_IMAGE_CLASS)

        thumbnail?.parent()
    }
}
