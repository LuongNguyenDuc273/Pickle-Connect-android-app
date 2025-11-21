package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Home.HomeResponse;
import com.datn06.pickleconnect.Login.LoginRequest;
import com.datn06.pickleconnect.Login.LoginResponse;
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

    // ⚠️ DEPRECATED: Use AuthApiService.login() instead
    // Login
    @POST("api-andr/account/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Get Profile (cần token)
//    @GET("auth/profile")
//    Call<ProfileResponse> getProfile();

    // ⚠️ DEPRECATED: Use AuthApiService.register() instead
    // Register
    @POST("api-andr/account/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

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
}
