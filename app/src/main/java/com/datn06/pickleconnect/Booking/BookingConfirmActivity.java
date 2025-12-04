package com.datn06.pickleconnect.Booking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.CreateBookingCourtRequest;
import com.datn06.pickleconnect.Model.PaymentUrlResponse;
import com.datn06.pickleconnect.Model.SelectedSlotDTO;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for confirming booking details before payment
 */
public class BookingConfirmActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton btnBack;
    private TextView tvFacilityName, tvAddress, tvBookingDate, tvCourtType;
    private RecyclerView rvSelectedSlots;
    private TextView tvTotalHours, tvPaymentTotalHours, tvTotalAmount;
    private EditText etNote, etPhone, etEmail;
    private TextView tvUserName;
    private CheckBox cbAgreeTerms;
    private MaterialButton btnPayment;
    private ProgressBar progressBar;
    
    // Expandable sections
    private ImageButton btnExpandSection1, btnExpandSection2, btnExpandSection3;
    private LinearLayout layoutSection1Content, layoutSection2Content, layoutSection3Content;
    
    // Data
    private Long facilityId;
    private String facilityName;
    private String bookingDate;
    private List<SelectedSlotDTO> selectedSlots;
    private BigDecimal totalAmount;
    private double totalHours;
    
    // Adapter
    private SelectedSlotAdapter slotAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        
        // Get data from intent
        getIntentData();
        
        // Initialize views
        initViews();
        
        // Setup listeners
        setupListeners();
        
        // Display data
        displayBookingInfo();
        
        // Load user info
        loadUserInfo();
    }
    
    /**
     * Get data from intent
     */
    private void getIntentData() {
        Intent intent = getIntent();

        facilityId = intent.getLongExtra("facilityId", 0);
        facilityName = intent.getStringExtra("facilityName");
        bookingDate = intent.getStringExtra("bookingDate");

        String selectedSlotsJson = intent.getStringExtra("selectedSlots");
        selectedSlots = new Gson().fromJson(
                selectedSlotsJson,
                new TypeToken<List<SelectedSlotDTO>>(){}.getType()
        );

        String totalAmountStr = intent.getStringExtra("totalAmount");
        totalAmount = new BigDecimal(totalAmountStr);

        totalHours = intent.getDoubleExtra("totalHours", 0.0);  // ← SỬA: Đổi từ getIntExtra sang getDoubleExtra

        if (facilityId == 0 || selectedSlots == null || selectedSlots.isEmpty()) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin đặt sân", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Initialize views
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        
        // Section 1: Court Info
        btnExpandSection1 = findViewById(R.id.btnExpandSection1);
        layoutSection1Content = findViewById(R.id.layoutSection1Content);
        tvFacilityName = findViewById(R.id.tvFacilityName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvCourtType = findViewById(R.id.tvCourtType);
        rvSelectedSlots = findViewById(R.id.rvSelectedSlots);
        tvTotalHours = findViewById(R.id.tvTotalHours);
        etNote = findViewById(R.id.etNote);
        
        // Section 2: User Info
        btnExpandSection2 = findViewById(R.id.btnExpandSection2);
        layoutSection2Content = findViewById(R.id.layoutSection2Content);
        tvUserName = findViewById(R.id.tvUserName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        
        // Section 3: Payment Info
        btnExpandSection3 = findViewById(R.id.btnExpandSection3);
        layoutSection3Content = findViewById(R.id.layoutSection3Content);
        tvPaymentTotalHours = findViewById(R.id.tvPaymentTotalHours);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        cbAgreeTerms = findViewById(R.id.cbAgreeTerms);
        
        // Bottom button
        btnPayment = findViewById(R.id.btnPayment);
        progressBar = findViewById(R.id.progressBar);
        
        // Setup RecyclerView
        rvSelectedSlots.setLayoutManager(new LinearLayoutManager(this));
        slotAdapter = new SelectedSlotAdapter(selectedSlots);
        rvSelectedSlots.setAdapter(slotAdapter);
    }
    
    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Expandable sections
        btnExpandSection1.setOnClickListener(v -> toggleSection(layoutSection1Content, btnExpandSection1));
        btnExpandSection2.setOnClickListener(v -> toggleSection(layoutSection2Content, btnExpandSection2));
        btnExpandSection3.setOnClickListener(v -> toggleSection(layoutSection3Content, btnExpandSection3));
        
        // Terms checkbox
        cbAgreeTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updatePaymentButtonState());
        
        // Phone and Email validation
        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updatePaymentButtonState();
            }
        });
        
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updatePaymentButtonState();
            }
        });
        
        // Payment button
        btnPayment.setOnClickListener(v -> onPaymentClicked());
    }
    
    /**
     * Toggle section visibility
     */
    private void toggleSection(LinearLayout layout, ImageButton button) {
        if (layout.getVisibility() == View.VISIBLE) {
            layout.setVisibility(View.GONE);
            button.setRotation(0);
        } else {
            layout.setVisibility(View.VISIBLE);
            button.setRotation(180);
        }
    }
    
    /**
     * Display booking information
     */
    private void displayBookingInfo() {
        // Facility name
        tvFacilityName.setText(facilityName);

        // TODO: Get address from facility details (for now, use placeholder)
        tvAddress.setText("Địa chỉ sẽ được cập nhật");

        // Booking date (format from yyyy-MM-dd to dd/MM/yyyy)
        tvBookingDate.setText(formatDateForDisplay(bookingDate));

        // Court type
        tvCourtType.setText("Đặt sân");

        // Total hours - SỬA: Hiển thị đúng định dạng
        String hoursText = String.format(Locale.US, "%.1f giờ", totalHours);  // ← Hiển thị: "1.5 giờ"
        tvTotalHours.setText(hoursText);
        tvPaymentTotalHours.setText(hoursText);

        // Total amount
        tvTotalAmount.setText(formatCurrency(totalAmount));

        // Set terms text with clickable link
        String termsText = "Tôi xác nhận đồng ý với các điều khoản và chính sách về đặt và hủy sân trên Pickle Connect";
        cbAgreeTerms.setText(termsText);
    }
    
    /**
     * Load user information from SharedPreferences
     */
    private void loadUserInfo() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        
        String fullName = prefManager.getFullName();
        String username = prefManager.getUsername();
        String userEmail = prefManager.getEmail();
        String userPhone = prefManager.getPhone();
        
        // ✅ Use fullName if available, fallback to username
        String displayName = fullName != null && !fullName.isEmpty() ? fullName : 
                           username != null && !username.isEmpty() ? username : "User";
        tvUserName.setText(displayName);
        
        if (userEmail != null && !userEmail.isEmpty()) {
            etEmail.setText(userEmail);
        }
        
        if (userPhone != null && !userPhone.isEmpty()) {
            etPhone.setText(userPhone);
        }
    }
    
    /**
     * Update payment button state based on validation
     */
    private void updatePaymentButtonState() {
        boolean isPhoneValid = isValidPhone(etPhone.getText().toString());
        boolean isEmailValid = isValidEmail(etEmail.getText().toString());
        boolean isTermsAccepted = cbAgreeTerms.isChecked();
        
        btnPayment.setEnabled(isPhoneValid && isEmailValid && isTermsAccepted);
    }
    
    /**
     * Validate phone number
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= 10 && phone.matches("^[0-9]+$");
    }
    
    /**
     * Validate email
     */
    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Handle payment button click
     */
    private void onPaymentClicked() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Create booking request
        CreateBookingCourtRequest request = buildBookingRequest();
        
        // Call API
        createBooking(request);
    }
    
    /**
     * Validate all inputs
     */
    private boolean validateInputs() {
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        
        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return false;
        }
        
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }
        
        if (!cbAgreeTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    /**
     * Build booking request object
     */
    private CreateBookingCourtRequest buildBookingRequest() {
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);

        String orderDescription = etNote.getText().toString().trim();
        if (orderDescription.isEmpty()) {
            orderDescription = "Đặt sân ngày " + formatDateForDisplay(bookingDate);
        }

        // SỬA: Làm tròn lên để tính giờ chơi (VD: 1.5 giờ = 2 giờ cho hệ thống)
        int hours = (int) Math.ceil(totalHours);  //

        return CreateBookingCourtRequest.builder()
                .facilityId(facilityId)
                .userId(Long.parseLong(prefManager.getUserId()))
                .userName(tvUserName.getText().toString())
                .userEmail(etEmail.getText().toString().trim())
                .phoneNumber(etPhone.getText().toString().trim())
                .bookingDate(bookingDate)
                .selectedSlots(selectedSlots)
                .totalAmount(totalAmount)
                .totalHours(hours)
                .paymentMethodCode("VNPPGW")
                .orderDescription(orderDescription)
                .build();
    }
    
    /**
     * Call API to create booking
     */
    private void createBooking(CreateBookingCourtRequest request) {
        showLoading(true);
        
        // Log request for debugging
        android.util.Log.d("BookingConfirm", "Creating booking with request: " + new Gson().toJson(request));
        
        CourtApiService courtService = ApiClient.createService(
            ServiceHost.COURT_SERVICE,
            CourtApiService.class
        );
        
        courtService.createBooking(request)
            .enqueue(new Callback<BaseResponse<PaymentUrlResponse>>() {
                @Override
                public void onResponse(Call<BaseResponse<PaymentUrlResponse>> call,
                                     Response<BaseResponse<PaymentUrlResponse>> response) {
                    showLoading(false);
                    
                    android.util.Log.d("BookingConfirm", "Response code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<PaymentUrlResponse> baseResponse = response.body();
                        
                        android.util.Log.d("BookingConfirm", "Response: " + new Gson().toJson(baseResponse));
                        
                        if (baseResponse.isSuccess()) {
                            PaymentUrlResponse paymentData = baseResponse.getData();
                            openPaymentUrl(paymentData.getPaymentUrl());
                        } else {
                            showError(baseResponse.getErrorMessage());
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            android.util.Log.e("BookingConfirm", "Error response: " + errorBody);
                            showError("Lỗi kết nối server: " + response.code());
                        } catch (Exception e) {
                            showError("Lỗi kết nối server: " + response.code());
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<BaseResponse<PaymentUrlResponse>> call, Throwable t) {
                    showLoading(false);
                    android.util.Log.e("BookingConfirm", "API call failed", t);
                    showError("Lỗi kết nối: " + t.getMessage());
                }
            });
    }
    
    /**
     * Open payment URL in browser
     */
    private void openPaymentUrl(String paymentUrl) {
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không nhận được URL thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Save facilityId to SharedPreferences for later use in PaymentResultActivity
            SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
            prefManager.saveBookingFacilityId(String.valueOf(facilityId));
            
            // Open VNPay payment URL in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);
            
            // Show success message
            Toast.makeText(this, "Đang chuyển đến trang thanh toán VNPay...", Toast.LENGTH_LONG).show();
            
            // Finish activity để khi quay lại sẽ không bị duplicate
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi mở trang thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Format date from yyyy-MM-dd to dd/MM/yyyy
     */
    private String formatDateForDisplay(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    /**
     * Format currency
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return String.format(Locale.US, "%,d VND", amount.intValue());
    }
    
    /**
     * Show loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPayment.setEnabled(!show);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Simple TextWatcher implementation
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
