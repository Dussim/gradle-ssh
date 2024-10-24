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
package xyz.dussim.gradlessh.remote

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import java.io.Serializable

/**
 * Base interface for all [Remote] types. It does not hold any data by itself.
 */
sealed interface Remote :
    ExtensionAware,
    Named,
    Serializable {
    companion object
}

/**
 * This interface represents a Remote Address, providing essential server details.
 */
sealed interface RemoteAddress {
    companion object

    /**
     * Returns a formatted string with user, host, and port information.
     */
    val address: String get() = "${user.get()}@${host.get()}:${port.get()}"

    /**
     * Holds the host/server information.
     */
    val host: Property<String>

    /**
     * Holds port information for the server.
     */
    val port: Property<Int>

    /**
     * Holds user information for authentication.
     */
    val user: Property<String>
}

/**
 * This interface represents a Remote that uses password authentication.
 * It holds essential server login details of [RemoteAddress] in addition to password.
 */
interface PasswordAuthenticatedRemote :
    Remote,
    RemoteAddress {
    companion object;

    /**
     * User password for authentication.
     */
    var password: Property<String>
}

/**
 * This interface represents a Remote that uses public key authentication.
 * It holds essential server login details of [RemoteAddress].
 * It is assumed that the user has set up public key authentication on the server.
 */
interface PublicKeyAuthenticatedRemote :
    Remote,
    RemoteAddress {
    companion object;
}

/**
 * This interface represents a set of [Remote] objects, possibly nested [RemoteCollection].
 * It extends [NamedDomainObjectSet] to provide a named collection of [Remote] objects.
 *
 * IMPORTANT NOTE: this [Set] implementation does not preserve insertion order,
 * rather than iteration order is based on natural order of [Named.getName].
 */
interface RemoteCollection :
    Remote,
    NamedDomainObjectSet<Remote> {
    companion object
}
