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

package dev.d1s.panoramadiscordbroadcaster.translation

import dev.d1s.panoramadiscordbroadcaster.config.ApplicationConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

@JvmInline
value class TranslatedText(
    val text: String
)

interface TranslationService {

    suspend fun translateText(text: String): TranslatedText
}

class YandexCloudTranslationService : TranslationService, KoinComponent {

    private val config by inject<ApplicationConfig>()

    private val translationConfig = requireNotNull(config.translation) {
        "Translation config is required."
    }

    private val yandexCloudCredentials = requireNotNull(config.yandexCloud) {
        "Translation config is present but Yandex.Cloud credentials are not set."
    }

    private val httpClient = makeHttpClient()

    private val log = logging()

    override suspend fun translateText(text: String): TranslatedText = withContext(Dispatchers.IO) {
        val translationRequest = makeTranslationRequest(text)

        val translationResponse = httpClient.post(YANDEX_TRANSLATION_SERVICE_URL) {
            contentType(ContentType.Application.Json)
            setBody(translationRequest)
        }

        log.d {
            "Translation response text: ${translationResponse.bodyAsText()}"
        }

        val translatedText = translationResponse.body<TranslationResponse>().translations.first().text

        TranslatedText(translatedText)
    }

    private fun makeHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Api-Key ${yandexCloudCredentials.apiKey}")
        }
    }

    private fun makeTranslationRequest(text: String) = TranslationRequest(
        yandexCloudCredentials.folderId,
        text,
        translationConfig.targetLanguageCode
    )

    private data class TranslationRequest(
        val folderId: String,
        val texts: List<String>,
        val sourceLanguageCode: String,
        val targetLanguageCode: String
    ) {
        constructor(folderId: String, text: String, targetLanguageCode: String) : this(
            folderId,
            listOf(text),
            LanguageCode.SOURCE_LANGUAGE_CODE,
            targetLanguageCode
        )
    }

    private data class TranslationResponse(
        val translations: List<Translation>
    )

    private data class Translation(
        val text: String
    )

    private companion object {

        private const val YANDEX_TRANSLATION_SERVICE_URL =
            "https://translate.api.cloud.yandex.net/translate/v2/translate"
    }
}