package com.happyops.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Order(
    @Contextual val id: Id<Order>? = null,
    val displayOrderId: String,
    val orderId: String,
    val orderType: OrderType,
    @Contextual val orderDate: Instant,
    val merchantOrderReference: String,
    var orderAmount: Amount,
    var orderStatus: OrderStatus,
    val merchantId: String,
    val channel: Channels,
    var orderStatusComment: String? = null,
    var customer: CustomerData? = null,
    var orderDetails: OrderDetailsData? = null
)

@Serializable
data class Amount(
    val value: Double,
    val currency: String = "INR"
)

@Serializable
enum class OrderType {
    ONLINE, OFFLINE, SUBSCRIPTION
}

@Serializable
enum class Channels {
    WEB, MOBILE, API, POS
}

@Serializable
data class CustomerData(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: Address? = null
)

@Serializable
data class Address(
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null
)

@Serializable
enum class OrderStatus {
    OPEN, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED, FAILED, CLOSED;

    fun canTransition(next: OrderStatus): Boolean {
        return validOrderStatusTransitions[this]?.contains(next) ?: false
    }
}

val validOrderStatusTransitions = mapOf(
    OrderStatus.OPEN to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
    OrderStatus.CONFIRMED to setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
    OrderStatus.PROCESSING to setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
    OrderStatus.SHIPPED to setOf(OrderStatus.DELIVERED, OrderStatus.FAILED),
    OrderStatus.DELIVERED to setOf(OrderStatus.CLOSED, OrderStatus.REFUNDED),
    OrderStatus.CANCELLED to setOf(OrderStatus.CLOSED),
    OrderStatus.REFUNDED to setOf(OrderStatus.CLOSED),
    OrderStatus.FAILED to setOf(OrderStatus.CLOSED)
)

@Serializable
data class OrderDetailsData(
    val items: List<Item>? = null,
    val metadata: Metadata? = null
)

@Serializable
data class Item(
    val name: String? = null,
    val quantity: Int? = null,
    val unitPrice: Double? = null
)

@Serializable
data class Metadata(
    val note: String? = null,
    val source: String? = null
)

@Serializable
data class CreateOrderRequest(
    val orderId: String,
    val orderType: OrderType,
    val merchantOrderReference: String,
    val orderAmount: Amount,
    val merchantId: String,
    val channel: Channels,
    val customer: CustomerData? = null,
    val orderDetails: OrderDetailsData? = null
)

@Serializable
data class UpdateOrderRequest(
    val orderStatus: OrderStatus? = null,
    val orderStatusComment: String? = null,
    val orderAmount: Amount? = null,
    val customer: CustomerData? = null,
    val orderDetails: OrderDetailsData? = null
)

@Serializable
data class OrderResponse(
    val id: String,
    val displayOrderId: String,
    val orderId: String,
    val orderType: OrderType,
    val orderDate: String,
    val merchantOrderReference: String,
    val orderAmount: Amount,
    val orderStatus: OrderStatus,
    val merchantId: String,
    val channel: Channels,
    val orderStatusComment: String? = null,
    val customer: CustomerData? = null,
    val orderDetails: OrderDetailsData? = null
)

fun Order.toResponse() = OrderResponse(
    id = id?.toString() ?: "",
    displayOrderId = displayOrderId,
    orderId = orderId,
    orderType = orderType,
    orderDate = orderDate.toString(),
    merchantOrderReference = merchantOrderReference,
    orderAmount = orderAmount,
    orderStatus = orderStatus,
    merchantId = merchantId,
    channel = channel,
    orderStatusComment = orderStatusComment,
    customer = customer,
    orderDetails = orderDetails
)
