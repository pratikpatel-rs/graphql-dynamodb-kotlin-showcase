package org.example.graphqldemo.repository

import kotlinx.coroutines.future.await
import org.example.graphqldemo.entity.Course
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

@Repository
class CourseRepository(enhancedAsyncClient: DynamoDbEnhancedAsyncClient) {

    private val courseTable = enhancedAsyncClient.table("course", TableSchema.fromBean(Course::class.java))

    suspend fun findById(id: String): Course? {
        val key = Key.builder().partitionValue(id).build()
        return courseTable.getItem(key).await()
    }

    suspend fun save(course: Course) {
        courseTable.putItem(course).await()
    }

    suspend fun deleteById(id: String) {
        val key = Key.builder().partitionValue(id).build()
        courseTable.deleteItem(key).await()
    }
}