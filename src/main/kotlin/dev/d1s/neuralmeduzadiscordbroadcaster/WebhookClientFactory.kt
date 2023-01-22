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

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.util.WebhookErrorHandler
import dev.d1s.neuralmeduzadiscordbroadcaster.config.ApplicationConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

interface WebhookClientFactory {

    val client: WebhookClient
}

class DefaultWebhookClientFactory : WebhookClientFactory, KoinComponent {

    override val client by lazy {
        makeClient()
    }

    private val config by inject<ApplicationConfig>()

    private val log = logging()

    private fun makeClient(): WebhookClient {
        val webhookConfig = config.webhook

        log.d {
            "Making webhook client according to the supplied config: $webhookConfig"
        }

        val webhookUrl = webhookConfig.url

        val webhookClient = WebhookClient.withUrl(webhookUrl).apply {
            setErrorHandler(webhookErrorHandler)
        }

        return webhookClient
    }

    private val webhookErrorHandler: WebhookErrorHandler = WebhookErrorHandler { _, failureMessage, _ ->
        log.e {
            "Failure while trying to send a message: $failureMessage"
        }
    }
}