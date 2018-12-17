/*
 * Copyright 2018 James Bacon
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

package uk.co.baconi.playground.kotlin.akka.hw

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors

interface HelloWorldCommand
data class HelloWorldRequest(val respondTo: ActorRef<HelloWorldMessage>) : HelloWorldCommand

interface HelloWorldResult
data class HelloWorldMessage(val message: String) : HelloWorldResult

object HelloWorldActor {

    fun apply(): Behavior<HelloWorldCommand> = Behaviors.receive { _, request ->
        when(request) {
            is HelloWorldRequest -> {
                request.respondTo.tell(HelloWorldMessage("Hello World"))
                Behaviors.same<HelloWorldCommand>()
            }
            else -> {
                Behaviors.unhandled<HelloWorldCommand>()
            }
        }
    }
}