package com.happyops.repository

import arrow.core.Either
import com.happyops.model.DomainError
import com.happyops.model.Order
import org.litote.kmongo.Id

interface OrderRepository {
    suspend fun createOrder(order: Order): Either<DomainError, Order>
    suspend fun getOrder(id: Id<Order>): Either<DomainError, Order>
    suspend fun listOrders(): Either<DomainError, List<Order>>
    suspend fun updateOrder(order: Order): Either<DomainError, Order>
    suspend fun deleteOrder(id: Id<Order>): Either<DomainError, Boolean>
}
