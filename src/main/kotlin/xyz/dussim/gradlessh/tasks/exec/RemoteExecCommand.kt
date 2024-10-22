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
package xyz.dussim.gradlessh.tasks.exec

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import java.io.Serializable

/**
 * This is a base interface for Remote Execution Commands,
 * representing a generic task to be executed in a remote server context.
 */
sealed interface RemoteExecCommand :
    ExtensionAware,
    Named,
    Serializable {
    companion object
}

/**
 * This interface provides a structure for String Representation of Remote Execution Commands.
 * It allows setting the command that will be executed remotely.
 */
interface RemoteExecCommandString : RemoteExecCommand {
    companion object

    val commands: ListProperty<String>
}

/**
 * This interface represents a set of [RemoteExecCommand] objects, possibly nested [RemoteExecCommandCollection].
 * It extends [NamedDomainObjectSet] to provide a named collection of [RemoteExecCommand] objects.
 *
 * IMPORTANT NOTE: this [Set] implementation does not preserve insertion order,
 * rather than iteration order is based on natural order of [Named.getName].
 *
 * If you need specific commands run in order you either add more commands to [RemoteExecCommandString.commands] or
 * declare separate tasks and use Gradle capabilities like [Task.dependsOn],
 * [Task.finalizedBy], [Task.mustRunAfter] etc.
 *
 * IMPORTANT NOTE: I do not plan to replicate above Gradle capabilities in scope of [RemoteExecCommandCollection]
 */
interface RemoteExecCommandCollection :
    RemoteExecCommand,
    NamedDomainObjectSet<RemoteExecCommand> {
    companion object
}
