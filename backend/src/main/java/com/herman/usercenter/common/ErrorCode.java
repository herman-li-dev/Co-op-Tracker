package com.herman.usercenter.common;

/**
 * Application error codes.
 */
public enum ErrorCode {

    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "Invalid request parameters", ""),
    NULL_ERROR(40001, "Requested data was not found", ""),
    NOT_LOGIN(40100, "Not signed in", ""),
    NO_AUTH(40101, "Access denied", ""),
    SYSTEM_ERROR(50000, "Internal server error", "");

    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}

