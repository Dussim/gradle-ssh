/**
 * Copyright (C) 2024 Dussim (Artur Tuzim) <artur@tuzim.xzy>
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
package xyz.dussim.gradlessh

import kotlin.properties.Delegates

internal class PublicKeyAuthenticatedRemoteImpl(
    private val name: String,
) : PublicKeyAuthenticatedRemote {
    override fun getName(): String = name

    override var host by Delegates.notNull<String>()
    override var user by Delegates.notNull<String>()
    override var port by Delegates.notNull<Int>()

    override fun toString(): String {
        return "PublicKeyAuthenticatedRemote[$user@$host:$port]"
    }
}

fun Remote.Companion.publicKeyAuthenticated(
    name: String,
    host: String,
    user: String,
    port: Int = 22,
): PublicKeyAuthenticatedRemote =
    PublicKeyAuthenticatedRemoteImpl(name)
        .also { remote ->
            remote.host = host
            remote.user = user
            remote.port = port
        }
