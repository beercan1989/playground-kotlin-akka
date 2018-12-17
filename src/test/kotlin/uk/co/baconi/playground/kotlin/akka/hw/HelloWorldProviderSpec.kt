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
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import uk.co.baconi.playground.kotlin.akka.testkit.MockingSupport
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class HelloWorldProviderSpec : MockingSupport, StringSpec({

    val mockActorSystem: ActorSystem = mockk()
    val mockActorRef: ActorRef<HelloWorldCommand> = mockk()
    val mockScheduler: Scheduler = mockk()

    val underTest: HelloWorldProvider = spyk()

    "[Happy Path] Spawn an actor per request with a unique name" {

        val uuid = UUID.randomUUID()
        every { underTest.actorIdProvider() } returns uuid
        every { underTest.spawn<HelloWorldCommand>(any(), any(), any()) } returns mockActorRef

        val completionStage: CompletionStage<HelloWorldMessage> = CompletableFuture.completedFuture(HelloWorldMessage(""))
        every { underTest.ask<HelloWorldCommand, HelloWorldMessage>(any(), any(), any(), any()) } returns completionStage

        every { mockActorSystem.scheduler } returns mockScheduler

        val result = underTest.invoke(mockActorSystem)
        result shouldBe completionStage

        verify { underTest.spawn<HelloWorldCommand>(any(), any(), "hello-world-$uuid") }
        verify { underTest.ask<HelloWorldCommand, HelloWorldMessage>(mockActorRef, any(), any(), mockScheduler) }
    }

})