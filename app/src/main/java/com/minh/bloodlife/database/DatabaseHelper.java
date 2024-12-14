package com.minh.bloodlife.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "blood_donation.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_DONATION_SITES = "DonationSites";
    public static final String TABLE_REGISTRATIONS = "Registrations";

    // Common column names
    public static final String KEY_ID = "id";

    // Users Table Columns
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USER_TYPE = "userType";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";

    // Donation Sites Table Columns
    public static final String KEY_SITE_ID = "siteId";
    public static final String KEY_SITE_NAME = "siteName";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_DONATION_HOURS = "donationHours";
    public static final String KEY_REQUIRED_BLOOD_TYPES = "requiredBloodTypes";
    public static final String KEY_MANAGER_ID = "managerId";
    public static final String KEY_BLOOD_COLLECTED = "bloodCollected"; //in mililiters
    public static final String KEY_BLOOD_TYPE_COLLECTED = "bloodTypeCollected"; //like A+, B-, O

    // Registrations Table Columns
    public static final String KEY_REGISTRATION_ID = "registrationId";
    public static final String KEY_IS_VOLUNTEER = "isVolunteer";
    public static final String KEY_NUM_DONORS = "numDonors";
    public static final String KEY_REGISTRATION_DATE = "registrationDate";

    // Table Create Statements
    // Users table create statement
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USERNAME + " TEXT,"
            + KEY_PASSWORD + " TEXT,"
            + KEY_EMAIL + " TEXT UNIQUE,"
            + KEY_USER_TYPE + " TEXT,"
            + KEY_FIRST_NAME + " TEXT,"
            + KEY_LAST_NAME + " TEXT"
            + ")";

    // Donation sites table create statement
    private static final String CREATE_TABLE_DONATION_SITES = "CREATE TABLE " + TABLE_DONATION_SITES + "("
            + KEY_SITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_SITE_NAME + " TEXT,"
            + KEY_ADDRESS + " TEXT,"
            + KEY_LATITUDE + " REAL,"
            + KEY_LONGITUDE + " REAL,"
            + KEY_DONATION_HOURS + " TEXT,"
            + KEY_REQUIRED_BLOOD_TYPES + " TEXT,"
            + KEY_MANAGER_ID + " INTEGER,"
            + KEY_BLOOD_COLLECTED + " INTEGER,"
            + KEY_BLOOD_TYPE_COLLECTED + " TEXT,"
            + "FOREIGN KEY(" + KEY_MANAGER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")"
            + ")";

    // Registrations table create statement
    private static final String CREATE_TABLE_REGISTRATIONS = "CREATE TABLE " + TABLE_REGISTRATIONS + "("
            + KEY_REGISTRATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_SITE_ID + " INTEGER,"
            + KEY_REGISTRATION_DATE + " DATE,"
            + KEY_IS_VOLUNTEER + " INTEGER,"
            + KEY_NUM_DONORS + " INTEGER,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + "),"
            + "FOREIGN KEY(" + KEY_SITE_ID + ") REFERENCES " + TABLE_DONATION_SITES + "(" + KEY_SITE_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_DONATION_SITES);
        db.execSQL(CREATE_TABLE_REGISTRATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONATION_SITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);

        // create new tables
        onCreate(db);
    }
}