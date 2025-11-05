package com.datn06.pickleconnect.Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.Login.Login;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.AlertHelper;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etEmail, etFullName, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginNow;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLoginNow);

        loadingDialog = new LoadingDialog(this);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvLoginNow.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!validateInputs(username, phone, email, fullName, password, confirmPassword)) {
            return;
        }

        showLoading(true);

        RegisterRequest request = new RegisterRequest(username, email, fullName, password, phone);

        ApiClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    if ("201".equals(registerResponse.getCode()) || "200".equals(registerResponse.getCode())) {
                        AlertHelper.showSuccess(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.");

                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(RegisterActivity.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }, 2000);

                    } else {
                        AlertHelper.showError(RegisterActivity.this, registerResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Đăng ký thất bại!";
                    if (response.code() == 409) {
                        errorMessage = "Email, số điện thoại hoặc tên đăng nhập đã tồn tại!";
                    } else if (response.code() == 400) {
                        errorMessage = "Thông tin không hợp lệ. Vui lòng kiểm tra lại!";
                    }
                    AlertHelper.showError(RegisterActivity.this, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                showLoading(false);
                AlertHelper.showError(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private boolean validateInputs(String username, String phone, String email,
                                   String fullName, String password, String confirmPassword) {
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

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            etPassword.requestFocus();
            return false;
        }

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

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingDialog.show();
            btnRegister.setEnabled(false);
            etUsername.setEnabled(false);
            etPhone.setEnabled(false);
            etEmail.setEnabled(false);
            etFullName.setEnabled(false);
            etPassword.setEnabled(false);
            etConfirmPassword.setEnabled(false);
        } else {
            loadingDialog.dismiss();
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