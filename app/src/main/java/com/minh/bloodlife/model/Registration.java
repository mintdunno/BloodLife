package com.minh.bloodlife.model;

import java.util.Date;

public class Registration {
    private int registrationId;
    private int userId; // Foreign key referencing User.userId
    private int siteId; // Foreign key referencing DonationSite.siteId
    private Date registrationDate;
    private boolean isVolunteer;
    private int numDonors;

    // Constructor
    public Registration() {
        // Default constructor
    }

    // Constructor with parameters
    public Registration(int userId, int siteId, Date registrationDate, boolean isVolunteer, int numDonors) {
        this.userId = userId;
        this.siteId = siteId;
        this.registrationDate = registrationDate;
        this.isVolunteer = isVolunteer;
        this.numDonors = numDonors;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
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