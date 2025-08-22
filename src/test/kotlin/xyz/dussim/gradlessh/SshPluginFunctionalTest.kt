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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SshPluginFunctionalTest {
    @Test
    fun `plugin applies and registers extensions`(
        @TempDir tempDir: File,
    ) {
        // Given a minimal settings and build file applying our plugin
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            plugins { id("xyz.dussim.gradle-ssh") }

            // verify the extensions exist and are accessible
            tasks.register("verifyExtensions") {
                doLast {
                    val exts = project.extensions
                    check(exts.getByName("remotes") != null) { "remotes extension missing" }
                    check(exts.getByName("remoteExecCommands") != null) { "remoteExecCommands extension missing" }
                    check(exts.getByName("remoteFileCommands") != null) { "remoteFileCommands extension missing" }
                    println("[TEST] EXTENSIONS_OK")
                }
            }
            """.trimIndent(),
        )

        // When
        val result =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("verifyExtensions", "--stacktrace")
                .build()

        // Then
        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyExtensions")?.outcome)
        assertTrue(result.output.contains("[TEST] EXTENSIONS_OK"))
    }

    @Test
    fun `plugin DSL can configure remotes and commands without executing`(
        @TempDir tempDir: File,
    ) {
        // Ensure plugin config DSL compiles and config phase runs without errors
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "dsl-config-test"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote
            import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandString

            plugins { id("xyz.dussim.gradle-ssh") }

            // Define one remote via DSL helper and ensure no exceptions in configuration
            val app by remotes.publicKeyAuthenticated {
                host.set("localhost")
                port.set(22)
                user.set("tester")
            }

            // Define one exec command via DSL helper (no execution)
            val echoCmd by remoteExecCommands.command {
                commands.add("echo hello")
            }

            tasks.register("verifyDsl") {
                doLast {
                    // ensure providers have names and are registered
                    check(app.name == "app") { "remote name not assigned correctly" }
                    check(echoCmd.name == "echoCmd") { "command name not assigned correctly" }
                    println("[TEST] DSL_OK")
                }
            }
            """.trimIndent(),
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("verifyDsl", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyDsl")?.outcome)
        assertTrue(result.output.contains("[TEST] DSL_OK"))
    }

    @Test
    fun `remote defaults and address formatting`(
        @TempDir tempDir: File,
    ) {
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "remote-defaults"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.remote.PublicKeyAuthenticatedRemote

            plugins { id("xyz.dussim.gradle-ssh") }

            val r by remotes.publicKeyAuthenticated {
                host.set("example.com")
                user.set("alice")
                // not setting port/connectionTimeout/readTimeout to test defaults
            }

            tasks.register("verifyRemoteDefaults") {
                doLast {
                    val remote = remotes.named("r").get() as PublicKeyAuthenticatedRemote
                    check(remote.port.get() == 22) { "default port not 22" }
                    check(remote.connectionTimeout.get() == 10_000) { "default connectionTimeout not 10000" }
                    check(remote.readTimeout.get() == 30_000) { "default readTimeout not 30000" }
                    check(remote.address == "${'$'}{remote.user.get()}@${'$'}{remote.host.get()}:${'$'}{remote.port.get()}") { "address formatting mismatch" }
                    println("[TEST] REMOTE_DEFAULTS_OK")
                }
            }
            """.trimIndent(),
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("verifyRemoteDefaults", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyRemoteDefaults")?.outcome)
        assertTrue(result.output.contains("[TEST] REMOTE_DEFAULTS_OK"))
    }

    @Test
    fun `create remote collection with multiple remotes`(
        @TempDir tempDir: File,
    ) {
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "remote-collection"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.remote.Remote

            plugins { id("xyz.dussim.gradle-ssh") }

            val app by remotes.publicKeyAuthenticated {
                host.set("h1")
                user.set("u1")
            }
            val db by remotes.passwordAuthenticated {
                host.set("h2")
                user.set("u2")
                password.set("secret")
            }

            val cluster by remotes.remoteCollection(app, db)

            tasks.register("verifyRemoteCollection") {
                doLast {
                    val names = mutableSetOf<String>()
                    cluster.get().forEach { names += it.name }
                    check(names.containsAll(listOf("app", "db"))) { "collection does not contain expected remotes: ${'$'}names" }
                    println("[TEST] REMOTE_COLLECTION_OK")
                }
            }
            """.trimIndent(),
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("verifyRemoteCollection", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyRemoteCollection")?.outcome)
        assertTrue(result.output.contains("[TEST] REMOTE_COLLECTION_OK"))
    }

    @Test
    fun `exec command collection with multiple commands`(
        @TempDir tempDir: File,
    ) {
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "exec-collection"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommand
            import xyz.dussim.gradlessh.tasks.exec.RemoteExecCommandString

            plugins { id("xyz.dussim.gradle-ssh") }

            val echo1 by remoteExecCommands.command { commands.add("echo one") }
            val echo2 by remoteExecCommands.command { commands.add("echo two"); commands.add("echo two again") }
            val all by remoteExecCommands.commandCollection(echo1, echo2)

            tasks.register("verifyExecCollection") {
                doLast {
                    val coll = all.get()
                    val names = mutableSetOf<String>()
                    coll.forEach { names += it.name }
                    check(names.containsAll(listOf("echo1", "echo2"))) { "expected command names not present: ${'$'}names" }
                    val c1 = echo1.get()
                    val c2 = echo2.get()
                    check(c1.commands.get().size == 1) { "echo1 commands size mismatch" }
                    check(c2.commands.get().size == 2) { "echo2 commands size mismatch" }
                    println("[TEST] EXEC_COLLECTION_OK")
                }
            }
            """.trimIndent(),
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("verifyExecCollection", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyExecCollection")?.outcome)
        assertTrue(result.output.contains("[TEST] EXEC_COLLECTION_OK"))
    }

    @Test
    fun `file command container upload download and collection`(
        @TempDir tempDir: File,
    ) {
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "file-commands"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.tasks.transfer.RemoteFileCommand
            import xyz.dussim.gradlessh.tasks.transfer.RemoteUploadCommandString
            import xyz.dussim.gradlessh.tasks.transfer.RemoteUploadCommandFile
            import xyz.dussim.gradlessh.tasks.transfer.RemoteDownloadCommandFile

            plugins { id("xyz.dussim.gradle-ssh") }

            val upStr by remoteFileCommands.uploadStringContent {
                remotePath.set("/tmp/hello.txt")
                textContent.set("HELLO")
                fileName.set("hello.txt")
            }
            val upFile by remoteFileCommands.uploadFileContent {
                remotePath.set("/tmp/from-file.bin")
                localFile.set(file("local.bin"))
            }
            val downFile by remoteFileCommands.downloadFileContent {
                remotePath.set("/var/log/app.log")
                localFile.set(file("app.log"))
            }

            val allFiles by remoteFileCommands.fileCommandCollection(upStr, upFile, downFile)

            tasks.register("verifyFileCommands") {
                doLast {
                    val coll = allFiles.get()
                    val names = mutableSetOf<String>()
                    coll.forEach { names += it.name }
                    check(names.containsAll(listOf("upStr", "upFile", "downFile"))) { "expected command names not present: ${'$'}names" }
                    val s = upStr.get()
                    check(s.remotePath.get() == "/tmp/hello.txt")
                    check(s.textContent.get() == "HELLO")
                    check(s.fileName.get() == "hello.txt")
                    val f = upFile.get()
                    check(f.remotePath.get() == "/tmp/from-file.bin")
                    check(f.localFile.get().name == "local.bin")
                    val d = downFile.get()
                    check(d.remotePath.get() == "/var/log/app.log")
                    check(d.localFile.get().name == "app.log")
                    println("[TEST] FILE_COMMANDS_OK")
                }
            }
            """.trimIndent(),
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("verifyFileCommands", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyFileCommands")?.outcome)
        assertTrue(result.output.contains("[TEST] FILE_COMMANDS_OK"))
    }

    @Test
    fun `configuration cache is reusable with plugin exec task (skipped)`(
        @TempDir tempDir: File,
    ) {
        // Given a project with our plugin and an SshRemoteExecutionTask that is skipped at execution time
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "config-cache-plugin-exec"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.tasks.exec.SshRemoteExecutionTask

            plugins { id("xyz.dussim.gradle-ssh") }

            // Define a remote and a command via the plugin DSL
            val app by remotes.publicKeyAuthenticated {
                host.set("localhost")
                port.set(22)
                user.set("tester")
            }
            val echo by remoteExecCommands.command {
                commands.add("echo hello from plugin task")
            }

            // Register the plugin task but skip its execution while keeping it in the graph
            val sshExec = tasks.register<SshRemoteExecutionTask>("sshExec") {
                remote.set(app)
                command.set(echo)
                appendRemoteNameToLines.set(true)
                onlyIf { false } // ensures task is considered but not executed
            }

            tasks.register("runAll") {
                dependsOn(sshExec)
                doLast { println("[TEST] CC_EXEC_TASK_OK") }
            }
            """.trimIndent(),
        )

        fun containsStored(output: String): Boolean =
            listOf(
                "Configuration cache entry stored.",
                "Configuration cache stored.",
                "Stored configuration cache entry",
            ).any { output.contains(it) }

        fun containsReused(output: String): Boolean =
            listOf(
                "Configuration cache entry reused.",
                "Reusing configuration cache.",
                "Reused configuration cache entry",
            ).any { output.contains(it) }

        // First run should store the configuration cache
        val first =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("runAll", "--configuration-cache", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, first.task(":runAll")?.outcome)
        assertTrue(first.output.contains("[TEST] CC_EXEC_TASK_OK"))
        assertTrue(
            containsStored(first.output),
            "Expected configuration cache to be stored. Output was:\n${first.output}",
        )

        // Second run should reuse the configuration cache
        val second =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("runAll", "--configuration-cache", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, second.task(":runAll")?.outcome)
        assertTrue(second.output.contains("[TEST] CC_EXEC_TASK_OK"))
        assertTrue(
            containsReused(second.output),
            "Expected configuration cache to be reused. Output was:\n${second.output}",
        )
    }

    @Test
    fun `configuration cache is reusable with plugin file task (skipped)`(
        @TempDir tempDir: File,
    ) {
        // Given a project with our plugin and an SftpRemoteFileTask that is skipped at execution time
        writeFile(
            File(tempDir, "settings.gradle.kts"),
            """
            rootProject.name = "config-cache-plugin-file"
            """.trimIndent(),
        )

        writeFile(
            File(tempDir, "build.gradle.kts"),
            """
            import xyz.dussim.gradlessh.tasks.sftp.SftpRemoteFileTask

            plugins { id("xyz.dussim.gradle-ssh") }

            // Define a remote and a file command via the plugin DSL
            val app by remotes.passwordAuthenticated {
                host.set("localhost")
                port.set(22)
                user.set("tester")
                password.set("secret")
            }
            val up by remoteFileCommands.uploadStringContent {
                remotePath.set("/tmp/hello.txt")
                textContent.set("HELLO")
                fileName.set("hello.txt")
            }

            // Register the plugin file task but skip its execution while keeping it in the graph
            val sftp = tasks.register<SftpRemoteFileTask>("sftpTransfer") {
                remote.set(app)
                command.set(up)
                onlyIf { false } // ensures task is considered but not executed
            }

            tasks.register("runTransfers") {
                dependsOn(sftp)
                doLast { println("[TEST] CC_FILE_TASK_OK") }
            }
            """.trimIndent(),
        )

        fun containsStored(output: String): Boolean =
            listOf(
                "Configuration cache entry stored.",
                "Configuration cache stored.",
                "Stored configuration cache entry",
            ).any { output.contains(it) }

        fun containsReused(output: String): Boolean =
            listOf(
                "Configuration cache entry reused.",
                "Reusing configuration cache.",
                "Reused configuration cache entry",
            ).any { output.contains(it) }

        // First run should store the configuration cache
        val first =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("runTransfers", "--configuration-cache", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, first.task(":runTransfers")?.outcome)
        assertTrue(first.output.contains("[TEST] CC_FILE_TASK_OK"))
        assertTrue(
            containsStored(first.output),
            "Expected configuration cache to be stored. Output was:\n${first.output}",
        )

        // Second run should reuse the configuration cache
        val second =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("runTransfers", "--configuration-cache", "--stacktrace")
                .build()

        assertEquals(TaskOutcome.SUCCESS, second.task(":runTransfers")?.outcome)
        assertTrue(second.output.contains("[TEST] CC_FILE_TASK_OK"))
        assertTrue(
            containsReused(second.output),
            "Expected configuration cache to be reused. Output was:\n${second.output}",
        )
    }

    private fun writeFile(
        file: File,
        content: String,
    ) {
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}
