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
package xyz.dussim.gradlessh.tasks.exec

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registering
import xyz.dussim.gradlessh.internal.RemoteExecCommandCollectionImpl
import xyz.dussim.gradlessh.internal.RemoteExecCommandStringImpl
import javax.inject.Inject

/**
 * The [RemoteExecCommandContainer] class is a container
 * extending [ExtensiblePolymorphicDomainObjectContainer] for managing different types of remote commands.
 * It provides helper methods to lazily and eagerly create [RemoteExecCommand] instances.
 *
 * @see RemoteExecCommand
 * @see RemoteExecCommandString
 * @see RemoteExecCommandCollection
 */
@Suppress("LeakingThis")
abstract class RemoteExecCommandContainer
    @Inject
    constructor(
        factory: ObjectFactory,
    ) : ExtensiblePolymorphicDomainObjectContainer<RemoteExecCommand> by factory.polymorphicDomainObjectContainer(
            RemoteExecCommand::class.java,
        ),
        ExtensionAware {
        companion object;

        init {
            registerFactory(RemoteExecCommandString::class.java) { name ->
                factory.newInstance<RemoteExecCommandStringImpl>(name)
            }
            registerFactory(RemoteExecCommandCollection::class.java) { name ->
                factory.newInstance<RemoteExecCommandCollectionImpl>(
                    name,
                    factory.namedDomainObjectSet(RemoteExecCommand::class.java),
                )
            }
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
        fun command(configuration: RemoteExecCommandString.() -> Unit) = registering(RemoteExecCommandString::class, configuration)

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
        fun commandCollection(configuration: RemoteExecCommandCollection.() -> Unit) =
            registering(RemoteExecCommandCollection::class, configuration)

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
        fun commandCollection(vararg command: NamedDomainObjectProvider<out RemoteExecCommand>) =
            commandCollection { command.forEach(::addLater) }
    }
