# 🎨 Booking Flow - Android App Design

## 📱 Màn hình 1: Chọn sân và thời gian (`FieldAvailabilityActivity`)

### UI Components:
```xml
- Date Picker (chọn ngày)
- Spinner/Dropdown (chọn cơ sở)
- RecyclerView (danh sách sân và slots)
  - Card mỗi sân
    - Tên sân
    - Grid layout các slot thời gian
    - Mỗi slot: [giờ][giá][checkbox]
    - Slot đã book: disabled + màu xám
    - Slot available: enabled + màu xanh
- TextView: Tổng giờ, Tổng tiền
- Button: "Tiếp tục"
```

### Business Logic:
```java
// 1. Load danh sách sân
void loadFieldAvailability() {
    String date = selectedDate.format("yyyy-MM-dd");
    BigInteger facilityId = selectedFacility.getId();
    
    CourtApiService courtService = ApiClient.createService(
        ServiceHost.COURT_SERVICE, 
        CourtApiService.class
    );
    
    courtService.getFieldAvailability(userId, facilityId, date)
        .enqueue(new Callback<FieldBookingResponse>() {
            @Override
            public void onResponse(Call call, Response<BaseResponse<FieldBookingResponse>> response) {
                if (response.isSuccessful() && "00".equals(response.body().getCode())) {
                    displayFields(response.body().getData());
                }
            }
        });
}

// 2. User chọn slots
List<SelectedSlotDTO> selectedSlots = new ArrayList<>();

void onSlotSelected(Slot slot, boolean isChecked) {
    if (isChecked) {
        selectedSlots.add(new SelectedSlotDTO(
            slot.getSlotId(),
            slot.getFieldId(),
            slot.getPrice()
        ));
    } else {
        selectedSlots.removeIf(s -> s.getSlotId().equals(slot.getSlotId()));
    }
    
    updateTotalPrice();
    updateTotalHours();
}

// 3. Tính tổng
void updateTotalPrice() {
    BigDecimal total = selectedSlots.stream()
        .map(SelectedSlotDTO::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    tvTotalPrice.setText(formatCurrency(total));
}

// 4. Tiếp tục
void onContinueClicked() {
    if (selectedSlots.isEmpty()) {
        showError("Vui lòng chọn ít nhất một slot");
        return;
    }
    
    // Chuyển sang màn xác nhận
    Intent intent = new Intent(this, BookingConfirmActivity.class);
    intent.putExtra("selectedSlots", new Gson().toJson(selectedSlots));
    intent.putExtra("facilityId", facilityId);
    intent.putExtra("bookingDate", bookingDate);
    intent.putExtra("totalAmount", totalAmount);
    intent.putExtra("totalHours", selectedSlots.size());
    startActivity(intent);
}
```

---

## 📱 Màn hình 2: Xác nhận đặt sân (`BookingConfirmActivity`)

### UI Components:
```xml
<ScrollView>
    <LinearLayout orientation="vertical">
        
        <!-- Thông tin cơ sở -->
        <CardView>
            <TextView>Tên cơ sở</TextView>
            <TextView>Địa chỉ</TextView>
            <TextView>Ngày đặt: 20/11/2025</TextView>
        </CardView>
        
        <!-- Danh sách slots đã chọn -->
        <CardView>
            <TextView>Chi tiết đặt sân</TextView>
            <RecyclerView>
                <!-- Item: Sân 1 - 08:00-09:00 - 100,000đ -->
            </RecyclerView>
        </CardView>
        
        <!-- Thông tin người đặt -->
        <CardView>
            <TextInputLayout hint="Họ tên">
                <TextInputEditText id="etFullName" />
            </TextInputLayout>
            
            <TextInputLayout hint="Số điện thoại">
                <TextInputEditText id="etPhone" />
            </TextInputLayout>
            
            <TextInputLayout hint="Email">
                <TextInputEditText id="etEmail" />
            </TextInputLayout>
        </CardView>
        
        <!-- Tổng tiền -->
        <CardView>
            <TextView>Tổng giờ: 3 giờ</TextView>
            <TextView>Tổng tiền: 300,000đ</TextView>
        </CardView>
        
        <!-- Phương thức thanh toán -->
        <CardView>
            <RadioGroup id="rgPaymentMethod">
                <RadioButton text="VNPay" value="VNPPGW" checked />
                <!-- Add more methods later -->
            </RadioGroup>
        </CardView>
        
        <!-- Actions -->
        <Button id="btnConfirmBooking" text="Xác nhận đặt sân" />
        <Button id="btnCancel" text="Hủy" style="outlined" />
    </LinearLayout>
</ScrollView>
```

### Business Logic:
```java
public class BookingConfirmActivity extends AppCompatActivity {
    
    private List<SelectedSlotDTO> selectedSlots;
    private String bookingDate;
    private BigInteger facilityId;
    private BigDecimal totalAmount;
    private int totalHours;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Parse intent data
        String slotsJson = getIntent().getStringExtra("selectedSlots");
        selectedSlots = new Gson().fromJson(slotsJson, 
            new TypeToken<List<SelectedSlotDTO>>(){}.getType());
        
        facilityId = (BigInteger) getIntent().getSerializableExtra("facilityId");
        bookingDate = getIntent().getStringExtra("bookingDate");
        totalAmount = (BigDecimal) getIntent().getSerializableExtra("totalAmount");
        totalHours = getIntent().getIntExtra("totalHours", 0);
        
        // Pre-fill user info from profile
        loadUserProfile();
        
        // Display booking summary
        displayBookingSummary();
    }
    
    private void loadUserProfile() {
        // Get from SharedPreferences or API
        String userName = SharedPrefManager.getUserName();
        String userEmail = SharedPrefManager.getUserEmail();
        String userPhone = SharedPrefManager.getUserPhone();
        
        etFullName.setText(userName);
        etEmail.setText(userEmail);
        etPhone.setText(userPhone);
    }
    
    private void displayBookingSummary() {
        // Show selected slots in RecyclerView
        adapter = new BookingSlotAdapter(selectedSlots);
        rvSelectedSlots.setAdapter(adapter);
        
        // Show totals
        tvTotalHours.setText(totalHours + " giờ");
        tvTotalAmount.setText(formatCurrency(totalAmount));
    }
    
    // Xác nhận đặt sân
    private void confirmBooking() {
        // Validate inputs
        String userName = etFullName.getText().toString().trim();
        String userEmail = etEmail.getText().toString().trim();
        String userPhone = etPhone.getText().toString().trim();
        
        if (!validateInputs(userName, userEmail, userPhone)) {
            return;
        }
        
        // Get selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        String paymentMethod = "VNPPGW"; // Default VNPay
        
        // Show loading
        showLoading(true);
        
        // Create booking request
        CreateBookingCourtRequest request = CreateBookingCourtRequest.builder()
            .facilityId(facilityId.longValue())
            .userId(SharedPrefManager.getUserId())
            .userName(userName)
            .userEmail(userEmail)
            .phoneNumber(userPhone)
            .paymentMethodCode(paymentMethod)
            .totalAmount(totalAmount)
            .totalHours(totalHours)
            .bookingDate(bookingDate)
            .selectedSlots(selectedSlots)
            .orderDescription("Đặt sân pickle ball ngày " + bookingDate)
            .build();
        
        // Call API
        CourtApiService courtService = ApiClient.createService(
            ServiceHost.COURT_SERVICE, 
            CourtApiService.class
        );
        
        courtService.createBooking(
            SharedPrefManager.getUserId().toString(), 
            request
        ).enqueue(new Callback<BaseResponse<PaymentUrlResponse>>() {
            @Override
            public void onResponse(Call call, Response<BaseResponse<PaymentUrlResponse>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PaymentUrlResponse> baseResponse = response.body();
                    
                    if ("00".equals(baseResponse.getCode())) {
                        // Success - Open payment URL
                        String paymentUrl = baseResponse.getData().getPaymentUrl();
                        openPaymentWebView(paymentUrl);
                    } 
                    else if ("01".equals(baseResponse.getCode())) {
                        // Slot unavailable
                        showError("Slot đã được đặt bởi người khác. Vui lòng chọn lại!");
                        // Go back to slot selection
                        finish();
                    }
                    else {
                        showError(baseResponse.getMessage());
                    }
                } else {
                    showError("Đặt sân thất bại. Vui lòng thử lại!");
                }
            }
            
            @Override
            public void onFailure(Call call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    private void openPaymentWebView(String paymentUrl) {
        Intent intent = new Intent(this, PaymentWebViewActivity.class);
        intent.putExtra("paymentUrl", paymentUrl);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }
}
```

---

## 📱 Màn hình 3: Thanh toán (`PaymentWebViewActivity`)

### UI Components:
```xml
<LinearLayout orientation="vertical">
    <ProgressBar id="progressBar" />
    <WebView id="webView" />
</LinearLayout>
```

### Business Logic:
```java
public class PaymentWebViewActivity extends AppCompatActivity {
    
    private WebView webView;
    private String paymentUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        paymentUrl = getIntent().getStringExtra("paymentUrl");
        
        setupWebView();
        webView.loadUrl(paymentUrl);
    }
    
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                
                // Check if callback URL
                if (url.startsWith("pickleconnect://payment/result")) {
                    handlePaymentCallback(url);
                    return true;
                }
                
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
    }
    
    private void handlePaymentCallback(String callbackUrl) {
        // Parse callback URL
        Uri uri = Uri.parse(callbackUrl);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        String txnRef = uri.getQueryParameter("vnp_TxnRef");
        String amount = uri.getQueryParameter("vnp_Amount");
        
        // Navigate to result screen
        Intent intent = new Intent(this, PaymentResultActivity.class);
        intent.putExtra("responseCode", responseCode);
        intent.putExtra("txnRef", txnRef);
        intent.putExtra("amount", amount);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Hủy thanh toán?")
            .setMessage("Bạn có chắc muốn hủy thanh toán?")
            .setPositiveButton("Có", (dialog, which) -> {
                setResult(RESULT_CANCELED);
                finish();
            })
            .setNegativeButton("Không", null)
            .show();
    }
}
```

---

## 📱 Màn hình 4: Kết quả thanh toán (`PaymentResultActivity`)

### UI Components:
```xml
<LinearLayout orientation="vertical" gravity="center">
    
    <!-- Success/Failed Icon -->
    <ImageView id="ivResult" 
        src="@drawable/ic_success" or "@drawable/ic_failed" />
    
    <!-- Status Text -->
    <TextView id="tvStatus" 
        text="Đặt sân thành công!" 
        textSize="24sp" 
        textStyle="bold" />
    
    <!-- Booking Info -->
    <CardView>
        <TextView>Mã đặt sân: BOOK-xxx</TextView>
        <TextView>Ngày đặt: 20/11/2025</TextView>
        <TextView>Tổng tiền: 300,000đ</TextView>
    </CardView>
    
    <!-- Actions -->
    <Button id="btnViewBooking" text="Xem chi tiết" />
    <Button id="btnBackHome" text="Về trang chủ" />
</LinearLayout>
```

### Business Logic:
```java
public class PaymentResultActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String responseCode = getIntent().getStringExtra("responseCode");
        String txnRef = getIntent().getStringExtra("txnRef");
        
        if ("00".equals(responseCode)) {
            // Success
            showSuccessUI();
            // Verify with backend (recommended)
            verifyPaymentWithBackend(txnRef);
        } else {
            // Failed
            showFailedUI(responseCode);
        }
    }
    
    private void showSuccessUI() {
        ivResult.setImageResource(R.drawable.ic_success);
        tvStatus.setText("Đặt sân thành công!");
        tvStatus.setTextColor(Color.GREEN);
    }
    
    private void showFailedUI(String responseCode) {
        ivResult.setImageResource(R.drawable.ic_failed);
        tvStatus.setText("Thanh toán thất bại!");
        tvStatus.setTextColor(Color.RED);
        
        // Show reason
        String reason = getFailureReason(responseCode);
        tvReason.setText(reason);
    }
    
    private void verifyPaymentWithBackend(String txnRef) {
        // Call API to get booking details
        CourtApiService courtService = ApiClient.createService(
            ServiceHost.COURT_SERVICE, 
            CourtApiService.class
        );
        
        // Option 1: Get by transaction ref
        // courtService.getBookingByTxnRef(txnRef).enqueue(...);
        
        // Option 2: Get latest booking from history
        courtService.getBookingHistory(
            userId, 
            "SUCCESS",  // status
            LocalDate.now().minusDays(1),
            LocalDate.now()
        ).enqueue(new Callback<BaseResponse<List<BookingHistoryDTO>>>() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    List<BookingHistoryDTO> bookings = response.body().getData();
                    if (!bookings.isEmpty()) {
                        BookingHistoryDTO latestBooking = bookings.get(0);
                        displayBookingInfo(latestBooking);
                    }
                }
            }
        });
    }
    
    private void displayBookingInfo(BookingHistoryDTO booking) {
        tvBookingCode.setText("Mã: " + booking.getBookingCode());
        tvBookingDate.setText("Ngày: " + formatDate(booking.getBookingDate()));
        tvTotalAmount.setText("Tổng: " + formatCurrency(booking.getTotalPrice()));
    }
    
    private String getFailureReason(String code) {
        switch (code) {
            case "07": return "Giao dịch bị nghi ngờ";
            case "09": return "Thẻ chưa đăng ký dịch vụ";
            case "10": return "Xác thực thông tin thẻ thất bại";
            case "11": return "Hết hạn chờ thanh toán";
            case "12": return "Thẻ bị khóa";
            case "13": return "Sai mật khẩu OTP";
            case "24": return "Giao dịch bị hủy";
            case "51": return "Tài khoản không đủ số dư";
            default: return "Lỗi không xác định";
        }
    }
}
```

---

## 📦 Tạo CourtApiService.java

```java
package com.datn06.pickleconnect.API;

import com.datn06.pickleconnect.Booking.*;
import com.vnpay.common.BaseResponse;
import retrofit2.Call;
import retrofit2.http.*;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Court API Service - Booking & Court Management
 * Base URL: http://10.0.2.2:9008/ (pickle-connect-court)
 */
public interface CourtApiService {

    /**
     * Get field availability for a facility on a specific date
     */
    @GET("api/v1/booking/fields/availability")
    Call<BaseResponse<FieldBookingResponse>> getFieldAvailability(
        @Header("X-Userinfo") String userId,
        @Query("facilityId") BigInteger facilityId,
        @Query("bookingDate") String bookingDate  // Format: yyyy-MM-dd
    );

    /**
     * Create a new court booking
     */
    @POST("api/v1/booking/create")
    Call<BaseResponse<PaymentUrlResponse>> createBooking(
        @Header("X-Userinfo") String userId,
        @Body CreateBookingCourtRequest request
    );

    /**
     * Save facility to user's favorites
     */
    @POST("api/v1/booking/save-facility-user")
    Call<BaseResponse<String>> saveFacilityUser(
        @Header("X-Userinfo") String userId,
        @Body SaveFacilityUserRequest request
    );

    /**
     * Get booking history
     */
    @GET("api/v1/booking/history")
    Call<BaseResponse<List<BookingHistoryDTO>>> getBookingHistory(
        @Header("X-Userinfo") String userId,
        @Query("status") String status,
        @Query("fromDate") String fromDate,
        @Query("toDate") String toDate
    );
}
```

---

## 📋 Checklist Implementation

### Phase 1: Data Models
- [ ] Create `FieldBookingResponse.java`
- [ ] Create `SelectedSlotDTO.java`
- [ ] Create `CreateBookingCourtRequest.java`
- [ ] Create `PaymentUrlResponse.java`
- [ ] Create `BookingHistoryDTO.java`
- [ ] Create `SaveFacilityUserRequest.java`

### Phase 2: API Service
- [ ] Create `CourtApiService.java`
- [ ] Add to `ServiceHost` enum (already done ✅)
- [ ] Test API calls with Postman

### Phase 3: UI Screens
- [ ] `FieldAvailabilityActivity` + layout
- [ ] `BookingConfirmActivity` + layout
- [ ] `PaymentWebViewActivity` + layout
- [ ] `PaymentResultActivity` + layout

### Phase 4: Adapters & ViewHolders
- [ ] `FieldSlotAdapter` (for slot grid)
- [ ] `BookingSlotAdapter` (for confirmation list)

### Phase 5: Integration
- [ ] Test full flow: Select → Confirm → Pay → Result
- [ ] Handle edge cases (slot taken, payment failed, etc.)
- [ ] Add deep link handling for callback

---

## 🔐 Security Notes

1. **Never trust client-side only**: Always verify payment on backend via IPN
2. **Use X-Userinfo header**: Backend validates user từ token
3. **Timeout handling**: Slots locked 5-10 phút, cần countdown trong UI
4. **Concurrent booking**: Backend có lock mechanism, nhưng UX nên refresh slots frequently

---

## 🎯 Next Steps

1. Tạo CourtApiService.java (template ở trên)
2. Tạo models (DTO classes)
3. Design layouts cho 4 màn hình
4. Implement từng màn theo thứ tự
5. Test end-to-end flow

Bạn muốn tôi tạo file nào trước? 😊
