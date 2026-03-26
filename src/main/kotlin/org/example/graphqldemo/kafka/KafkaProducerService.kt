package org.example.graphqldemo.kafka

import kotlinx.coroutines.future.await
import org.example.graphqldemo.data.PersonCreatedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val topicName = "person-created-topic"

    suspend fun publishPersonCreatedEvent(event: PersonCreatedEvent) {
        try {
            val result = kafkaTemplate.send(topicName, event.personId, event).await()
            println("Successfully published event for ${event.personId} at offset ${result.recordMetadata.offset()}")
        } catch (ex: Exception) {
            println("Failed to publish Kafka event: ${ex.message}")
        }
    }
}