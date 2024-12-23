package com.minh.bloodlife.model;

import java.util.Date;
import java.util.Map;

public class DonationDriveOutcome {
    private String siteId;
    private Date date;
    private String totalCollected;
    private Map<String, String> bloodTypeBreakdown;

    // Default constructor (required for Firestore)
    public DonationDriveOutcome() {
    }

    // Constructor
    public DonationDriveOutcome(String siteId, Date date, String totalCollected, Map<String, String> bloodTypeBreakdown) {
        this.siteId = siteId;
        this.date = date;
        this.totalCollected = totalCollected;
        this.bloodTypeBreakdown = bloodTypeBreakdown;
    }

    // Getters
    public String getSiteId() {
        return siteId;
    }

    public Date getDate() {
        return date;
    }

    public String getTotalCollected() {
        return totalCollected;
    }

    public Map<String, String> getBloodTypeBreakdown() {
        return bloodTypeBreakdown;
    }

    // Setters
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setTotalCollected(String totalCollected) {
        this.totalCollected = totalCollected;
    }

    public void setBloodTypeBreakdown(Map<String, String> bloodTypeBreakdown) {
        this.bloodTypeBreakdown = bloodTypeBreakdown;
    }
}