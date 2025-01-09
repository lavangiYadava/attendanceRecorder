package com.example.springboot;

import java.sql.*;
import java.util.List;
import java.util.Random;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbFunctions {
    professorObject newObj = new professorObject();

    int USERNAME_EXISTS = -1;
    int EMAIL_EXISTS = -2;
    int COURSE_EXISTS = -3;
    int ERROR = 0;
    int SUCCESS = 1;
    int ID_NONEXISTENT = -4;
    Connection conn = null;

    public dbFunctions() {
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5433/" + "Personal Project Database", "postgres", "ToffeeYadav#1");
            if (conn == null) {
                System.out.println("Connection is null");
                throw new SQLException("Failed to establish database connection");
            }
            System.out.println("Connection Established");
        } catch (SQLException e) {
            System.out.println("Connection Error Occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }



    /* WORKS */
    public int loggingIn(user obj) {
        String userQuery = null;
        String duplicateCheckQuery = null;
        System.out.println(obj.getID());
        Integer ID = obj.getID();
        String password = obj.getPassword();
        Integer userType = obj.getUserType();
        if (userType == 1) {
            userQuery = "SELECT * FROM public.\"Professor\" WHERE \"ProfessorID\" = ? AND \"ProfessorPassword\" = ?";
            duplicateCheckQuery = "SELECT COUNT(*) AS passwordCount FROM public.\"Professor\" WHERE \"ProfessorPassword\" = ?";
        }
        else if (userType == 0) {
            userQuery = "SELECT * FROM public.\"Student\" WHERE \"StudentID\" = ? AND \"StudentPassword\" = ?";
            duplicateCheckQuery = "SELECT COUNT(*) AS passwordCount FROM public.\"Student\" WHERE \"StudentPassword\" = ?";
        }
        else if (userType == 2) {
            userQuery = "SELECT * FROM public.\"Admin\" WHERE \"adminID\" = ? AND \"adminPassword\" = ?";
            duplicateCheckQuery = "SELECT COUNT(*) AS passwordCount FROM public.\"Admin\" WHERE \"adminPassword\" = ?";
        }


        try (PreparedStatement duplicateCheckStmt = conn.prepareStatement(duplicateCheckQuery)) {
            System.out.println("Executing query: " + userQuery);
            System.out.println("ID: " + ID);
            System.out.println("Password: " + password);

            duplicateCheckStmt.setString(1, password);
            try (ResultSet duplicateResult = duplicateCheckStmt.executeQuery()) {
                if (duplicateResult.next() && duplicateResult.getInt("passwordCount") > 1) {
                    System.out.println("Password is shared by multiple users!");
                    return ERROR;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error during duplicate check: " + e.getMessage());
            return ERROR;
        }
        try (PreparedStatement userStmt = conn.prepareStatement(userQuery)) {
            userStmt.setInt(1, ID);
            userStmt.setString(2, password);

            try (ResultSet userResult = userStmt.executeQuery()) {
                if (userResult.next()) {
                    System.out.println("Successfully logged in!");
                    return SUCCESS; // Replace with your success code
                } else {
                    System.out.println("Invalid credentials!");
                    return ERROR;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
            return ERROR;
        }
    }

    public Integer createProfessor(professorObject obj) throws SQLException {
        String email = obj.getEmail();
        String password = obj.getPassword();
        String username = obj.getUsername();
        String allEmails = "SELECT * FROM \"Professor\" WHERE \"ProfessorEmail\" = ?";
        try (PreparedStatement s = conn.prepareStatement(allEmails)) {
            s.setString(1, email);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    System.out.println("Email exists!");
                    return EMAIL_EXISTS;
                }
            }
        }
        String allUsernames = "SELECT * FROM \"Professor\" WHERE \"ProfessorUsername\" = ?";
        try (PreparedStatement s = conn.prepareStatement(allUsernames)) {
            s.setString(1, username);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    System.out.println("Username exists!");
                    return USERNAME_EXISTS;
                }
            }
        }
        String query = "INSERT INTO \"Professor\" (\"ProfessorID\", \"ProfessorEmail\", \"ProfessorUsername\", \"ProfessorPassword\") " +
                "VALUES (floor(random() * (9999 - 1000 + 1) + 1000), ?, ?, ?) RETURNING \"ProfessorID\"";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            // Set the parameters for email, username, and password
            statement.setString(1, email);
            statement.setString(2, username);
            statement.setString(3, password);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    newObj = new professorObject();
                    newObj.setID(rs.getInt("ProfessorID"));
                    System.out.println("SUCCESS");
                    System.out.println("ID WITHIN FUNCTION IS:" + newObj.getID());
                    return newObj.getID();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ERROR;
    }

    public Integer createStudent(studentObject student) throws SQLException {
        String email = student.getEmail();
        String firstName = student.getFirstName();
        String lastName = student.getLastName();
        String password = student.getPassword();

        String allEmails = "SELECT * FROM \"Student\" WHERE \"StudentEmail\" = ?";
        try (PreparedStatement s = conn.prepareStatement(allEmails)) {
            s.setString(1, email);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    System.out.println("Email exists!");
                    return EMAIL_EXISTS;
                }
            }
        }
        try {
            String query = "INSERT INTO \"Student\" (\"StudentID\", \"StudentEmail\", \"StudentFirstName\", \"StudentLastName\", \"StudentPassword\") " +
                    "VALUES (floor(random() * (9999 - 1000 + 1) + 1000), ?, ?, ?, ?) RETURNING \"StudentID\"";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, firstName);
                statement.setString(3, lastName);
                statement.setString(4, password);


                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        int studentID = rs.getInt("StudentID");
                        System.out.println("Student added! Your ID is " + studentID);
                        studentObject newObj = new studentObject(firstName, lastName, email, password, student.getID());
                        return studentID;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return ERROR;
    }

    public Integer createAdmin(adminObject admin) throws SQLException {
        String email = admin.getEmail();
        String password = admin.getPassword();
        String username = admin.getUsername();

        String checkEmailQuery = "SELECT 1 FROM \"Admin\" WHERE \"adminEmail\" = ?";
        try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery)) {
            checkEmailStmt.setString(1, email);
            try (ResultSet rs = checkEmailStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Email already exists!");
                    return EMAIL_EXISTS;
                }
            }
        }
        String allUsernames = "SELECT * FROM \"Admin\" WHERE \"adminUsername\" = ?";
        try (PreparedStatement s = conn.prepareStatement(allUsernames)) {
            s.setString(1, username);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    System.out.println("Username exists!");
                    return USERNAME_EXISTS;
                }
            }
        }

        String insertQuery = "INSERT INTO \"Admin\" (\"adminID\", \"adminEmail\", \"adminUsername\", \"adminPassword\") " +
                "VALUES (floor(random() * (9999 - 1000 + 1) + 1000), ?, ?, ?) RETURNING \"adminID\"";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, email);
            insertStmt.setString(2, username);
            insertStmt.setString(3, password);

            try (ResultSet rs = insertStmt.executeQuery()) {
                if (rs.next()) {
                    int adminID = rs.getInt("adminID");
                    System.out.println("Admin added! Your ID is " + adminID);

                    adminObject newAdmin = new adminObject(username, password, email, adminID);
                    return adminID;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error while inserting admin record.");
        }
        return ERROR;
    }



    public int createClass(courseObject course) {
        String courseName = course.getCourseName();
        String description = course.getCourseDescription();
        String time = course.getTime();
        Integer ID = course.getProfID();

        /* ensure that professor exists */

        String allNames = "SELECT * FROM \"Course\" WHERE \"CourseName\" = ?";
        try (PreparedStatement s = conn.prepareStatement(allNames)) {
            s.setString(1, courseName);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    System.out.println("Course name exists!");
                    return COURSE_EXISTS;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Statement statementRelationalTable;
        int courseID = 0;
        try {
            String queryCourse = "INSERT INTO \"Course\"(\"SectionID\", \"Description\", \"Time\", \"CourseName\") " +
                    "VALUES (floor(random() * (9999 - 1000 + 1) + 1000), ?, ?, ?) RETURNING \"SectionID\"";
            try (PreparedStatement statement = conn.prepareStatement(queryCourse)) {
                statement.setString(1, description);
                statement.setString(2, time);
                statement.setString(3, courseName);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        courseID = rs.getInt("SectionID");
                        System.out.println("The course has been successfully added!! The course ID is" + courseID);
                    }
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            /* add this into the course table */

            String queryRelational = "INSERT INTO \"ProfessorCourse\"(\"Key\", \"ProfessorID\", \"CourseID\") " +
                    "VALUES ((FLOOR(RANDOM() * 9000) + 1000), '" + ID + "', '" + courseID + "');";

            /* a statement object is made and linked to database, and is then executed */

            statementRelationalTable = conn.createStatement();
            statementRelationalTable.executeUpdate(queryRelational);
            System.out.println("course and professor have been mapped in relational table");
            return SUCCESS;

        } catch (
                Exception e) {
            System.out.println(e);
        }
        return ERROR;
    }

    /* student will join class based off of section ID */

    public int joinClass(Integer studentID, Integer courseID) throws SQLException {

        System.out.println("recieved courseID" + courseID);
        System.out.println("recieved studentID" + studentID);

        /* if a courseID is entered but that does not exist */

        String queryClassExists = "SELECT * FROM \"Course\" WHERE \"SectionID\" = ?";
        try (PreparedStatement s = conn.prepareStatement(queryClassExists)) {
            s.setInt(1, courseID);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    System.out.println("Course ID does not exist!");
                    return ID_NONEXISTENT;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        /* if the student is already apart of that class */

        String allEmails = "SELECT * FROM \"StudentCourse\" WHERE \"StudentID\" = ? AND \"ClassID\" = ?";
        try (PreparedStatement s = conn.prepareStatement(allEmails)) {
            s.setInt(1, studentID);
            s.setInt(2, courseID);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    System.out.println("Student exists!");
                    return ERROR;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        Statement statementRelationalTable;
        Random r = new Random();
        int randomizedKey = r.nextInt(9000) + 1000;

        try {
            String query = "INSERT INTO \"StudentCourse\"(\"Key\", \"StudentID\", \"ClassID\") " +
                    "VALUES (" + randomizedKey + ", " + studentID + ", " + courseID + ");";
            statementRelationalTable = conn.createStatement();
            statementRelationalTable.executeUpdate(query);
            System.out.println("student has been mapped to relational table");
            return SUCCESS;

        } catch (Exception e) {
            System.out.println(e);
        }
        return ERROR;

    }

    public String[] viewClasses(int ID, int usertype) {
        System.out.println("made it in");
        Statement statement;
        ResultSet result;
        List<String> classesList = new ArrayList<>();

        try {
            if (usertype == 2) {
                String query = "SELECT DISTINCT \"CourseName\", \"SectionID\" " +
                        "FROM \"ProfessorCourse\" " +
                        "JOIN \"Course\" ON \"Course\".\"SectionID\" = \"ProfessorCourse\".\"CourseID\" " +
                        "WHERE \"ProfessorCourse\".\"ProfessorID\" = " + ID;

                statement = conn.createStatement();
                result = statement.executeQuery(query);

                while (result.next()) {
                    String courseName = result.getString("CourseName");
                    Integer sectionID = result.getInt("SectionID");
                    classesList.add(String.format("%s --> Course ID: %d", courseName, sectionID));
                }

            } if (usertype == 0) {
                String query = "SELECT DISTINCT \"CourseName\", \"ClassID\" " +
                        "FROM \"StudentCourse\" " +
                        "JOIN \"Course\" ON \"Course\".\"SectionID\" = \"StudentCourse\".\"ClassID\" " +
                        "WHERE \"StudentCourse\".\"StudentID\" = " + ID;

                statement = conn.createStatement();
                result = statement.executeQuery(query);


                while (result.next()) {
                    String courseName = result.getString("CourseName");
                    System.out.println("the course name is " + courseName);
                    Integer sectionID = result.getInt("ClassID");
                    System.out.println("course is :" + courseName);
                    classesList.add(String.format("%s -->  Course ID: %d", courseName, sectionID));
                }
            }
            else {

                return classesList.toArray(new String[0]);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(classesList);
        return classesList.toArray(new String[0]);
    }

    public String getCourseDetails(int courseID) {
        String courseTime = "Unknown";
        String professorName = "Unknown";
        String queryCourseTime = "SELECT \"Time\" FROM \"Course\" WHERE \"SectionID\" = ?";
        String queryProfessorName =
                "SELECT p.\"ProfessorUsername\" " +
                        "FROM \"ProfessorCourse\" pc " +
                        "JOIN \"Professor\" p ON pc.\"ProfessorID\" = p.\"ProfessorID\" " +
                        "WHERE pc.\"CourseID\" = ?";

        try {
            try (PreparedStatement statement = conn.prepareStatement(queryCourseTime)) {
                statement.setInt(1, courseID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        courseTime = resultSet.getString("Time");
                    }
                }
            }

            try (PreparedStatement statement = conn.prepareStatement(queryProfessorName)) {
                statement.setInt(1, courseID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        professorName = resultSet.getString("ProfessorUsername");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving course details.";
        }

        System.out.println(professorName);
        System.out.println(courseTime);
        // Combine results
        // Combine results
        return "Professor: " + professorName + ", Time: " + courseTime;
    }


    public String[] viewStudents(Integer sectionID) {
        PreparedStatement statement;
        ResultSet studentList;
        List<String> studentsList = new ArrayList<>();

        String queryClassExists = "SELECT * FROM \"Course\" WHERE \"SectionID\" = ?";

        try (PreparedStatement s = conn.prepareStatement(queryClassExists)) {

            s.setInt(1, sectionID);

            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {

                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }

        String query = "SELECT DISTINCT s.\"StudentFirstName\", s.\"StudentLastName\" " +
                "FROM \"Student\" s " +
                "JOIN \"StudentCourse\" sc ON s.\"StudentID\" = sc.\"StudentID\" " +
                "WHERE sc.\"ClassID\" = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, sectionID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String studentFirstName = rs.getString("StudentFirstName");
                    String studentLastName = rs.getString("StudentLastName");


                    // Concatenate first and last name with a space
                    String fullName = studentFirstName + " " + studentLastName;

                    // Add the full name to the list
                    studentsList.add(fullName);

                    // Print the full name (optional)
                    System.out.println(fullName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // This will print the stack trace and show where the error occurred
            System.out.println("Before error printed");
            System.out.println(e);
        }

        // Return the list of full student names as an array
        return studentsList.toArray(new String[0]);
    }



    public String getTime(Integer sectionID) {
        Statement statement;
        ResultSet time;

        /* if class does not exist */
        String queryClassExists = "SELECT * FROM \"Course\" WHERE \"SectionID\" = ?";
        try (PreparedStatement s = conn.prepareStatement(queryClassExists)) {
            s.setInt(1, sectionID);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    return "Course ID does not exist!";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            String query = "SELECT DISTINCT \"Time\" " +
                    "FROM \"ProfessorCourse\" " +
                    "JOIN \"Course\" ON \"Course\".\"SectionID\" = \"ProfessorCourse\".\"CourseID\" " +
                    "WHERE \"ProfessorCourse\".\"CourseID\" = " + sectionID;

            statement = conn.createStatement();
            time = statement.executeQuery(query);

            while (time.next()) {
                String actualTime = time.getString("Time");
                return actualTime;
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return "There has been an error in fetching the time.";

    }
    /* get all collections */

    public String[] getAllClasses() {
        ResultSet resultSet = null;
        List<String> courses = new ArrayList<>();

        // Corrected query to join Course, Course_Professor, and Professor tables
        String query = "SELECT DISTINCT c.\"CourseName\", c.\"Time\", p.\"ProfessorUsername\" " +
                "FROM \"Course\" c " +
                "JOIN \"ProfessorCourse\" cp ON c.\"SectionID\" = cp.\"CourseID\" " +
                "JOIN \"Professor\" p ON cp.\"ProfessorID\" = p.\"ProfessorID\"";

        try (PreparedStatement s = conn.prepareStatement(query)) {
            resultSet = s.executeQuery();

            if (!resultSet.next()) {
                System.out.println("No results found!");
                return null;
            }

            // Iterate through the result set
            do {
                String courseName = resultSet.getString("CourseName");
                String time = resultSet.getString("Time");
                String professorName = resultSet.getString("ProfessorUsername");

                System.out.println(courseName);  // Debugging
                System.out.println(time);        // Debugging
                System.out.println(professorName);  // Debugging

                // Add formatted string with professor name included
                courses.add(String.format("Course name: %s, Time: %s, Professor: %s", courseName, time, professorName));
            } while (resultSet.next());
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
            return null;
        }

        // Return courses as array
        return courses.toArray(new String[0]);
    }




    public String[] getAllStudents() {
        ResultSet resultSet;
        List<String> studentList = new ArrayList<>();

        // Modified query to include the StudentID
        String query = "SELECT DISTINCT \"StudentID\", \"StudentFirstName\", \"StudentLastName\" FROM \"Student\"";

        try (PreparedStatement s = conn.prepareStatement(query)) {
            resultSet = s.executeQuery();
            while (resultSet.next()) {
                // Retrieve the student ID along with first and last name
                int studentID = resultSet.getInt("StudentID");
                String firstName = resultSet.getString("StudentFirstName");
                String lastName = resultSet.getString("StudentLastName");

                // Add the formatted string with Student ID, Name
                studentList.add(String.format("ID: %d - %s %s", studentID, firstName, lastName));
            }

        } catch (SQLException e) {
            return null;
        }
        return studentList.toArray(new String[0]);
    }



    public String[] getAllProfessors() {
        ResultSet resultSet;
        List<String> professorList = new ArrayList<>();

        String query = "SELECT DISTINCT \"ProfessorUsername\" FROM \"Professor\"";
        try (PreparedStatement s = conn.prepareStatement(query)) {
            resultSet = s.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("ProfessorUsername");
                professorList.add(String.format("%s", username));
            }
        } catch (SQLException e) {
            return null;
        }
        return professorList.toArray(new String[0]);
    }


    public String getProfessors(int courseID) {
        ResultSet result;
        String username = null;
        String query = "SELECT \"ProfessorUsername\" FROM \"ProfessorCourse\" JOIN \"Professor\"" +
                "ON \"ProfessorCourse\".\"ProfessorID\" = \"Professor\".\"ProfessorID\"" +
                "WHERE \"ProfessorCourse\".\"CourseID\" = " + courseID;

        try (PreparedStatement s = conn.prepareStatement(query)) {
            result = s.executeQuery();
            while (result.next()) {
                username = result.getString("ProfessorUsername");
            }

        } catch (SQLException e) {
            return null;
        }
        return username;
    }


    /* edit classes (PUT) */
        /* edit time */

    public int updateClassTime(String time, int sectionID) {
        String query = "UPDATE public.\"Course\" SET \"Time\" = ? WHERE \"SectionID\" = ?";
        int rowsAffected = 0;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, time);
            ps.setInt(2, sectionID);

            rowsAffected = ps.executeUpdate();
        } catch (SQLException e) {
            return ERROR;
        }

        if (rowsAffected > 0) {
            return SUCCESS;
        }
        return ERROR;

    }

    public int deleteCourseAdmin(int courseID) {
        int rowAffected = 0;
        System.out.println("course id to delete from tables is : " + courseID);

        // Query for deleting from ProfessorCourse and StudentCourse tables
        String queryProfCourseTable = "DELETE FROM public.\"ProfessorCourse\" WHERE \"CourseID\" = ?";
        String queryStudentCourseTable = "DELETE FROM public.\"StudentCourse\" WHERE \"ClassID\" = ?";
        String queryCourseTable = "DELETE FROM public.\"Course\" WHERE \"SectionID\" = ?";

        System.out.println("after queries");
        try (PreparedStatement ps2 = conn.prepareStatement(queryProfCourseTable);
             PreparedStatement ps3 = conn.prepareStatement(queryStudentCourseTable);
        PreparedStatement ps1 = conn.prepareStatement(queryCourseTable)) {

            // Set the courseID parameter for both queries
            ps2.setInt(1, courseID);
            ps3.setInt(1, courseID);
            ps1.setInt(1, courseID);

            // Execute the delete operations on both tables
            rowAffected += ps1.executeUpdate();
            rowAffected += ps2.executeUpdate();
            rowAffected += ps3.executeUpdate();

            // Check if any rows were affected by the delete operations
            if (rowAffected > 0) {
                System.out.println("Course related records have been deleted from ProfessorCourse and StudentCourse tables.");
                return SUCCESS; // Return success if any rows were deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting course records.");
            return ERROR; // Return error if something goes wrong
        }

        System.out.println("No records were deleted.");
        return ERROR; // Return error if no rows were affected
    }


    public int deleteCourse(int courseID, int usertype) {
        int rowAffected = 0;
        System.out.println("course id to delete from tables is : " + courseID);

        String queryProfCourseTable = "DELETE FROM public.\"ProfessorCourse\" WHERE \"CourseID\" = ?";
        String queryStudentCourseTable = "DELETE FROM public.\"StudentCourse\" WHERE \"ClassID\" = ?";
        String queryCourseTable = "DELETE FROM public.\"Course\" WHERE \"SectionID\" = ?";

        System.out.println("after queries");
        try (PreparedStatement ps2 = conn.prepareStatement(queryProfCourseTable);
             PreparedStatement ps3 = conn.prepareStatement(queryStudentCourseTable);
             PreparedStatement ps1 = conn.prepareStatement(queryCourseTable)) {


            ps3.setInt(1, courseID);
            if (usertype == 2) {
                ps2.setInt(1, courseID);
                ps1.setInt(1, courseID);
            }

            // Execute the delete operations on both tables

            rowAffected += ps3.executeUpdate();
            if (usertype == 2) {
                rowAffected += ps2.executeUpdate();
                rowAffected += ps1.executeUpdate();
            }


            // Check if any rows were affected by the delete operations
            if (rowAffected > 0) {
                System.out.println("Course related records have been deleted from ProfessorCourse and StudentCourse tables.");
                return SUCCESS; // Return success if any rows were deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting course records.");
            return ERROR; // Return error if something goes wrong
        }

        System.out.println("No records were deleted.");
        return ERROR; // Return error if no rows were affected
    }



    public String userInformation(int id, int userType) {
        ResultSet rs;
        String information = "";
        String query = "";

        if (userType == 1) { // Professor
            query = "SELECT \"ProfessorID\", \"ProfessorUsername\", \"ProfessorPassword\" FROM public.\"Professor\" WHERE \"ProfessorID\" = ?";
        } else if (userType == 0) { // Student
            query = "SELECT \"StudentID\", \"StudentFirstName\", \"StudentLastName\", \"StudentPassword\" FROM public.\"Student\" WHERE \"StudentID\" = ?";
        }
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                if (userType == 1) {

                    information = "Professor ID: " + rs.getInt("ProfessorID") +
                            ", Username: " + rs.getString("ProfessorUsername") +
                            ", Password: " + rs.getString("ProfessorPassword");
                } else {

                    information = "Student ID: " + rs.getInt("StudentID") +
                            ", First Name: " + rs.getString("StudentFirstName") +
                            ", Last Name: " + rs.getString("StudentLastName") +
                            ", Password: " + rs.getString("StudentPassword");
                }
            } else {
                information = "No user found with the given ID.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            information = "Error retrieving user information.";
        }

        return information;
    }



    //admin functions :







}


