package org.example.graphqldemo.kafka

import org.example.graphqldemo.data.PersonCreatedEvent
import org.springframework.kafka.annotation.BackOff
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DltStrategy
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class PersonEventConsumer {

    /**
     * If this method throws an exception, Spring will automatically retry it 3 times,
     * waiting 2 seconds between each attempt. If it fails 3 times, it sends the
     * message to "person-created-topic-dlt".
     */
    @RetryableTopic(
        attempts = "3",
        backOff = BackOff(delay = 2000, multiplier = 2.0),
        dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = ["person-created-topic"], groupId = "person-service-group")
    fun consumePersonCreatedEvent(event: PersonCreatedEvent) {
        println("Received Kafka event! Sending welcome email to: ${event.email}")

        if (event.email.contains("error")) {
            throw RuntimeException("Simulated email failure for ${event.email}")
        }

        println("Email sent successfully to ${event.email}!")
    }

    /**
     * This method automatically listens to the Dead Letter Queue.
     * When a message completely fails all retries, it lands here.
     * THIS is where you debug broken messages!
     */
    @DltHandler
    fun handleDeadLetterQueue(
        event: PersonCreatedEvent,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) errorMessage: String
    ) {
        println("🚨 DLQ ALERT 🚨")
        println("Message for ${event.personId} failed all retries.")
        println("Reason: $errorMessage")
    }
}