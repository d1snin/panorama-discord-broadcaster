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

import dev.d1s.panoramadiscordbroadcaster.util.Url
import kotlinx.coroutines.delay
import org.lighthousegames.logging.logging

interface Fetcher<R : Any> {

    suspend fun fetch(url: Url? = null): R

    suspend fun fetchCatching(url: Url? = null): R = try {
        fetch(url)
    } catch (throwable: Throwable) {
        log.e {
            "Failure while fetching from $url: ${throwable.message}. Retrying in 5 seconds..."
        }

        delay(5_000)

        fetchCatching(url)
    }

    private companion object {

        private val log = logging()
    }
}