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

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.kotlin.dsl.create

class RemoteContainer(
    private val container: ExtensiblePolymorphicDomainObjectContainer<Remote>,
) : PolymorphicDomainObjectContainer<Remote> by container {
    init {
        val setRemoteDefaults = { remote: Remote ->
            remote.host = "localhost"
            remote.port = 22
        }

        container.registerFactory(PublicKeyAuthenticatedRemote::class.java) { name ->
            PublicKeyAuthenticatedRemoteImpl(name).apply(setRemoteDefaults)
        }
        container.registerFactory(PasswordAuthenticatedRemote::class.java) { name ->
            PasswordAuthenticatedRemoteImpl(name).apply(setRemoteDefaults)
        }
    }

    fun publicKeyAuthenticated(
        name: String,
        configuration: PublicKeyAuthenticatedRemote.() -> Unit,
    ) {
        container.create<PublicKeyAuthenticatedRemote>(name, configuration)
    }

    fun passwordAuthenticated(
        name: String,
        configuration: PasswordAuthenticatedRemote.() -> Unit,
    ) {
        container.create<PasswordAuthenticatedRemote>(name, configuration)
    }
}
