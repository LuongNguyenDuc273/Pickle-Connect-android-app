package com.datn06.pickleconnect.Model;

import com.datn06.pickleconnect.Common.BaseRequest;

/**
 * Request DTO for getting user's saved courts
 * Matches backend: com.vnpay.bean.court.GetSavedCourtsRequest
 */
public class GetSavedCourtsRequest extends BaseRequest {
    private Long userId;
    private Integer page;
    private Integer size;
    private String sortOrder; // "DESC" = newest first, "ASC" = oldest first

    public GetSavedCourtsRequest() {
        super();
        this.page = 0;
        this.size = 20;
        this.sortOrder = "DESC"; // Default: newest first
    }

    public GetSavedCourtsRequest(Long userId, Integer page, Integer size, String sortOrder) {
        super();
        this.userId = userId;
        this.page = page;
        this.size = size;
        this.sortOrder = sortOrder;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
