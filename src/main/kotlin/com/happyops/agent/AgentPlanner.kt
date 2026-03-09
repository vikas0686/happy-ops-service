package com.happyops.agent

import arrow.core.Either
import arrow.core.left
import com.happyops.adapter.GrokClient
import com.happyops.adapter.Message
import com.happyops.model.DomainError
import com.happyops.model.ToolDefinition
import com.happyops.observability.AgentLogger

class AgentPlanner(private val grokClient: GrokClient) {

    suspend fun plan(
        messages: List<Message>,
        tools: List<ToolDefinition>
    ): Either<DomainError, PlanResult> {
        AgentLogger.logAgentDecision("Sending request to Grok with ${tools.size} tools")
        
        return grokClient.chat(messages, tools).map { response ->
            AgentLogger.logGrokResponse(response.hasToolCalls(), response.finishReason)
            
            if (response.hasToolCalls()) {
                PlanResult.ToolCallRequired(response.getToolCalls(), response.message)
            } else {
                PlanResult.FinalAnswer(response.getContent() ?: "No response")
            }
        }
    }
}

sealed class PlanResult {
    data class ToolCallRequired(
        val toolCalls: List<com.happyops.adapter.ToolCall>,
        val message: Message
    ) : PlanResult()
    
    data class FinalAnswer(val content: String) : PlanResult()
}
