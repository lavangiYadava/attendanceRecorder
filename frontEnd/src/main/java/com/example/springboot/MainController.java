package com.example.springboot;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController

public class MainController {

    user user;
    dbFunctions connFunc = new dbFunctions();


    public MainController() {
    }

    @GetMapping("/user")
    public String index() {
        return "Greetings from Spring Boot!";
    }


    @PostMapping("/professor/newAccount")
    public ResponseEntity<String> newProfessorAccount(@RequestBody professorObject prof) throws SQLException {
        int result = connFunc.createProfessor(prof);
        if (result == connFunc.USERNAME_EXISTS) {
            return new ResponseEntity<>("This username already exists.", HttpStatus.CONFLICT);
        } else if (result == connFunc.EMAIL_EXISTS) {
            return new ResponseEntity<>("This email already exists.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        user = prof;
        System.out.println(result);
        return new ResponseEntity<>("Success! Your account has been created. You can now log in with the ID : " + result, HttpStatus.CREATED);

    }

    @PostMapping("/student/newAccount")
    public ResponseEntity<String> newStudentAccount(@RequestBody studentObject student) throws SQLException {
        int result = connFunc.createStudent(student);

        if (result == connFunc.EMAIL_EXISTS) {
            return new ResponseEntity<>("The email you entered is already registered. Please use another email.", HttpStatus.CONFLICT);
        } else if (result == connFunc.ERROR) {
            return new ResponseEntity<>("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        user = student;
        return new ResponseEntity<>("Success! Your account has been created. You can now log in with the ID : " + result, HttpStatus.CREATED);
    }
    @PostMapping("/admin/newAccount")
    public ResponseEntity<String> newAdminAccount(@RequestBody adminObject admin) throws SQLException {
        int result = connFunc.createAdmin(admin);
        if (result == connFunc.USERNAME_EXISTS) {
            return new ResponseEntity<>("This username already exists.", HttpStatus.CONFLICT);
        } else if (result == connFunc.EMAIL_EXISTS) {
            return new ResponseEntity<>("This email already exists.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        user = admin;
        System.out.println(result);
        return new ResponseEntity<>("Success! Your account has been created. You can now log in with the ID : " + result, HttpStatus.CREATED);

    }
    @PostMapping("/login")
    public ResponseEntity<String> logIn(@RequestBody user obj) throws SQLException {
        if (connFunc.loggingIn(obj) == connFunc.SUCCESS) {
            user = obj;
            return new ResponseEntity<>("Success! You have been logged in.", HttpStatus.OK);
        }
        return new ResponseEntity<>("You were not able to be logged in.", HttpStatus.BAD_REQUEST);
    }
    /* list all collections */

    @GetMapping("/courses")
    public ResponseEntity<?> allCourses() {
        String[] result = connFunc.getAllClasses();
        if (result == null || result.length == 0) {
            return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message", "There are no classes to view."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("classes", result);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/students")
    public ResponseEntity<?> allStudents() {
        String[] result = connFunc.getAllStudents();
        if (result == null || result.length == 0) {
            return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message", "There are no students to view."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("students", result);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/professors")
    public ResponseEntity<?> allProfessors() {
        String[] result = connFunc.getAllProfessors();
        if (result == null || result.length == 0) {
            return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message", "There are no professors to view."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("professors", result);

        return ResponseEntity.ok(response);
    }


    /* as a professor or student, get classes */
    @GetMapping("/viewClasses/{id}")
    public String[] getClasses(@PathVariable int id, @RequestParam int usertype) {
        System.out.println("usertype is"  + usertype);
        System.out.println("id is"  + id);
        return connFunc.viewClasses(id, usertype);
    }


    @GetMapping("/getDetails/{id}")
    public String getDetails(@PathVariable int id) {
        return connFunc.getCourseDetails(id);

    }

    @GetMapping("/students/{courseID}")
    public String[] getStudents(
            @PathVariable int courseID) {
        System.out.println("has reached the get students api");
        return connFunc.viewStudents(courseID);

    }

    @GetMapping("/time/ID")
    public String getTime(
            @RequestParam(value = "courseID", defaultValue = "-1") int courseID) {

        if (courseID == -1) {
            return "ID or user field is missing.";
        }
        String result = connFunc.getTime(courseID);
        if (result == null) {
            return "There is no time allocated for this class";
        }
        return String.format("The time for this course is at %s", result);
    }

    @GetMapping("/professor/ID")
    public String getProfessor(
            @RequestParam(value = "courseID", defaultValue = "-1") int courseID) {

        if (courseID == -1) {
            return "ID or user field is missing.";
        }
        String result = connFunc.getProfessors(courseID);
        if (result == null) {
            return "There is no professor for this class.";
        }
        return String.format("The professor is : %s <br>", result);
    }

    /* post methods for new resources */


    @PostMapping("/professor/newClass")
    public ResponseEntity<String> newClass(@RequestBody courseObject course) throws SQLException {
        System.out.println("course ID" + course.getCourseID());
        System.out.println("course name" + course.getCourseName());
        System.out.println("course description" + course.getCourseDescription());
        System.out.println("time" + course.getTime());

        if (connFunc.createClass(course) != connFunc.SUCCESS) {
            return new ResponseEntity<>("Failure! Your class was not able to be created.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Success! Your class has been created.", HttpStatus.OK);
    }

    @PostMapping("/student/newClass")
    public ResponseEntity<String> joiningNewClass(
            @RequestParam(value = "studentID") int studentID,
            @RequestParam(value = "courseID") int courseID) throws SQLException {
        System.out.println("henlo");

        System.out.println("student ID : " + studentID);
        System.out.println("course ID : " + courseID);


        // Assuming connFunc.joinClass handles the logic to add the student to the class
        if (connFunc.joinClass(studentID, courseID) == connFunc.SUCCESS) {
            return ResponseEntity.ok("Success! You were able to join the class.");
        } else {
            return ResponseEntity.badRequest().body("Failure! You were not able to join the class.");
        }
    }


    @PutMapping("/professor/time")
    public ResponseEntity<String> changeTime(
            @RequestParam(value = "courseID") int courseID,
            @RequestParam(value = "time") String time) throws SQLException {

        if (connFunc.updateClassTime(time, courseID) != connFunc.SUCCESS) {
            System.out.println("error");
            return new ResponseEntity<>("The time of the class was not updated successfully.", HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("Success! The time of the class has been updated.", HttpStatus.OK);
    }


    @PostMapping("/professor/deleteCourse")
    public ResponseEntity<String> deleteCourse(@RequestParam(value="courseID") int courseID, @RequestParam(value="usertype") int usertype) throws SQLException {
        if (connFunc.deleteCourse(courseID, usertype) == connFunc.SUCCESS) {
            return new ResponseEntity<>("The course has been successfully deleted.", HttpStatus.OK);
        }
        return new ResponseEntity<>("The course was not deleted.", HttpStatus.CONFLICT);
    }

    @PostMapping("/admin/deleteCourse")
    public ResponseEntity<String> deleteCourseAdmin(@RequestParam(value="courseID") int courseID) throws SQLException {
        if (connFunc.deleteCourseAdmin(courseID) == connFunc.SUCCESS) {
            return new ResponseEntity<>("The course has been successfully deleted.", HttpStatus.OK);
        }
        return new ResponseEntity<>("The course was not deleted.", HttpStatus.CONFLICT);
    }


    @GetMapping("/user/getInformation")
    public String getInfo(@RequestParam(value="id") int id,
                                          @RequestParam(value="usertype") int usertype) throws SQLException {
String returned = connFunc.userInformation(id, usertype);
        if (returned == null) {
            return null;
        }
        System.out.println(returned);
        return returned;
    }




}



