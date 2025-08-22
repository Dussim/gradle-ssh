/**
 * Copyright (C) 2025 Dussim (Artur Tuzim) <artur@tuzim.xzy>
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
import org.gradle.kotlin.dsl.registering
import xyz.dussim.gradlessh.internal.PasswordAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.PublicKeyAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.RemoteCollectionImpl
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
    ) : ExtensiblePolymorphicDomainObjectContainer<Remote> by factory.polymorphicDomainObjectContainer(Remote::class.java),
        ExtensionAware {
        companion object;

        init {
            registerFactory(PublicKeyAuthenticatedRemote::class.java) { name ->
                factory.newInstance<PublicKeyAuthenticatedRemoteImpl>(name).apply {
                    port.convention(22)
                    // Default timeouts
                    connectionTimeout.convention(10_000)
                    readTimeout.convention(30_000)
                }
            }
            registerFactory(PasswordAuthenticatedRemote::class.java) { name ->
                factory.newInstance<PasswordAuthenticatedRemoteImpl>(name).apply {
                    port.convention(22)
                    // Default timeouts
                    connectionTimeout.convention(10_000)
                    readTimeout.convention(30_000)
                }
            }
            registerFactory(RemoteCollection::class.java) { name ->
                factory.newInstance<RemoteCollectionImpl>(
                    name,
                    factory.namedDomainObjectSet(Remote::class.java),
                )
            }
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
