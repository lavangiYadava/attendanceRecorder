package com.example.springboot;
public class professorObject extends user{

    private String email;
    private String username;
    private String password;
    public professorObject() {
    }
    public professorObject(String usernameGiven, String passwordGiven, String emailGiven, Integer ID) {
        this.username = usernameGiven;
        this.password = passwordGiven;
        this.email = emailGiven;
        this.setUserType(0);
        this.setID(ID);
    }

    public String getUsername() {
        return username;
    }


}
