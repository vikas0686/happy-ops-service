package com.happyops.controller

import com.happyops.model.ChatRequest
import com.happyops.model.ChatResponse
import com.happyops.model.DomainError
import com.happyops.service.ChatService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chatRoutes(chatService: ChatService) {
    route("/chat") {
        post {
            val request = call.receive<ChatRequest>()
            
            chatService.chat(request.message, request.sessionId).fold(
                { error -> call.respond(error.toHttpStatus(), mapOf("error" to error.message)) },
                { response -> call.respond(ChatResponse(response)) }
            )
        }

        delete("/session/{sessionId}") {
            val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing session ID")
            )
            chatService.clearSession(sessionId)
            call.respond(HttpStatusCode.NoContent)
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
