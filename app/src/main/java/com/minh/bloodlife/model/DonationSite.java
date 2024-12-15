package com.minh.bloodlife.model;

import com.google.firebase.firestore.GeoPoint;

public class DonationSite {
    private String siteId;
    private String siteName;
    private String address;
    private GeoPoint location; // Use GeoPoint for storing latitude and longitude
    private String donationHours;
    private String requiredBloodTypes;
    private String managerId;

    // Default constructor
    public DonationSite() {}

    // Constructor with parameters
    public DonationSite(String siteId, String siteName, String address, GeoPoint location, String donationHours, String requiredBloodTypes, String managerId) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.address = address;
        this.location = location;
        this.donationHours = donationHours;
        this.requiredBloodTypes = requiredBloodTypes;
        this.managerId = managerId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getDonationHours() {
        return donationHours;
    }

    public void setDonationHours(String donationHours) {
        this.donationHours = donationHours;
    }

    public String getRequiredBloodTypes() {
        return requiredBloodTypes;
    }

    public void setRequiredBloodTypes(String requiredBloodTypes) {
        this.requiredBloodTypes = requiredBloodTypes;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}