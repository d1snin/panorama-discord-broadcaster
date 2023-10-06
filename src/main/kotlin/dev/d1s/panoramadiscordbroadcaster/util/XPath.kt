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

package dev.d1s.panoramadiscordbroadcaster.util

object XPath {

    const val LATEST_POST = "/html/body/div[3]/div/div/div[2]/ul[1]/li[1]/a"

    const val POST_TITLE = "/html/body/div[3]/div[2]/div/div[1]/div[1]/div/h1"
    const val POST_CATEGORIES = "/html/body/div[3]/div[2]/div/div[1]/div[2]/div/div[3]"
    const val POST_AUTHOR_META = "/html/body/div[3]/div[2]/div/div[1]/div[2]/div/div[2]/meta"
    const val POST_IMAGE = "/html/body/div[3]/div[1]/div/div/div"
    const val POST_TEXT = "/html/body/div[3]/div[2]/div/div[1]/div[3]"
}