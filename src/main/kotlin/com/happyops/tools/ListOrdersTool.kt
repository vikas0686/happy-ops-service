package com.happyops.tools

import arrow.core.Either
import arrow.core.left
import com.happyops.model.*
import com.happyops.service.OrderService
import kotlinx.serialization.json.*

class ListOrdersTool(private val orderService: OrderService) : Tool {
    override val name = "list_orders"
    override val description = "List recent orders (max 10), optionally filtered by customer name"

    override suspend fun execute(input: ToolRequest): Either<DomainError, ToolResponse> {
        return try {
            orderService.listOrders().map { orders ->
                val customerName = input.arguments["customerName"]?.takeIf { it !is kotlinx.serialization.json.JsonNull }?.jsonPrimitive?.content
                val limit = input.arguments["limit"]?.takeIf { it !is kotlinx.serialization.json.JsonNull }?.jsonPrimitive?.intOrNull ?: 10
                
                val filtered = if (customerName != null) {
                    orders.filter { it.customer?.name?.contains(customerName, ignoreCase = true) == true }
                } else {
                    orders
                }
                
                val limited = filtered.take(limit.coerceAtMost(10))
                
                ToolResponse(
                    success = true,
                    data = Json.encodeToJsonElement(limited.map { it.toResponse() })
                )
            }
        } catch (e: Exception) {
            DomainError.ToolExecutionError("Failed to list orders: ${e.message}").left()
        }
    }

    override fun getDefinition() = ToolDefinition(
        function = FunctionDefinition(
            name = name,
            description = description,
            parameters = ParametersSchema(
                properties = mapOf(
                    "customerName" to PropertySchema("string", "Optional customer name to filter orders"),
                    "limit" to PropertySchema("number", "Maximum number of orders to return (default 10, max 10)")
                ),
                required = emptyList()
            )
        )
    )
}
