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
package xyz.dussim.gradlessh

import net.schmizz.sshj.SSHClient
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.useToRun

abstract class SshRemoteExecutionTask : DefaultTask() {
    @get:Input
    abstract val remote: Property<Remote>

    @get:Input
    abstract val command: Property<String>

    @get:Input
    @get:Optional
    abstract val appendRemoteNameToLines: Property<Boolean>

    init {
        group = "ssh"
        description = "Executes a command on a remote machine via SSH"
    }

    @TaskAction
    fun execute() {
        val appendRemoteNameToLines = appendRemoteNameToLines.getOrElse(false)
        val remote = remote.get()
        val command = command.get()
        println("Executing '$command' at ${remote.address}")

        SSHClient().useToRun {
            loadKnownHosts()
            connect(remote.host, remote.port)
            when (remote) {
                is PasswordAuthenticatedRemote -> authPassword(remote.user, remote.password)
                is PublicKeyAuthenticatedRemote -> authPublickey(remote.user)
            }
            startSession().useToRun {
                val execCommand = exec(command)
                execCommand.useToRun {
                    inputStream.bufferedReader().forEachLine {
                        if (appendRemoteNameToLines) {
                            println("${remote.address}|> $it")
                        } else {
                            println(it)
                        }
                    }
                }
                if (execCommand.exitStatus != 0) {
                    throw IllegalStateException(
                        "Command failed with exit status ${execCommand.exitStatus}\n" +
                            "ExitMessage: ${execCommand.exitErrorMessage}\n" +
                            "Error: ${execCommand.errorStream.bufferedReader().readText().trim()}",
                    )
                }
            }
        }
    }
}
