package com.minh.bloodlife.model;

import java.util.Date;

public class Registration {
    private String registrationId;
    private String userId;
    private String siteId;
    private Date registrationDate;
    private boolean isVolunteer; // true if volunteering, false if donating
    private int numDonors; // Number of people donating (if applicable)

    // Default constructor (required for Firestore)
    public Registration() {
    }

    // Constructor
    public Registration(String userId, String siteId, Date registrationDate, boolean isVolunteer, int numDonors) {
        this.userId = userId;
        this.siteId = siteId;
        this.registrationDate = registrationDate;
        this.isVolunteer = isVolunteer;
        this.numDonors = numDonors;
    }

    // Getters and setters for all fields...
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isVolunteer() {
        return isVolunteer;
    }

    public void setVolunteer(boolean volunteer) {
        isVolunteer = volunteer;
    }

    public int getNumDonors() {
        return numDonors;
    }

    public void setNumDonors(int numDonors) {
        this.numDonors = numDonors;
    }
}