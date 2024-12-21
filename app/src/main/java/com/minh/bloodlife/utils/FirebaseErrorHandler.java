// FirebaseErrorHandler.java
package com.minh.bloodlife.utils;

import com.google.firebase.auth.FirebaseAuthException;

public class FirebaseErrorHandler {

    public static String getAuthErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "Invalid email format.";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect password.";
                case "ERROR_USER_NOT_FOUND":
                    return "No account found with this email.";
                case "ERROR_USER_DISABLED":
                    return "This account has been disabled.";
                case "ERROR_TOO_MANY_REQUESTS":
                    return "Too many login attempts. Please try again later.";
                default:
                    return "Authentication error: " + errorCode;
            }
        }
        return "An unexpected error occurred.";
    }
}
