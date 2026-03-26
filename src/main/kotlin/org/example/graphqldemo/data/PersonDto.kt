package org.example.graphqldemo.data

import org.example.graphqldemo.entity.Address

data class PersonRequest(
    val name: String,
    val age: Int,
    val email: String,
    val address: Address?
)

data class PersonResponse(
    val personId: String,
    val name: String,
    val age: Int,
    val email: String,
    val address: Address?,
    val enrolledCourseIds: List<String>
)

data class PersonCreatedEvent(
    val personId: String,
    val name: String,
    val email: String
)