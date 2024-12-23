# BloodLife - Android Application

## Student Details

- **Student ID:** sadasdas
- **Student Name:** ASDSDADSAD

---

## Project Overview

BloodLife is a location-based Android application designed to connect blood donors with donation sites. It simplifies the organization and management of blood donation drives, allowing donors to locate nearby sites and register for donation events effortlessly. The app is developed using pure Java and leverages Firebase for authentication, database storage, and backend services.

---

## Functionality

### For All Users:

#### **User Registration and Login**

- Register using email and password.
- Log in to existing accounts.
- Basic input validation (e.g., email format, password length).

#### **View Donation Sites on Map**

- Map view with custom markers for nearby donation sites.
- Clickable markers displaying detailed site information:
  - Site name
  - Address
  - Donation hours
  - Required blood types
  - Operating days
  - Contact details (phone/email)
  - Status (Active/Inactive)
  - Description
- Integration with Google Maps for navigation.

#### **Search and Filter Donation Sites**

- Search by site name or address.
- Filters available:
  - Date range
  - Required blood types
  - "Near Me" (uses current location with permissions).

#### **View and Manage Profile**

- **Profile features:**
  - View/update personal information (name, email, phone, blood type).
  - Change profile picture (upload or take a photo).
  - Update password.
  - Enable/disable notifications.

### For Site Managers:

Includes all functionalities available to all users, plus:

#### **Create New Donation Sites**

- Input details:
  - Site name
  - Address (Google Places Autocomplete)
  - Latitude and longitude (auto-populated)
  - Donation hours and operating days
  - Required blood types
  - Contact details
  - Description
  - Status (default: "Active")
- Basic input validation ensures data integrity.

#### **Manage Existing Sites**

- View, edit, and delete sites created by the manager.
- View a list of registered volunteers.

#### **Post-Donation Data Input**

- Input donation outcomes:
  - Total blood volume collected.
  - Breakdown of blood types and volumes (e.g., A+: 500ml).

#### **Download Donor List**

- Export donor lists for specific donation sites in CSV format.

### For Super Users:

Includes all functionalities available to site managers, plus:

#### **Generate Reports**

- Generate detailed donation reports:
  - Total donors
  - Total blood volume collected
  - Blood type breakdown
  - Filter by date range
- Reports displayed in tabular format.

---

## Technology Stack

### Programming Language

- Java (pure Java, no external frameworks).

### IDE

- Android Studio (latest version).

### Firebase Services

- **Firebase Authentication:** User registration and login management.
- **Cloud Firestore:** NoSQL database for:
  - User data (`users` collection)
  - Donation site data (`donationSites` collection)
  - Registration data (`registrations` collection)
  - Post-donation data (`DonationDriveOutcomes` collection)
- **Firebase Cloud Messaging (FCM):** (Planned) Push notifications for updates.
- **Firebase Cloud Functions:** (Partially implemented) Manage Super User claims and trigger notifications.

### Other Technologies

- **Google Maps SDK:** Displays map view and donation site markers.
- **Google Places API:** Address autocompletion for site creation.
- **Android Location Services:** "Near Me" functionality.
- **Storage Access Framework (SAF):** Save CSV files.
- **Bitmap/Base64:** Handle profile pictures.
- **AtomicInteger:** Thread-safe counter updates.

---

## Drawbacks

- **No Offline Support:** Requires constant internet connection.
- **Limited Error Handling:** Basic user feedback mechanisms.
- **Basic UI:** Functional but lacks visual polish.
- **No Charting:** Reports are displayed as text.
- **Manual Super User Management:** Requires manual `userType` configuration in Firestore.
- **Limited Location Filtering:** Only "Near Me" filtering implemented.
- **No Notifications:** FCM push notifications are planned but not implemented.

---

## Future Improvements

1. **Push Notifications:** Notify users of updates and new sites.
2. **Enhanced Reports:**
   - Add location-based filtering.
   - Introduce charts for better visualization.
   - Enable CSV/PDF export options.
3. **UI/UX Refinements:** Improve design, add animations, and enhance user experience.
4. **Offline Support:** Implement local caching with SQLite or Room.
5. **Robust Error Handling:** Improve user feedback for errors.
6. **Automated Super User Role Assignment:** Use Cloud Functions for seamless role management.
7. **Security Rules:** Strengthen Firestore security rules.
8. **Code Refactoring:** Enhance code readability and maintainability.

---

This README provides a structured overview of BloodLife, detailing its features, technology stack, limitations, and future improvements. Let me know if you need further assistance or additional details!
