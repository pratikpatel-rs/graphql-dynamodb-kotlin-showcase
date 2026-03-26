package org.example.graphqldemo.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.graphqldemo.data.CourseRequest
import org.example.graphqldemo.data.CourseResponse
import org.example.graphqldemo.entity.Course
import org.example.graphqldemo.repository.CourseRepository
import org.springframework.stereotype.Service
import org.example.graphqldemo.exception.ResourceNotFoundException

@Service
class CourseService(private val courseRepository: CourseRepository) {

    suspend fun createCourse(request: CourseRequest): CourseResponse {
        val course = Course(
            title = request.title,
            description = request.description,
            price = request.price,
        )
        courseRepository.save(course)
        return course.toResponse()
    }

    suspend fun getCourse(id: String): CourseResponse {
        val course = courseRepository.findById(id) ?: throw ResourceNotFoundException("Course not found")
        return course.toResponse()
    }

    suspend fun deleteCourseById(id: String) {
        courseRepository.deleteById(id)
    }

    suspend fun getCoursesByIds(courseIds: List<String>): List<CourseResponse> = coroutineScope {
        if (courseIds.isEmpty()) return@coroutineScope emptyList()

        val fetchedCourses = courseIds.map { courseId ->
            async { courseRepository.findById(courseId) }
        }.awaitAll().filterNotNull()

        fetchedCourses.map { it.toResponse() }
    }

    private fun Course.toResponse() = CourseResponse(
        courseId = this.courseId,
        title = this.title ?: "",
        description = this.description ?: "",
        price = this.price ?: 0.0
    )
}