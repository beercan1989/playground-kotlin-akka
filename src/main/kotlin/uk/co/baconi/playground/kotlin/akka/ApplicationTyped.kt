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

package uk.co.baconi.playground.kotlin.akka

import akka.stream.ActorMaterializer

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Adapter.toUntyped
import akka.actor.typed.javadsl.Behaviors
import akka.http.javadsl.ConnectHttp

import akka.http.javadsl.Http
import akka.util.Timeout
import uk.co.baconi.playground.kotlin.akka.JsonSupport.withJsonRejectionHandling

import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldActor
import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldController
import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldRoute

import java.util.concurrent.TimeUnit.SECONDS

interface Command
private data class Start(val host: String, val port: Int) : Command

class ApplicationTyped(private val actorSystem: ActorSystem<Command>) {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            ApplicationTyped()
                    .start("127.0.0.1", 8081)
        }
    }

    constructor() : this(
        ActorSystem.create(Behaviors.setup { context ->

            val actor = context.spawn(HelloWorldActor.apply(), "hello-world")

            val controller = HelloWorldController(actor, Timeout.apply(1, SECONDS), context.system.scheduler())

            val route = HelloWorldRoute.apply(controller)

            val unTypedActorSystem = toUntyped(context.system)

            val materializer = ActorMaterializer.create(unTypedActorSystem)

            val flow = withJsonRejectionHandling { route }.flow(unTypedActorSystem, materializer)

            val http = Http.get(unTypedActorSystem)

            Behaviors.receiveMessage<Command> { cmd ->

                when (cmd) {
                    is Start -> {
                        context.system.log().info("Starting server at http://${cmd.host}:${cmd.port}/")
                        http.bindAndHandle(flow, ConnectHttp.toHost(cmd.host, cmd.port), materializer)
                        Behaviors.same()
                    }
                    else -> Behaviors.ignore()
                }

            }
        }, "application")
    )

    fun start(host: String, port: Int): ApplicationTyped {
        actorSystem.tell(Start(host, port))
        return this
    }
}
