package com.happyops.config

import io.ktor.server.config.*

data class GrokConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String
) {
    companion object {
        fun from(config: ApplicationConfig) = GrokConfig(
            apiKey = config.propertyOrNull("grok.apiKey")?.getString() 
                ?: System.getenv("GROK_API_KEY") 
                ?: "",
            baseUrl = config.property("grok.baseUrl").getString(),
            model = config.property("grok.model").getString()
        )
    }
}
