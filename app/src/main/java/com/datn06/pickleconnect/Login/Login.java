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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegisterNow, tvForgotPassword;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        loadingDialog = new LoadingDialog(this);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, com.datn06.pickleconnect.ForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvRegisterNow.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        LoginRequest request = new LoginRequest(username, password);

        AuthApiService authService = ApiClient.createService(ServiceHost.API_SERVICE, AuthApiService.class);
        authService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if ("200".equals(loginResponse.getCode()) || "201".equals(loginResponse.getCode())) {
                        LoginResponse.DataLogin data = loginResponse.getData();

                        // DÙNG TokenManager
                        TokenManager tokenManager = TokenManager.getInstance(Login.this);
                        tokenManager.saveLoginData(
                                data.getToken(),                      // token
                                data.getRefreshToken(),               // refreshToken
                                String.valueOf(data.getAccountId()),  // userId
                                data.getUserName(),                   // username
                                data.getFullName(),                   // fullName
                                data.getEmail(),                      // email
                                data.getPhoneNumber()                 // phoneNumber ✅ THÊM
                        );

                        //Set token cho ApiClient (để Interceptor tự động thêm vào header)
                        ApiClient.setAuthToken(data.getToken());

                        AlertHelper.showSuccess(Login.this,
                                "Đăng nhập thành công! Xin chào " + data.getFullName());

                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(Login.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }, 2000);

                    } else {
                        AlertHelper.showError(Login.this, loginResponse.getMessage());
                    }
                } else {
                    AlertHelper.showError(Login.this, "Đăng nhập thất bại! Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                AlertHelper.showError(Login.this, "Lỗi kết nối: " + t.getMessage());
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