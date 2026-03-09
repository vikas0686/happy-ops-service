package com.happyops.memory

import com.happyops.adapter.Message
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ConversationEntry(
    val timestamp: Instant,
    val role: String,
    val content: String?
)

class ConversationMemory {
    private val history = mutableListOf<Message>()

    fun addMessage(message: Message) {
        history.add(message)
    }

    fun addUserMessage(content: String) {
        history.add(Message(role = "user", content = content))
    }

    fun addAssistantMessage(content: String) {
        history.add(Message(role = "assistant", content = content))
    }

    fun getMessages(): List<Message> = history.toList()

    fun clear() = history.clear()

    fun size(): Int = history.size
}
