package com.datn06.pickleconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AuthApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Login.Login;
import com.datn06.pickleconnect.Models.ForgotPasswordRequest;
import com.datn06.pickleconnect.Models.ForgotPasswordResponse;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Forgot Password Activity
 * Step 1 of password reset flow:
 * - User enters email
 * - Backend verifies email exists
 * - Backend sends OTP to email (with username in content)
 * - Navigate to OTP Verification screen
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etEmail;
    private MaterialButton btnContinue;
    private TextView tvBackToLogin;
    private LoadingDialog loadingDialog;
    
    private AuthApiService authApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize API service
        authApiService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);
        
        // Initialize loading dialog
        loadingDialog = new LoadingDialog(this);

        // Initialize views
        initViews();
        
        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etEmail = findViewById(R.id.etEmail);
        btnContinue = findViewById(R.id.btnContinue);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        
        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> handleContinue());
        
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleContinue() {
        String email = etEmail.getText().toString().trim();
        
        // Validate email
        if (!validateEmail(email)) {
            return;
        }
        
        // Call forgot password API
        showLoading(true);
        
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        
        authApiService.forgotPassword(request).enqueue(new Callback<BaseResponse<ForgotPasswordResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<ForgotPasswordResponse>> call, 
                                 Response<BaseResponse<ForgotPasswordResponse>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<ForgotPasswordResponse> baseResponse = response.body();
                    
                    if ("00".equals(baseResponse.getCode())) {
                        // Email exists - OTP sent successfully
                        ForgotPasswordResponse data = baseResponse.getData();
                        
                        Toast.makeText(ForgotPasswordActivity.this, 
                            "Mã OTP đã được gửi đến email của bạn", 
                            Toast.LENGTH_LONG).show();
                        
                        // Navigate to OTP Verification Activity
                        Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("email", data.getEmail());
                        intent.putExtra("username", data.getUsername());
                        intent.putExtra("maskedEmail", data.getMaskedEmail());
                        intent.putExtra("otpExpireTime", data.getOtpExpireTime() != null ? data.getOtpExpireTime() : 300);
                        intent.putExtra("mode", "forgot_password"); // ✅ Important: Mode for OTP flow
                        startActivity(intent);
                        finish();
                        
                    } else if ("02".equals(baseResponse.getCode())) {
                        // Email not found
                        etEmail.setError("Email không tồn tại trong hệ thống");
                        etEmail.requestFocus();
                        Toast.makeText(ForgotPasswordActivity.this, 
                            "Email không tồn tại trong hệ thống", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, 
                            baseResponse.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, 
                        "Có lỗi xảy ra. Vui lòng thử lại!", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ForgotPasswordResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }
        
        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            if (loadingDialog != null) {
                loadingDialog.show();
            }
            btnContinue.setEnabled(false);
            etEmail.setEnabled(false);
        } else {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            btnContinue.setEnabled(true);
            etEmail.setEnabled(true);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
