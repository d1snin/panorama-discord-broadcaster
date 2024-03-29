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

import dev.d1s.panoramadiscordbroadcaster.database.RedisClientFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

class PanoramaDiscordBroadcasterApplication : KoinComponent {

    private val broadcaster by inject<PostBroadcaster>()

    private val redisClientFactory by inject<RedisClientFactory>()

    private val log = logging()

    suspend fun run() {
        log.i {
            "Panorama Discord broadcaster ($VERSION) is starting..."
        }

        redisClientFactory.connect()

        broadcaster.start()
    }
}