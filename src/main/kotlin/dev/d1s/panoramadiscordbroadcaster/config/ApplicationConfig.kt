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

package dev.d1s.panoramadiscordbroadcaster.config

import dev.d1s.panoramadiscordbroadcaster.translation.LanguageCode

data class ApplicationConfig(
    val webhook: Webhook,
    val redis: Redis,
    val fetcher: Fetcher,
    val yandexCloud: YandexCloudCredentials? = null,
    val translation: Translation? = null
) {
    data class Webhook(
        val url: String
    )

    data class Redis(
        val endpoint: String
    )

    data class Fetcher(
        val delay: Long
    )

    data class YandexCloudCredentials(
        val apiKey: String,
        val folderId: String
    )

    data class Translation(
        val targetLanguageCode: String
    )
}

fun ApplicationConfig.Translation?.valid() =
    this != null && targetLanguageCode != LanguageCode.SOURCE_LANGUAGE_CODE