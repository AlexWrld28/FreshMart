package com.grocery.dto;

public class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email != null ? email.toLowerCase().trim() : null; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
