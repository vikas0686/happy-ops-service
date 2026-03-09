package com.happyops.observability

import mu.KotlinLogging

object AgentLogger {
    private val logger = KotlinLogging.logger {}

    fun logAgentDecision(decision: String) {
        logger.info { "🤖 Agent Decision: $decision" }
    }

    fun logToolExecution(toolName: String, duration: Long) {
        logger.info { "🔧 Tool Executed: $toolName (${duration}ms)" }
    }

    fun logGrokResponse(hasToolCalls: Boolean, finishReason: String) {
        logger.info { "💬 Grok Response: tool_calls=$hasToolCalls, finish_reason=$finishReason" }
    }

    fun logError(context: String, error: String) {
        logger.error { "❌ Error in $context: $error" }
    }

    fun logChatRequest(message: String) {
        logger.info { "📨 Chat Request: $message" }
    }

    fun logChatResponse(response: String) {
        logger.info { "📤 Chat Response: ${response.take(100)}..." }
    }
}
