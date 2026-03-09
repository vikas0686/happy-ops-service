package com.happyops.tools

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.happyops.model.*
import com.happyops.service.OrderService
import kotlinx.serialization.json.*

class GetOrderTool(private val orderService: OrderService) : Tool {
    override val name = "get_order"
    override val description = "Get details of a specific order by ID"

    override suspend fun execute(input: ToolRequest): Either<DomainError, ToolResponse> {
        return try {
            val orderId = input.arguments["orderId"]?.jsonPrimitive?.content
                ?: return DomainError.ToolExecutionError("Missing orderId").left()

            orderService.getOrder(orderId).map { order ->
                ToolResponse(
                    success = true,
                    data = Json.encodeToJsonElement(order.toResponse())
                )
            }
        } catch (e: Exception) {
            DomainError.ToolExecutionError("Failed to get order: ${e.message}").left()
        }
    }

    override fun getDefinition() = ToolDefinition(
        function = FunctionDefinition(
            name = name,
            description = description,
            parameters = ParametersSchema(
                properties = mapOf(
                    "orderId" to PropertySchema("string", "Order ID")
                ),
                required = listOf("orderId")
            )
        )
    )
}
