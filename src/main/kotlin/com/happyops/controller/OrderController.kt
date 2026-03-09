package com.happyops.controller

import com.happyops.model.*
import com.happyops.service.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(orderService: OrderService) {
    route("/orders") {
        post {
            val request = call.receive<CreateOrderRequest>()
            orderService.createOrder(request).fold(
                { error -> call.respond(error.toHttpStatus(), mapOf("error" to error.message)) },
                { order -> call.respond(HttpStatusCode.Created, order.toResponse()) }
            )
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing order ID")
            )
            orderService.getOrder(id).fold(
                { error -> call.respond(error.toHttpStatus(), mapOf("error" to error.message)) },
                { order -> call.respond(order.toResponse()) }
            )
        }

        get {
            orderService.listOrders().fold(
                { error -> call.respond(error.toHttpStatus(), mapOf("error" to error.message)) },
                { orders -> call.respond(orders.map { it.toResponse() }) }
            )
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing order ID")
            )
            val request = call.receive<UpdateOrderRequest>()
            orderService.updateOrder(id, request).fold(
                { error -> call.respond(error.toHttpStatus(), mapOf("error" to error.message)) },
                { order -> call.respond(order.toResponse()) }
            )
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing order ID")
            )
            orderService.deleteOrder(id).fold(
                { error -> call.respond(error.toHttpStatus(), mapOf("error" to error.message)) },
                { success -> call.respond(if (success) HttpStatusCode.NoContent else HttpStatusCode.NotFound) }
            )
        }
    }
}

private fun DomainError.toHttpStatus(): HttpStatusCode = when (this) {
    is DomainError.NotFound -> HttpStatusCode.NotFound
    is DomainError.ValidationError -> HttpStatusCode.BadRequest
    is DomainError.DatabaseError -> HttpStatusCode.InternalServerError
    is DomainError.ExternalServiceError -> HttpStatusCode.ServiceUnavailable
    is DomainError.ToolExecutionError -> HttpStatusCode.InternalServerError
}
