package com.datn06.pickleconnect.API;

/**
 * Enum for named service hosts (base URLs). Add new entries here for extra ports/services.
 * Keep values pointing to the host:port (and optional context path) you need for emulator/device.
 */
public enum ServiceHost {
    // Member Command API - Authentication & Registration (port 9005)
    AUTH_SERVICE("http://10.0.2.2:9005/", "Auth Service (9005)"),
    
    // Court Service - Booking & Court Management (port 9008)
    COURT_SERVICE("http://10.0.2.2:9008/", "Court Service (9008)"),
    
    // Main API Service (port 9003) - if still needed
    API_SERVICE("http://10.0.2.2:9003/", "API Service (9003)"),
    
    // Transaction Service - Booking History (port 9009)
    TXN_SERVICE("http://10.0.2.2:9020/", "Transaction Service (9020)"),
    
    // Payment Service (port 9010) - if available
    PAYMENT_SERVICE("http://10.0.2.2:9010/", "Payment Service (9010)");

    private final String url;
    private final String label;

    ServiceHost(String url, String label) {
        this.url = url;
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public String getLabel() {
        return label;
    }
}
