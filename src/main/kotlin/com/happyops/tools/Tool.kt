package com.happyops.tools

import arrow.core.Either
import com.happyops.model.DomainError
import com.happyops.model.ToolDefinition
import com.happyops.model.ToolRequest
import com.happyops.model.ToolResponse

interface Tool {
    val name: String
    val description: String
    
    suspend fun execute(input: ToolRequest): Either<DomainError, ToolResponse>
    
    fun getDefinition(): ToolDefinition
}
