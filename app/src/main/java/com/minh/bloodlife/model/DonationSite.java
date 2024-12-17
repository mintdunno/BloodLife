package com.minh.bloodlife.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class DonationSite {
    private String siteId;
    private String siteName;
    private String address;
    private GeoPoint location;
    private String donationStartTime;
    private String donationEndTime;
    private List<String> donationDays;
    private List<String> requiredBloodTypes;
    private String managerId;
    private String startDate;
    private String endDate;

    // Default constructor
    public DonationSite() {}

    // Constructor
    public DonationSite(String siteName, String address, GeoPoint location,
                        String donationStartTime, String donationEndTime, List<String> donationDays, // Update constructor
                        List<String> requiredBloodTypes, String managerId, String startDate, String endDate) {
        this.siteName = siteName;
        this.address = address;
        this.location = location;
        this.donationStartTime = donationStartTime;
        this.donationEndTime = donationEndTime;
        this.donationDays = donationDays;
        this.requiredBloodTypes = requiredBloodTypes;
        this.managerId = managerId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public String getDonationStartTime() {
        return donationStartTime;
    }

    public void setDonationStartTime(String donationStartTime) {
        this.donationStartTime = donationStartTime;
    }

    public String getDonationEndTime() {
        return donationEndTime;
    }

    public void setDonationEndTime(String donationEndTime) {
        this.donationEndTime = donationEndTime;
    }

    public List<String> getDonationDays() {
        return donationDays;
    }

    public void setDonationDays(List<String> donationDays) {
        this.donationDays = donationDays;
    }

    public List<String> getRequiredBloodTypes() {
        return requiredBloodTypes;
    }

    public void setRequiredBloodTypes(List<String> requiredBloodTypes) {
        this.requiredBloodTypes = requiredBloodTypes;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}