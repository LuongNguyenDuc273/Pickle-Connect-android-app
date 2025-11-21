package com.datn06.pickleconnect.Register;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AuthApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Login.Login;
import com.datn06.pickleconnect.OtpVerificationActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.AlertHelper;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etEmail, etFullName, etDob;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private MaterialButton btnRegister;
    private TextView tvLoginNow, tvTerms;
    private CheckBox cbTerms;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etDob = findViewById(R.id.etDob);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLoginNow);
        tvTerms = findViewById(R.id.tvTerms);
        cbTerms = findViewById(R.id.cbTerms);

        loadingDialog = new LoadingDialog(this);

        setupTermsText();
        setupDatePicker();

        btnRegister.setOnClickListener(v -> handleRegister());

        tvLoginNow.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupDatePicker() {
        etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
                        etDob.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupTermsText() {
        String fullText = "Tôi xác nhận đồng ý với Chính sách chia sẻ dữ liệu và bảo mật của Pickle Connect";
        SpannableString spannableString = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Chính sách chia sẻ dữ liệu và bảo mật của Pickle Connect");
        int endIndex = fullText.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Open PolicyActivity
                Intent intent = new Intent(RegisterActivity.this, com.datn06.pickleconnect.Policy.PolicyActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#00BFA5")); // Green color
                ds.setUnderlineText(false); // Remove underline
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTerms.setText(spannableString);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerms.setHighlightColor(Color.TRANSPARENT);
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        
        // Check if terms are accepted
        if (!cbTerms.isChecked()) {
            AlertHelper.showError(this, "Vui lòng đồng ý với chính sách chia sẻ dữ liệu và bảo mật");
            return;
        }

        // Get gender
        String gender = "";
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == rbMale.getId()) {
            gender = "1"; // Male
        } else if (selectedGenderId == rbFemale.getId()) {
            gender = "0"; // Female
        }

        if (!validateInputs(username, phone, email, fullName, dob, gender)) {
            return;
        }

        showLoading(true);

        RegisterRequest request = new RegisterRequest();
        request.setUserName(username);
        request.setEmail(email);
        request.setFullName(fullName);
        request.setPhoneNumber(phone);
        request.setDateOfBirth(dob);
        request.setGender(gender);

        // Use AuthApiService from AUTH_SERVICE host
        AuthApiService authService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);
        authService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    if ("00".equals(registerResponse.getCode())) {
                        // Registration successful - OTP sent to email
                        RegisterResponse.RegisterData data = registerResponse.getData();
                        
                        Toast.makeText(RegisterActivity.this,
                            "Đăng ký thành công! Mã OTP đã được gửi đến email của bạn.", 
                            Toast.LENGTH_LONG).show();

                        // Navigate to OTP Verification Activity
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", data.getEmail());
                            intent.putExtra("username", username); // ✅ Add username for auto-login
                            intent.putExtra("maskedEmail", data.getMaskedEmail());
                            intent.putExtra("otpExpireTime", data.getOtpExpireTime() != null ? data.getOtpExpireTime() : 300);
                            startActivity(intent);
                            finish(); // Close register activity
                        }, 1500);

                    } else {
                        AlertHelper.showError(RegisterActivity.this, registerResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Đăng ký thất bại!";
                    if (response.code() == 409) {
                        errorMessage = "Email hoặc số điện thoại đã tồn tại!";
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
                                   String fullName, String dob, String gender) {
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

        if (dob.isEmpty()) {
            etDob.setError("Vui lòng chọn ngày sinh");
            etDob.requestFocus();
            return false;
        }

        if (gender.isEmpty()) {
            AlertHelper.showError(this, "Vui lòng chọn giới tính");
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            if (loadingDialog != null) {
                loadingDialog.show();
            }
            btnRegister.setEnabled(false);
            etUsername.setEnabled(false);
            etPhone.setEnabled(false);
            etEmail.setEnabled(false);
            etFullName.setEnabled(false);
            etDob.setEnabled(false);
            rgGender.setEnabled(false);
        } else {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            btnRegister.setEnabled(true);
            etUsername.setEnabled(true);
            etPhone.setEnabled(true);
            etEmail.setEnabled(true);
            etFullName.setEnabled(true);
            etDob.setEnabled(true);
            rgGender.setEnabled(true);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup loading dialog to prevent memory leak
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}