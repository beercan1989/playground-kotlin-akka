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
import akka.http.javadsl.model.ContentTypes.APPLICATION_JSON
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.StatusCodes.INTERNAL_SERVER_ERROR
import akka.http.javadsl.model.StatusCodes.OK
import io.kotlintest.assertSoftly
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import uk.co.baconi.playground.kotlin.akka.ErrorResult
import uk.co.baconi.playground.kotlin.akka.JsonSupport.unmarshaller
import uk.co.baconi.playground.kotlin.akka.testkit.RouteTestKit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeoutException

class HelloWorldRouteSpec : StringSpec(), RouteTestKit, HelloWorldRoute {

    override val helloWorldProvider: (ActorSystem) -> CompletionStage<HelloWorldMessage> = mockk()

    private val underTest = routeTestKit.testRoute(helloWorldRoute())

    init {
        """[Happy Path] GET on /hello-world should return `{"message":"Hello World"}`""" {

            every { helloWorldProvider.invoke(any()) } returns completedFuture(HelloWorldMessage("Hello World"))

            val result = underTest.run(HttpRequest.GET("/hello-world"))
            assertSoftly {
                result.status() shouldBe OK
                result.contentType() shouldBe APPLICATION_JSON
                result.entityString() shouldBe """{"message":"Hello World"}"""
                result.entity(unmarshaller<HelloWorldMessage>()) shouldBe HelloWorldMessage("Hello World")
            }

            verify { helloWorldProvider.invoke(any()) }
        }

        """[Unhappy Path] GET on /hello-world should return `{"status":500,"error":"Internal Server Error"}` on failed future""" {

            every { helloWorldProvider.invoke(any()) } returns CompletableFuture.supplyAsync<HelloWorldMessage> {
                throw TimeoutException()
            }

            val result = underTest.run(HttpRequest.GET("/hello-world"))
            assertSoftly {
                result.status() shouldBe INTERNAL_SERVER_ERROR
                result.contentType() shouldBe APPLICATION_JSON
                result.entityString() shouldBe """{"status":500,"error":"Internal Server Error"}"""
                result.entity(unmarshaller<ErrorResult>()) shouldBe ErrorResult(500, "Internal Server Error")
            }

            verify { helloWorldProvider.invoke(any()) }
        }
    }
}