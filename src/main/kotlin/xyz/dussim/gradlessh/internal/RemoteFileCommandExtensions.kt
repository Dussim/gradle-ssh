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
package xyz.dussim.gradlessh.internal

import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.LocalDestFile
import net.schmizz.sshj.xfer.LocalSourceFile
import org.gradle.api.provider.Provider
import xyz.dussim.gradlessh.tasks.transfer.DownloadFileContent
import xyz.dussim.gradlessh.tasks.transfer.RemoteDownloadCommandFile
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommand
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandCollection
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileContent
import xyz.dussim.gradlessh.tasks.transfer.RemoteUploadCommandFile
import xyz.dussim.gradlessh.tasks.transfer.RemoteUploadCommandString
import xyz.dussim.gradlessh.tasks.transfer.UploadFileContent

internal fun RemoteFileCommand.uploadsFlatten(): Set<UploadFileContent> =
    when (this) {
        is RemoteFileCommandCollection -> flatMapTo(mutableSetOf(), RemoteFileCommand::uploadsFlatten)
        is UploadFileContent -> setOf(this)
        is DownloadFileContent -> emptySet()
    }

internal fun Provider<out RemoteFileCommand>.uploadsFlatten(): Set<UploadFileContent> = get().uploadsFlatten()

internal fun RemoteFileCommand.downloadsFlatten(): Set<DownloadFileContent> =
    when (this) {
        is RemoteFileCommandCollection -> flatMapTo(mutableSetOf(), RemoteFileCommand::downloadsFlatten)
        is DownloadFileContent -> setOf(this)
        is UploadFileContent -> emptySet()
    }

internal fun Provider<out RemoteFileCommand>.downloadsFlatten(): Set<DownloadFileContent> = get().downloadsFlatten()

internal fun RemoteFileCommand.flatten(): Set<RemoteFileContent> =
    when (this) {
        is RemoteFileCommandCollection -> flatMapTo(mutableSetOf(), RemoteFileCommand::flatten)
        is RemoteFileContent -> setOf(this)
    }

internal fun Provider<out RemoteFileCommand>.flatten(): Set<RemoteFileContent> = get().flatten()

internal fun UploadFileContent.toFileSource(): LocalSourceFile =
    when (this) {
        is RemoteUploadCommandFile -> {
            FileSystemFile(localFile.get())
        }

        is RemoteUploadCommandString -> {
            StringInMemorySourceFile(
                content = textContent.get(),
                fileName = fileName.get(),
            )
        }
    }

internal fun DownloadFileContent.toDestFile(): LocalDestFile =
    when (this) {
        is RemoteDownloadCommandFile -> FileSystemFile(localFile.get())
    }
