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
package xyz.dussim.gradlessh

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.newInstance
import xyz.dussim.gradlessh.internal.PasswordAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.PublicKeyAuthenticatedRemoteImpl
import xyz.dussim.gradlessh.internal.RemoteCollectionImpl
import xyz.dussim.gradlessh.internal.RemoteDownloadCommandFileImpl
import xyz.dussim.gradlessh.internal.RemoteExecCommandCollectionImpl
import xyz.dussim.gradlessh.internal.RemoteExecCommandStringImpl
import xyz.dussim.gradlessh.internal.RemoteFileCommandCollectionImpl
import xyz.dussim.gradlessh.internal.RemoteUploadCommandFileImpl
import xyz.dussim.gradlessh.internal.RemoteUploadCommandStringImpl
import xyz.dussim.gradlessh.remote.PasswordAuthenticatedRemote
import xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote
import xyz.dussim.gradlessh.remote.Remote
import xyz.dussim.gradlessh.remote.RemoteCollection
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommand
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandCollection
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandString
import xyz.dussim.gradlessh.tasks.transfer.RemoteDownloadCommandFile
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommand
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandCollection
import xyz.dussim.gradlessh.tasks.transfer.RemoteUploadCommandFile
import xyz.dussim.gradlessh.tasks.transfer.RemoteUploadCommandString

internal class SshPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            val remotes =
                objects.polymorphicDomainObjectContainer(Remote::class.java).apply {
                    registerFactory(PublicKeyAuthenticatedRemote::class.java) { name ->
                        objects.newInstance<PublicKeyAuthenticatedRemoteImpl>(name).apply {
                            port.convention(22)
                            connectionTimeout.convention(10_000)
                            readTimeout.convention(30_000)
                        }
                    }
                    registerFactory(PasswordAuthenticatedRemote::class.java) { name ->
                        objects.newInstance<PasswordAuthenticatedRemoteImpl>(name).apply {
                            port.convention(22)
                            connectionTimeout.convention(10_000)
                            readTimeout.convention(30_000)
                        }
                    }
                    registerFactory(RemoteCollection::class.java) { name ->
                        objects.newInstance<RemoteCollectionImpl>(
                            name,
                            objects.namedDomainObjectSet(Remote::class.java),
                        )
                    }
                }
            val remoteExecCommands =
                objects.polymorphicDomainObjectContainer(RemoteExecCommand::class.java).apply {
                    registerFactory(RemoteExecCommandString::class.java) { name ->
                        objects.newInstance<RemoteExecCommandStringImpl>(name)
                    }
                    registerFactory(RemoteExecCommandCollection::class.java) { name ->
                        objects.newInstance<RemoteExecCommandCollectionImpl>(
                            name,
                            objects.namedDomainObjectSet(RemoteExecCommand::class.java),
                        )
                    }
                }
            val remoteFileCommands =
                objects.polymorphicDomainObjectContainer(RemoteFileCommand::class.java).apply {
                    registerFactory(RemoteUploadCommandString::class.java) { name ->
                        objects.newInstance<RemoteUploadCommandStringImpl>(name)
                    }
                    registerFactory(RemoteUploadCommandFile::class.java) { name ->
                        objects.newInstance<RemoteUploadCommandFileImpl>(name)
                    }
                    registerFactory(RemoteDownloadCommandFile::class.java) { name ->
                        objects.newInstance<RemoteDownloadCommandFileImpl>(name)
                    }
                    registerFactory(RemoteFileCommandCollection::class.java) { name ->
                        objects.newInstance<RemoteFileCommandCollectionImpl>(
                            name,
                            objects.namedDomainObjectSet(RemoteFileCommand::class.java),
                        )
                    }
                }

            extensions.add<ExtensiblePolymorphicDomainObjectContainer<Remote>>(
                name = "remotes",
                extension = remotes,
            )
            extensions.add<ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>>(
                name = "remoteExecCommands",
                extension = remoteExecCommands,
            )
            extensions.add<ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>>(
                name = "remoteFileCommands",
                extension = remoteFileCommands,
            )
        }
}
