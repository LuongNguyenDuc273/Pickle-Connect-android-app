package com.datn06.pickleconnect.Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.Login.Login;
import com.datn06.pickleconnect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etEmail, etFullName, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLoginNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Kết nối với XML
        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginNow = findViewById(R.id.tvLoginNow);

        // Xử lý sự kiện click button Đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        // Xử lý sự kiện click "Đăng nhập ngay"
        tvLoginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình đăng nhập
                Intent intent = new Intent(RegisterActivity.this, Login.class);
                startActivity(intent);
                finish(); // Đóng màn hình đăng ký
            }
        });
    }

    private void handleRegister() {
        // Lấy giá trị từ các EditText
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate các trường
        if (!validateInputs(username, phone, email, fullName, password, confirmPassword)) {
            return;
        }

        // Hiện loading
        showLoading(true);

        // Tạo request object
        RegisterRequest request = new RegisterRequest(username, email, fullName, password, phone);

        // GỌI API REGISTER
        ApiClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                // Ẩn loading
                showLoading(false);

                // Kiểm tra response
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    // Kiểm tra code từ server
                    if ("201".equals(registerResponse.getCode()) || "200".equals(registerResponse.getCode())) {

                        Toast.makeText(RegisterActivity.this,
                                "Đăng ký thành công! Vui lòng đăng nhập.",
                                Toast.LENGTH_LONG).show();

                        // Chuyển về màn hình đăng nhập
                        Intent intent = new Intent(RegisterActivity.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Đóng màn hình đăng ký

                    } else {
                        // Server trả về lỗi
                        Toast.makeText(RegisterActivity.this,
                                registerResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    String errorMessage = "Đăng ký thất bại!";

                    // Xử lý lỗi 409 (Conflict) - Email/Username/Phone đã tồn tại
                    if (response.code() == 409) {
                        errorMessage = "Email, số điện thoại hoặc tên đăng nhập đã tồn tại!";
                    } else if (response.code() == 400) {
                        errorMessage = "Thông tin không hợp lệ. Vui lòng kiểm tra lại!";
                    }

                    Toast.makeText(RegisterActivity.this,
                            errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                // Ẩn loading
                showLoading(false);

                // Hiển thị lỗi
                Toast.makeText(RegisterActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm validate các trường input
    private boolean validateInputs(String username, String phone, String email,
                                   String fullName, String password, String confirmPassword) {

        // Kiểm tra tên đăng nhập
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            etUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
            etUsername.requestFocus();
            return false;
        }

        // Kiểm tra số điện thoại
        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10 || phone.length() > 11) {
            etPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return false;
        }

        // Kiểm tra email
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

        // Kiểm tra họ tên
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên đầy đủ");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 3) {
            etFullName.setError("Họ tên phải có ít nhất 3 ký tự");
            etFullName.requestFocus();
            return false;
        }

        // Kiểm tra mật khẩu
        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        // Kiểm tra xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    // Hàm hiển thị/ẩn loading
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            etUsername.setEnabled(false);
            etPhone.setEnabled(false);
            etEmail.setEnabled(false);
            etFullName.setEnabled(false);
            etPassword.setEnabled(false);
            etConfirmPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            etUsername.setEnabled(true);
            etPhone.setEnabled(true);
            etEmail.setEnabled(true);
            etFullName.setEnabled(true);
            etPassword.setEnabled(true);
            etConfirmPassword.setEnabled(true);
        }
    }
}