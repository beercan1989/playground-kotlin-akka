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

import akka.actor.Scheduler
import akka.stream.ActorMaterializer

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Adapter.toUntyped

import akka.http.javadsl.Http
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.server.Route
import akka.util.Timeout
import uk.co.baconi.playground.kotlin.akka.JsonSupport.withJsonRejectionHandling

import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldActor
import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldCommand
import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldController
import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldRoute

import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit.SECONDS

class Application {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Application()
                    .startHelloWorldServer("127.0.0.1", 8080)
        }
    }

    private val actorSystem: ActorSystem<HelloWorldCommand> = ActorSystem.create(HelloWorldActor.apply(),"application")

    private val actorScheduler: Scheduler = actorSystem.scheduler()
    private val helloWorldTimeout: Timeout = Timeout.apply(1, SECONDS) // TODO - Extract into config

    // Probably not done the typed actor setup right given I'm doing this, need to do further reading.
    private val helloWorldController = HelloWorldController(actorSystem, helloWorldTimeout, actorScheduler)

    fun startHelloWorldServer(host: String, port: Int): Application {
        startHttpServer(host, port) {
            HelloWorldRoute.apply(helloWorldController)
        }
        return this
    }

    private fun startHttpServer(host: String, port: Int, routes: () -> Route): CompletionStage<ServerBinding> {

        val actorMaterializer = ActorMaterializer.create(toUntyped(actorSystem))

        val routeFlow = withJsonRejectionHandling(routes).flow(toUntyped(actorSystem), actorMaterializer)
        val http = Http.get(toUntyped(actorSystem))

        actorSystem.log().info("Starting server at http://$host:$port/")

        return http.bindAndHandle(routeFlow, ConnectHttp.toHost(host, port), actorMaterializer)
    }
}
