package com.happyops.tools

import arrow.core.Either
import arrow.core.left
import com.happyops.model.*
import com.happyops.service.OrderService
import kotlinx.serialization.json.*

class CreateOrderTool(private val orderService: OrderService) : Tool {
    override val name = "create_order"
    override val description = "Create a new order"

    override suspend fun execute(input: ToolRequest): Either<DomainError, ToolResponse> {
        return try {
            val orderId = input.arguments["orderId"]?.jsonPrimitive?.content
                ?: return DomainError.ValidationError("orderId is required").left()
            val merchantId = input.arguments["merchantId"]?.jsonPrimitive?.content
                ?: return DomainError.ValidationError("merchantId is required").left()
            val amountValue = input.arguments["amount"]?.jsonPrimitive?.double
                ?: return DomainError.ValidationError("amount is required").left()
            val merchantRef = input.arguments["merchantOrderReference"]?.jsonPrimitive?.content ?: orderId
            
            val request = CreateOrderRequest(
                orderId = orderId,
                orderType = OrderType.ONLINE,
                merchantOrderReference = merchantRef,
                orderAmount = Amount(amountValue),
                merchantId = merchantId,
                channel = Channels.API,
                customer = input.arguments["customerName"]?.jsonPrimitive?.content?.let {
                    CustomerData(name = it)
                }
            )
            
            orderService.createOrder(request).map { order ->
                ToolResponse(success = true, data = Json.encodeToJsonElement(order.toResponse()))
            }
        } catch (e: Exception) {
            DomainError.ToolExecutionError("Failed to create order: ${e.message}").left()
        }
    }

    override fun getDefinition() = ToolDefinition(
        function = FunctionDefinition(
            name = name,
            description = description,
            parameters = ParametersSchema(
                properties = mapOf(
                    "orderId" to PropertySchema("string", "Unique order identifier"),
                    "merchantId" to PropertySchema("string", "Merchant identifier"),
                    "amount" to PropertySchema("number", "Order amount"),
                    "merchantOrderReference" to PropertySchema("string", "Merchant order reference"),
                    "customerName" to PropertySchema("string", "Customer name (optional)")
                ),
                required = listOf("orderId", "merchantId", "amount")
            )
        )
    )
}
