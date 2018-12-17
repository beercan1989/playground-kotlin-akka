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

import akka.actor.testkit.typed.javadsl.BehaviorTestKit
import akka.actor.testkit.typed.javadsl.TestInbox
import io.kotlintest.specs.StringSpec

class HelloWorldActorSpec : StringSpec({

    val underTest = BehaviorTestKit.create(HelloWorldActor.apply())

    "[Happy Path] Process HelloWorldRequest's" {

        val testInbox = TestInbox.create<HelloWorldMessage>()

        underTest.run(HelloWorldRequest(testInbox.ref))

        testInbox.expectMessage(HelloWorldMessage("Hello World"))
    }
})