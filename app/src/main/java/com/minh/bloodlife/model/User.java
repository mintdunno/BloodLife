package com.minh.bloodlife.model;

public class User {
    private String uid;
    private String email;
    private String userType;
    private String firstName;
    private String lastName;
    private String bloodType;
    private String phoneNumber;
    private String profilePictureUrl;
    private boolean notificationEnabled;
    private boolean pushNotificationsEnabled;

    // Default constructor (required for Firestore)
    public User() {}

    // Constructor
    public User(String uid, String email, String userType, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.notificationEnabled = true;
        this.pushNotificationsEnabled = true;
    }

    public User(String uid, String email, String userType, String firstName, String lastName, String bloodType, String phoneNumber, String profilePictureUrl, boolean notificationEnabled, boolean pushNotificationsEnabled) {
        this.uid = uid;
        this.email = email;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bloodType = bloodType;
        this.phoneNumber = phoneNumber;
        this.profilePictureUrl = profilePictureUrl;
        this.notificationEnabled = notificationEnabled;
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }

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

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public boolean isPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }

    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }
}