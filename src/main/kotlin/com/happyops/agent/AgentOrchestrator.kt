package com.happyops.agent

import arrow.core.Either
import arrow.core.flatMap
import com.happyops.adapter.Message
import com.happyops.memory.ConversationMemory
import com.happyops.model.DomainError
import com.happyops.observability.AgentLogger
import com.happyops.tools.ToolRegistry

class AgentOrchestrator(
    private val planner: AgentPlanner,
    private val executor: AgentExecutor,
    private val toolRegistry: ToolRegistry
) {

    suspend fun processMessage(
        userMessage: String,
        conversationMemory: ConversationMemory? = null
    ): Either<DomainError, String> {
        AgentLogger.logChatRequest(userMessage)
        
        val messages = mutableListOf<Message>()
        
        // Include conversation history
        conversationMemory?.let {
            messages.addAll(it.getMessages())
        }
        
        // Add current user message
        messages.add(Message(role = "user", content = userMessage))
        
        val tools = toolRegistry.getToolDefinitions()
        
        return planner.plan(messages, tools).flatMap { planResult ->
            when (planResult) {
                is PlanResult.FinalAnswer -> {
                    AgentLogger.logChatResponse(planResult.content)
                    
                    // Save to memory
                    conversationMemory?.addUserMessage(userMessage)
                    conversationMemory?.addAssistantMessage(planResult.content)
                    
                    Either.Right(planResult.content)
                }
                
                is PlanResult.ToolCallRequired -> {
                    AgentLogger.logAgentDecision("Tool calls required: ${planResult.toolCalls.size}")
                    
                    executor.execute(planResult.toolCalls).flatMap { results ->
                        // Add assistant message with tool calls
                        messages.add(planResult.message)
                        
                        // Add tool results
                        results.forEach { result ->
                            val content = if (result.response.success) {
                                result.response.data?.toString() ?: "Success"
                            } else {
                                result.response.error ?: "Unknown error"
                            }
                            
                            messages.add(
                                Message(
                                    role = "tool",
                                    content = content,
                                    tool_call_id = result.toolCallId,
                                    name = result.toolName
                                )
                            )
                        }
                        
                        // Get final answer from Grok
                        planner.plan(messages, emptyList()).flatMap { finalResult ->
                            when (finalResult) {
                                is PlanResult.FinalAnswer -> {
                                    AgentLogger.logChatResponse(finalResult.content)
                                    
                                    // Save to memory
                                    conversationMemory?.addUserMessage(userMessage)
                                    conversationMemory?.addAssistantMessage(finalResult.content)
                                    
                                    Either.Right(finalResult.content)
                                }
                                is PlanResult.ToolCallRequired -> {
                                    Either.Right("Unable to generate final response")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
