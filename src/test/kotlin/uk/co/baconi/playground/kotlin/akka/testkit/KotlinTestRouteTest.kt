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

package uk.co.baconi.playground.kotlin.akka.testkit

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.server.RouteResult
import akka.http.javadsl.testkit.RouteTest
import akka.http.javadsl.testkit.TestRouteResult
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import io.kotlintest.Failures
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.should
import scala.concurrent.Future

class KotlinTestRouteTest(private val actorSystem: ActorSystem) : RouteTest() {

    override fun system(): ActorSystem = actorSystem

    override fun materializer(): Materializer = ActorMaterializer.create(actorSystem)

    override fun createTestRouteResultAsync(request: HttpRequest?, result: Future<RouteResult>?): TestRouteResult {
        return object : TestRouteResult(result, awaitDuration(), system().dispatcher, materializer()) {

            override fun fail(message: String) {
                throw Failures.failure(message)
            }

            override fun assertEquals(expected: Any?, actual: Any?, message: String) {
                reportDetails {
                    expected should object : Matcher<Any?> {
                        override fun test(value: Any?) = Result(value == actual, message, message)
                    }
                }
            }

            override fun assertEquals(expected: Int, actual: Int, message: String) {
                expected should object : Matcher<Int> {
                    override fun test(value: Int) = Result(value == actual, message, message)
                }
            }

            override fun assertTrue(predicate: Boolean, message: String) {
                predicate should object : Matcher<Boolean> {
                    override fun test(value: Boolean) = Result(value, message, message)
                }
            }

            private fun <T> reportDetails(block: () -> T): T = try {
                block()
            } catch (throwable: Throwable) {
                throw AssertionError(
                        """
                            ${throwable.message}
                            Request was: $request
                            Route result was: $result
                        """.trimIndent(),
                        throwable
                )
            }
        }
    }
}