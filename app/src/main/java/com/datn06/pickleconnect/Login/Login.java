package com.datn06.pickleconnect.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.MainActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Register.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegisterNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Kết nối với XML
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);

        // Xử lý sự kiện click button Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Xử lý sự kiện click "Đăng ký ngay"
        tvRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình đăng ký
                Intent intent = new Intent(Login.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        // Lấy giá trị từ EditText
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validate
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

        // Hiện loading
        showLoading(true);

        // Tạo request object
        LoginRequest request = new LoginRequest(username, password);

        // GỌI API LOGIN
        ApiClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Ẩn loading
                showLoading(false);

                // Kiểm tra response
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Kiểm tra code từ server
                    if ("200".equals(loginResponse.getCode()) || "201".equals(loginResponse.getCode())) {

                        // Lấy dữ liệu từ response
                        LoginResponse.DataLogin data = loginResponse.getData();
                        String token = data.getToken();
                        String refreshToken = data.getRefreshToken();
                        Long accountId = data.getAccountId();
                        String fullName = data.getFullName();

                        // Lưu token vào SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                        prefs.edit()
                                .putString("token", token)
                                .putString("refreshToken", refreshToken)
                                .putLong("accountId", accountId)
                                .putString("fullName", fullName)
                                .apply();

                        // Set token cho ApiClient
                        ApiClient.setAuthToken(token);

                        Toast.makeText(Login.this,
                                "Đăng nhập thành công! Xin chào " + fullName,
                                Toast.LENGTH_SHORT).show();

                        // Chuyển sang MainActivity
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Đóng màn hình login

                    } else {
                        // Server trả về lỗi
                        Toast.makeText(Login.this,
                                loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    Toast.makeText(Login.this,
                            "Đăng nhập thất bại! Mã lỗi: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Ẩn loading
                showLoading(false);

                // Hiển thị lỗi
                Toast.makeText(Login.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm hiển thị/ẩn loading
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            etUsername.setEnabled(false);
            etPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            etUsername.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }
}