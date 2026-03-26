package org.example.graphqldemo.data

data class CourseRequest(
    val title: String,
    val description: String,
    val price: Double
)

data class CourseResponse(
    val courseId: String,
    val title: String,
    val description: String,
    val price: Double
)