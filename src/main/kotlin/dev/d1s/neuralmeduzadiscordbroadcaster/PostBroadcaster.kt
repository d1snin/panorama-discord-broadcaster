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

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessage
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.d1s.neuralmeduzadiscordbroadcaster.di.Qualifier
import dev.d1s.neuralmeduzadiscordbroadcaster.translation.PostTranslator
import dev.d1s.neuralmeduzadiscordbroadcaster.util.Urls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

interface PostBroadcaster {

    suspend fun start()
}

class DefaultPostBroadcaster : PostBroadcaster, KoinComponent {

    private val webhookClientFactory by inject<WebhookClientFactory>()

    private val latestPostUrlChangeListener by inject<LatestPostUrlChangeListener>()

    private val postFetcher by inject<Fetcher<FetchedPost>>(Qualifier.PostFetcher)

    private val postParser by inject<Parser<FetchedPost, Post>>(Qualifier.PostParser)

    private val postTranslator by inject<PostTranslator>()

    private val broadcastingScope = CoroutineScope(Dispatchers.IO)

    private val log = logging()

    override suspend fun start() {
        log.i {
            "Starting to broadcast posts now..."
        }

        val job = latestPostUrlChangeListener.onPostUrlChange { url ->
            val fetchedPost = postFetcher.fetch(url)
            val post = postParser.parse(fetchedPost)

            val translatedPost = postTranslator.translate(post) ?: post

            val embed = translatedPost.toDiscordEmbed()

            val message = makeMessage(embed)

            launchMessage(message)
        }

        log.i {
            "NMDB is ready."
        }

        job.join()
    }

    private fun launchMessage(message: WebhookMessage) {
        val webhookClient = webhookClientFactory.client

        broadcastingScope.launch {
            webhookClient.send(message)
        }
    }

    private fun makeMessage(embed: WebhookEmbed): WebhookMessage {
        val message = WebhookMessageBuilder().apply {
            addEmbeds(embed)

            setUsername(AUTHOR_NAME)
            setAvatarUrl(AUTHOR_ICON_URL)
        }

        return message.build()
    }

    private fun Post.toDiscordEmbed(): WebhookEmbed {
        val embed = WebhookEmbedBuilder().apply {
            val post = this@toDiscordEmbed

            setColor()
            setAuthor()
            setTitle(post)
            setDescription(post)
            setImageUrl(post)
            setFooter()
            setTimestamp(post)
        }

        return embed.build()
    }

    private fun WebhookEmbedBuilder.setColor() {
        setColor(EMBED_COLOR)
    }

    private fun WebhookEmbedBuilder.setAuthor() {
        val embedAuthor = WebhookEmbed.EmbedAuthor(
            AUTHOR_NAME, AUTHOR_ICON_URL, EMBED_AUTHOR_URL
        )

        setAuthor(embedAuthor)
    }

    private fun WebhookEmbedBuilder.setTitle(post: Post) {
        val title = post.title
        val url = post.url

        val embedTitle = EmbedTitle(title, url)

        setTitle(embedTitle)
    }

    private fun WebhookEmbedBuilder.setDescription(post: Post) {
        val description = buildString {
            val separator = " / "

            val categories = post.categories.joinToString(separator) {
                it.name.bold()
            }

            appendLine()

            append(categories)

            appendLine()
            appendLine()

            append(post.text)
        }

        setDescription(description)
    }

    private fun WebhookEmbedBuilder.setImageUrl(post: Post) {
        val imageUrl = post.image.url

        setImageUrl(imageUrl)
    }

    private fun WebhookEmbedBuilder.setFooter() {
        val embedFooter = WebhookEmbed.EmbedFooter(
            EMBED_FOOTER_TEXT,
            null
        )

        setFooter(embedFooter)
    }

    private fun WebhookEmbedBuilder.setTimestamp(post: Post) {
        setTimestamp(post.date)
    }

    private fun String.bold() = "**$this**"

    private companion object {

        private const val AUTHOR_NAME = "Neural Meduza"
        private const val AUTHOR_ICON_URL =
            "https://neuralmeduza.online/wp-content/uploads/2023/01/cropped-cropped-cropped-photo_2020-11-18_20-55-30-e1673240972948-1.jpg"

        private const val EMBED_COLOR = 0xb68b5a
        private const val EMBED_AUTHOR_URL = Urls.NEURAL_MEDUZA_BASE_URL
        private const val EMBED_FOOTER_TEXT =
            "Neural Meduza Discord Broadcaster v$VERSION (github.com/d1snin/neural-meduza-discord-broadcaster)"
    }
}