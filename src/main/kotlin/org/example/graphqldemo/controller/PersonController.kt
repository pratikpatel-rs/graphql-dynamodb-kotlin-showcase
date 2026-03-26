package org.example.graphqldemo.controller

import org.example.graphqldemo.data.CourseResponse
import org.example.graphqldemo.data.PaginatedResponse
import org.example.graphqldemo.data.PersonRequest
import org.example.graphqldemo.data.PersonResponse
import org.example.graphqldemo.service.CourseService
import org.example.graphqldemo.service.PersonService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class PersonController(
    private val personService: PersonService,
    private val courseService: CourseService
) {

    @QueryMapping
    suspend fun getPerson(@Argument id: String): PersonResponse {
        return personService.getPerson(id)
    }

    @QueryMapping
    suspend fun getAllPersons(
        @Argument limit: Int?,
        @Argument lastKey: String?
    ): PaginatedResponse<PersonResponse> {
        return personService.getPersonsPaginated(limit ?: 10, lastKey)
    }

    @MutationMapping
    suspend fun createPerson(@Argument request: PersonRequest): PersonResponse {
        return personService.createPerson(request)
    }

    @MutationMapping
    suspend fun updatePerson(@Argument id: String, @Argument request: PersonRequest): PersonResponse {
        return personService.updatePerson(id, request)
    }

    @MutationMapping
    suspend fun deletePerson(@Argument id: String): Boolean {
        personService.deletePerson(id)
        return true
    }

    @MutationMapping
    suspend fun enrollInCourse(@Argument personId: String, @Argument courseId: String): PersonResponse {
        return personService.enrollInCourse(personId, courseId)
    }

    // Resolves nested courses ONLY when explicitly requested in the GraphQL query
    @SchemaMapping(typeName = "PersonResponse", field = "enrolledCourses")
    suspend fun resolveEnrolledCourses(person: PersonResponse): List<CourseResponse> {
        return courseService.getCoursesByIds(person.enrolledCourseIds)
    }
}