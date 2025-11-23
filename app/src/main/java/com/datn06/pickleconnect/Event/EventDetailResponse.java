package com.datn06.pickleconnect.Event;

import com.datn06.pickleconnect.Model.EventDetailDTO;
import com.google.gson.annotations.SerializedName;

public class EventDetailResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private EventDetailDTO data;

    @SerializedName("requestId")
    private String requestId;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public EventDetailDTO getData() { return data; }
    public void setData(EventDetailDTO data) { this.data = data; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public boolean isSuccess() {
        return "00".equals(code);
    }
}