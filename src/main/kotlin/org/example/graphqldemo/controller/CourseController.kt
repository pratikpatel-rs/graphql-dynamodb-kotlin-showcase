package org.example.graphqldemo.controller

import org.example.graphqldemo.data.CourseRequest
import org.example.graphqldemo.data.CourseResponse
import org.example.graphqldemo.service.CourseService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CourseController(
    private val courseService: CourseService
) {

    @QueryMapping
    suspend fun getCourse(@Argument id: String): CourseResponse {
        return courseService.getCourse(id)
    }

    @MutationMapping
    suspend fun createCourse(@Argument request: CourseRequest): CourseResponse {
        return courseService.createCourse(request)
    }

    @MutationMapping
    suspend fun deleteCourse(@Argument id: String): Boolean {
        courseService.deleteCourseById(id)
        return true // GraphQL mutations often return a boolean for deletes
    }
}