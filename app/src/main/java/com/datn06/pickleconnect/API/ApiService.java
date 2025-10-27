package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Login.LoginRequest;
import com.datn06.pickleconnect.Login.LoginResponse;
import com.datn06.pickleconnect.Register.RegisterRequest;
import com.datn06.pickleconnect.Register.RegisterResponse;

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
}
