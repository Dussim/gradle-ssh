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
package xyz.dussim.gradlessh.tasks.transfer

import org.gradle.api.provider.Property
import java.io.File

/**
 * This is a base interface for Remote Upload Commands,
 * representing a generic upload task to be performed against a remote server.
 */
sealed interface RemoteUploadCommand : RemoteFileCommand {
    companion object
}

/**
 * Marker interface for interfaces that model actual data to upload.
 * */
sealed interface UploadFileContent : RemoteFileContent {
    companion object

    val remotePath: Property<String>
}

/**
 * This interface represents a [RemoteUploadCommand] to specified remote file path and a content of provided string.
 * */
interface RemoteUploadCommandString :
    RemoteUploadCommand,
    UploadFileContent {
    companion object

    val textContent: Property<String>
    val fileName: Property<String>
}

/**
 * This interface represents a [RemoteUploadCommand] to specified remote file path and a content and name of provided local file.
 * */
interface RemoteUploadCommandFile :
    RemoteUploadCommand,
    UploadFileContent {
    companion object

    val localFile: Property<File>
}
