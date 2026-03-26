package org.example.graphqldemo.entity

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean

@DynamoDbBean
data class Address(
    var city: String? = null,
    var state: String? = null,
    var pinCode: Int? = null
)