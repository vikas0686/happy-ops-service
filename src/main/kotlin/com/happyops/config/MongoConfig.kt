package com.happyops.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import io.ktor.server.config.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class MongoConfig(config: ApplicationConfig) {
    private val uri = config.property("mongo.uri").getString()
    private val databaseName = config.property("mongo.database").getString()

    private val client: CoroutineClient by lazy {
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .build()
        KMongo.createClient(settings).coroutine
    }

    val database: CoroutineDatabase by lazy {
        client.getDatabase(databaseName)
    }
}
