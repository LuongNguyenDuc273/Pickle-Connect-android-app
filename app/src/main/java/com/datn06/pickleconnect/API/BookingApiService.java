package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Booking.BookingHistoryRequest;
import com.datn06.pickleconnect.Model.BookingHistoryDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Booking Transaction API Service
 * Base URL: http://10.0.2.2:9009/ (pickle-connect-txn)
 * Endpoints under /transaction path
 */
public interface BookingApiService {

    /**
     * Get booking history for a user
     * 
     * @param request Request containing userId, status, fromDate, toDate
     * @return List of booking history
     */
    @POST("transaction/history")
    Call<List<BookingHistoryDTO>> getBookingHistory(
        @Body BookingHistoryRequest request
    );

    /**
     * Get booking detail by booking ID
     * 
     * @param bookingId Booking ID
     * @return Detailed booking information
     */
    @GET("transaction/detail/{bookingId}")
    Call<BookingHistoryDTO> getBookingDetail(
        @Path("bookingId") Long bookingId
    );
}
