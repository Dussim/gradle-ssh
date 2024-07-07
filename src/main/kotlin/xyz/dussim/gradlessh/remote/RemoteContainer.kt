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
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import xyz.dussim.gradlessh.internal.PasswordAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.PublicKeyAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.RemoteCollectionImpl
import xyz.dussim.gradlessh.internal.extensions.container
import xyz.dussim.gradlessh.internal.extensions.namedDomainObjectSet
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommand
import javax.inject.Inject

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
@Suppress("LeakingThis")
abstract class RemoteContainer
    @Inject
    constructor(
        factory: ObjectFactory,
    ) : ExtensiblePolymorphicDomainObjectContainer<Remote> by factory.container(), ExtensionAware {
        companion object;

        init {
            registerFactory(PublicKeyAuthenticatedRemote::class.java) { name ->
                factory.newInstance<PublicKeyAuthenticatedRemoteImpl>(name).apply { port.set(22) }
            }
            registerFactory(PasswordAuthenticatedRemote::class.java) { name ->
                factory.newInstance<PasswordAuthenticatedRemoteImpl>(name).apply { port.set(22) }
            }
            registerFactory(RemoteCollection::class.java) { name ->
                factory.newInstance<RemoteCollectionImpl>(
                    name,
                    factory.namedDomainObjectSet<RemoteExecCommand>(),
                )
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
        @Deprecated("Stick to kotlin by delegate")
        fun publicKeyAuthenticated(
            name: String,
            configuration: PublicKeyAuthenticatedRemote.() -> Unit,
        ): NamedDomainObjectProvider<PublicKeyAuthenticatedRemote> {
            return register<PublicKeyAuthenticatedRemote>(name, configuration)
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
        fun publicKeyAuthenticated(configuration: PublicKeyAuthenticatedRemote.() -> Unit) =
            registering(PublicKeyAuthenticatedRemote::class, configuration)

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
        @Deprecated("Stick to kotlin by delegate")
        fun passwordAuthenticated(
            name: String,
            configuration: PasswordAuthenticatedRemote.() -> Unit,
        ): NamedDomainObjectProvider<PasswordAuthenticatedRemote> {
            return register<PasswordAuthenticatedRemote>(name, configuration)
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
        fun passwordAuthenticated(configuration: PasswordAuthenticatedRemote.() -> Unit) =
            registering(PasswordAuthenticatedRemote::class, configuration)

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
        @Deprecated("Stick to kotlin by delegate")
        fun remoteCollection(
            name: String,
            configuration: RemoteCollection.() -> Unit,
        ): NamedDomainObjectProvider<RemoteCollection> {
            return register<RemoteCollection>(name, configuration)
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
        fun remoteCollection(configuration: RemoteCollection.() -> Unit) = registering(RemoteCollection::class, configuration)

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
        @Deprecated("Stick to kotlin by delegate")
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
        @Deprecated("Use overload which accepts providers")
        fun remoteCollection(vararg remote: Remote) = remoteCollection { this.addAll(remote) }

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
        @Deprecated("Stick to kotlin by delegate")
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
        fun remoteCollection(vararg remote: NamedDomainObjectProvider<out Remote>) = remoteCollection { remote.forEach(::addLater) }
    }
