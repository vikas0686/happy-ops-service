package com.happyops

import com.happyops.config.GrokConfig
import com.happyops.config.MongoConfig
import com.happyops.controller.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureDependencies()
    configureRouting()
    
    logger.info { "🚀 Happy Ops Service started successfully" }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowHeader("Authorization")
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }
    }
}

fun Application.configureDependencies() {
    val mongoConfig = MongoConfig(environment.config)
    val grokConfig = GrokConfig.from(environment.config)
    
    attributes.put(MongoConfigKey, mongoConfig)
    attributes.put(GrokConfigKey, grokConfig)
    
    logger.info { "✅ Dependencies configured" }
}

val MongoConfigKey = AttributeKey<MongoConfig>("MongoConfig")
val GrokConfigKey = AttributeKey<GrokConfig>("GrokConfig")
