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

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.useToRun
import xyz.dussim.gradlessh.internal.authMethod
import xyz.dussim.gradlessh.internal.flatten
import xyz.dussim.gradlessh.remote.PasswordAuthenticatedRemote
import xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote
import xyz.dussim.gradlessh.remote.Remote
import xyz.dussim.gradlessh.remote.RemoteAddress
import xyz.dussim.gradlessh.remote.RemoteCollection
import xyz.dussim.gradlessh.remote.RemoteContainer

/**
 * Represents a task that executes a command on a remote machine via SSH.
 */
abstract class SshRemoteExecutionTask : DefaultTask() {
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
     * Represents a property for the remote execution command.
     *
     * This property is used to specify the command to be executed in a remote server context.
     *
     * In case a value is a [RemoteExecCommandCollection],
     * the task will execute all the commands recursively retrieved from the collection.
     *
     * @property command The remote execution command property.
     *
     * @see RemoteExecCommand
     * @see RemoteExecCommandString
     * @see RemoteExecCommandCollection
     * @see RemoteExecCommandContainer
     */
    @get:Input
    abstract val command: Property<RemoteExecCommand>

    /**
     * A boolean property.
     *
     * When set to true, this property ensures that the name of the remote server is appended in the form of
     * `user@host:port|>`
     * to the lines of the standard output the command executed by the [SshRemoteExecutionTask].
     *
     * This might be helpful in scenarios where logs from multiple remote servers are being aggregated,
     * and you would like to know which server's logs you are looking at.
     *
     * This property is [Optional] and defaults to `false`.
     */
    @get:Input
    @get:Optional
    abstract val appendRemoteNameToLines: Property<Boolean>

    init {
        group = "ssh"
        description = "Executes a command on a remote machine via SSH"
    }

    @TaskAction
    fun execute() {
        val remotes = remote.get().flatten()
        val commands = command.get().flatten()

        remotes.forEachIndexed { i, remote ->
            SSHClient().useToRun {
                loadKnownHosts()
                if (i > 0) {
                    println("-".repeat(20))
                }
                executeCommand(remote, commands)
            }
        }
    }

    private fun SSHClient.executeCommand(
        remoteAddress: RemoteAddress,
        commands: Set<RemoteExecCommandString>,
    ) {
        connect(remoteAddress.host, remoteAddress.port)
        remoteAddress.authMethod(this)

        commands.forEach { command ->
            startSession().useToRun { execute(remoteAddress, command) }
        }
    }

    private fun Session.execute(
        remoteAddress: RemoteAddress,
        command: RemoteExecCommandString,
    ) {
        val appendRemoteNameToLines = appendRemoteNameToLines.getOrElse(false)
        val execCommand = exec(command.command)

        execCommand.useToRun {
            inputStream.bufferedReader().forEachLine {
                if (appendRemoteNameToLines) {
                    println("${remoteAddress.address}|> $it")
                } else {
                    println(it)
                }
            }
        }
        if (execCommand.exitStatus != 0) {
            throw CommandExecutionException(execCommand)
        }
    }

    private class CommandExecutionException(
        execCommand: Session.Command,
    ) : IllegalStateException(
            "Command failed with exit status ${execCommand.exitStatus}\n" +
                "ExitMessage: ${execCommand.exitErrorMessage}\n" +
                "Error: ${execCommand.errorStream.bufferedReader().readText().trim()}",
        )
}
