package com.idve.backend.dto;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String verificationStatus;
    private String documentPath;

    public UserResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = "USER";
        this.verificationStatus = "PENDING";
        this.documentPath = null;
    }

    public UserResponse(Long id, String name, String email, String role, String verificationStatus) {
        this(id, name, email, role, verificationStatus, null);
    }

    public UserResponse(Long id, String name, String email, String role, String verificationStatus, String documentPath) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.verificationStatus = verificationStatus;
        this.documentPath = documentPath;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public String getDocumentPath() {
        return documentPath;
    }
}
