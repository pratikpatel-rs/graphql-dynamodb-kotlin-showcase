package org.example.graphqldemo.entity

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import java.util.UUID

@DynamoDbBean
data class Person(
    @get:DynamoDbPartitionKey
    var personId: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var age: Int? = null,
    var email: String? = null,
    var address: Address? = null,
    var enrolledCourseIds: List<String> = emptyList()
)