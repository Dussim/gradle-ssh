# gradle-ssh - SSH plugin for Gradle

### [Gradle Plugin Portal Link](https://plugins.gradle.org/plugin/xyz.dussim.gradle-ssh)

# Preface

**This plugin is in very early stages; there will be bugs, missing features and breaking changes.**

## Primer

Simple plugin to execute fire and forget ssh commands as Gradle tasks.

It uses the [sshj](https://github.com/hierynomus/sshj) library to execute the commands.

## Features

- Ssh:
    - define and reuse remotes
    - define and reuse commands
    - execute commands on remotes

## Compatibility and requirements

- Plugin 0.0.4 requires:
  - Gradle 9.x (minimum 9.0)
  - Java 17+
  - Kotlin 2.2.0+ (for Kotlin DSL build scripts)
- Runtime libraries bundled by the plugin:
  - sshj 0.40.0
  - jzlib 1.1.3 (compression)
- SSH prerequisites and behavior:
  - Public key authentication uses your default SSH agent/keys. Custom key providers are not yet exposed by the DSL (planned); if you need a specific key, configure your agent accordingly or use password-auth remotes.
  - Remote/command/file-command collections do NOT preserve insertion order; iteration is based on the natural order of names. To enforce execution order:
    - Put multiple shell lines inside a single RemoteExecCommandString.commands, or
    - Split into separate tasks and use Gradle task ordering (dependsOn, mustRunAfter, finalizedBy).

## Usage

### Apply the plugin

```kotlin
// build.gradle.kts
plugins {
    id("xyz.dussim.gradle-ssh").version("0.0.4")
}
```

### Configure the remotes and commands

```kotlin
// build.gradle.kts
remotes {
    val firstRemote by publicKeyAuthenticated {
        host = "first-remote-host"
        user = "first-remote-username"
        // optionally, defaults to 22
        port = 40
    }

    val secondRemote by passwordAuthenticated {
        host = "second-remote-host"
        user = "second-remote-username"
        password = providers.environmentVariable("PASSWORD").get()
    }

    val bothRemotes by remoteCollection(firstRemote, secondRemote)
}
```

### Configure remote executed commands

```kotlin
// build.gradle.kts
remoteExecCommands {
    val listFiles by command {
        command = "ls"
    }

    val helloWorld by command {
        command = "echo Hello, World!"
    }

    val bothCommands by commandCollection(listFiles, helloWorld)
}
```

### Configure remote upload/download commands

```kotlin
// build.gradle.kts
remoteFileCommands {
    val downloadFile by downloadFileContent {
        remotePath = "directory/file"
        localFile = layout.projectDirectory.file("data/downloaded.txt").asFile
    }

    val upload by uploadFileContent {
        localFile = layout.projectDirectory.file("data/payload.txt").asFile
        remotePath = ""
    }

    val transfers by fileCommandCollection(
        upload,
        downloadFile,
    )
}
```

### Configure tasks

```kotlin
// build.gradle.kts
tasks.register<SshRemoteExecutionTask>("remoteTasks") {
    remote = remotes["bothRemotes"]
    command = remoteExecCommands["bothCommands"]
    appendRemoteNameToLines = true
}

tasks.register<ScpRemoteFileTask>("fileCommands") {
    remote = remotes["firstRemote"]
    command = remoteFileCommands["transfers"]
}
```

<details>
<summary>Different ways to configure remotes and commands</summary>

Plugin supports typical options to create and configure objects in `remoteExecCommands` and `remotes` containers as well
as some helper methods to lazily register them.

For people not familiar with Gradle's Kotlin DSL, here are some examples:

```kotlin
// build.gradle.kts

// top level declaration, this object is lazily created and configured
val bothCommands by remoteExecCommands.commandCollection(
    remoteExecCommands.named("listFiles"),
    remoteExecCommands.named("helloWorld")
)

// similar to the above, but for remotes, 
// all those remotes will be lazily created and configured

val remote1 by remotes.publicKeyAuthenticated {
    host = "host1"
    user = "user1"
}

val remote2 by remotes.publicKeyAuthenticated {
    host = "host2"
    user = "user2"
}

val bothRemotes by remotes.remoteCollection(remote1, remote2)

// those top level declarations can be used in tasks configuration

tasks.register<SshRemoteExecutionTask>("remoteTasks") {
    remote = bothRemotes
    command = bothCommands
    appendRemoteNameToLines = true
}
```

</details>

### Run the task

```shell
./gradlew :remoteTasks
```

<details>
<summary>Output</summary>

```shell
> Task :remoteTasks
first-remote-username@first-remote-host:40|> Hello, World!
first-remote-username@first-remote-host:40|> file1.txt
first-remote-username@first-remote-host:40|> file2.sh
--------------------
second-remote-username@second-remote-host:22|> Hello, World!
second-remote-username@second-remote-host:22|> script.sh
second-remote-username@second-remote-host:22|> config.json
```

</details>

## Planned features

- Ssh:
    - ~~download/upload files via scp~~ done, use `ScpRemoteFileTask`
    - ~~download/upload files via sftp~~ done, use `SftpRemoteFileTask`
- Gradle:
    - implement missing features of [sshj](https://github.com/hierynomus/sshj) like custom key providers

## Upgrade guide

- Upgrading to 0.0.4 (breaking):
  - Requires Gradle 9.x, Java 17+, Kotlin 2.2.0+.
  - Remove usages of deprecated APIs previously scheduled for removal; use the container DSL helpers shown in the Usage section (publicKeyAuthenticated, passwordAuthenticated, command, commandCollection, fileCommandCollection, etc.).
  - SFTP is now available via `SftpRemoteFileTask`. You can continue to use `ScpRemoteFileTask` or migrate if your environment prefers SFTP.
  - Note: Collections (remotes/commands/file-commands) are sets; iteration order is by name, not insertion. If you relied on implicit order, switch to explicit task ordering or consolidate multi-line commands into a single `RemoteExecCommandString`.
- Upgrading to 0.0.3 (breaking):
  - All methods marked `@Deprecated` in earlier versions were removed. Where you previously relied on convenience creation methods, prefer the registering DSL, or use `PolymorphicDomainObjectContainer` APIs directly.
- Upgrading to 0.0.2 (breaking API):
  - All model properties were migrated from mutable vars to Gradle `Property<T>` types.
    - Before: `host = "example.com"`
    - After:  `host.set("example.com")`
    - Apply the same pattern to `user`, `port`, `password`, `remotePath`, `localFile`, etc.

## Changelog

### ***gradle-ssh 0.0.4***

- **BREAKING CHANGES**:
    - Library is now targeting Gradle 9.x
    - The minimum supported Java version is now 17
    - The minimum supported kotlin version is now 2.2.0
- New features:
    - Added SFTP file transfers via `SftpRemoteFileTask` (upload and download)
- Dependencies updates:
    - `com.hierynomus:sshj` from `0.39.0` to `0.40.0`

### ***gradle-ssh 0.0.3***

- **BREAKING CHANGES**:
    - `@Deprecated` methods were removed
- Dependencies updates:
    - `com.hierynomus:sshj` from `0.38.0` to `0.39.0`

### ***gradle-ssh 0.0.2***

- **BREAKING CHANGES**:
    - all existing models properties are not `var` anymore but `Property<*>`
    - some of the methods in `RemoteContainer` and `RemoteExecCommandContainer` were marked as `@Deprecated` and will be
      removed in `0.0.3`.
      If you need that functionality it is not gone, it will just not be as nice to use, refer to
      `PolymorphicDomainObjectContainer` docs.
- Implemented `ScpRemoteFileTask` and way to define upload download commands via respectively `RemoteUploadCommand` and
  `RemoteDownloadCommand`