package com.datn06.pickleconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentResultActivity extends AppCompatActivity {
    private static final String TAG = "PaymentResult";
    
    private ImageView ivResultIcon;
    private TextView tvResultTitle, tvResultMessage;
    private TextView tvBookingId, tvTotalAmount, tvPaymentStatus;
    private Button btnViewBooking, btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        initViews();
        handleDeepLink(getIntent());
        setupClickListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void initViews() {
        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        btnViewBooking = findViewById(R.id.btnViewBooking);
        btnBackHome = findViewById(R.id.btnBackHome);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            Log.e(TAG, "No deep link data received");
            showFailureResult();
            return;
        }

        String path = data.getPath();
        Log.d(TAG, "Deep link path: " + path);
        Log.d(TAG, "Deep link full URI: " + data.toString());

        if (path != null) {
            if (path.contains("success")) {
                // Extract query parameters
                String txnRef = data.getQueryParameter("txnRef");
                String amount = data.getQueryParameter("amount");
                String orderInfo = data.getQueryParameter("orderInfo");
                
                Log.d(TAG, "Success params - txnRef: " + txnRef + ", amount: " + amount + ", orderInfo: " + orderInfo);
                showSuccessResult(txnRef, amount, orderInfo);
            } else if (path.contains("fail")) {
                showFailureResult();
            } else if (path.contains("cancel")) {
                showCancelResult();
            } else {
                Log.w(TAG, "Unknown deep link path: " + path);
                showFailureResult();
            }
        }
    }

    private void showSuccessResult(String txnRef, String amount, String orderInfo) {
        ivResultIcon.setImageResource(R.drawable.ic_success);
        tvResultTitle.setText("Đặt sân thành công!");
        tvResultTitle.setTextColor(getColor(R.color.success_green));
        tvResultMessage.setText("Vui lòng xuất trình phiếu đặt sân tại sân đã đặt để nhận sân");
        
        // Display booking ID (txnRef)
        if (txnRef != null && !txnRef.isEmpty()) {
            tvBookingId.setText("Mã phiếu: SA" + txnRef);
        } else {
            tvBookingId.setText("Mã phiếu: Đang cập nhật...");
        }
        
        // Display total amount
        if (amount != null && !amount.isEmpty()) {
            try {
                long amountValue = Long.parseLong(amount);
                tvTotalAmount.setText(String.format("%,d VNĐ", amountValue));
            } catch (NumberFormatException e) {
                tvTotalAmount.setText("Đang cập nhật...");
            }
        } else {
            tvTotalAmount.setText("Đang cập nhật...");
        }
        
        tvPaymentStatus.setText("Đã thanh toán");
        tvPaymentStatus.setTextColor(getColor(R.color.success_green));
    }

    private void showFailureResult() {
        ivResultIcon.setImageResource(R.drawable.ic_error);
        tvResultTitle.setText("Thanh toán thất bại!");
        tvResultTitle.setTextColor(getColor(R.color.error_red));
        tvResultMessage.setText("Đã có lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại.");
        tvPaymentStatus.setText("Thất bại");
        tvPaymentStatus.setTextColor(getColor(R.color.error_red));
        btnViewBooking.setEnabled(false);
    }

    private void showCancelResult() {
        ivResultIcon.setImageResource(R.drawable.ic_cancel);
        tvResultTitle.setText("Đã hủy thanh toán");
        tvResultTitle.setTextColor(getColor(R.color.warning_orange));
        tvResultMessage.setText("Bạn đã hủy giao dịch thanh toán.");
        tvPaymentStatus.setText("Đã hủy");
        tvPaymentStatus.setTextColor(getColor(R.color.warning_orange));
        btnViewBooking.setEnabled(false);
    }

    private void setupClickListeners() {
        btnViewBooking.setOnClickListener(v -> {
            // Call save-facility API
            saveFacilityToUser();
        });

        btnBackHome.setOnClickListener(v -> {
            // Navigate to home screen
            navigateToHome();
        });
    }
    
    private void saveFacilityToUser() {
        try {
            com.datn06.pickleconnect.Utils.SharedPrefManager prefManager = 
                com.datn06.pickleconnect.Utils.SharedPrefManager.getInstance(this);
            
            String facilityIdStr = prefManager.getBookingFacilityId();
            String userIdStr = prefManager.getUserId();
            
            if (facilityIdStr == null || userIdStr == null) {
                android.widget.Toast.makeText(this, "Không tìm thấy thông tin cơ sở hoặc người dùng", 
                    android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            Long facilityId = Long.parseLong(facilityIdStr);
            Long userId = Long.parseLong(userIdStr);
            
            com.datn06.pickleconnect.API.CourtApiService.SaveFacilityUserRequest request = 
                new com.datn06.pickleconnect.API.CourtApiService.SaveFacilityUserRequest(userId, facilityId);
            
            com.datn06.pickleconnect.API.CourtApiService courtService = 
                com.datn06.pickleconnect.API.ApiClient.createService(
                    com.datn06.pickleconnect.API.ServiceHost.COURT_SERVICE,
                    com.datn06.pickleconnect.API.CourtApiService.class
                );
            
            courtService.saveFacilityUser(request).enqueue(new retrofit2.Callback<com.datn06.pickleconnect.Common.BaseResponse<String>>() {
                @Override
                public void onResponse(retrofit2.Call<com.datn06.pickleconnect.Common.BaseResponse<String>> call,
                                     retrofit2.Response<com.datn06.pickleconnect.Common.BaseResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        android.widget.Toast.makeText(PaymentResultActivity.this, 
                            "Đã lưu sân vào danh sách yêu thích!", android.widget.Toast.LENGTH_SHORT).show();
                        btnViewBooking.setEnabled(false);
                        btnViewBooking.setText("Đã lưu sân");
                    } else {
                        android.widget.Toast.makeText(PaymentResultActivity.this, 
                            "Không thể lưu sân. Vui lòng thử lại sau!", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<com.datn06.pickleconnect.Common.BaseResponse<String>> call, Throwable t) {
                    Log.e(TAG, "Failed to save facility", t);
                    android.widget.Toast.makeText(PaymentResultActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error saving facility", e);
            android.widget.Toast.makeText(this, "Đã có lỗi xảy ra", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToHome() {
        try {
            Intent intent = new Intent(this, Class.forName("com.datn06.pickleconnect.Home.HomeActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "HomeActivity not found", e);
            // Fallback to MainActivity if HomeActivity doesn't exist
            Intent intent = new Intent(this, com.datn06.pickleconnect.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
