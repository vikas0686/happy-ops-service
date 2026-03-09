package com.happyops.model

sealed class DomainError(open val message: String) {
    data class NotFound(override val message: String) : DomainError(message)
    data class ValidationError(override val message: String) : DomainError(message)
    data class DatabaseError(override val message: String) : DomainError(message)
    data class ExternalServiceError(override val message: String) : DomainError(message)
    data class ToolExecutionError(override val message: String) : DomainError(message)
}
