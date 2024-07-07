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
package xyz.dussim.gradlessh.tasks.scp

import net.schmizz.sshj.SSHClient
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.useToRun
import xyz.dussim.gradlessh.internal.connectAndAuthenticate
import xyz.dussim.gradlessh.internal.flatten
import xyz.dussim.gradlessh.internal.toDestFile
import xyz.dussim.gradlessh.internal.toFileSource
import xyz.dussim.gradlessh.remote.PasswordAuthenticatedRemote
import xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote
import xyz.dussim.gradlessh.remote.Remote
import xyz.dussim.gradlessh.remote.RemoteAddress
import xyz.dussim.gradlessh.remote.RemoteCollection
import xyz.dussim.gradlessh.remote.RemoteContainer
import xyz.dussim.gradlessh.tasks.transfer.DownloadFileContent
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommand
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandCollection
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommandContainer
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileContent
import xyz.dussim.gradlessh.tasks.transfer.UploadFileContent

/**
 * Represents a task that executes an upload/download command on a remote machine via SCP.
 *
 * @since 0.0.2
 * */
abstract class ScpRemoteFileTask : DefaultTask() {
    /**
     * Specifies the remote server to execute the commands via SSH.
     *
     * This property is used to specify the remote server(s) on which the command(s) should be executed.
     *
     * In case a value is a [RemoteCollection],
     * the task will execute the command on all recursively retrieved remote servers
     *
     * @property remote The remote server property.
     *
     * @see Remote
     * @see RemoteAddress
     * @see PublicKeyAuthenticatedRemote
     * @see PasswordAuthenticatedRemote
     * @see RemoteCollection
     * @see RemoteContainer
     */
    @get:Input
    abstract val remote: Property<Remote>

    /**
     * Represents a property for the remote file operation command.
     *
     * This property is used to specify the command to be executed in a remote server context.
     *
     * In case a value is a [RemoteFileCommandCollection],
     * the task will execute all the commands recursively retrieved from the collection.
     *
     * @property command The remote execution command property.
     *
     * @see RemoteFileCommand
     * @see UploadFileContent
     * @see DownloadFileContent
     * @see RemoteFileCommandCollection
     * @see RemoteFileCommandContainer
     */
    @get:Input
    abstract val command: Property<RemoteFileCommand>

    init {
        group = "ssh"
        description = "Uploads a file to a remote machine via SCP"
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

    private fun SSHClient.executeCommand(
        remoteAddress: RemoteAddress,
        files: Set<RemoteFileContent>,
    ) {
        remoteAddress.connectAndAuthenticate(this)
        val scpFileTransfer = newSCPFileTransfer()

        files.forEach { file ->
            when (file) {
                is UploadFileContent ->
                    scpFileTransfer.upload(
                        file.toFileSource(),
                        file.remotePath.get(),
                    )

                is DownloadFileContent ->
                    scpFileTransfer.download(
                        file.remotePath.get(),
                        file.toDestFile(),
                    )
            }
        }
    }
}
