package com.datn06.pickleconnect.Common;

import com.google.gson.annotations.SerializedName;

/**
 * Base response wrapper for all API responses
 * Matches backend's BaseResponse structure exactly
 * 
 * Backend fields:
 * - requestId: ID của request gửi lên
 * - responseId: ID của response trả về
 * - responseTime: Thời gian response (format: yyyyMMddHHmmss)
 * - code: Mã kết quả ("00" = success)
 * - message: Thông báo kết quả
 * - data: Dữ liệu trả về (generic type T)
 */
public class BaseResponse<T> {
    
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("responseId")
    private String responseId;
    
    @SerializedName("responseTime")
    private String responseTime;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private T data;
    
    // Constructors
    public BaseResponse() {}
    
    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getResponseId() {
        return responseId;
    }
    
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
    
    public String getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
    
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
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    // Helper methods
    
    /**
     * Check if response is successful
     * Backend uses "00" for success
     */
    public boolean isSuccess() {
        return "00".equals(code);
    }
    
    /**
     * Check if response has data
     */
    public boolean hasData() {
        return data != null;
    }
    
    /**
     * Get error message or default message
     */
    public String getErrorMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        
        // Map common error codes
        if ("01".equals(code)) {
            return "Slot sân không khả dụng";
        } else if ("99".equals(code)) {
            return "Hệ thống xảy ra lỗi";
        } else if ("400".equals(code)) {
            return "Dữ liệu không hợp lệ";
        } else if ("401".equals(code)) {
            return "Chưa đăng nhập";
        } else if ("403".equals(code)) {
            return "Không có quyền truy cập";
        } else if ("404".equals(code)) {
            return "Không tìm thấy dữ liệu";
        } else if ("500".equals(code)) {
            return "Lỗi server";
        }
        
        return "Có lỗi xảy ra";
    }
    
    @Override
    public String toString() {
        return "BaseResponse{" +
                "requestId='" + requestId + '\'' +
                ", responseId='" + responseId + '\'' +
                ", responseTime='" + responseTime + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", hasData=" + hasData() +
                '}';
    }
}
