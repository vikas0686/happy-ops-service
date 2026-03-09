package com.happyops.memory

import java.util.concurrent.ConcurrentHashMap

class MemoryStore {
    private val conversations = ConcurrentHashMap<String, ConversationMemory>()

    fun getOrCreateConversation(sessionId: String): ConversationMemory =
        conversations.getOrPut(sessionId) { ConversationMemory() }

    fun getConversation(sessionId: String): ConversationMemory? =
        conversations[sessionId]

    fun clearConversation(sessionId: String) {
        conversations[sessionId]?.clear()
    }

    fun deleteConversation(sessionId: String) {
        conversations.remove(sessionId)
    }

    fun getAllSessionIds(): Set<String> = conversations.keys.toSet()
}
