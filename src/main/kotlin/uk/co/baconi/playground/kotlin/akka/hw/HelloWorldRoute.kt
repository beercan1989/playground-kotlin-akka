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
import akka.http.javadsl.model.StatusCodes.OK
import akka.http.javadsl.server.Directives.*
import akka.http.javadsl.server.Route
import uk.co.baconi.playground.kotlin.akka.JsonSupport.marshaller
import uk.co.baconi.playground.kotlin.akka.JsonSupport.withJsonExceptionHandling
import uk.co.baconi.playground.kotlin.akka.JsonSupport.withJsonRejectionHandling
import java.util.concurrent.CompletionStage

interface HelloWorldRoute {

    val helloWorldProvider: (ActorSystem) -> CompletionStage<HelloWorldMessage>

    fun helloWorldRoute(): Route = withJsonExceptionHandling {
        withJsonRejectionHandling {
            path("hello-world") {
                get {
                    extractActorSystem { actorSystem ->
                        extractLog { log ->
                            log.info("Processing GET /hello-world")
                            onSuccess(helloWorldProvider(actorSystem)) { result ->
                                log.info("Processed GET /hello-world")
                                complete(OK, result, marshaller<HelloWorldMessage>())
                            }
                        }
                    }
                }
            }
        }
    }
}