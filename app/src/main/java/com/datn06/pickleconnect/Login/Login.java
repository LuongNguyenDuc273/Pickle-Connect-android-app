package com.datn06.pickleconnect.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AuthApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Home.HomeActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Register.RegisterActivity;
import com.datn06.pickleconnect.Register.RegisterResponse;
import com.datn06.pickleconnect.Utils.AlertHelper;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Utils.SharedPrefManager;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvRegisterNow, tvForgotPassword;
    private LoadingDialog loadingDialog;
    private TextView tvLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvLoginError = findViewById(R.id.tvLoginError);

        loadingDialog = new LoadingDialog(this);

        btnLogin.setOnClickListener(v -> handleLogin());

        // Xử lý cả 2 nút "Quên mật khẩu"
        TextView tvForgotPasswordTop = findViewById(R.id.tvForgotPasswordTop);
        tvForgotPasswordTop.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, com.datn06.pickleconnect.ForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, com.datn06.pickleconnect.ForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvRegisterNow.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Xóa lỗi khi người dùng bắt đầu nhập
        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilUsername.setError(null);
                tilUsername.setErrorEnabled(false);
                tvLoginError.setVisibility(View.GONE);
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilPassword.setError(null);
                tilPassword.setErrorEnabled(false);
                tvLoginError.setVisibility(View.GONE);
            }
        });
    }

    private void handleLogin() {
        // Xóa các lỗi cũ
        tilUsername.setError(null);
        tilPassword.setError(null);
        tvLoginError.setVisibility(View.GONE);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validate và hiển thị lỗi BÊN TRONG TextInputLayout
        boolean hasError = false;

        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            hasError = true;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            if (!hasError) {
                etPassword.requestFocus();
            }
            hasError = true;
        }

        if (hasError) {
            return;
        }

        showLoading(true);

        LoginRequest request = new LoginRequest(username, password);

        AuthApiService authService = ApiClient.createService(ServiceHost.API_SERVICE, AuthApiService.class);
        authService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                LoginResponse loginResponse = response.body();

                // ✅ KIỂM TRA HTTP STATUS CODE TRƯỚC
                if (response.isSuccessful() && loginResponse != null &&
                        ("200".equals(loginResponse.getCode()) || "201".equals(loginResponse.getCode()))) {
                    // ĐĂNG NHẬP THÀNH CÔNG
                    LoginResponse.DataLogin data = loginResponse.getData();

                    TokenManager tokenManager = TokenManager.getInstance(Login.this);
                    tokenManager.saveLoginData(
                            data.getToken(),
                            data.getRefreshToken(),
                            String.valueOf(data.getAccountId()),
                            data.getUserName(),
                            data.getFullName(),
                            data.getEmail(),
                            data.getPhoneNumber(),
                            null
                    );

                    SharedPrefManager prefManager = SharedPrefManager.getInstance(Login.this);
                    prefManager.saveUser(
                            String.valueOf(data.getAccountId()),
                            data.getUserName(),
                            data.getEmail(),
                            data.getPhoneNumber(),
                            data.getFullName()
                    );
                    prefManager.saveTokens(data.getToken(), data.getRefreshToken());

                    ApiClient.setAuthToken(data.getToken());
                    ApiClient.reloadToken();

                    AlertHelper.showSuccess(Login.this,
                            "Đăng nhập thành công! Xin chào " + data.getFullName());

                    new android.os.Handler().postDelayed(() -> {
                        Intent intent = new Intent(Login.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("forceRefresh", true);
                        startActivity(intent);
                        finish();
                    }, 2000);

                } else {
                    // ❌ ĐĂNG NHẬP THẤT BẠI
                    String errorMessage = "Đăng nhập thất bại";

                    // Ưu tiên lấy message từ response body
                    if (loginResponse != null && loginResponse.getMessage() != null && !loginResponse.getMessage().isEmpty()) {
                        errorMessage = loginResponse.getMessage();
                    }
                    // Nếu HTTP 500 và không có message từ body
                    else if (response.code() == 500) {
                        errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng";
                    }
                    // Các lỗi server khác
                    else if (response.code() >= 500) {
                        errorMessage = "Lỗi server. Vui lòng thử lại sau";
                    }
                    // Lỗi 4xx (Bad Request, Unauthorized, etc.)
                    else if (response.code() >= 400) {
                        errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng";
                    }

                    // ⭐ Hiển thị lỗi ở TextView bên dưới Username
                    final String finalErrorMessage = errorMessage;
                    tvLoginError.post(() -> {
                        tvLoginError.setText(finalErrorMessage);
                        tvLoginError.setVisibility(View.VISIBLE);
                        tvLoginError.invalidate();
                    });
                    etUsername.requestFocus();

                    // Debug log
                    android.util.Log.e("LOGIN_ERROR", "Code: " + response.code());
                    android.util.Log.e("LOGIN_ERROR", "Message: " + errorMessage);
                    android.util.Log.e("LOGIN_ERROR", "TextView visible: " + (tvLoginError.getVisibility() == View.VISIBLE));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);

                // Lỗi kết nối
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                tvLoginError.setText(errorMsg);
                tvLoginError.setVisibility(View.VISIBLE);
                etUsername.requestFocus();

                android.util.Log.e("LOGIN_FAILURE", errorMsg);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingDialog.show();
            btnLogin.setEnabled(false);
            etUsername.setEnabled(false);
            etPassword.setEnabled(false);
        } else {
            loadingDialog.dismiss();
            btnLogin.setEnabled(true);
            etUsername.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }
}