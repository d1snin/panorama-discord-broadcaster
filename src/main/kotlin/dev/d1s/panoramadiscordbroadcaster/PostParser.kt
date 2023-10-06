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

import dev.d1s.panoramadiscordbroadcaster.util.CssName
import dev.d1s.panoramadiscordbroadcaster.util.getFirstByClass
import dev.d1s.panoramadiscordbroadcaster.util.trySelect
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging
import java.time.Instant

class PostParser : Parser<FetchedPost, Post>, KoinComponent {

    private val log = logging()

    override suspend fun parse(obj: FetchedPost): Post {
        log.d {
            "Parsing fetched post contents: $obj"
        }

        val document = obj.document

        val url = obj.url
        val title = document.extractTitle()
        val categories = document.extractCategories()
        val date = document.extractPublishedTime()
        val image = document.extractImage()
        val postText = document.extractPostText()

        return Post(url, title, categories, date, image, postText)
    }

    private fun Document.extractTitle(): String {
        val titleHeading = findTitle()

        return titleHeading.text()
    }

    private fun Document.extractCategories(): List<PostCategory> {
        val categoriesContainer = findCategoriesContainer()

        val categories = categoriesContainer.getElementsByTag(CssName.LINK_TAG).map { element ->
            val text = element.text().replaceFirstChar {
                it.uppercase()
            }

            PostCategory(name = text)
        }

        return categories
    }

    private fun Document.extractPublishedTime(): Instant {
        val timeMeta = extractPublishedTimeMeta()
        val rawDate = timeMeta.attr(CssName.META_CONTENT_ATTRIBUTE)

        return Instant.parse(rawDate)
    }

    private fun Document.extractImage(): PostImage {
        val image = findImage()
        val imageSrc = image.attr(CssName.SRC_ATTRIBUTE)

        return PostImage(url = imageSrc)
    }

    private fun Document.extractPostText(): String {
        val textContainer = findTextContainer()

        val paragraphs = textContainer.getElementsByTag(CssName.PARAGRAPH_TAG)

        return buildString {
            paragraphs.forEach {
                append(it.text())
                appendLine()
                appendLine()
            }
        }
    }

    private fun Document.findTitle() = trySelect("title (by .${CssName.POST_TITLE_CLASS})") {
        getFirstByClass(CssName.POST_TITLE_CLASS)
    }

    private fun Document.findCategoriesContainer() =
        trySelect("categories (by ${CssName.CATEGORIES_CONTAINER_CLASS})") {
            getFirstByClass(CssName.CATEGORIES_CONTAINER_CLASS)
        }

    private fun Document.extractPublishedTimeMeta() = trySelect("$PUBLISHED_TIME_META_PROPERTY meta") {
        getElementsByTag(CssName.META_TAG).find {
            it.attr(CssName.META_PROPERTY_ATTRIBUTE) == PUBLISHED_TIME_META_PROPERTY
        }
    }

    private fun Document.findImage() = trySelect("image (by ${CssName.POST_IMAGE_CLASS})") {
        getFirstByClass(CssName.POST_IMAGE_CLASS)
    }

    private fun Document.findTextContainer() = trySelect("text container (by ${CssName.POST_TEXT_CONTAINER_CLASS})") {
        getFirstByClass(CssName.POST_TEXT_CONTAINER_CLASS)
    }

    private companion object {

        private const val PUBLISHED_TIME_META_PROPERTY = "article:published_time"
    }
}