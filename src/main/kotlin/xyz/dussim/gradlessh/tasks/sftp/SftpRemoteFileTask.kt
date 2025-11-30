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
package xyz.dussim.gradlessh.tasks.sftp

import net.schmizz.sshj.SSHClient
import xyz.dussim.gradlessh.internal.connectAndAuthenticate
import xyz.dussim.gradlessh.internal.toDestFile
import xyz.dussim.gradlessh.internal.toFileSource
import xyz.dussim.gradlessh.remote.RemoteAddress
import xyz.dussim.gradlessh.tasks.AbstractRemoteFileTask
import xyz.dussim.gradlessh.tasks.transfer.DownloadFileContent
import xyz.dussim.gradlessh.tasks.transfer.RemoteFileContent
import xyz.dussim.gradlessh.tasks.transfer.UploadFileContent

/**
 * Represents a task that executes an upload/download command on a remote machine via SFTP.
 *
 * @since 0.0.4
 * */
abstract class SftpRemoteFileTask : AbstractRemoteFileTask() {
    init {
        description = "Transfers files to/from a remote machine via SFTP"
    }

    override fun SSHClient.executeCommand(
        remoteAddress: RemoteAddress,
        files: Set<RemoteFileContent>,
    ) {
        remoteAddress.connectAndAuthenticate(this)
        newSFTPClient().use { sftp ->
            files.forEach { file ->
                when (file) {
                    is UploadFileContent -> {
                        sftp.put(
                            file.toFileSource(),
                            file.remotePath.get(),
                        )
                    }

                    is DownloadFileContent -> {
                        sftp.get(
                            file.remotePath.get(),
                            file.toDestFile(),
                        )
                    }
                }
            }
        }
    }
}
