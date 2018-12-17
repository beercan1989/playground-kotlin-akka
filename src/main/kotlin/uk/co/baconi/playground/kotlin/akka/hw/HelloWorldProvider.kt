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

import akka.actor.ActorSystem
import akka.actor.Scheduler
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.javadsl.AskPattern
import akka.util.Timeout
import java.util.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

class HelloWorldProvider(
        private val timeout: Timeout = DEFAULT_TIMEOUT
) : (ActorSystem) -> CompletionStage<HelloWorldMessage> {

    companion object {
        val DEFAULT_TIMEOUT: Timeout = Timeout.apply(100, TimeUnit.MILLISECONDS)
    }

    fun actorIdProvider(): UUID = UUID.randomUUID()

    fun <T> spawn(actorSystem: ActorSystem, behavior: Behavior<T>, name: String): ActorRef<T> {
        return Adapter.spawn(actorSystem, behavior, name) // TODO - Work out whether we should just spawn anonymously
    }

    fun <T, U> ask(actor: ActorRef<T>, message: (ActorRef<U>) -> T, timeout: Timeout, scheduler: Scheduler): CompletionStage<U> {
        return AskPattern.ask(actor, message, timeout, scheduler)
    }

    override fun invoke(actorSystem: ActorSystem): CompletionStage<HelloWorldMessage> {
        return ask(
                spawn(actorSystem, HelloWorldActor.apply(), "hello-world-${actorIdProvider()}"),
                { respondTo -> HelloWorldRequest(respondTo) },
                timeout,
                actorSystem.scheduler
        )
    }
}