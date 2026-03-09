package com.happyops.agent

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.happyops.adapter.ToolCall
import com.happyops.model.DomainError
import com.happyops.model.ToolRequest
import com.happyops.model.ToolResponse
import com.happyops.observability.AgentLogger
import com.happyops.observability.ToolMetrics
import com.happyops.tools.ToolRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.system.measureTimeMillis

class AgentExecutor(private val toolRegistry: ToolRegistry) {

    suspend fun execute(toolCalls: List<ToolCall>): Either<DomainError, List<ToolExecutionResult>> {
        val results = mutableListOf<ToolExecutionResult>()
        
        for (toolCall in toolCalls) {
            val result = executeSingleTool(toolCall)
            results.add(result)
            
            // Stop on first error
            if (result.response.success.not()) {
                break
            }
        }
        
        return results.right()
    }

    private suspend fun executeSingleTool(toolCall: ToolCall): ToolExecutionResult {
        val toolName = toolCall.function.name
        val tool = toolRegistry.getToolByName(toolName)
        
        if (tool == null) {
            AgentLogger.logError("Tool execution", "Tool not found: $toolName")
            return ToolExecutionResult(
                toolCallId = toolCall.id,
                toolName = toolName,
                response = ToolResponse(success = false, error = "Tool not found: $toolName")
            )
        }

        return try {
            val arguments = Json.parseToJsonElement(toolCall.function.arguments)
                .let { it as? kotlinx.serialization.json.JsonObject }
                ?.toMap() ?: emptyMap()
            
            val request = ToolRequest(toolName, arguments)
            
            var response: ToolResponse? = null
            val duration = measureTimeMillis {
                tool.execute(request).fold(
                    { error -> response = ToolResponse(success = false, error = error.message) },
                    { result -> response = result }
                )
            }
            
            val success = response?.success ?: false
            ToolMetrics.recordExecution(toolName, duration, success)
            AgentLogger.logToolExecution(toolName, duration)
            
            ToolExecutionResult(
                toolCallId = toolCall.id,
                toolName = toolName,
                response = response!!
            )
        } catch (e: Exception) {
            AgentLogger.logError("Tool execution", "${toolName}: ${e.message}")
            ToolMetrics.recordExecution(toolName, 0, false)
            ToolExecutionResult(
                toolCallId = toolCall.id,
                toolName = toolName,
                response = ToolResponse(success = false, error = e.message)
            )
        }
    }
}

data class ToolExecutionResult(
    val toolCallId: String,
    val toolName: String,
    val response: ToolResponse
)
