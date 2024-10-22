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
package xyz.dussim.gradlessh.internal

import org.gradle.api.NamedDomainObjectSet
import xyz.dussim.gradlessh.remote.Remote
import xyz.dussim.gradlessh.remote.RemoteCollection
import javax.inject.Inject

internal abstract class RemoteCollectionImpl
    @Inject
    constructor(
        private val name: String,
        private val remotes: NamedDomainObjectSet<Remote>,
    ) : RemoteCollection,
        NamedDomainObjectSet<Remote> by remotes {
        override fun getName(): String = name
    }
