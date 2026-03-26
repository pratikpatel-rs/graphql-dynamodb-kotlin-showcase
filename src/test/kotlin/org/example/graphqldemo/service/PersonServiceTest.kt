package org.example.graphqldemo.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.graphqldemo.data.PersonRequest
import org.example.graphqldemo.entity.Course
import org.example.graphqldemo.entity.Person
import org.example.graphqldemo.exception.ResourceNotFoundException
import org.example.graphqldemo.kafka.KafkaProducerService
import org.example.graphqldemo.repository.CourseRepository
import org.example.graphqldemo.repository.PersonRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PersonServiceTest {

    private val personRepository = mockk<PersonRepository>()
    private val courseRepository = mockk<CourseRepository>()
    private val kafkaProducer = mockk<KafkaProducerService>()

    private val personService = PersonService(personRepository, courseRepository, kafkaProducer)

    @Test
    fun `should create person successfully and publish kafka event`() = runTest {
        val request = PersonRequest(
            name = "Pratik",
            age = 30,
            email = "pratik@gmail.com",
            address = null
        )

        coEvery { personRepository.save(any()) } returns Unit

        coEvery { kafkaProducer.publishPersonCreatedEvent(any()) } returns Unit

        val response = personService.createPerson(request)

        assertNotNull(response.personId)
        assertEquals("Jitu", response.name)
        assertEquals(60, response.age)
        assertEquals("jitu@gmail.com", response.email)

        coVerify(exactly = 1) { personRepository.save(any()) }
        coVerify(exactly = 1) { kafkaProducer.publishPersonCreatedEvent(any()) }
    }

    @Test
    fun `should get person when id exists`() = runTest {
        val validId = "12345"
        val mockPerson = Person(
            personId = validId,
            name = "Pratik",
            age = 30,
            email = "pratik@example.com",
            enrolledCourseIds = listOf("course-1")
        )
        coEvery { personRepository.findById(validId) } returns mockPerson

        val response = personService.getPerson(validId)

        assertEquals(validId, response.personId)
        assertEquals("Pratik", response.name)

        assertEquals(1, response.enrolledCourseIds.size)
        assertEquals("course-1", response.enrolledCourseIds.first())
    }

    @Test
    fun `should throw ResourceNotFoundException when person does not exist`() = runTest {
        val invalidId = "99999"
        coEvery { personRepository.findById(invalidId) } returns null

        val exception = assertThrows<ResourceNotFoundException> {
            personService.getPerson(invalidId)
        }

        assertEquals("Person not found", exception.message)
        coVerify(exactly = 1) { personRepository.findById(invalidId) }
    }

    @Test
    fun `should successfully enroll person in a course`() = runTest {
        val personId = "user-123"
        val courseId = "course-999"
        val existingCourseId = "existing-course-1"

        val mockPerson = Person(
            personId = personId,
            name = "Pratik",
            enrolledCourseIds = listOf(existingCourseId)
        )

        val mockNewCourse = Course(courseId, "New Course", "Description", 15.0)

        coEvery { personRepository.findById(personId) } returns mockPerson

        coEvery { courseRepository.findById(courseId) } returns mockNewCourse

        coEvery { personRepository.save(any()) } returns Unit

        val response = personService.enrollInCourse(personId, courseId)

        coVerify(exactly = 1) { personRepository.save(any()) }

        assertEquals(2, response.enrolledCourseIds.size)
        assertTrue(response.enrolledCourseIds.contains(courseId))
        assertTrue(response.enrolledCourseIds.contains(existingCourseId))
    }

    @Test
    fun `should throw exception when enrolling in non-existent course`() = runTest {
        val personId = "user-123"
        val invalidCourseId = "fake-course-id"

        val mockPerson = Person(personId = personId, name = "Pratik")

        coEvery { personRepository.findById(personId) } returns mockPerson

        coEvery { courseRepository.findById(invalidCourseId) } returns null

        val exception = assertThrows<ResourceNotFoundException> {
            personService.enrollInCourse(personId, invalidCourseId)
        }

        assertEquals("Course not found", exception.message)

        coVerify(exactly = 0) { personRepository.save(any()) }
    }
}