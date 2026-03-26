package org.example.graphqldemo.data

data class PaginatedResponse<T>(
    val items: List<T>,
    val lastEvaluatedKey: String?
)