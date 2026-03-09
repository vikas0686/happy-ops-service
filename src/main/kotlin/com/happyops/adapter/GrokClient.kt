package com.happyops.adapter

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.happyops.config.GrokConfig
import com.happyops.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class GrokClient(private val config: GrokConfig) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    suspend fun chat(
        messages: List<Message>,
        tools: List<ToolDefinition>
    ): Either<DomainError, GrokResponse> {
        return try {
            val json = Json { 
                encodeDefaults = true
                explicitNulls = false
                ignoreUnknownKeys = true
            }
            
            val toolsJson = if (tools.isNotEmpty()) {
                tools.map { tool ->
                    """
                    {
                        "type": "function",
                        "function": {
                            "name": "${tool.function.name}",
                            "description": "${tool.function.description}",
                            "parameters": ${json.encodeToString(ParametersSchema.serializer(), tool.function.parameters)}
                        }
                    }
                    """.trimIndent()
                }.joinToString(",", "[", "]")
            } else ""
            
            val messagesJson = json.encodeToString(kotlinx.serialization.builtins.ListSerializer(Message.serializer()), messages)
            
            val jsonBody = """
            {
                "model": "${config.model}",
                "messages": $messagesJson,
                ${if (tools.isNotEmpty()) "\"tools\": $toolsJson," else ""}
                "stream": false
            }
            """.trimIndent()

            logger.info { "Sending request to Grok API with ${tools.size} tools" }

            val httpResponse = client.post("${config.baseUrl}/chat/completions") {
                header("Authorization", "Bearer ${config.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            
            val responseText = httpResponse.body<String>()
            logger.info { "Raw response: $responseText" }
            
            val response = json.decodeFromString<GrokApiResponse>(responseText)
            
            if (response.error != null) {
                return DomainError.ExternalServiceError("Grok API error: ${response.error.message}").left()
            }

            logger.info { "Received response from Grok API: ${response.choices.firstOrNull()?.finish_reason}" }
            
            val choice = response.choices.firstOrNull()
                ?: return DomainError.ExternalServiceError("No response from Grok").left()

            GrokResponse(
                message = choice.message,
                finishReason = choice.finish_reason
            ).right()
        } catch (e: Exception) {
            logger.error(e) { "Grok API call failed" }
            DomainError.ExternalServiceError("Grok API error: ${e.message}").left()
        }
    }
}

@Serializable
data class GrokRequest(
    val model: String,
    val messages: List<Message>,
    val tools: List<ToolDefinition>? = null
)

@Serializable
data class Message(
    val role: String,
    val content: String? = null,
    val tool_calls: List<ToolCall>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: String
)

@Serializable
data class GrokApiResponse(
    val id: String? = null,
    val choices: List<Choice> = emptyList(),
    val error: ErrorResponse? = null
)

@Serializable
data class ErrorResponse(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

@Serializable
data class Choice(
    val message: Message,
    val finish_reason: String
)

data class GrokResponse(
    val message: Message,
    val finishReason: String
) {
    fun hasToolCalls(): Boolean = !message.tool_calls.isNullOrEmpty()
    fun getToolCalls(): List<ToolCall> = message.tool_calls ?: emptyList()
    fun getContent(): String? = message.content
}
