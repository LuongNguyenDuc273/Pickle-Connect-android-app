package com.datn06.pickleconnect.Event;

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
import com.datn06.pickleconnect.Model.EventDetailDTO;
import com.datn06.pickleconnect.R;
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
        Log.d(TAG, "API Service initialized for port 9003 (EventDetail)");
    }

    private void getEventIdFromIntent() {
        if (getIntent() != null) {
            eventId = getIntent().getStringExtra("eventId");
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
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        // TODO: Implement event registration API call
        Toast.makeText(this,
                "Đăng ký " + quantity + " vé cho sự kiện: " + currentEvent.getEventName(),
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "Register event - EventId: " + eventId + ", Quantity: " + quantity + ", Note: " + note);

        // Sau khi đăng ký thành công:
        // finish();
    }
}