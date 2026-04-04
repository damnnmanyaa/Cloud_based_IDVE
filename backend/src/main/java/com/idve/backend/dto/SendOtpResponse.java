package com.idve.backend.dto;

public class SendOtpResponse {
    private String message;
    private String otp;

    public SendOtpResponse(String message, String otp) {
        this.message = message;
        this.otp = otp;
    }

    public String getMessage() {
        return message;
    }

    public String getOtp() {
        return otp;
    }
}
