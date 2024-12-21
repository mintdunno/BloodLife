package com.minh.bloodlife.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Registration {
    private String userId;
    private String siteId;
    private Date registrationDate;
    private boolean isVolunteer;
    private int numDonors;
    private List<Map<String, Object>> registrants; // Update: List of Maps

    // Constructor, getters, and setters

    public Registration() {
        // Default constructor required for Firestore
    }

    public Registration(String userId, String siteId, Date registrationDate, boolean isVolunteer, int numDonors, List<Map<String, Object>> registrants) {
        this.userId = userId;
        this.siteId = siteId;
        this.registrationDate = registrationDate;
        this.isVolunteer = isVolunteer;
        this.numDonors = numDonors;
        this.registrants = registrants;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getSiteId() {
        return siteId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public boolean isVolunteer() {
        return isVolunteer;
    }

    public int getNumDonors() {
        return numDonors;
    }

    public List<Map<String, Object>> getRegistrants() {
        return registrants;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setVolunteer(boolean volunteer) {
        isVolunteer = volunteer;
    }

    public void setNumDonors(int numDonors) {
        this.numDonors = numDonors;
    }

    public void setRegistrants(List<Map<String, Object>> registrants) {
        this.registrants = registrants;
    }
}