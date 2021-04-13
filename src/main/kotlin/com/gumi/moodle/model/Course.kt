package com.gumi.moodle.model

class Course(
    var _id: String?,
    var name: String,
    var description: String,
    var studentLimit: Int = 100,
    var students: MutableMap<String, Array<Unit>> = mutableMapOf(),  //student id ->
    var teachers: List<String>,  //list of teacher ids
) {
    fun filterStudents(id: String) {
        students.keys.retainAll { it == id }
    }
}