package com.happyops.tools

import arrow.core.Either
import com.happyops.model.DomainError
import com.happyops.model.ToolDefinition

class ToolRegistry(private val tools: List<Tool>) {
    
    fun getToolByName(name: String): Tool? = 
        tools.find { it.name == name }
    
    fun getAllTools(): List<Tool> = tools
    
    fun getToolDefinitions(): List<ToolDefinition> = 
        tools.map { it.getDefinition() }
    
    fun hasToolNamed(name: String): Boolean = 
        tools.any { it.name == name }
}
