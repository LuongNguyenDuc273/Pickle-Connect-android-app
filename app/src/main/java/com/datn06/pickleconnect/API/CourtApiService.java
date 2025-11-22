package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Common.BaseRequest;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.CreateBookingCourtRequest;
import com.datn06.pickleconnect.Model.FacilitySearchResponse;
import com.datn06.pickleconnect.Model.FieldBookingResponse;
import com.datn06.pickleconnect.Model.PaymentUrlResponse;
import com.datn06.pickleconnect.Model.SearchCourtRequest;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Court API Service - Handles court booking and field management
 * Base URL: http://10.0.2.2:9008/ (pickle-connect-court)
 * Endpoints under /api/v1 path
 */
public interface CourtApiService {

    /**
     * Get field availability for a facility on a specific date
     * 
     * ✅ UPDATED: Removed @Header("X-Userinfo") - ApiClient interceptor auto-adds this header
     * 
     * @param userId User ID (query parameter)
     * @param facilityId ID of the facility to query
     * @param bookingDate Date to check availability (format: yyyy-MM-dd, e.g., "2025-04-22")
     * @return Response containing all fields and their available time slots
     */
    @GET("api/v1/booking/fields/availability")
    Call<BaseResponse<FieldBookingResponse>> getFieldAvailability(
        @Query("userId") String userId,
        @Query("facilityId") Long facilityId,
        @Query("bookingDate") String bookingDate
    );

    /**
     * Create a new court booking
     * 
     * ✅ UPDATED: Removed @Header("X-Userinfo") - ApiClient interceptor auto-adds this header
     * 
     * @param request Booking details including selected slots, user info, payment method
     * @return Response containing payment URL for VNPay
     */
    @POST("api/v1/booking/create")
    Call<BaseResponse<PaymentUrlResponse>> createBooking(
        @Body CreateBookingCourtRequest request
    );

    /**
     * Save facility to user's favorites
     * 
     * ✅ UPDATED: Removed @Header("X-Userinfo") - ApiClient interceptor auto-adds this header
     * 
     * @param request Request containing userId and facilityId
     * @return Success response
     */
    @POST("api/v1/booking/save-facility-user")
    Call<BaseResponse<String>> saveFacilityUser(
        @Body SaveFacilityUserRequest request
    );

    /**
     * Search courts with filters
     * 
     * @param request Search parameters including location, distance, pagination
     * @return Response containing list of facilities and pagination info
     */
    @POST("court/search")
    Call<FacilitySearchResponse> searchCourts(
        @Body SearchCourtRequest request
    );

    /**
     * Get all active cities/provinces
     * 
     * @return List of all cities with status=1
     */
    @GET("court/cities")
    Call<java.util.List<com.datn06.pickleconnect.Model.CityDTO>> getCities();

    /**
     * Get districts by city ID
     * 
     * @param cityId ID of the city/province
     * @return List of districts belonging to the city
     */
    @GET("court/districts")
    Call<java.util.List<com.datn06.pickleconnect.Model.DistrictDTO>> getDistricts(
        @Query("cityId") String cityId
    );

    /**
     * Get user's saved facilities
     * 
     * @param request Request containing userId, page, size, sortOrder
     * @return Response containing list of saved facilities
     */
    @POST("court/get-saved-facility")
    Call<BaseResponse<FacilitySearchResponse>> getSavedFacilities(
        @Body com.datn06.pickleconnect.Model.GetSavedCourtsRequest request
    );

    /**
     * Request DTO for saving facility to favorites
     */
    class SaveFacilityUserRequest extends BaseRequest {
        private Long userId;
        private Long facilityId;

        public SaveFacilityUserRequest(Long userId, Long facilityId) {
            this.userId = userId;
            this.facilityId = facilityId;
            // BaseRequest auto-generates requestId
            setRequestId(String.valueOf(System.currentTimeMillis()));
        }
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public Long getFacilityId() {
            return facilityId;
        }
        
        public void setFacilityId(Long facilityId) {
            this.facilityId = facilityId;
        }
        
    }

    // Future endpoints to be implemented:
    
    /**
     * Get booking history for a user
     * 
     * @param userId User ID
     * @param status Booking status filter ("SUCCESS", "INIT", "FAILED", etc.)
     * @param fromDate Start date (yyyy-MM-dd)
     * @param toDate End date (yyyy-MM-dd)
     * @return List of booking history
     */
    // @GET("api/v1/booking/history")
    // Call<BaseResponse<List<BookingHistoryDTO>>> getBookingHistory(
    //     @Header("X-Userinfo") String userId,
    //     @Query("status") String status,
    //     @Query("fromDate") String fromDate,
    //     @Query("toDate") String toDate
    // );

    /**
     * Get detailed booking information by ID
     * 
     * @param userId User ID
     * @param bookingId Booking ID
     * @return Detailed booking information
     */
    // @GET("api/v1/booking/{bookingId}")
    // Call<BaseResponse<BookingHistoryDTO>> getBookingById(
    //     @Header("X-Userinfo") String userId,
    //     @Path("bookingId") Long bookingId
    // );

    /**
     * Cancel a booking
     * 
     * @param userId User ID
     * @param bookingId Booking ID to cancel
     * @return Success response
     */
    // @POST("api/v1/booking/{bookingId}/cancel")
    // Call<BaseResponse<String>> cancelBooking(
    //     @Header("X-Userinfo") String userId,
    //     @Path("bookingId") Long bookingId
    // );

    /**
     * Get user's saved facilities
     * 
     * @param userId User ID
     * @return List of saved facilities
     */
    // @GET("api/v1/booking/saved-facilities")
    // Call<BaseResponse<List<FacilityDTO>>> getSavedFacilities(
    //     @Header("X-Userinfo") String userId
    // );
}
