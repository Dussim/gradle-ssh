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

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import xyz.dussim.gradlessh.internal.RemoteExecCommandCollectionImpl
import xyz.dussim.gradlessh.internal.RemoteExecCommandStringImpl

/**
 * The [RemoteExecCommandContainer] class is a container
 * extending [ExtensiblePolymorphicDomainObjectContainer] for managing different types of remote commands.
 * It provides helper methods to lazily and eagerly create [RemoteExecCommand] instances.
 *
 * @see RemoteExecCommand
 * @see RemoteExecCommandString
 * @see RemoteExecCommandCollection
 */
class RemoteExecCommandContainer internal constructor(
    private val container: ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>,
    project: Project,
) : ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand> by container {
    companion object;

    init {
        container.registerFactory(RemoteExecCommandString::class.java) { name ->
            RemoteExecCommandStringImpl(name = name)
        }
        container.registerFactory(RemoteExecCommandCollection::class.java) { name ->
            RemoteExecCommandCollectionImpl(name = name, commands = project.container<RemoteExecCommand>())
        }
    }

    /**
     * Register a [RemoteExecCommandString] instance with the given [name] and [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this command will be string "differentName"
     * val someCommand by remoteExecCommands.command("differentName") { /* configuration */ }
     * ```
     * @param name The name of the command.
     * @param configuration The configuration of the command.
     *
     * @return The provider for the registered [RemoteExecCommandString].
     * */
    fun command(
        name: String,
        configuration: RemoteExecCommandString.() -> Unit,
    ): NamedDomainObjectProvider<RemoteExecCommandString> {
        return container.register<RemoteExecCommandString>(name, configuration)
    }

    /**
     * Register a [RemoteExecCommandString] instance with the given [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this command will be string "someCommand"
     * val someCommand by remoteExecCommands.command { /* configuration */ }
     * ```
     * @param configuration The configuration of the command.
     *
     * @return The registering provider for [RemoteExecCommandString].
     * */
    fun command(
        configuration: RemoteExecCommandString.() -> Unit,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>, RemoteExecCommandString> {
        return container.registering(RemoteExecCommandString::class, configuration)
    }

    /**
     * Register a [RemoteExecCommandCollection] instance with the given [name] and [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this command will be string "differentName"
     * val someCommand by remoteExecCommands.commandCollection("differentName"){ /* configuration */ }
     * ```
     * @param name The name of the command.
     * @param configuration The configuration of the command.
     *
     * @return The provider for the registered [RemoteExecCommandCollection].
     * */
    fun commandCollection(
        name: String,
        configuration: RemoteExecCommandCollection.() -> Unit,
    ): NamedDomainObjectProvider<RemoteExecCommandCollection> {
        return container.register<RemoteExecCommandCollection>(name, configuration)
    }

    /**
     * Register a [RemoteExecCommandCollection] instance with the given [configuration].
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * // name of this command will be string "someCommand"
     * val someCommand by remoteExecCommands.commandCollection { /* configuration */ }
     * ```
     * @param configuration The configuration of the command.
     *
     * @return The registering provider for [RemoteExecCommandCollection].
     * */
    fun commandCollection(
        configuration: RemoteExecCommandCollection.() -> Unit,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>, RemoteExecCommandCollection> {
        return container.registering(RemoteExecCommandCollection::class, configuration)
    }

    /**
     * Register a [RemoteExecCommand] instance with the given [name] and [command] instances.
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val command1: RemoteExecCommand= remoteExecCommands.getByName("someName1")
     * val command1: RemoteExecCommand = remoteExecCommands.getByName("someName2")
     * // name of this command will be string "differentName"
     * val someCommand by remoteExecCommands.commandCollection("differentName", command1, command2)
     * ```
     * @param name The name of the command.
     * @param command The [RemoteExecCommand] instances to eagerly add to registered [RemoteExecCommandCollection].
     *
     * @return The provider for the registered [RemoteExecCommand].
     * */
    fun commandCollection(
        name: String,
        vararg command: RemoteExecCommand,
    ): NamedDomainObjectProvider<RemoteExecCommandCollection> {
        return container.register<RemoteExecCommandCollection>(name) { this.addAll(command) }
    }

    /**
     * Register a [RemoteExecCommand] instance with the given [command] instances.
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val command1: RemoteExecCommand= remoteExecCommands.getByName("someName1")
     * val command1: RemoteExecCommand = remoteExecCommands.getByName("someName2")
     * // name of this command will be string "someCommand"
     * val someCommand by remoteExecCommands.commandCollection(command1, command2)
     * ```
     * @param command The [RemoteExecCommand] instances to eagerly add to registered [RemoteExecCommandCollection].
     *
     * @return The registering provider for [RemoteExecCommandCollection].
     * */
    fun commandCollection(
        vararg command: RemoteExecCommand,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>, RemoteExecCommandCollection> {
        return commandCollection { this.addAll(command) }
    }

    /**
     * Register a [RemoteExecCommand] instance with the given [name] and [command] instances.
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val command1: NamedDomainObjectProvider<RemoteExecCommand> = remoteExecCommands.command("someName1") { /* configuration */ }
     * val command1: NamedDomainObjectProvider<RemoteExecCommand> = remoteExecCommands.command("someName2") { /* configuration */ }
     * // name of this command will be string "differentName"
     * val someCommand by remoteExecCommands.commandCollection("differentName", command1, command2)
     * ```
     * @param name The name of the command.
     * @param command The [RemoteExecCommand] instance providers to lazily add to registered [RemoteExecCommandCollection].
     *
     * @return The provider for the registered [RemoteExecCommand].
     * */
    fun commandCollection(
        name: String,
        vararg command: Provider<out RemoteExecCommand>,
    ): NamedDomainObjectProvider<RemoteExecCommandCollection> {
        return commandCollection(name) { command.forEach(::addLater) }
    }

    /**
     * Register a [RemoteExecCommand] instance with the given [command] instances.
     * Instance is lazily created when needed.
     *
     * ```kotlin
     * val command1: NamedDomainObjectProvider<RemoteExecCommand> = remoteExecCommands.command("someName1") { /* configuration */ }
     * val command1: NamedDomainObjectProvider<RemoteExecCommand> = remoteExecCommands.command("someName2") { /* configuration */ }
     * // name of this command will be string "someCommand"
     * val someCommand by remoteExecCommands.commandCollection(command1, command2)
     * ```
     * @param command The [RemoteExecCommand] instance providers to lazily add to registered [RemoteExecCommandCollection].
     *
     * @return The provider for the registered [RemoteExecCommand].
     * */
    fun commandCollection(
        vararg command: Provider<out RemoteExecCommand>,
    ): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand>, RemoteExecCommandCollection> {
        return commandCollection { command.forEach(::addLater) }
    }
}
