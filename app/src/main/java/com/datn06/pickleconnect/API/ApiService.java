package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Event.EventDetailResponse;
import com.datn06.pickleconnect.Event.EventResponse;
import com.datn06.pickleconnect.Home.HomeResponse;
import com.datn06.pickleconnect.Login.LoginRequest;
import com.datn06.pickleconnect.Login.LoginResponse;
import com.datn06.pickleconnect.Model.EventRegistrationRequest;
import com.datn06.pickleconnect.Model.EventRegistrationResponse;
import com.datn06.pickleconnect.Register.RegisterRequest;
import com.datn06.pickleconnect.Register.RegisterResponse;
import com.datn06.pickleconnect.Search.SearchResponse;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Legacy API Service - For backward compatibility
 *
 * ⚠️ DEPRECATED: Consider using domain-specific services instead:
 * - AuthApiService for auth/register/login (ServiceHost.AUTH_SERVICE)
 * - CourtApiService for court/booking (ServiceHost.COURT_SERVICE)
 * - etc.
 *
 * See: SERVICE_HOST_USAGE.txt for examples
 */
public interface ApiService {

    // =========================================================================
    //                            AUTHENTICATION APIs
    // =========================================================================

    // ⚠️ DEPRECATED: Use AuthApiService.login() instead
    // Login
    @POST("api-andr/account/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ⚠️ DEPRECATED: Use AuthApiService.register() instead
    // Register
    @POST("api-andr/account/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);


    // =========================================================================
    //                            HOME & SEARCH APIs
    // =========================================================================

    // Home - Still valid for main API service
    @GET("api-andr/home/data")
    Call<HomeResponse> getHomePageData(
            @Query("userLat") Double userLat,
            @Query("userLng") Double userLng
    );

    @GET("api-andr/home/facilities/search")
    Call<SearchResponse> searchFacilities(
            @Query("keyword") String keyword,
            @Query("userLat") Double userLat,
            @Query("userLng") Double userLng,
            @Query("maxDistanceKm") Double maxDistanceKm,
            @Query("limit") Integer limit
    );

    // =========================================================================
    //                            EVENT APIs
    // =========================================================================

    // Get Event List
    @GET("api-andr/events/list")
    Call<EventResponse> getEventList(
            @Query("facilityId") String facilityId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // Get Event Detail
    @GET("api-andr/events/{eventId}")
    Call<EventDetailResponse> getEventDetail(
            @Path("eventId") String eventId
    );

    // Register Event
    @POST("api-andr/events/register")
    Call<BaseResponse<EventRegistrationResponse>> registerEvent(
//            @Header("X-Userinfo") String xUserinfo,
            @Body EventRegistrationRequest request
    );
}
