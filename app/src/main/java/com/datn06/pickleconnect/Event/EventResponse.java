package com.datn06.pickleconnect.Event;

import com.datn06.pickleconnect.Model.EventListDTO;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<EventListDTO> data;

    @SerializedName("requestId")
    private String requestId;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<EventListDTO> getData() { return data; }
    public void setData(List<EventListDTO> data) { this.data = data; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public boolean isSuccess() {
        return "00".equals(code);
    }
}
