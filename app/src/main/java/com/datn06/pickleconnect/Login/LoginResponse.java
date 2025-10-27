package com.datn06.pickleconnect.Login;

public class LoginResponse {
    private String code;
    private String message;
    private DataLogin data;

    public static class DataLogin {
        private String token;
        private String refreshToken;
        private Long accountId;
        private String userName;
        private String fullName;
        private String email;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public DataLogin getData() { return data; }
    public void setData(DataLogin data) { this.data = data; }
}
