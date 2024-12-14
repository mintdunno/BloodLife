package com.minh.bloodlife.model;

public class User {
    private String userId; // You might not need this if you are using Firebase Auth UID
    private String username; // You might not need this since you are using email for login
    private String password; // Not needed if using Firebase Auth (it handles passwords)
    private String email;
    private String userType;
    private String firstName;
    private String lastName;

    // Constructor
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    // Constructor with parameters
    public User(String username, String password, String email, String userType, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and setters for all fields

    // You might want to add @Exclude to methods you don't want to store in Firebase
    // For example, you wouldn't store the password in the database if using Firebase Auth

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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