package com.happyops.controller

import com.happyops.GrokConfigKey
import com.happyops.MongoConfigKey
import com.happyops.adapter.GrokClient
import com.happyops.adapter.MongoAdapter
import com.happyops.agent.AgentExecutor
import com.happyops.agent.AgentOrchestrator
import com.happyops.agent.AgentPlanner
import com.happyops.memory.MemoryStore
import com.happyops.observability.ToolMetrics
import com.happyops.repository.impl.OrderRepositoryImpl
import com.happyops.service.ChatService
import com.happyops.service.OrderService
import com.happyops.tools.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Happy Ops Service - AI Agent Backend")
        }
        
        get("/health") {
            call.respond(mapOf("status" to "healthy"))
        }
        
        get("/metrics") {
            call.respond(ToolMetrics.getMetrics())
        }
        
        // Initialize services and add routes
        val mongoAdapter = MongoAdapter(application.attributes[MongoConfigKey])
        val grokClient = GrokClient(application.attributes[GrokConfigKey])
        
        val orderRepository = OrderRepositoryImpl(mongoAdapter)
        val orderService = OrderService(orderRepository)
        
        val tools = listOf(
            CreateOrderTool(orderService),
            GetOrderTool(orderService),
            ListOrdersTool(orderService),
            OrderStatsTool(orderService)
        )
        val toolRegistry = ToolRegistry(tools)
        
        val agentPlanner = AgentPlanner(grokClient)
        val agentExecutor = AgentExecutor(toolRegistry)
        val agentOrchestrator = AgentOrchestrator(agentPlanner, agentExecutor, toolRegistry)
        
        val memoryStore = MemoryStore()
        val chatService = ChatService(agentOrchestrator, memoryStore)
        
        this.chatRoutes(chatService)
        this.orderRoutes(orderService)
        
        application.log.info("✅ All routes registered successfully")
    }
}
