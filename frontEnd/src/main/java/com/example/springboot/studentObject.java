package com.example.springboot;
public class studentObject extends user{
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Integer ID;
    public studentObject() {
    }
    public studentObject(String firstname, String lastname, String email, String password, Integer ID) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.password = password;
        this.setUserType(1);
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
}

