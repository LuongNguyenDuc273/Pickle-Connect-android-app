package com.datn06.pickleconnect.Event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.EventDetailDTO;
import com.datn06.pickleconnect.Model.EventRegistrationRequest;
import com.datn06.pickleconnect.Model.EventRegistrationResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.datn06.pickleconnect.Utils.XUserInfoHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    // ✅ ADDED: API Service field
    private ApiService apiService;
    
    // User authentication
    private TokenManager tokenManager;

    // Header
    private ImageView btnBack;

    // Card Thông tin sân
    private TextView tvFacilityName;
    private TextView tvFacilityAddress;

    // Card Thông tin sự kiện
    private TextView tvEventName;
    private TextView tvEventDescription;
    private TextView tvEventDate;
    private TextView tvFields;
    private TextView tvEventTime;
    private TextView tvPrice;
    private TextView tvEventLevel;
    private TextView tvAvailableSlots;

    // Card Đặt vé
    private MaterialCardView cardBooking;
    private MaterialButton btnDecrease;
    private TextView tvQuantity;
    private MaterialButton btnIncrease;
    private TextInputEditText etNote;

    // Bottom bar
    private LinearLayout bottomBar;
    private TextView tvTotalPrice;
    private MaterialButton btnBookNow;

    private String eventId;
    private EventDetailDTO currentEvent;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // ✅ ADDED: Initialize API Service first
        initApiService();

        getEventIdFromIntent();
        initViews();
        setupListeners();

        if (eventId != null) {
            loadEventDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sự kiện", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ✅ ADDED: Initialize API Service with correct port
    private void initApiService() {
        apiService = ApiClient.createService(ServiceHost.API_SERVICE, ApiService.class);
        tokenManager = TokenManager.getInstance(this);
        Log.d(TAG, "API Service initialized for port 9003 (EventDetail)");
    }

    private void getEventIdFromIntent() {
        if (getIntent() != null) {
            // Nhận eventId dưới dạng Long hoặc String
            if (getIntent().hasExtra("eventId")) {
                Object eventIdObj = getIntent().getExtras().get("eventId");
                if (eventIdObj instanceof Long) {
                    eventId = String.valueOf(eventIdObj);
                } else if (eventIdObj instanceof String) {
                    eventId = (String) eventIdObj;
                }
            }
            Log.d(TAG, "Received eventId: " + eventId);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        // Card Thông tin sân
        MaterialCardView cardProductInfo = findViewById(R.id.cardProductInfo);
        LinearLayout productInfoContainer = (LinearLayout) cardProductInfo.getChildAt(0);
        tvFacilityName = findTextViewInRow(productInfoContainer, 1);
        tvFacilityAddress = findTextViewInRow(productInfoContainer, 2);

        // Card Thông tin sự kiện
        MaterialCardView cardEventInfo = findViewById(R.id.cardEventInfo);
        LinearLayout eventInfoContainer = (LinearLayout) cardEventInfo.getChildAt(0);
        tvEventName = findTextViewInRow(eventInfoContainer, 1);
        tvEventDescription = findTextViewInEventDescription(eventInfoContainer, 2);
        tvEventDate = findTextViewInRow(eventInfoContainer, 3);
        tvFields = findTextViewInRow(eventInfoContainer, 4);
        tvEventTime = findTextViewInRow(eventInfoContainer, 5);
        tvPrice = findTextViewInRow(eventInfoContainer, 6);
        tvEventLevel = findTextViewInRow(eventInfoContainer, 7);
        tvAvailableSlots = findTextViewInRow(eventInfoContainer, 8);

        // Card Đặt vé
        cardBooking = findViewById(R.id.cardBooking);
        btnDecrease = findViewById(R.id.btnDecrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        etNote = findViewById(R.id.etNote);

        // Bottom bar
        bottomBar = findViewById(R.id.bottomBar);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnBookNow = findViewById(R.id.btnBookNow);
    }

    // Helper method để tìm TextView trong row có 2 cột
    private TextView findTextViewInRow(LinearLayout container, int rowIndex) {
        try {
            // Bỏ qua title (index 0) và divider (index 1)
            int actualIndex = rowIndex + 1; // +1 vì có divider ở giữa

            LinearLayout row = (LinearLayout) container.getChildAt(actualIndex);
            // TextView thứ 2 trong row (index 1) là data
            return (TextView) row.getChildAt(1);
        } catch (Exception e) {
            Log.e(TAG, "Error finding TextView in row " + rowIndex, e);
            TextView fallback = new TextView(this);
            fallback.setText("-");
            return fallback;
        }
    }

    // Helper method riêng cho description (layout khác)
    private TextView findTextViewInEventDescription(LinearLayout container, int rowIndex) {
        try {
            int actualIndex = rowIndex + 1;
            LinearLayout descSection = (LinearLayout) container.getChildAt(actualIndex);
            // TextView thứ 2 (index 1) là description text
            return (TextView) descSection.getChildAt(1);
        } catch (Exception e) {
            Log.e(TAG, "Error finding description TextView", e);
            TextView fallback = new TextView(this);
            fallback.setText("Không có mô tả");
            return fallback;
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityDisplay();
                updateTotalPrice();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (currentEvent != null && quantity < currentEvent.getAvailableSlots()) {
                quantity++;
                updateQuantityDisplay();
                updateTotalPrice();
            } else {
                Toast.makeText(this, "Đã đạt số lượng tối đa", Toast.LENGTH_SHORT).show();
            }
        });

        btnBookNow.setOnClickListener(v -> {
            if (currentEvent != null && currentEvent.getCanRegister()) {
                registerEvent();
            }
        });
    }

    private void loadEventDetail() {
        btnBookNow.setEnabled(false);
        btnBookNow.setText("Đang tải...");

        // ✅ FIXED: Use the initialized apiService with correct port
        Call<EventDetailResponse> call = apiService.getEventDetail(eventId);

        call.enqueue(new Callback<EventDetailResponse>() {
            @Override
            public void onResponse(Call<EventDetailResponse> call, Response<EventDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventDetailResponse eventResponse = response.body();

                    if (eventResponse.isSuccess()) {
                        currentEvent = eventResponse.getData();
                        displayEventDetail(currentEvent);
                        btnBookNow.setEnabled(true);
                        btnBookNow.setText("Đặt ngay");
                    } else {
                        Toast.makeText(EventDetailActivity.this,
                                "Lỗi: " + eventResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    Toast.makeText(EventDetailActivity.this,
                            "Không thể tải chi tiết sự kiện",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<EventDetailResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(EventDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayEventDetail(EventDetailDTO event) {
        // Thông tin sân
        if (event.getFacility() != null) {
            tvFacilityName.setText(event.getFacility().getFacilityName());
            String fullAddress = event.getFacility().getFullAddress();
            if (fullAddress == null || fullAddress.isEmpty()) {
                fullAddress = String.format("%s, %s, %s, %s",
                        event.getFacility().getStreetAddress(),
                        event.getFacility().getWard(),
                        event.getFacility().getDistrict(),
                        event.getFacility().getProvince());
            }
            tvFacilityAddress.setText(fullAddress);
        }

        // Thông tin sự kiện
        tvEventName.setText(event.getEventName());

        if (event.getEventDescription() != null && !event.getEventDescription().isEmpty()) {
            tvEventDescription.setText(event.getEventDescription());
        } else {
            tvEventDescription.setText("Không có mô tả");
        }

        tvEventDate.setText(event.getEventDate());
        tvFields.setText(event.getFieldsAsString());
        tvEventTime.setText(event.getTimeDisplay());
        tvPrice.setText(event.getPriceDisplay());

        if (event.getEventType() != null) {
            tvEventLevel.setText(event.getEventType());
        } else {
            tvEventLevel.setText("-");
        }

        tvAvailableSlots.setText(String.valueOf(event.getAvailableSlots()));

        // Set màu cho giá
        if (event.isFree()) {
            tvPrice.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvPrice.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // XỬ LÝ HIỂN THỊ DựA TRÊN LOẠI SỰ KIỆN VÀ TRẠNG THÁI
        boolean isFreeEvent = event.isFree();
        boolean hasAvailableSlots = event.getAvailableSlots() > 0;
        boolean canRegister = event.getCanRegister();

        if (isFreeEvent) {
            // SỰ KIỆN MIỄN PHÍ - CHỈ THÔNG BÁO
            cardBooking.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);

        } else if (!hasAvailableSlots || !canRegister) {
            // HẾT VÉ HOẶC KHÔNG NHẬN ĐĂNG KÝ - DISABLE TẤT CẢ
            cardBooking.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);

            // Disable và làm mờ các nút
            btnDecrease.setEnabled(false);
            btnDecrease.setAlpha(0.4f);

            btnIncrease.setEnabled(false);
            btnIncrease.setAlpha(0.4f);

            tvQuantity.setAlpha(0.4f);

            etNote.setEnabled(false);
            etNote.setAlpha(0.5f);

            btnBookNow.setEnabled(false);
            btnBookNow.setAlpha(0.5f);

            if (!hasAvailableSlots) {
                btnBookNow.setText("Hết chỗ");
                tvAvailableSlots.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                btnBookNow.setText("Không nhận đăng ký");
                tvAvailableSlots.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

        } else {
            // CÒN VÉ VÀ NHẬN ĐĂNG KÝ - ENABLE TẤT CẢ
            cardBooking.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);

            btnDecrease.setEnabled(true);
            btnIncrease.setEnabled(true);
            etNote.setEnabled(true);
            btnBookNow.setEnabled(true);
            btnBookNow.setText("Đặt ngay");

            tvAvailableSlots.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

            // Update total price
            updateTotalPrice();
        }
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void updateTotalPrice() {
        if (currentEvent != null && currentEvent.getTicketPrice() != null) {
            BigDecimal totalPrice = currentEvent.getTicketPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            String formattedPrice = String.format("%,d VNĐ", totalPrice.longValue());
            tvTotalPrice.setText(formattedPrice);
        }
    }

    private void registerEvent() {
        // Validate user logged in
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng ký sự kiện", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to login screen
            return;
        }


        // Get user info
        String userId = tokenManager.getUserId();
        String userName = tokenManager.getUsername();
        String fullName = tokenManager.getFullName();
        String userEmail = tokenManager.getEmail();
        String phoneNumber = tokenManager.getPhoneNumber();

        // Log for debugging
        Log.d(TAG, "User info - userId: " + userId + ", userName: " + userName + ", fullName: " + fullName + ", email: " + userEmail + ", phone: " + phoneNumber);

        // Validate required fields
        if (userId == null || userEmail == null || phoneNumber == null) {
            Toast.makeText(this, "Thiếu thông tin người dùng. Vui lòng cập nhật profile", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get note
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        // ✅ Use fullName if available, fallback to userName, then "User"
        String displayName = fullName != null && !fullName.isEmpty() ? fullName : 
                           userName != null && !userName.isEmpty() ? userName : "User";

        // ✅ Calculate total amount
        BigDecimal totalPrice = currentEvent.getTicketPrice()
                .multiply(BigDecimal.valueOf(quantity));

        // Build request
        EventRegistrationRequest request = EventRegistrationRequest.builder()
                .eventId(eventId)
                .userId(Long.parseLong(userId))
                .userName(displayName)
                .userEmail(userEmail)
                .phoneNumber(phoneNumber)
                .quantity(quantity)
                .notes(note)
                .paymentMethodCode("VNPPGW")
                .orderDescription(String.format("Đăng ký sự kiện %s - %d vé", 
                        currentEvent.getEventName(), quantity))
                // ✅ ADDED: New fields for booking creation
                .totalAmount(totalPrice.toString()) // Convert BigDecimal to String
                .totalHours(0) // Event doesn't use hours
                .bookingDate(currentEvent.getEventDate()) // Event date (yyyy-MM-dd format)
                .facilityId(currentEvent.getFacility().getFacilityId().toString())
                .build();

        Log.d(TAG, "Registering event: " + request.toString());

        // Disable button while processing
        btnBookNow.setEnabled(false);
        btnBookNow.setText("Đang xử lý...");

        // Generate X-Userinfo header (Base64-encoded JSON)
//        String xUserinfo = XUserInfoHelper.generateXUserInfo(userName, userId, userEmail);
//        Log.d(TAG, "X-Userinfo: " + xUserinfo);

        // Call API
        Call<BaseResponse<EventRegistrationResponse>> call = apiService.registerEvent(request);
        
        call.enqueue(new Callback<BaseResponse<EventRegistrationResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<EventRegistrationResponse>> call,
                                 Response<BaseResponse<EventRegistrationResponse>> response) {
                btnBookNow.setEnabled(true);
                btnBookNow.setText("Đặt ngay");

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<EventRegistrationResponse> baseResponse = response.body();

                    if (baseResponse.isSuccess()) {
                        EventRegistrationResponse registrationData = baseResponse.getData();
                        
                        if (registrationData != null && registrationData.hasPaymentUrl()) {
                            Log.d(TAG, "Registration successful: " + registrationData.toString());
                            
                            // Open VNPay payment URL
                            openPaymentUrl(registrationData.getPaymentUrl());
                        } else {
                            Toast.makeText(EventDetailActivity.this,
                                    "Lỗi: Không nhận được URL thanh toán",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle error codes from backend
                        String errorMsg = baseResponse.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = baseResponse.getErrorMessage();
                        }
                        Toast.makeText(EventDetailActivity.this,
                                errorMsg,
                                Toast.LENGTH_LONG).show();
                        
                        Log.e(TAG, "Registration failed: " + baseResponse.toString());
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    Toast.makeText(EventDetailActivity.this,
                            "Lỗi: Không thể đăng ký sự kiện (Code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<EventRegistrationResponse>> call, Throwable t) {
                btnBookNow.setEnabled(true);
                btnBookNow.setText("Đặt ngay");
                
                Log.e(TAG, "API call failed", t);
                Toast.makeText(EventDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Open VNPay payment URL in browser
     */
    private void openPaymentUrl(String paymentUrl) {
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không nhận được URL thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "Opening payment URL: " + paymentUrl);
            
            // Open VNPay payment URL in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);

            // Show success message
            Toast.makeText(this, "Đang chuyển đến trang thanh toán VNPay...", Toast.LENGTH_LONG).show();

            // Finish activity để khi quay lại sẽ không bị duplicate
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error opening payment URL", e);
            Toast.makeText(this, "Lỗi mở trang thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}