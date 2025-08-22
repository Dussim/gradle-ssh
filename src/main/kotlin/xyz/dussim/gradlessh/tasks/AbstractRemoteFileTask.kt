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
package xyz.dussim.gradlessh.tasks

import net.schmizz.sshj.SSHClient
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.useToRun
import xyz.dussim.gradlessh.internal.flatten
import xyz.dussim.gradlessh.remote.Remote
import xyz.dussim.gradlessh.remote.RemoteAddress
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommand
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileContent

/**
 * Base task for file transfers to/from a remote machine over SSH-based protocols.
 * Provides common properties and execution flow; subclasses must implement protocol-specific
 * transfer logic in [executeCommand].
 *
 * @since 0.0.4
 */
abstract class AbstractRemoteFileTask : DefaultTask() {
    /**
     * Specifies the remote server to execute the commands via SSH.
     *
     * This property is used to specify the remote server(s) on which the command(s) should be executed.
     *
     * In case a value is a [xyz.dussim.gradlessh.remote.RemoteCollection],
     * the task will execute the command on all recursively retrieved remote servers
     *
     * @property remote The remote server property.
     *
     * @see xyz.dussim.gradlessh.remote.Remote
     * @see xyz.dussim.gradlessh.remote.RemoteAddress
     * @see xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote
     * @see xyz.dussim.gradlessh.remote.PasswordAuthenticatedRemote
     * @see xyz.dussim.gradlessh.remote.RemoteCollection
     * @see xyz.dussim.gradlessh.remote.RemoteContainer
     */
    @get:Input
    abstract val remote: Property<Remote>

    /**
     * Represents a property for the remote file operation command.
     *
     * This property is used to specify the command to be executed in a remote server context.
     *
     * In case a value is a [xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandCollection],
     * the task will execute all the commands recursively retrieved from the collection.
     *
     * @property command The remote execution command property.
     *
     * @see xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommand
     * @see xyz.dussim.gradlessh.tasks.transfer.UploadFileContent
     * @see xyz.dussim.gradlessh.tasks.transfer.DownloadFileContent
     * @see xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandCollection
     * @see xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandContainer
     */
    @get:Input
    abstract val command: Property<RemoteFileCommand>

    init {
        group = "ssh"
    }

    @TaskAction
    fun execute() {
        val remotes = remote.flatten()
        val commands = command.flatten()

        remotes.forEachIndexed { i, remote ->
            SSHClient().useToRun {
                loadKnownHosts()
                useCompression()
                if (i > 0) {
                    println("-".repeat(20))
                }
                executeCommand(remote, commands)
            }
        }
    }

    protected abstract fun SSHClient.executeCommand(
        remoteAddress: RemoteAddress,
        files: Set<RemoteFileContent>,
    )
}
