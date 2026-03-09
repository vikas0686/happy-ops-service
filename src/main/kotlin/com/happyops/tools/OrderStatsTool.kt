package com.happyops.tools

import arrow.core.Either
import arrow.core.left
import com.happyops.model.*
import com.happyops.service.OrderService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class OrderStatsTool(private val orderService: OrderService) : Tool {
    override val name = "order_stats"
    override val description = "Get order statistics (count, total amount, status breakdown)"

    override suspend fun execute(input: ToolRequest): Either<DomainError, ToolResponse> {
        return try {
            val customerName = input.arguments["customerName"]?.jsonPrimitive?.content

            orderService.listOrders().map { orders ->
                val filteredOrders = if (customerName != null) {
                    orders.filter { it.customer?.name?.contains(customerName, ignoreCase = true) == true }
                } else {
                    orders
                }
                
                val stats = OrderStats(
                    totalOrders = filteredOrders.size,
                    totalAmount = filteredOrders.sumOf { it.orderAmount.value },
                    statusBreakdown = filteredOrders.groupingBy { it.orderStatus }.eachCount(),
                    averageAmount = if (filteredOrders.isNotEmpty()) 
                        filteredOrders.sumOf { it.orderAmount.value } / filteredOrders.size else 0.0
                )
                
                ToolResponse(
                    success = true,
                    data = Json.encodeToJsonElement(stats)
                )
            }
        } catch (e: Exception) {
            DomainError.ToolExecutionError("Failed to calculate stats: ${e.message}").left()
        }
    }

    override fun getDefinition() = ToolDefinition(
        function = FunctionDefinition(
            name = name,
            description = description,
            parameters = ParametersSchema(
                properties = mapOf(
                    "customerName" to PropertySchema("string", "Optional customer name to filter statistics")
                ),
                required = emptyList()
            )
        )
    )
}

@Serializable
data class OrderStats(
    val totalOrders: Int,
    val totalAmount: Double,
    val statusBreakdown: Map<OrderStatus, Int>,
    val averageAmount: Double
)
