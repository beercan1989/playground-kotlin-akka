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

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.marshalling.Marshaller
import akka.http.javadsl.model.ContentTypes.APPLICATION_JSON
import akka.http.javadsl.model.HttpEntity
import akka.http.javadsl.model.RequestEntity
import akka.http.javadsl.model.StatusCode
import akka.http.javadsl.server.Directives.handleRejections
import akka.http.javadsl.server.RejectionHandler
import akka.http.javadsl.server.Route
import akka.http.javadsl.server.directives.RouteAdapter
import akka.http.javadsl.unmarshalling.Unmarshaller
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

interface JsonSupport {

    fun withJsonRejectionHandling(inner: () -> Route): RouteAdapter = handleRejections(jsonRejectionHandler, inner)

    val objectMapper: ObjectMapper
        get() = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()

    private val jsonRejectionHandler: RejectionHandler
        get() = RejectionHandler.defaultHandler().mapRejectionResponse { response ->
            when(response.entity()) {
                is HttpEntity.Strict -> response.withEntity(APPLICATION_JSON, toErrorResult(response.status()))
                else -> response
            }
        }

    private fun toErrorResult(statusCode: StatusCode) = objectMapper.writeValueAsBytes(ErrorResult(statusCode))
}

inline fun <reified A: Any> JsonSupport.unmarshaller(): Unmarshaller<HttpEntity, A> =
        Jackson.unmarshaller(objectMapper, A::class.java)

inline fun <reified A: Any> JsonSupport.marshaller(): Marshaller<A, RequestEntity> =
        Jackson.marshaller<A>(objectMapper)
