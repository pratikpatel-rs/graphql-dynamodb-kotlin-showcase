package org.example.graphqldemo.repository

import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitFirst
import org.example.graphqldemo.entity.Person
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@Repository
class PersonRepository(
    enhancedAsyncClient: DynamoDbEnhancedAsyncClient
) {

    private val personTable = enhancedAsyncClient.table("person", TableSchema.fromBean(Person::class.java))

    suspend fun save(person: Person) {
        personTable.putItem(person).await()
    }

    suspend fun findById(id: String): Person? {
        val key = Key.builder().partitionValue(id).build()
        return personTable.getItem(key).await()
    }

    suspend fun deleteById(id: String) {
        val key = Key.builder().partitionValue(id).build()
        personTable.deleteItem(key).await()
    }

    suspend fun updatePerson(person: Person): Person {
        return personTable.updateItem(person).await()
    }

    suspend fun findAllPaginated(
        limit: Int,
        lastEvaluatedKey: Map<String, AttributeValue>?
    ): Pair<List<Person>, Map<String, AttributeValue>?> {

        val requestBuilder = ScanEnhancedRequest.builder()
            .limit(limit)

        if (!lastEvaluatedKey.isNullOrEmpty()) {
            requestBuilder.exclusiveStartKey(lastEvaluatedKey)
        }

        // 2. Execute the scan and await the first page of results
        // personTable.scan() returns a reactive Publisher<Page<Person>>
        val page = personTable.scan(requestBuilder.build()).awaitFirst()

        // 3. Return the automatically mapped items and the pagination key
        return Pair(page.items(), page.lastEvaluatedKey())
    }
}