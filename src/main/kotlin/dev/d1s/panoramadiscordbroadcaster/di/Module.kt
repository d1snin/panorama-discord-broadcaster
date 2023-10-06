/*
 * Copyright 2022-2023 Mikhail Titov
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

package dev.d1s.panoramadiscordbroadcaster.di

import dev.d1s.panoramadiscordbroadcaster.*
import dev.d1s.panoramadiscordbroadcaster.config.ApplicationConfig
import dev.d1s.panoramadiscordbroadcaster.config.ApplicationConfigFactory
import dev.d1s.panoramadiscordbroadcaster.config.ApplicationConfigFactoryImpl
import dev.d1s.panoramadiscordbroadcaster.config.valid
import dev.d1s.panoramadiscordbroadcaster.database.DefaultRedisClientFactory
import dev.d1s.panoramadiscordbroadcaster.database.RedisClientFactory
import dev.d1s.panoramadiscordbroadcaster.translation.DefaultPostTranslator
import dev.d1s.panoramadiscordbroadcaster.translation.PostTranslator
import dev.d1s.panoramadiscordbroadcaster.translation.TranslationService
import dev.d1s.panoramadiscordbroadcaster.translation.YandexCloudTranslationService
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger

fun setupDi() {
    startKoin {
        logger(SLF4JLogger())

        val mainModule = module {
            application()
            applicationConfigFactory()
            applicationConfig()
            broadcaster()
            redisClientFactory()
            webhookClientFactory()
            changeListener()
            fetchers()
            parsers()
            translation()
        }

        modules(mainModule)
    }
}

fun Module.application() {
    singleOf(::PanoramaDiscordBroadcasterApplication)
}

fun Module.applicationConfigFactory() {
    singleOf<ApplicationConfigFactory>(::ApplicationConfigFactoryImpl)
}

fun Module.applicationConfig() {
    single {
        get<ApplicationConfigFactory>().config
    }
}

fun Module.broadcaster() {
    singleOf<PostBroadcaster>(::DefaultPostBroadcaster)
}

fun Module.redisClientFactory() {
    singleOf<RedisClientFactory>(::DefaultRedisClientFactory)
}

fun Module.webhookClientFactory() {
    singleOf<WebhookClientFactory>(::DefaultWebhookClientFactory)
}

fun Module.changeListener() {
    singleOf<LatestPostUrlChangeListener>(::DefaultLatestPostUrlChangeListener)
}

fun Module.fetchers() {
    singleOf<Fetcher<FetchedMainPage>>(::MainPageFetcher) {
        qualifier = Qualifier.MainPageFetcher
    }

    singleOf<Fetcher<FetchedPost>>(::PostFetcher) {
        qualifier = Qualifier.PostFetcher
    }
}

fun Module.parsers() {
    singleOf<Parser<FetchedMainPage, MainPage>>(::MainPageParser) {
        qualifier = Qualifier.MainPageParser
    }

    singleOf<Parser<FetchedPost, Post>>(::PostParser) {
        qualifier = Qualifier.PostParser
    }
}

fun Module.translation() {
    single<PostTranslator?> {
        ifValidTranslationConfig {
            DefaultPostTranslator()
        }
    }

    single<TranslationService?> {
        ifValidTranslationConfig {
            YandexCloudTranslationService()
        }
    }
}

private inline fun <R> Scope.ifValidTranslationConfig(bean: () -> R) =
    if (get<ApplicationConfig>().translation.valid()) {
        bean()
    } else {
        null
    }