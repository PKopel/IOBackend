package com.gumi.moodle.rest_controllers

import com.gumi.moodle.MalformedRouteException
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.atKey
import com.gumi.moodle.dao.withGradeID
import com.gumi.moodle.getParameters
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.Role.TEACHER
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.*


class GradeController

fun Application.gradeRoutes() {
    val dao = CourseDAO()

    routing {
        authenticate("basicAuth") {
            withRole(ADMIN, TEACHER) {
                route("/grade/{course_id}") {
                    post {
                        val grade = call.receive<Grade>()
                        val courseID = call.parameters["course_id"] ?: return@post call.respondText(
                            "Missing or malformed course id",
                            status = HttpStatusCode.BadRequest
                        )
                        val updated = dao.updateOne(
                            courseID,
                            push(Course::gradeModel, grade)
                        ) { Course::_id eq it }

                        if (updated) call.respond(HttpStatusCode.OK)
                        else call.respond(HttpStatusCode.NotModified)
                    }
                }
                route("/grade/{course_id}/{grade_id}") {
                    post {
                        try {
                            val grade = call.receive<Grade>()
                            val (courseID, gradeID) = call.getParameters("course_id", "grade_id")
                            val updated = dao.updateOne(
                                courseID,
                                set(Course::gradeModel.posOp setTo grade)
                            ) { Course::_id eq it withGradeID gradeID }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)

                        } catch (e: MalformedRouteException) {
                            return@post call.respondText(e.msg, status = HttpStatusCode.BadRequest)
                        }
                    }
                    delete {
                        try {
                            val (courseID, gradeID) = call.getParameters("course_id", "grade_id")
                            val updated = dao.updateOne(
                                courseID,
                                pullByFilter(Course::gradeModel, Grade::_id eq gradeID)
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)

                        } catch (e: MalformedRouteException) {
                            return@delete call.respondText(e.msg, status = HttpStatusCode.BadRequest)
                        }
                    }
                }
                route("/grade/{course_id}/{grade_id}/{student_id}") {
                    post {
                        try {
                            val grade = call.receive<Int>()
                            val (courseID, gradeID, studentID) = call.getParameters(
                                "course_id",
                                "grade_id",
                                "student_id"
                            )
                            val updated = dao.updateOne(
                                courseID,
                                set(Course::gradeModel.posOp / Grade::studentPoints atKey studentID setTo grade)
                            ) { Course::_id eq it withGradeID gradeID }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)

                        } catch (e: MalformedRouteException) {
                            return@post call.respondText(e.msg, status = HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
        }
    }
}