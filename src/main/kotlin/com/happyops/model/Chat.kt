package com.happyops.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val sessionId: String = "default"
)

@Serializable
data class ChatResponse(
    val response: String
)
