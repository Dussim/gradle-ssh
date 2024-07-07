# gradle-ssh - SSH plugin for Gradle

# Preface

**This plugin is in very early stages, there will be bugs, missing features and breaking changes.**

## Primer

Simple plugin to execute fire and forget ssh commands as Gradle tasks.

It uses the [sshj](https://github.com/hierynomus/sshj) library to execute the commands.

## Features

- Ssh:
    - define and reuse remotes
    - define and reuse commands
    - execute commands on remotes

## Usage

### Apply the plugin

```kotlin
// build.gradle.kts
plugins {
    id("xyz.dussim.gradle-ssh").version("0.0.1")
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
        download,
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
    command = remoteFileCommands["fileCommands"]
}
```

<details>
<summary>Different ways to configure remotes and commands</summary>

Plugin supports typical options to create and configure objects in `remoteExecCommands` and `remotes` containers as well
as some helper methods to lazily register them.

Fore people not familiar with Gradle's Kotlin DSL, here are some examples:

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

## Future planned features

- Ssh:
    - ~~download/upload files via scp~~ done, use `ScpRemoteFileTask`
    - download/upload files via sftp
- Gradle:
    - implement missing features of [sshj](https://github.com/hierynomus/sshj) like custom key providers

## Changelog

### ***gradle-ssh 0.0.2***

- **BREAKING CHANGES**:
    - all existing models properties are not `var` anymore but `Property<*>`
    - some of the methods in `RemoteContainer` and `RemoteExecCommandContainer` were marked as `@Deprecated` and will be
      removed in `0.0.3`.
      If you need that functionality it is not gone, it will just not be as nice to use, refer to
      `PolymorphicDomainObjectContainer` docs.
- Implemented `ScpRemoteFileTask` and way to define upload download commands via respectively `RemoteUploadCommand` and
  `RemoteDownloadCommand`