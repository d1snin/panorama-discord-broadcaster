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

package dev.d1s.panoramadiscordbroadcaster.translation

import dev.d1s.panoramadiscordbroadcaster.Post
import dev.d1s.panoramadiscordbroadcaster.PostCategory
import dev.d1s.panoramadiscordbroadcaster.config.ApplicationConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface PostTranslator {

    suspend fun translate(post: Post): Post?
}

class DefaultPostTranslator : PostTranslator, KoinComponent {

    private val translationService by inject<TranslationService>()

    private val config by inject<ApplicationConfig>()

    override suspend fun translate(post: Post): Post? {
        val translationConfig = config.translation

        if (translationConfig == null || translationConfig.targetLanguageCode == LanguageCode.SOURCE_LANGUAGE_CODE) {
            return null
        }

        val translatedTitle = translate(post.title)
        val translatedCategories = translateCategories(post.categories)
        val translatedText = translate(post.text)

        return Post(post.url, translatedTitle, translatedCategories, post.date, post.image, translatedText)
    }

    private suspend fun translateCategories(categories: List<PostCategory>) =
        categories.map {
            val translatedCategoryName = translate(it.name)

            PostCategory(translatedCategoryName)
        }

    private suspend fun translate(text: String) = translationService.translateText(text).text
}