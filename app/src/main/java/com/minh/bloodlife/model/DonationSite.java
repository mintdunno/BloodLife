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
    private String contactPhone;
    private String contactEmail;
    private String description;

    // Default constructor
    public DonationSite() {}

    // Constructor

    public DonationSite(String siteId, String endDate, String startDate, List<String> requiredBloodTypes, String managerId, String donationEndTime, List<String> donationDays, String donationStartTime, GeoPoint location, String address, String siteName) {
        this.siteId = siteId;
        this.endDate = endDate;
        this.startDate = startDate;
        this.requiredBloodTypes = requiredBloodTypes;
        this.managerId = managerId;
        this.donationEndTime = donationEndTime;
        this.donationDays = donationDays;
        this.donationStartTime = donationStartTime;
        this.location = location;
        this.address = address;
        this.siteName = siteName;
    }

    public DonationSite(String siteName, String address, GeoPoint location,
                        String donationStartTime, String donationEndTime, List<String> donationDays,
                        List<String> requiredBloodTypes, String managerId, String startDate, String endDate,
                        String contactPhone, String contactEmail, String description) {
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
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.description = description;
    }

    // Getters
    public String getSiteId() {
        return siteId;
    }
    public String getSiteName() {
        return siteName;
    }

    public String getAddress() {
        return address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getDonationStartTime() {
        return donationStartTime;
    }

    public String getDonationEndTime() {
        return donationEndTime;
    }

    public List<String> getDonationDays() {
        return donationDays;
    }

    public List<String> getRequiredBloodTypes() {
        return requiredBloodTypes;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public void setDonationStartTime(String donationStartTime) {
        this.donationStartTime = donationStartTime;
    }

    public void setDonationEndTime(String donationEndTime) {
        this.donationEndTime = donationEndTime;
    }

    public void setDonationDays(List<String> donationDays) {
        this.donationDays = donationDays;
    }

    public void setRequiredBloodTypes(List<String> requiredBloodTypes) {
        this.requiredBloodTypes = requiredBloodTypes;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}