package org.example.graphqldemo.entity

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import java.util.UUID

@DynamoDbBean
data class Course(
    @get:DynamoDbPartitionKey
    var courseId: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var description: String? = null,
    var price: Double? = null
)