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
@file:Suppress("unused")

package org.gradle.kotlin.dsl

import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
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

fun ExtensiblePolymorphicDomainObjectContainer<Remote>.publicKeyAuthenticated(
    name: String,
    configuration: Action<PublicKeyAuthenticatedRemote> = Action {},
): NamedDomainObjectProvider<PublicKeyAuthenticatedRemote> =
    register(
        name,
        PublicKeyAuthenticatedRemote::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<Remote>.passwordAuthenticated(
    name: String,
    configuration: Action<PasswordAuthenticatedRemote> = Action {},
): NamedDomainObjectProvider<PasswordAuthenticatedRemote> =
    register(
        name,
        PasswordAuthenticatedRemote::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<Remote>.remoteCollection(
    name: String,
    configuration: Action<RemoteCollection> = Action {},
): NamedDomainObjectProvider<RemoteCollection> =
    register(
        name,
        RemoteCollection::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<Remote>.remoteCollection(
    name: String,
    vararg remote: NamedDomainObjectProvider<out Remote>,
): NamedDomainObjectProvider<RemoteCollection> =
    register(
        name,
        RemoteCollection::class.java,
        Action { remote.forEach(::addLater) },
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>.command(
    name: String,
    configuration: Action<RemoteExecCommandString> = Action {},
): NamedDomainObjectProvider<RemoteExecCommandString> =
    register(
        name,
        RemoteExecCommandString::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>.commandCollection(
    name: String,
    configuration: Action<RemoteExecCommandCollection> = Action {},
): NamedDomainObjectProvider<RemoteExecCommandCollection> =
    register(
        name,
        RemoteExecCommandCollection::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>.commandCollection(
    name: String,
    vararg command: NamedDomainObjectProvider<out RemoteExecCommand>,
): NamedDomainObjectProvider<RemoteExecCommandCollection> =
    register(
        name,
        RemoteExecCommandCollection::class.java,
        Action { command.forEach(::addLater) },
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>.uploadStringContent(
    name: String,
    configuration: Action<RemoteUploadCommandString> = Action {},
): NamedDomainObjectProvider<RemoteUploadCommandString> =
    register(
        name,
        RemoteUploadCommandString::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>.uploadFileContent(
    name: String,
    configuration: Action<RemoteUploadCommandFile> = Action {},
): NamedDomainObjectProvider<RemoteUploadCommandFile> =
    register(
        name,
        RemoteUploadCommandFile::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>.downloadFileContent(
    name: String,
    configuration: Action<RemoteDownloadCommandFile> = Action {},
): NamedDomainObjectProvider<RemoteDownloadCommandFile> =
    register(
        name,
        RemoteDownloadCommandFile::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>.fileCommandCollection(
    name: String,
    configuration: Action<RemoteFileCommandCollection> = Action {},
): NamedDomainObjectProvider<RemoteFileCommandCollection> =
    register(
        name,
        RemoteFileCommandCollection::class.java,
        configuration,
    )

fun ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>.fileCommandCollection(
    name: String,
    vararg remoteFileCommand: NamedDomainObjectProvider<out RemoteFileCommand>,
): NamedDomainObjectProvider<RemoteFileCommandCollection> =
    register(
        name,
        RemoteFileCommandCollection::class.java,
        Action { remoteFileCommand.forEach(::addLater) },
    )
