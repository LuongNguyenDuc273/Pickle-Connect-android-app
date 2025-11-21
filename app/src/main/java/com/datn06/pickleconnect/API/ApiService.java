package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Event.EventDetailResponse;
import com.datn06.pickleconnect.Event.EventResponse;
import com.datn06.pickleconnect.Home.HomeResponse;
import com.datn06.pickleconnect.Login.LoginRequest;
import com.datn06.pickleconnect.Login.LoginResponse;
import com.datn06.pickleconnect.Register.RegisterRequest;
import com.datn06.pickleconnect.Register.RegisterResponse;
import com.datn06.pickleconnect.Search.SearchResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Login
    @POST("api-andr/account/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Get Profile (cần token)
//    @GET("api-andr/account/profile")
//    Call<ProfileResponse> getProfile();

    // Register
    @POST("api-andr/account/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // Home
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
}
