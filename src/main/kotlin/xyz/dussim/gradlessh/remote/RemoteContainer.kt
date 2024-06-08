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

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import xyz.dussim.gradlessh.internal.PasswordAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.PublicKeyAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.RemoteCollectionImpl

/**
 * The [RemoteContainer] class is a container
 * extending [ExtensiblePolymorphicDomainObjectContainer] for managing different types of remote connections.
 * It provides helper methods to lazily and eagerly create [Remote] instances.
 *
 * @see Remote
 * @see PasswordAuthenticatedRemote
 * @see PublicKeyAuthenticatedRemote
 * @see RemoteCollection
 */
class RemoteContainer internal constructor(
    private val container: ExtensiblePolymorphicDomainObjectContainer<Remote>,
    project: Project,
) : ExtensiblePolymorphicDomainObjectContainer<Remote> by container {
    companion object;

    init {
        container.registerFactory(PublicKeyAuthenticatedRemote::class.java) { name ->
            PublicKeyAuthenticatedRemoteImpl(name = name).apply { port = 22 }
        }
        container.registerFactory(PasswordAuthenticatedRemote::class.java) { name ->
            PasswordAuthenticatedRemoteImpl(name = name).apply { port = 22 }
        }
        container.registerFactory(RemoteCollection::class.java) { name ->
            RemoteCollectionImpl(name = name, remotes = project.container<Remote>())
        }
    }

    /**
     * Register a [PublicKeyAuthenticatedRemote] instance with the given [name] and [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this remote will be string "differentName"
     * val someRemote by remotes.publicKeyAuthenticated("differentName") { /* configuration */ }
     * ```
     * @param name The name of the remote.
     * @param configuration The configuration of the remote.
     *
     * @return The provider for the registered [PublicKeyAuthenticatedRemote].
     * */
    fun publicKeyAuthenticated(
        name: String,
        configuration: PublicKeyAuthenticatedRemote.() -> Unit,
    ): NamedDomainObjectProvider<PublicKeyAuthenticatedRemote> {
        return container.register<PublicKeyAuthenticatedRemote>(name, configuration)
    }

    /**
     * Register a [PublicKeyAuthenticatedRemote] instance with given [configuration] by delegate
     * which will use name of property for name of [PublicKeyAuthenticatedRemote].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this remote will be string "someRemote"
     * val someRemote by remotes.publicKeyAuthenticated { /* configuration */ }
     * ```
     * @param configuration The configuration of the remote.
     * @return The registering provider for that [PublicKeyAuthenticatedRemote].
     * */
    fun publicKeyAuthenticated(
        configuration: PublicKeyAuthenticatedRemote.() -> Unit,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<Remote>, PublicKeyAuthenticatedRemote> {
        return container.registering(PublicKeyAuthenticatedRemote::class, configuration)
    }

    /**
     * Register a [PasswordAuthenticatedRemote] instance with the given [name] and [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this remote will be string "differentName"
     * val someRemote by remotes.passwordAuthenticated("differentName") { /* configuration */ }
     * ```
     * @param name The name of the remote.
     * @param configuration The configuration of the remote.
     * @return The provider for the registered [PasswordAuthenticatedRemote].
     * */
    fun passwordAuthenticated(
        name: String,
        configuration: PasswordAuthenticatedRemote.() -> Unit,
    ): NamedDomainObjectProvider<PasswordAuthenticatedRemote> {
        return container.register<PasswordAuthenticatedRemote>(name, configuration)
    }

    /**
     * Register a [PasswordAuthenticatedRemote] instance with given [configuration] by delegate
     * which will use name of property for name of [PasswordAuthenticatedRemote].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this remote will be string "someRemote"
     * val someRemote by remotes.passwordAuthenticated { /* configuration */ }
     * ```
     * @param configuration The configuration of the remote.
     * @return The registering provider for that [PasswordAuthenticatedRemote].
     * */
    fun passwordAuthenticated(
        configuration: PasswordAuthenticatedRemote.() -> Unit,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<Remote>, PasswordAuthenticatedRemote> {
        return container.registering(PasswordAuthenticatedRemote::class, configuration)
    }

    /**
     * Register a [RemoteCollection] instance with the given [name] and [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this remote will be string "differentName"
     * val someRemote by remotes.remoteCollection("differentName") { /* configuration */ }
     * ```
     * @param name The name of the remote.
     * @param configuration The configuration of the remote.
     * @return The provider for the registered [RemoteCollection].
     * */
    fun remoteCollection(
        name: String,
        configuration: RemoteCollection.() -> Unit,
    ): NamedDomainObjectProvider<RemoteCollection> {
        return container.register<RemoteCollection>(name, configuration)
    }

    /**
     * Register a [RemoteCollection] instance with given [configuration] by delegate
     * which will use name of property for name of [RemoteCollection].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this remote will be string "someRemote"
     * val someRemote by remotes.remoteCollection { /* configuration */ }
     * ```
     * @param configuration The configuration of the remote.
     * @return The registering provider for that [RemoteCollection].
     * */
    fun remoteCollection(
        configuration: RemoteCollection.() -> Unit,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<Remote>, RemoteCollection> {
        return container.registering(RemoteCollection::class, configuration)
    }

    /**
     * Register a [RemoteCollection] instance with the given [name] and [remote] instances.
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val remote1: Remote = remotes.getByName("someName1")
     * val remote2: Remote = remotes.getByName("someName2")
     *
     * // name of this remote will be string "differentName"
     * val someRemote by remotes.remoteCollection("differentName", remote1, remote2)
     * ```
     * @param name The name of the remote.
     * @param remote The [Remote] instances to eagerly add to registered [RemoteCollection].
     * @return The provider for the registered [RemoteCollection].
     * */
    fun remoteCollection(
        name: String,
        vararg remote: Remote,
    ): NamedDomainObjectProvider<RemoteCollection> {
        return remoteCollection(name) { this.addAll(remote) }
    }

    /**
     * Register a [RemoteCollection] instance with given [remote] instances by delegate
     * which will use name of property for name of [RemoteCollection].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val remote1: Remote = remotes.getByName("someName1")
     * val remote2: Remote = remotes.getByName("someName2")
     *
     * // name of this remote will be string "someRemote"
     * val someRemote by remotes.remoteCollection(remote1, remote2)
     * ```
     * @param remote The [Remote] instances to eagerly add to registered [RemoteCollection].
     * @return The registering provider for that [RemoteCollection].
     * */
    fun remoteCollection(
        vararg remote: Remote,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<Remote>, RemoteCollection> {
        return remoteCollection { this.addAll(remote) }
    }

    /**
     * Register a [RemoteCollection] instance with given [remote] instances by delegate
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val remote1: NamedDomainObjectProvider<Remote> = remotes.publicKeyAuthenticated("someName1") { /* configuration */ }
     * val remote2: NamedDomainObjectProvider<Remote> = remotes.publicKeyAuthenticated("someName2") { /* configuration */ }
     *
     * // name of this remote will be string "differentName"
     * val someRemote by remotes.remoteCollection("differentName", remote1, remote2)
     * ```
     * @param name The name of the remote.
     * @param remote The [Remote] instance providers to lazily add to registered [RemoteCollection].
     * @return The provider for the registered [RemoteCollection].
     * */
    fun remoteCollection(
        name: String,
        vararg remote: NamedDomainObjectProvider<out Remote>,
    ): NamedDomainObjectProvider<RemoteCollection> {
        return remoteCollection(name) { remote.forEach(::addLater) }
    }

    /**
     * Register a [RemoteCollection] instance with given [remote] instances by delegate
     * which will use name of property for name of [RemoteCollection].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val remote1: NamedDomainObjectProvider<Remote> = remotes.publicKeyAuthenticated("someName1") { /* configuration */ }
     * val remote2: NamedDomainObjectProvider<Remote> = remotes.publicKeyAuthenticated("someName2") { /* configuration */ }
     *
     * // name of this remote will be string "someRemote"
     * val someRemote by remotes.remoteCollection(remote1, remote2)
     * ```
     * @param remote The [Remote] instance providers to lazily add to registered [RemoteCollection].
     * @return The registering provider for that [RemoteCollection].
     * */
    fun remoteCollection(
        vararg remote: NamedDomainObjectProvider<out Remote>,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<Remote>, RemoteCollection> {
        return remoteCollection { remote.forEach(::addLater) }
    }
}
