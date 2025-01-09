package com.example.springboot;
public class adminObject extends user {

    private String email;
    private String username;
    private String password;
    public adminObject() {
    }
    public adminObject(String usernameGiven, String passwordGiven, String emailGiven, Integer ID) {
        this.username = usernameGiven;
        this.password = passwordGiven;
        this.email = emailGiven;
        this.setUserType(2);
        this.setID(ID);
    }

    public String getUsername() {
        return username;
    }


}
