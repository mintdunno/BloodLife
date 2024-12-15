package com.minh.bloodlife.model;

public class User {
    private String uid; // Firebase Authentication UID
    private String email; // Used for login
    private String userType;
    private String firstName;
    private String lastName;

    // Default constructor (required for Firestore)
    public User() {}

    // Constructor
    public User(String uid, String email, String userType, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}