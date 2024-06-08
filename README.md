# gradle-ssh - SSH plugin for Gradle

# Primer

Simple plugin to execute fire and forget ssh commands as Gradle tasks.

It uses the [sshj](https://github.com/hierynomus/sshj) library to execute the commands.

# Usage

## Apply the plugin

<details open>
<summary>Kotlin</summary>

```kotlin
// build.gradle.kts
plugins {
    id("xyz.dussim.gradle-ssh").version("0.0.1")
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
// build.gradle
plugins {
    id 'xyz.dussim.gradle-ssh' version '0.0.1'
}
```

</details>

## Configure the remotes

<details open>
<summary>Kotlin</summary>

```kotlin
// build.gradle.kts
remotes {
    // will use the public key authentication with default key location of ~/.ssh/id_rsa and ~/.ssh/id_dsa
    publicKeyAuthenticated("first-remote") {
        host = "first-remote-host"
        user = "first-remote-username"
        // optionally, defaults to 22
        port = 40
    }
  
    passwordAuthenticated("second-remote") {
        host = "second-remote-host"
        user = "second-remote-username"
        password = providers.environmentVariable("PASSWORD").get()
    }
}
```

</details>

## Configure task

<details open>
<summary>Kotlin</summary>

```kotlin
// build.gradle.kts
tasks.register<SshRemoteExecutionTask>("listFilesAtRemote") {
    remote = remotes.getByName("first-remote")
    command = "ls -la"
}
```

</details>

## Run the task

```shell
./gradlew :listFilesAtRemote
```

## Features

- Ssh:
    - execute fire and forget ssh commands
- Gradle:
    - configuration-cache compatible

## Future planned features

- Ssh:
    - download/upload files
- Gradle:
    - implement missing features of [sshj](https://github.com/hierynomus/sshj) like custom key providers
    - add tags to remotes to group them and add ability for tasks to accept collection of remotes as a target