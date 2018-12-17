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
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.Directives.*
import akka.http.javadsl.server.ExceptionHandler
import akka.http.javadsl.server.RejectionHandler
import akka.http.javadsl.server.Route
import akka.http.javadsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.IllegalRequestException
import akka.japi.pf.FI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import scala.util.control.NonFatal

object JsonSupport {

    fun withJsonRejectionHandling(inner: () -> Route): Route = handleRejections(jsonRejectionHandler, inner)

    fun withJsonExceptionHandling(inner: () -> Route): Route = handleExceptions(jsonExceptionHandler, inner)

    inline fun <reified A : Any> unmarshaller(): Unmarshaller<HttpEntity, A> = Jackson.unmarshaller(objectMapper, A::class.java)

    inline fun <reified A : Any> marshaller(): Marshaller<A, RequestEntity> = Jackson.marshaller<A>(objectMapper)

    val objectMapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()

    private val errorResultMarshaller = JsonSupport.marshaller<ErrorResult>()

    private val onNonFatalExceptions = FI.TypedPredicate<Throwable> { t -> NonFatal.apply(t) }

    private val jsonExceptionHandler = ExceptionHandler.newBuilder()
            .match(IllegalRequestException::class.java) { throwable ->
                val status = throwable.status()
                val errorResult = ErrorResult(status.intValue(), throwable.info().format(false))
                complete(status, errorResult, errorResultMarshaller)
            }.match(Throwable::class.java, onNonFatalExceptions, FI.Apply { exception ->
                extractLog { logger ->
                    logger.error("Unhandled exception hit: {}", exception::class)
                    val errorResult = ErrorResult(StatusCodes.INTERNAL_SERVER_ERROR)
                complete(StatusCodes.INTERNAL_SERVER_ERROR, errorResult, errorResultMarshaller)
                }
            }).build()

    private val jsonRejectionHandler = RejectionHandler.defaultHandler().mapRejectionResponse { response ->
        when (response.entity()) {
            is HttpEntity.Strict -> response.withEntity(APPLICATION_JSON, toErrorResult(response.status()))
            else -> response
        }
    }

    private fun toErrorResult(statusCode: StatusCode) = objectMapper.writeValueAsBytes(ErrorResult(statusCode))
}
