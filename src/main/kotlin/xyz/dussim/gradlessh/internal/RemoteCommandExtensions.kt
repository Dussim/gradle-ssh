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

import org.gradle.api.provider.Provider
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommand
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandCollection
import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandString

internal fun RemoteExecCommand.flatten(): Set<RemoteExecCommandString> =
    when (this) {
        is RemoteExecCommandCollection -> flatMapTo(mutableSetOf(), RemoteExecCommand::flatten)
        is RemoteExecCommandString -> setOf(this)
    }

internal fun Provider<out RemoteExecCommand>.flatten(): Set<RemoteExecCommandString> = get().flatten()
