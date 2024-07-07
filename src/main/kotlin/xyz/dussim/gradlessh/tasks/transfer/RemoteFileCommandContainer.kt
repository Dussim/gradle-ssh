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
package xyz.dussim.gradlessh.tasks.transfer

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registering
import xyz.dussim.gradlessh.internal.RemoteDownloadCommandFileImpl
import xyz.dussim.gradlessh.internal.RemoteFileCommandCollectionImpl
import xyz.dussim.gradlessh.internal.RemoteUploadCommandFileImpl
import xyz.dussim.gradlessh.internal.RemoteUploadCommandStringImpl
import xyz.dussim.gradlessh.internal.extensions.container
import xyz.dussim.gradlessh.internal.extensions.namedDomainObjectSet
import javax.inject.Inject

@Suppress("LeakingThis")
abstract class RemoteFileCommandContainer
    @Inject
    constructor(
        factory: ObjectFactory,
    ) : ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand> by factory.container(), ExtensionAware {
        init {
            registerFactory(RemoteUploadCommandString::class.java) { name ->
                factory.newInstance<RemoteUploadCommandStringImpl>(name)
            }
            registerFactory(RemoteUploadCommandFile::class.java) { name ->
                factory.newInstance<RemoteUploadCommandFileImpl>(name)
            }
            registerFactory(RemoteDownloadCommandFile::class.java) { name ->
                factory.newInstance<RemoteDownloadCommandFileImpl>(name)
            }
            registerFactory(RemoteFileCommandCollection::class.java) { name ->
                factory.newInstance<RemoteFileCommandCollectionImpl>(
                    name,
                    factory.namedDomainObjectSet<RemoteFileCommand>(),
                )
            }
        }

        /**
         * Register a [RemoteUploadCommandString] instance with the given [configuration].
         * Instance is lazily created when needed.
         *
         * ```kotlin
         * // name of this command will be string "someCommand"
         * val someCommand by remoteFileCommands.uploadStringContent { /* configuration */ }
         * ```
         * @param configuration The configuration of the command.
         *
         * @return The registering provider for [RemoteUploadCommandString].
         * */
        fun uploadStringContent(configuration: RemoteUploadCommandString.() -> Unit) =
            registering(RemoteUploadCommandString::class, configuration)

        /**
         * Register a [RemoteUploadCommandFile] instance with the given [configuration].
         * Instance is lazily created when needed.
         *
         * ```kotlin
         * // name of this command will be string "someCommand"
         * val someCommand by remoteFileCommands.uploadFileContent { /* configuration */ }
         * ```
         * @param configuration The configuration of the command.
         *
         * @return The registering provider for [RemoteUploadCommandFile].
         * */
        fun uploadFileContent(configuration: RemoteUploadCommandFile.() -> Unit) =
            registering(RemoteUploadCommandFile::class, configuration)

        /**
         * Register a [RemoteDownloadCommandFile] instance with the given [configuration].
         * Instance is lazily created when needed.
         *
         * ```kotlin
         * // name of this command will be string "someCommand"
         * val someCommand by remoteFileCommands.downloadFileContent { /* configuration */ }
         * ```
         * @param configuration The configuration of the command.
         *
         * @return The registering provider for [RemoteDownloadCommandFile].
         * */
        fun downloadFileContent(configuration: RemoteDownloadCommandFile.() -> Unit) =
            registering(RemoteDownloadCommandFile::class, configuration)

        /**
         * Register a [RemoteFileCommandCollection] instance with the given [configuration].
         * Instance is lazily created when needed.
         *
         * ```kotlin
         * // name of this command will be string "someCommand"
         * val someCommand by remoteFileCommands.fileCommandCollection { /* configuration */ }
         * ```
         * @param configuration The configuration of the command.
         *
         * @return The registering provider for [RemoteFileCommandCollection].
         * */
        fun fileCommandCollection(configuration: RemoteFileCommandCollection.() -> Unit) =
            registering(RemoteFileCommandCollection::class, configuration)

        /**
         * Register a [RemoteFileCommandCollection] instance with given [remoteFileCommand] instances by delegate
         * which will use name of property for name of [RemoteFileCommandCollection].
         * Instance is lazily created when needed.
         *
         * ```kotlin
         * val upload1: NamedDomainObjectProvider<Remote> = remoteFileCommands.uploadFileContent { /* configuration */ }
         * val upload2: NamedDomainObjectProvider<Remote> = remoteFileCommands.uploadStringContent { /* configuration */ }
         *
         * // name of this remote will be string "someRemote"
         * val someUploadCommands by remoteFileCommands.fileCommandCollection(upload1, upload2)
         * ```
         * @param remoteFileCommand The [RemoteFileCommand] instance providers to lazily add to registered [RemoteFileCommandCollection].
         * @return The registering provider for that [RemoteFileCommandCollection].
         * */
        fun fileCommandCollection(vararg remoteFileCommand: NamedDomainObjectProvider<out RemoteFileCommand>) =
            fileCommandCollection { remoteFileCommand.forEach(::addLater) }
    }
