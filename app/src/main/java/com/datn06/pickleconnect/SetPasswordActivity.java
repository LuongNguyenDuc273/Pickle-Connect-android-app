package com.datn06.pickleconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AuthApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Home.HomeActivity;
import com.datn06.pickleconnect.Login.Login;
import com.datn06.pickleconnect.Login.LoginRequest;
import com.datn06.pickleconnect.Login.LoginResponse;
import com.datn06.pickleconnect.Models.ResetPasswordRequest;
import com.datn06.pickleconnect.Models.ResetPasswordResponse;
import com.datn06.pickleconnect.Utils.SharedPrefManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Set Password Activity for Registration Flow
 * Step 3: Set password after OTP verification
 */
public class SetPasswordActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private TextView tvRequirement1Icon, tvRequirement2Icon, tvRequirement3Icon, tvRequirement4Icon;
    private TextView tvRequirement1, tvRequirement2, tvRequirement3, tvRequirement4;
    private MaterialButton btnConfirm;
    private ProgressBar progressBar;

    private AuthApiService authApiService;

    // Data from previous screen
    private String email;
    private String phoneNumber;
    private String username;
    private String mode; // ✅ "registration" or "forgot_password"

    // Password validation flags
    private boolean hasMinLength = false;
    private boolean hasUpperCase = false;
    private boolean hasLowerCase = false;
    private boolean hasDigit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        // Initialize API service for reset password (port 9005)
        authApiService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);

        // Get data from intent
        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        mode = getIntent().getStringExtra("mode"); // ✅ Get mode
        if (mode == null || mode.isEmpty()) {
            mode = "registration"; // Default to registration
        }

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        tvRequirement1Icon = findViewById(R.id.tvRequirement1Icon);
        tvRequirement2Icon = findViewById(R.id.tvRequirement2Icon);
        tvRequirement3Icon = findViewById(R.id.tvRequirement3Icon);
        tvRequirement4Icon = findViewById(R.id.tvRequirement4Icon);
        
        tvRequirement1 = findViewById(R.id.tvRequirement1);
        tvRequirement2 = findViewById(R.id.tvRequirement2);
        tvRequirement3 = findViewById(R.id.tvRequirement3);
        tvRequirement4 = findViewById(R.id.tvRequirement4);
        
        btnConfirm = findViewById(R.id.btnConfirm);
        progressBar = findViewById(R.id.progressBar);

        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initially disable confirm button
        btnConfirm.setEnabled(false);
    }

    private void setupListeners() {
        // Password validation on text change
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                updateConfirmButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm password validation
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConfirmButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm button click
        btnConfirm.setOnClickListener(v -> setPassword());
    }

    private void validatePassword(String password) {
        // Check minimum length (8 characters)
        hasMinLength = password.length() >= 8;
        updateRequirementUI(tvRequirement1Icon, tvRequirement1, hasMinLength);

        // Check for uppercase letter
        hasUpperCase = password.matches(".*[A-Z].*");
        updateRequirementUI(tvRequirement2Icon, tvRequirement2, hasUpperCase);

        // Check for lowercase letter
        hasLowerCase = password.matches(".*[a-z].*");
        updateRequirementUI(tvRequirement3Icon, tvRequirement3, hasLowerCase);

        // Check for digit
        hasDigit = password.matches(".*\\d.*");
        updateRequirementUI(tvRequirement4Icon, tvRequirement4, hasDigit);
    }

    private void updateRequirementUI(TextView iconView, TextView textView, boolean isMet) {
        if (isMet) {
            iconView.setText("✓");
            iconView.setTextColor(getResources().getColor(R.color.green, null));
            textView.setTextColor(getResources().getColor(R.color.green, null));
        } else {
            iconView.setText("×");
            iconView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            textView.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    private void updateConfirmButtonState() {
        String password = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean allRequirementsMet = hasMinLength && hasUpperCase && hasLowerCase && hasDigit;
        boolean passwordsMatch = !password.isEmpty() && password.equals(confirmPassword);

        btnConfirm.setEnabled(allRequirementsMet && passwordsMatch);
    }

    private void setPassword() {
        String password = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Final validation
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        showLoading(true);

        // Create request - BaseRequest auto-generates clientId, requestId, requestTime
        ResetPasswordRequest request = new ResetPasswordRequest(
                phoneNumber,
                email,
                password,
                confirmPassword
        );

        // Call API
        authApiService.resetPassword(request).enqueue(new Callback<BaseResponse<ResetPasswordResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<ResetPasswordResponse>> call, 
                                 Response<BaseResponse<ResetPasswordResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<ResetPasswordResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        // Password set successfully
                        Toast.makeText(SetPasswordActivity.this, 
                            "Đặt mật khẩu thành công!", 
                            Toast.LENGTH_SHORT).show();
                        
                        // ✅ Check mode to determine next action
                        if ("registration".equals(mode)) {
                            // Registration flow: Auto login → Home
                            String loginIdentifier = (username != null && !username.isEmpty()) ? username : email;
                            autoLogin(loginIdentifier, password);
                        } else if ("forgot_password".equals(mode)) {
                            // Forgot password flow: Navigate to Login screen
                            showLoading(false);
                            
                            new android.os.Handler().postDelayed(() -> {
                                Toast.makeText(SetPasswordActivity.this, 
                                    "Vui lòng đăng nhập lại với mật khẩu mới", 
                                    Toast.LENGTH_LONG).show();
                                
                                Intent intent = new Intent(SetPasswordActivity.this, Login.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }, 1500);
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(SetPasswordActivity.this, 
                            baseResponse.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(SetPasswordActivity.this, 
                        "Đặt mật khẩu thất bại. Vui lòng thử lại!", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ResetPasswordResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SetPasswordActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void autoLogin(String username, String password) {
        Log.d("SetPasswordActivity", "Auto-login with username: " + username);
        LoginRequest loginRequest = new LoginRequest(username, password);

        // ✅ FIXED: Use API_SERVICE (port 9003) for login endpoint
        AuthApiService loginService = ApiClient.createService(ServiceHost.API_SERVICE, AuthApiService.class);
        
        loginService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                Log.d("SetPasswordActivity", "Auto-login response code: " + response.code());
                Log.d("SetPasswordActivity", "Auto-login successful: " + response.isSuccessful());
                Log.d("SetPasswordActivity", "Auto-login body null: " + (response.body() == null));

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d("SetPasswordActivity", "Login response code: " + loginResponse.getCode());
                    Log.d("SetPasswordActivity", "Login response message: " + loginResponse.getMessage());

                    if ("200".equals(loginResponse.getCode()) || "201".equals(loginResponse.getCode())) {
                        // Login successful - Save token and navigate to Home
                        LoginResponse.DataLogin data = loginResponse.getData();
                        String token = data.getToken();
                        String refreshToken = data.getRefreshToken();
                        Long accountId = data.getAccountId();
                        String fullName = data.getFullName();
                        String userEmail = data.getEmail();

                        // ✅ UPDATED: Use SharedPrefManager instead of SharedPreferences directly
                        SharedPrefManager prefManager = SharedPrefManager.getInstance(SetPasswordActivity.this);
                        prefManager.saveUser(
                            String.valueOf(accountId), // userId = accountId
                            username,                    // username
                            userEmail,                   // email
                            null,                        // phone (optional)
                            fullName                     // fullName
                        );
                        prefManager.saveTokens(token, refreshToken);

                        // Also save to "MyApp" SharedPreferences for backward compatibility with ApiClient.init()
                        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                        prefs.edit()
                                .putString("token", token)
                                .putString("refreshToken", refreshToken)
                                .putLong("accountId", accountId)
                                .putString("fullName", fullName)
                                .apply();

                        // Set token to ApiClient for future requests
                        ApiClient.setAuthToken(token);

                        Toast.makeText(SetPasswordActivity.this, 
                            "Đăng ký thành công! Xin chào " + fullName,
                            Toast.LENGTH_SHORT).show();

                        // Navigate to Home
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(SetPasswordActivity.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    } else {
                        // Login failed - navigate to Login screen
                        Log.e("SetPasswordActivity", "Auto-login failed: code=" + loginResponse.getCode() + ", message=" + loginResponse.getMessage());
                        Toast.makeText(SetPasswordActivity.this, 
                            "Đăng ký thành công! Vui lòng đăng nhập.", 
                            Toast.LENGTH_LONG).show();
                        
                        Intent intent = new Intent(SetPasswordActivity.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // Auto login failed - navigate to Login screen
                    Log.e("SetPasswordActivity", "Auto-login response failed: isSuccessful=" + response.isSuccessful() + ", body=" + response.body());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("SetPasswordActivity", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("SetPasswordActivity", "Cannot read error body", e);
                        }
                    }
                    Toast.makeText(SetPasswordActivity.this, 
                        "Đăng ký thành công! Vui lòng đăng nhập.", 
                        Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(SetPasswordActivity.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                
                // Auto login failed - navigate to Login screen
                Log.e("SetPasswordActivity", "Auto-login network error", t);
                Toast.makeText(SetPasswordActivity.this, 
                    "Đăng ký thành công! Vui lòng đăng nhập.", 
                    Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(SetPasswordActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnConfirm.setEnabled(false);
            etNewPassword.setEnabled(false);
            etConfirmPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnConfirm.setEnabled(true);
            etNewPassword.setEnabled(true);
            etConfirmPassword.setEnabled(true);
        }
    }
}
