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

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.plugins.ExtensionAware
import java.io.Serializable

/**
 * This is a base interface for Remote Upload/Download Commands,
 * representing a generic transfer task to be performed against a remote server.
 */
sealed interface RemoteFileCommand :
    Named,
    ExtensionAware,
    Serializable

/**
 * Marker interface for interfaces that model actual data to upload/download.
 * */
sealed interface RemoteFileContent

/**
 * This interface represents a set of [RemoteFileCommand] objects, possibly nested [RemoteFileCommandCollection].
 * It extends [NamedDomainObjectSet] to provide a named collection of [RemoteFileCommand] objects.
 *
 * IMPORTANT NOTE: this [Set] implementation does not preserve insertion order,
 * rather iteration order is based on the natural order of [Named.getName].
 * If you need a specific sequence, prefer naming with numeric prefixes (e.g., 01_copy, 02_unpack)
 * or split commands into multiple tasks to enforce order. All uploads and downloads
 * should ideally be independent of each other.
 */
interface RemoteFileCommandCollection :
    RemoteFileCommand,
    NamedDomainObjectSet<RemoteFileCommand> {
    companion object
}
