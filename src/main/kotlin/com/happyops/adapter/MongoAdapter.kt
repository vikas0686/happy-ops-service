package com.happyops.adapter

import com.happyops.config.MongoConfig
import org.litote.kmongo.coroutine.CoroutineDatabase

class MongoAdapter(mongoConfig: MongoConfig) {
    val database: CoroutineDatabase = mongoConfig.database
}
