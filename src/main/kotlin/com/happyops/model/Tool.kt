package com.happyops.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ToolRequest(
    val name: String,
    val arguments: Map<String, JsonElement>
)

@Serializable
data class ToolResponse(
    val success: Boolean,
    val data: JsonElement? = null,
    val error: String? = null
)

@Serializable
data class ToolDefinition(
    val type: String = "function",
    val function: FunctionDefinition
)

@Serializable
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: ParametersSchema
)

@Serializable
data class ParametersSchema(
    val type: String = "object",
    val properties: Map<String, PropertySchema>,
    val required: List<String> = emptyList()
)

@Serializable
data class PropertySchema(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)
