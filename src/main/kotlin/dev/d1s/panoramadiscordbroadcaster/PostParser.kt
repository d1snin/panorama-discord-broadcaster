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

import dev.d1s.panoramadiscordbroadcaster.util.CssName
import dev.d1s.panoramadiscordbroadcaster.util.XPath
import dev.d1s.panoramadiscordbroadcaster.util.trySelect
import dev.d1s.panoramadiscordbroadcaster.util.xpath
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging

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
        val author = document.extractAuthor()
        val image = document.extractImage()
        val postText = document.extractText()

        val post = Post(url, title, categories, author, image, postText)

        log.d {
            "Parsed post contents: $post"
        }

        return post
    }

    private fun Document.extractTitle(): String {
        val titleHeading = findTitle()
        return titleHeading.text()
    }

    private fun Document.extractCategories(): List<PostCategory> {
        val categoriesContainer = findCategoriesContainer()

        val categories = categoriesContainer.getElementsByTag(CssName.LINK_TAG).map { element ->
            val text = element.text()
            PostCategory(name = text)
        }

        return categories
    }

    private fun Document.extractAuthor(): String {
        val authorMeta = findAuthor()
        return authorMeta.attr(CssName.CONTENT_ATTRIBUTE)
    }

    private fun Document.extractImage(): PostImage {
        val component = findImageComponent()
        val imageSrc = component.attr(CssName.DATA_BG_IMAGE_WEBP_ATTRIBUTE)

        return PostImage(url = imageSrc)
    }

    private fun Document.extractText(): String {
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

    private fun Document.findTitle() =
        trySelect("title (by ${XPath.POST_TITLE})") {
            xpath(XPath.POST_TITLE)
        }

    private fun Document.findCategoriesContainer() =
        trySelect("categories (by ${XPath.POST_CATEGORIES})") {
            xpath(XPath.POST_CATEGORIES)
        }

    private fun Document.findAuthor() =
        trySelect("author (by ${XPath.POST_AUTHOR_META})") {
            xpath(XPath.POST_AUTHOR_META)
        }

    private fun Document.findImageComponent() =
        trySelect("image (by ${XPath.POST_IMAGE})") {
            xpath(XPath.POST_IMAGE)
        }

    private fun Document.findTextContainer() =
        trySelect("text container (by ${XPath.POST_TEXT})") {
            xpath(XPath.POST_TEXT)
        }
}