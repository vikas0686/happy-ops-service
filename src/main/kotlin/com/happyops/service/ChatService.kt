package com.happyops.service

import arrow.core.Either
import com.happyops.agent.AgentOrchestrator
import com.happyops.memory.MemoryStore
import com.happyops.model.DomainError

class ChatService(
    private val agentOrchestrator: AgentOrchestrator,
    private val memoryStore: MemoryStore
) {

    suspend fun chat(message: String, sessionId: String): Either<DomainError, String> {
        val conversationMemory = memoryStore.getOrCreateConversation(sessionId)
        return agentOrchestrator.processMessage(message, conversationMemory)
    }

    fun clearSession(sessionId: String) {
        memoryStore.clearConversation(sessionId)
    }
}
