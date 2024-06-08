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
package xyz.dussim.gradlessh.internal

import net.schmizz.sshj.SSHClient
import xyz.dussim.gradlessh.remote.PasswordAuthenticatedRemote
import xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote
import xyz.dussim.gradlessh.remote.Remote
import xyz.dussim.gradlessh.remote.RemoteAddress
import xyz.dussim.gradlessh.remote.RemoteCollection

internal val RemoteAddress.authMethod: SSHClient.() -> Unit
    get() =
        when (this) {
            is PublicKeyAuthenticatedRemote -> authMethod
            is PasswordAuthenticatedRemote -> authMethod
        }

internal val PasswordAuthenticatedRemote.authMethod: SSHClient.() -> Unit
    get() = { authPassword(user, password) }

internal val PublicKeyAuthenticatedRemote.authMethod: SSHClient.() -> Unit
    get() = { authPublickey(user) }

internal fun Remote.flatten(): Set<RemoteAddress> {
    return when (this) {
        is RemoteCollection -> flatMapTo(mutableSetOf(), Remote::flatten)
        is RemoteAddress -> setOf(this)
    }
}
