package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Login.LoginRequest;
import com.datn06.pickleconnect.Login.LoginResponse;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.ForgotPasswordRequest;
import com.datn06.pickleconnect.Models.ForgotPasswordResponse;
import com.datn06.pickleconnect.Models.OtpVerifyRequest;
import com.datn06.pickleconnect.Models.OtpVerifyResponse;
import com.datn06.pickleconnect.Models.ResendOtpRequest;
import com.datn06.pickleconnect.Models.ResetPasswordRequest;
import com.datn06.pickleconnect.Models.ResetPasswordResponse;
import com.datn06.pickleconnect.Policy.PolicyRequest;
import com.datn06.pickleconnect.Policy.PolicyResponse;
import com.datn06.pickleconnect.Register.RegisterRequest;
import com.datn06.pickleconnect.Register.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Auth API Service - Handles authentication and registration
 * Base URL: http://10.0.2.2:9005/ (member-command-api)
 * Endpoints under /auth path
 */
public interface AuthApiService {

    // Register new user (Step 1: Send OTP to email)
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // Verify OTP (Step 2: Verify OTP code from email)
    @POST("auth/verify-otp")
    Call<BaseResponse<OtpVerifyResponse>> verifyOtp(@Body OtpVerifyRequest request);

    // Forgot Password (Step 1: Verify email exists and send OTP)
    @POST("auth/forgot-password")
    Call<BaseResponse<ForgotPasswordResponse>> forgotPassword(@Body ForgotPasswordRequest request);

    // Resend OTP via Email (Uses existing sendEmail API)
    @POST("auth/send-email")
    Call<BaseResponse<String>> resendOtp(@Body ResendOtpRequest request);

    // Reset Password (Step 3: Set password after OTP verification)
    @POST("auth/reset-password")
    Call<BaseResponse<ResetPasswordResponse>> resetPassword(@Body ResetPasswordRequest request);

    // Login
    @POST("api-andr/account/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Get policy
    @POST("auth/policy")
    Call<BaseResponse<PolicyResponse>> getPolicy(@Body PolicyRequest request);

    // Reset password (Future implementation)
    // @POST("auth/reset-password")
    // Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);
}
