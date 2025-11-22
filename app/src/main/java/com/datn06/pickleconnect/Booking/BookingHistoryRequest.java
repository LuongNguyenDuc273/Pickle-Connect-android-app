package com.datn06.pickleconnect.Booking;

import com.datn06.pickleconnect.Common.BaseRequest;

public class BookingHistoryRequest extends BaseRequest {
    private Long userId;
    private String status;
    private String fromDate; // Format: "yyyy-MM-dd"
    private String toDate;   // Format: "yyyy-MM-dd"

    public BookingHistoryRequest() {
        setRequestId(String.valueOf(System.currentTimeMillis()));
    }

    public BookingHistoryRequest(Long userId, String status, String fromDate, String toDate) {
        this.userId = userId;
        this.status = status;
        this.fromDate = fromDate;
        this.toDate = toDate;
        setRequestId(String.valueOf(System.currentTimeMillis()));
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
