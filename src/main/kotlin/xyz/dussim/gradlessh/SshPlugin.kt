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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.newInstance
import xyz.dussim.gradlessh.remote.RemoteContainer
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandContainer
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandContainer

internal class SshPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            extensions.add(
                publicType = RemoteContainer::class,
                name = "remotes",
                extension = objects.newInstance(),
            )
            extensions.add(
                publicType = RemoteExecCommandContainer::class,
                name = "remoteExecCommands",
                extension = objects.newInstance(),
            )
            extensions.add(
                publicType = RemoteFileCommandContainer::class,
                name = "remoteFileCommands",
                extension = objects.newInstance(),
            )
        }
}
