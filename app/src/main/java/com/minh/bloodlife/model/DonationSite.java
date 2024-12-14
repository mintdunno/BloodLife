package com.minh.bloodlife.model;
public class DonationSite {
    private int siteId;
    private String siteName;
    private String address;
    private double latitude;
    private double longitude;
    private String donationHours;
    private String requiredBloodTypes;
    private int managerId; // Foreign key referencing User.userId
    private int bloodCollected;
    private String bloodTypeCollected;

    // Constructor
    public DonationSite() {
        // Default constructor
    }

    // Constructor with parameters
    public DonationSite(String siteName, String address, double latitude, double longitude,
                        String donationHours, String requiredBloodTypes, int managerId, int bloodCollected, String bloodTypeCollected) {
        this.siteName = siteName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.donationHours = donationHours;
        this.requiredBloodTypes = requiredBloodTypes;
        this.managerId = managerId;
        this.bloodCollected = bloodCollected;
        this.bloodTypeCollected = bloodTypeCollected;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getBloodCollected() {
        return bloodCollected;
    }

    public void setBloodCollected(int bloodCollected) {
        this.bloodCollected = bloodCollected;
    }

    public String getBloodTypeCollected() {
        return bloodTypeCollected;
    }

    public void setBloodTypeCollected(String bloodTypeCollected) {
        this.bloodTypeCollected = bloodTypeCollected;
    }
}
