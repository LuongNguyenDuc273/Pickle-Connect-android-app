package com.datn06.pickleconnect.Booking;

import com.datn06.pickleconnect.Model.BookingHistoryDTO;
import java.util.List;

public class BookingHistoryResponse {
    private String code;
    private String message;
    private List<BookingHistoryDTO> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BookingHistoryDTO> getData() {
        return data;
    }

    public void setData(List<BookingHistoryDTO> data) {
        this.data = data;
    }
}
