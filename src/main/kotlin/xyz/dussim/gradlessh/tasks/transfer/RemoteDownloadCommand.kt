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

import org.gradle.api.provider.Property
import java.io.File

/**
 * This is a base interface for Remote Download Commands,
 * representing a generic download task to be performed against a remote server.
 */
sealed interface RemoteDownloadCommand : RemoteFileCommand {
    companion object
}

/**
 * Marker interface for interfaces that model actual data to download.
 * */
sealed interface DownloadFileContent : RemoteFileContent {
    companion object

    val remotePath: Property<String>
}

/**
 * This interface represents a [RemoteDownloadCommand] to specified local file path.
 * You can also set file from a path using extension function [setFromPath].
 * */
interface RemoteDownloadCommandFile : RemoteDownloadCommand, DownloadFileContent {
    companion object

    val localFile: Property<File>
}

/**
 * Helper extension function to set [RemoteDownloadCommandFile.localFile] from [remotePath]
 * */
fun RemoteDownloadCommandFile.setFromPath(remotePath: String) {
    localFile.set(File(remotePath))
}
