package com.datn06.pickleconnect.Login;

public class LoginRequest {
    private String loginIdentifier;
    private String password;

    public LoginRequest(String loginIdentifier, String password) {
        this.loginIdentifier = loginIdentifier;
        this.password = password;
    }

    public String getLoginIdentifier() {
        return loginIdentifier;
    }

    public void setLoginIdentifier(String loginIdentifier) {
        this.loginIdentifier = loginIdentifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}