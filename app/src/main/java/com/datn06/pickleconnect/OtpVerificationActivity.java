package com.datn06.pickleconnect;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AuthApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.OtpVerifyRequest;
import com.datn06.pickleconnect.Models.OtpVerifyResponse;
import com.datn06.pickleconnect.Models.ResendOtpRequest;
import com.datn06.pickleconnect.Policy.PolicyResponse;
import com.datn06.pickleconnect.Register.RegisterResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    // UI Components
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private TextView tvEmail, tvTimer;
    private MaterialButton btnVerify, btnResendOtp;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;

    // Data
    private String email;
    private String username; // ✅ Add username field
    private String maskedEmail;
    private String mode; // ✅ "registration" or "forgot_password"
    private int otpExpireTime; // in seconds

    // Timer
    private CountDownTimer countDownTimer;
    private boolean canResend = false;

    // API
    private AuthApiService authApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize API
        authApiService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);

        // Get data from Intent
        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username"); // ✅ Get username from Intent
        maskedEmail = getIntent().getStringExtra("maskedEmail");
        mode = getIntent().getStringExtra("mode"); // ✅ Get mode: "registration" or "forgot_password"
        if (mode == null || mode.isEmpty()) {
            mode = "registration"; // Default to registration for backward compatibility
        }
        otpExpireTime = getIntent().getIntExtra("otpExpireTime", 300); // Default 5 minutes

        // Initialize Views
        initViews();

        // Setup Toolbar
        setupToolbar();

        // Setup OTP Input
        setupOtpInput();

        // Start Timer
        startCountdownTimer(otpExpireTime);

        // Setup Buttons
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        tvEmail = findViewById(R.id.tvEmail);
        tvTimer = findViewById(R.id.tvTimer);
        btnVerify = findViewById(R.id.btnVerify);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        progressBar = findViewById(R.id.progressBar);

        // Display masked email
        tvEmail.setText(maskedEmail != null ? maskedEmail : email);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xác thực OTP");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupOtpInput() {
        // Auto-focus next field when digit entered
        addOtpTextWatcher(etOtp1, null, etOtp2);
        addOtpTextWatcher(etOtp2, etOtp1, etOtp3);
        addOtpTextWatcher(etOtp3, etOtp2, etOtp4);
        addOtpTextWatcher(etOtp4, etOtp3, etOtp5);
        addOtpTextWatcher(etOtp5, etOtp4, etOtp6);
        addOtpTextWatcher(etOtp6, etOtp5, null);

        // Handle backspace to go to previous field
        addBackspaceListener(etOtp2, etOtp1);
        addBackspaceListener(etOtp3, etOtp2);
        addBackspaceListener(etOtp4, etOtp3);
        addBackspaceListener(etOtp5, etOtp4);
        addBackspaceListener(etOtp6, etOtp5);

        // Auto-submit when all 6 digits entered
        etOtp6.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyOtp();
                return true;
            }
            return false;
        });

        // Focus first field
        etOtp1.requestFocus();
    }

    private void addOtpTextWatcher(EditText current, EditText previous, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Auto-verify when all 6 digits entered
                if (isOtpComplete()) {
                    btnVerify.setEnabled(true);
                } else {
                    btnVerify.setEnabled(false);
                }
            }
        });
    }

    private void addBackspaceListener(EditText current, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && 
                event.getAction() == KeyEvent.ACTION_DOWN && 
                current.getText().toString().isEmpty() && 
                previous != null) {
                previous.requestFocus();
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        // Verify Button
        btnVerify.setOnClickListener(v -> verifyOtp());

        // Resend OTP Button
        btnResendOtp.setOnClickListener(v -> resendOtp());
    }

    private void startCountdownTimer(int seconds) {
        btnResendOtp.setEnabled(false);
        canResend = false;

        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format("Gửi lại mã OTP sau: %ds", secondsLeft));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Mã OTP đã hết hạn");
                btnResendOtp.setEnabled(true);
                canResend = true;
                Toast.makeText(OtpVerificationActivity.this, 
                    "Mã OTP đã hết hạn. Vui lòng gửi lại!", 
                    Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private boolean isOtpComplete() {
        return !etOtp1.getText().toString().isEmpty() &&
               !etOtp2.getText().toString().isEmpty() &&
               !etOtp3.getText().toString().isEmpty() &&
               !etOtp4.getText().toString().isEmpty() &&
               !etOtp5.getText().toString().isEmpty() &&
               !etOtp6.getText().toString().isEmpty();
    }

    private String getOtpCode() {
        return etOtp1.getText().toString() +
               etOtp2.getText().toString() +
               etOtp3.getText().toString() +
               etOtp4.getText().toString() +
               etOtp5.getText().toString() +
               etOtp6.getText().toString();
    }

    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void verifyOtp() {
        if (!isOtpComplete()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        String otpCode = getOtpCode();

        // Show loading
        showLoading(true);

        // Create request
        OtpVerifyRequest request = new OtpVerifyRequest(
            email,
            otpCode,
            UUID.randomUUID().toString()
        );

        // Call API
        authApiService.verifyOtp(request).enqueue(new Callback<BaseResponse<OtpVerifyResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<OtpVerifyResponse>> call, 
                                 Response<BaseResponse<OtpVerifyResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<OtpVerifyResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        // OTP verified successfully
                        OtpVerifyResponse data = baseResponse.getData();
                        
                        Toast.makeText(OtpVerificationActivity.this, 
                            "Xác thực OTP thành công!", 
                            Toast.LENGTH_SHORT).show();

                        // Navigate to Set Password Activity with mode
                        Intent intent = new Intent(OtpVerificationActivity.this, SetPasswordActivity.class);
                        intent.putExtra("email", email);
                        // ✅ Use username from Intent (passed from RegisterActivity or ForgotPasswordActivity)
                        intent.putExtra("username", username != null ? username : (data != null ? data.getUsername() : ""));
                        intent.putExtra("mode", mode); // ✅ Pass mode to SetPasswordActivity
                        startActivity(intent);
                        finish();
                    } else {
                        // OTP verification failed
                        Toast.makeText(OtpVerificationActivity.this, 
                            baseResponse.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        clearOtpFields();
                    }
                } else {
                    Toast.makeText(OtpVerificationActivity.this, 
                        "Xác thực OTP thất bại. Vui lòng thử lại!", 
                        Toast.LENGTH_SHORT).show();
                    clearOtpFields();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<OtpVerifyResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OtpVerificationActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
                clearOtpFields();
            }
        });
    }

    private void resendOtp() {
        if (!canResend) {
            Toast.makeText(this, "Vui lòng đợi hết thời gian đếm ngược", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        showLoading(true);

        // Create request
        ResendOtpRequest request = new ResendOtpRequest(
            email,
            UUID.randomUUID().toString()
        );

        // Call API (send-email endpoint returns BaseResponse<String>)
        authApiService.resendOtp(request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<String> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        // OTP resent successfully
                        Toast.makeText(OtpVerificationActivity.this, 
                            "Mã OTP mới đã được gửi đến email của bạn!", 
                            Toast.LENGTH_LONG).show();

                        // Restart timer (default 5 minutes = 300 seconds)
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        
                        startCountdownTimer(300); // Default OTP timeout
                        clearOtpFields();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, 
                            baseResponse.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(OtpVerificationActivity.this, 
                        "Gửi lại OTP thất bại. Vui lòng thử lại!", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(OtpVerificationActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(false);
            btnResendOtp.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnVerify.setEnabled(isOtpComplete());
            btnResendOtp.setEnabled(canResend);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
