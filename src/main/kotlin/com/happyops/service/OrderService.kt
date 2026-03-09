package com.happyops.service

import arrow.core.Either
import arrow.core.flatMap
import com.happyops.model.*
import com.happyops.repository.OrderRepository
import kotlinx.datetime.Clock
import org.litote.kmongo.Id
import org.litote.kmongo.toId

class OrderService(private val repository: OrderRepository) {

    suspend fun createOrder(request: CreateOrderRequest): Either<DomainError, Order> {
        if (request.orderAmount.value <= 0) {
            return Either.Left(DomainError.ValidationError("Amount must be positive"))
        }
        if (request.orderId.isBlank()) {
            return Either.Left(DomainError.ValidationError("Order ID is required"))
        }

        val order = Order(
            displayOrderId = generateDisplayOrderId(),
            orderId = request.orderId,
            orderType = request.orderType,
            orderDate = Clock.System.now(),
            merchantOrderReference = request.merchantOrderReference,
            orderAmount = request.orderAmount,
            orderStatus = OrderStatus.OPEN,
            merchantId = request.merchantId,
            channel = request.channel,
            customer = request.customer,
            orderDetails = request.orderDetails
        )
        return repository.createOrder(order)
    }

    suspend fun getOrder(id: String): Either<DomainError, Order> =
        repository.getOrder(id.toId())

    suspend fun listOrders(): Either<DomainError, List<Order>> =
        repository.listOrders()

    suspend fun updateOrder(id: String, request: UpdateOrderRequest): Either<DomainError, Order> {
        return repository.getOrder(id.toId()).flatMap { order ->
            request.orderStatus?.let {
                if (!order.orderStatus.canTransition(it)) {
                    return Either.Left(DomainError.ValidationError("Invalid status transition from ${order.orderStatus} to $it"))
                }
                order.orderStatus = it
            }
            request.orderStatusComment?.let { order.orderStatusComment = it }
            request.orderAmount?.let { order.orderAmount = it }
            request.customer?.let { order.customer = it }
            request.orderDetails?.let { order.orderDetails = it }
            
            repository.updateOrder(order)
        }
    }

    suspend fun deleteOrder(id: String): Either<DomainError, Boolean> =
        repository.deleteOrder(id.toId())

    private fun generateDisplayOrderId(): String =
        "ORD-${Clock.System.now().toEpochMilliseconds()}"
}
