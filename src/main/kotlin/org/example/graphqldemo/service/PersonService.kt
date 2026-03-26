package org.example.graphqldemo.service

import org.apache.kafka.clients.producer.KafkaProducer
import org.example.graphqldemo.data.PaginatedResponse
import org.example.graphqldemo.data.PersonCreatedEvent
import org.example.graphqldemo.data.PersonRequest
import org.example.graphqldemo.data.PersonResponse
import org.example.graphqldemo.entity.Person
import org.example.graphqldemo.repository.CourseRepository
import org.example.graphqldemo.repository.PersonRepository
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import org.example.graphqldemo.exception.ResourceNotFoundException
import org.example.graphqldemo.kafka.KafkaProducerService

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val courseRepository: CourseRepository,
    private val kafkaProducer: KafkaProducerService
) {
    suspend fun createPerson(request: PersonRequest): PersonResponse {
        val person = Person(
            name = request.name,
            age = request.age,
            email = request.email,
            address = request.address,
            enrolledCourseIds = emptyList()
        )
        // 1. Save to DynamoDB
        personRepository.save(person)

        // 2. Publish to Kafka
        val event = PersonCreatedEvent(person.personId, person.name ?: "", person.email ?: "")
        kafkaProducer.publishPersonCreatedEvent(event)
        return person.toResponse()
    }

    suspend fun getPerson(id: String): PersonResponse {
        val person = personRepository.findById(id) ?: throw ResourceNotFoundException("Person not found")
        return person.toResponse()
    }

    suspend fun deletePerson(id: String) {
        personRepository.deleteById(id)
    }

    suspend fun updatePerson(id: String, request: PersonRequest): PersonResponse {
        val existingPerson = personRepository.findById(id) ?: throw ResourceNotFoundException("Person not found")

        existingPerson.name = request.name
        existingPerson.age = request.age
        existingPerson.email = request.email
        existingPerson.address = request.address

        val updated = personRepository.updatePerson(existingPerson)
        return updated.toResponse()
    }

    suspend fun getPersonsPaginated(limit: Int, lastKey: String?): PaginatedResponse<PersonResponse> {
        val startKeyMap = lastKey?.let { mapOf("personId" to AttributeValue.builder().s(it).build()) }
        val (persons, newLastKeyMap) = personRepository.findAllPaginated(limit, startKeyMap)

        val nextKeyString = newLastKeyMap?.get("personId")?.s()

        return PaginatedResponse(
            items = persons.map { it.toResponse() },
            lastEvaluatedKey = nextKeyString
        )
    }

    suspend fun enrollInCourse(personId: String, courseId: String): PersonResponse {
        val person = personRepository.findById(personId) ?: throw ResourceNotFoundException("Person not found")
        courseRepository.findById(courseId) ?: throw ResourceNotFoundException("Course not found")

        val currentCourses = person.enrolledCourseIds.toMutableList()
        if (!currentCourses.contains(courseId)) {
            currentCourses.add(courseId)
            person.enrolledCourseIds = currentCourses
            personRepository.save(person)
        }

        return person.toResponse()
    }

    private fun Person.toResponse() = PersonResponse(
        personId = this.personId,
        name = this.name ?: "",
        age = this.age ?: 0,
        email = this.email ?: "",
        address = this.address,
        enrolledCourseIds = this.enrolledCourseIds
    )
}