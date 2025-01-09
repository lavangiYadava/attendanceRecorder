package com.example.springboot;

public class user {

    private Integer userType;
    private String email;
    private String password;
    private Integer ID;

    public user() {

    }
    public Integer getUserType() {
        return userType;
    }
    public Integer getID() {
        return ID;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }
}
