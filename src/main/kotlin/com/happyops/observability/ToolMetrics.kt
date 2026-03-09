package com.happyops.observability

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object ToolMetrics {
    private val executionCounts = ConcurrentHashMap<String, AtomicLong>()
    private val executionTimes = ConcurrentHashMap<String, AtomicLong>()
    private val errorCounts = ConcurrentHashMap<String, AtomicLong>()

    fun recordExecution(toolName: String, durationMs: Long, success: Boolean) {
        executionCounts.getOrPut(toolName) { AtomicLong(0) }.incrementAndGet()
        executionTimes.getOrPut(toolName) { AtomicLong(0) }.addAndGet(durationMs)
        if (!success) {
            errorCounts.getOrPut(toolName) { AtomicLong(0) }.incrementAndGet()
        }
    }

    fun getMetrics(): Map<String, ToolMetric> =
        executionCounts.keys.associateWith { toolName ->
            val count = executionCounts[toolName]?.get() ?: 0
            val totalTime = executionTimes[toolName]?.get() ?: 0
            val errors = errorCounts[toolName]?.get() ?: 0
            ToolMetric(
                executions = count,
                averageDurationMs = if (count > 0) totalTime / count else 0,
                errors = errors
            )
        }
}

data class ToolMetric(
    val executions: Long,
    val averageDurationMs: Long,
    val errors: Long
)
