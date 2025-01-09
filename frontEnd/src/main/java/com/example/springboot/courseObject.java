package com.example.springboot;
public class courseObject {
    private String courseName;
    private String courseDescription;
    private String time;
    private Integer ID;
    private Integer profID;


    public courseObject(String courseDescription, String  courseName, String time, Integer ID, Integer profID) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.profID = profID;
        this.time = time;
        this.ID = ID;
    }
    public courseObject() {
    }
    public String getCourseName() {
        return this.courseName;
    }
    public String getCourseDescription() {
        return this.courseDescription;
    }
    public String getTime() {
        return this.time;
    }

    public Integer getProfID() {
        return profID;
    }
    public Integer getCourseID() {
        return this.ID;
    }

}