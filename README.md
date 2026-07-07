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

- Plugin 0.0.6 requires:
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
    id("xyz.dussim.gradle-ssh").version("0.0.6")
}
```

### Configure the remotes and commands

```kotlin
// build.gradle.kts
val firstRemote = remotes.publicKeyAuthenticated("firstRemote") {
    host.set("first-remote-host")
    user.set("first-remote-username")
    // optionally, defaults to 22
    port.set(40)
}

val secondRemote = remotes.passwordAuthenticated("secondRemote") {
    host.set("second-remote-host")
    user.set("second-remote-username")
    password.set(providers.environmentVariable("PASSWORD"))
}

val bothRemotes = remotes.remoteCollection("bothRemotes", firstRemote, secondRemote)
```

### Configure remote executed commands

```kotlin
// build.gradle.kts
val listFiles = remoteExecCommands.command("listFiles") {
    commands.add("ls")
}

val helloWorld = remoteExecCommands.command("helloWorld") {
    commands.add("echo Hello, World!")
}

val bothCommands = remoteExecCommands.commandCollection("bothCommands", listFiles, helloWorld)
```

### Configure remote upload/download commands

```kotlin
// build.gradle.kts
val downloadFile = remoteFileCommands.downloadFileContent("downloadFile") {
    remotePath.set("directory/file")
    localFile.set(layout.projectDirectory.file("data/downloaded.txt").asFile)
}

val upload = remoteFileCommands.uploadFileContent("upload") {
    localFile.set(layout.projectDirectory.file("data/payload.txt").asFile)
    remotePath.set("")
}

val transfers = remoteFileCommands.fileCommandCollection("transfers", upload, downloadFile)
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
val bothCommands = remoteExecCommands.commandCollection(
    "bothCommands",
    remoteExecCommands.named("listFiles"),
    remoteExecCommands.named("helloWorld")
)

// similar to the above, but for remotes, 
// all those remotes will be lazily created and configured

val remote1 = remotes.publicKeyAuthenticated("remote1") {
    host.set("host1")
    user.set("user1")
}

val remote2 = remotes.publicKeyAuthenticated("remote2") {
    host.set("host2")
    user.set("user2")
}

val bothRemotes = remotes.remoteCollection("bothRemotes", remote1, remote2)

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

- Upgrading to 0.0.6 (breaking):
  - **Why this changed:** the previous `RemoteContainer`, `RemoteExecCommandContainer`, and `RemoteFileCommandContainer`
    types were custom containers that exposed convenience *registering* DSL methods (such as `publicKeyAuthenticated`,
    `command`, `uploadFileContent`, etc.). Those registering extensions relied on Gradle container registration APIs
    that Gradle deprecated and scheduled for removal in a future release. To stay compatible with Gradle 9.x and to
    avoid the deprecation warnings, the plugin no longer ships its own containers with these registering extensions.
  - **What changed:** `RemoteContainer`, `RemoteExecCommandContainer`, and `RemoteFileCommandContainer` were removed.
    The `remotes`, `remoteExecCommands`, and `remoteFileCommands` extensions are now plain, unmodified
    `ExtensiblePolymorphicDomainObjectContainer`s provided directly by Gradle.
  - **Helper methods moved:** the convenience methods that used to live on the custom containers are now top-level Kotlin
    extension functions (declared in the `org.gradle.kotlin.dsl` package, so they are available implicitly in
    `build.gradle.kts`). They now require an explicit name argument, e.g. `remotes.publicKeyAuthenticated("app") { ... }`
    instead of relying on a name inferred by the container.
  - **How to migrate:**
    - Remove any explicit references to the removed types (`RemoteContainer`, `RemoteExecCommandContainer`,
      `RemoteFileCommandContainer`) from your build scripts and imports. You rarely referenced them directly, but if you
      did (for example when passing them around), switch to `ExtensiblePolymorphicDomainObjectContainer<Remote>`,
      `ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>`, and
      `ExtensiblePolymorphicDomainObjectContainer<RemoteFileCommand>` respectively.
    - Make sure every helper call passes an explicit name as its first argument, e.g.
      `remotes.publicKeyAuthenticated("firstRemote") { ... }`, `remoteExecCommands.command("listFiles") { ... }`,
      `remoteFileCommands.uploadFileContent("upload") { ... }`.
    - The available helper functions are unchanged in behavior: `publicKeyAuthenticated`, `passwordAuthenticated`,
      `remoteCollection`, `command`, `commandCollection`, `uploadStringContent`, `uploadFileContent`,
      `downloadFileContent`, and `fileCommandCollection`.
    - No behavioral change is expected at runtime; this is purely an API/structural change driven by Gradle's own
      deprecations.
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

### ***gradle-ssh 0.0.6***

- **BREAKING CHANGES**:
    - Removed `RemoteContainer`, `RemoteExecCommandContainer`, and `RemoteFileCommandContainer`. The `remotes`,
      `remoteExecCommands`, and `remoteFileCommands` extensions are now plain
      `ExtensiblePolymorphicDomainObjectContainer`s.
    - Helper APIs are now top-level extension functions that require an explicit name,
      e.g. `remotes.publicKeyAuthenticated("app") { ... }`.
    - This was driven by deprecations in Gradle itself: the custom containers' *registering* DSL extensions relied on
      Gradle container registration APIs that were deprecated and scheduled for removal, so the plugin switched to plain
      Gradle-provided containers.
- Dependencies updates:
    - Gradle wrapper updated to `9.6.1`
- See the [Upgrade guide](#upgrade-guide) for migration details.

### ***gradle-ssh 0.0.5***

- Libraries updated

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
    - some methods in `RemoteContainer` and `RemoteExecCommandContainer` were marked as `@Deprecated` and will be
      removed in `0.0.3`.
      If you need that functionality it is not gone, it will just not be as nice to use, refer to
      `PolymorphicDomainObjectContainer` docs.
- Implemented `ScpRemoteFileTask` and way to define upload download commands via respectively `RemoteUploadCommand` and
  `RemoteDownloadCommand`
