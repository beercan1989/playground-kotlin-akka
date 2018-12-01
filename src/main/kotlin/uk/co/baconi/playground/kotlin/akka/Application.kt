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

import akka.actor.ActorSystem
import akka.http.javadsl.Http
import akka.stream.ActorMaterializer
import uk.co.baconi.playground.kotlin.akka.hw.HelloWorldRoute
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.server.Route
import java.util.concurrent.CompletionStage



class Application : HelloWorldRoute {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Application()
                    .startHelloWorldServer("127.0.0.1", 8080)
        }
    }

    private val actorSystem: ActorSystem = ActorSystem.create("application")

    fun startHelloWorldServer(host: String, port: Int): Application {
        startHttpServer(host, port) {
            helloWorldRoute()
        }
        return this
    }

    private fun startHttpServer(host: String, port: Int, routes: () -> Route): CompletionStage<ServerBinding> {

        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val routeFlow = withJsonRejectionHandling(routes).flow(actorSystem, actorMaterializer)
        val http = Http.get(actorSystem)

        actorSystem.log().info("Starting server at http://$host:$port/")

        return http.bindAndHandle(routeFlow, ConnectHttp.toHost(host, port), actorMaterializer)
    }
}
