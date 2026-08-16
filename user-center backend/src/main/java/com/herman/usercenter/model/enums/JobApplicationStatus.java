package com.herman.usercenter.model.enums;

/**
 * Supported stages in the application pipeline.
 */
public enum JobApplicationStatus {
    SAVED,
    APPLIED,
    ASSESSMENT,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (JobApplicationStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
