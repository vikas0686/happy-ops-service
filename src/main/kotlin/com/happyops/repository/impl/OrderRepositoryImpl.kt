package com.happyops.repository.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.happyops.adapter.MongoAdapter
import com.happyops.model.DomainError
import com.happyops.model.Order
import com.happyops.repository.OrderRepository
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

class OrderRepositoryImpl(mongoAdapter: MongoAdapter) : OrderRepository {
    private val collection: CoroutineCollection<Order> = 
        mongoAdapter.database.getCollection("orders")

    override suspend fun createOrder(order: Order): Either<DomainError, Order> = try {
        collection.insertOne(order)
        order.right()
    } catch (e: Exception) {
        DomainError.DatabaseError("Failed to create order: ${e.message}").left()
    }

    override suspend fun getOrder(id: Id<Order>): Either<DomainError, Order> = try {
        collection.findOneById(id)?.right()
            ?: DomainError.NotFound("Order not found").left()
    } catch (e: Exception) {
        DomainError.DatabaseError("Failed to get order: ${e.message}").left()
    }

    override suspend fun listOrders(): Either<DomainError, List<Order>> = try {
        collection.find().descendingSort(Order::orderDate).toList().right()
    } catch (e: Exception) {
        DomainError.DatabaseError("Failed to list orders: ${e.message}").left()
    }

    override suspend fun updateOrder(order: Order): Either<DomainError, Order> = try {
        collection.save(order)
        order.right()
    } catch (e: Exception) {
        DomainError.DatabaseError("Failed to update order: ${e.message}").left()
    }

    override suspend fun deleteOrder(id: Id<Order>): Either<DomainError, Boolean> = try {
        val result = collection.deleteOneById(id)
        (result.deletedCount > 0).right()
    } catch (e: Exception) {
        DomainError.DatabaseError("Failed to delete order: ${e.message}").left()
    }
}
