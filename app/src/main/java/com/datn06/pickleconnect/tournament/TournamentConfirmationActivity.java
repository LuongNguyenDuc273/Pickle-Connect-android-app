package com.datn06.pickleconnect.tournament;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.API.TournamentApiService;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.PaymentUrlResponse;
import com.datn06.pickleconnect.Models.Tournament.*;
import com.datn06.pickleconnect.PaymentResultActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.SharedPrefManager;
import com.datn06.pickleconnect.Utils.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TournamentConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "TournamentConfirm";

    // Views - Tournament Info
    private TextView tvTournamentName;
    private TextView tvTournamentDate;
    private TextView tvTournamentLocation;

    // Views - Player Info
    private TextView tvPlayerName;
    private TextView tvPlayerPhone;
    private TextView tvPlayerGender;
    private TextView tvRegistrationFee;

    // Views - Total & Confirm
    private TextView tvTotalAmount;
    private Button btnConfirm;
    private ImageView btnBack;
    private ProgressBar progressBar;

    // API
    private TournamentApiService tournamentApiService;
    private TokenManager tokenManager;
    private SharedPrefManager sharedPrefManager;

    // Data
    private String tournamentId;
    private String tournamentDetailId;
    private String matchTypeId;
    private String matchTypeName;
    private String playerName;
    private String playerPhone;
    private String playerEmail;
    private String playerGender;
    private String playerDateOfBirth;
    private String dynamicFormDataJson;
    private double registrationFee;
    private String orderId; // ✅ From tourney-reg API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_confirmation);

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║       TournamentConfirmationActivity onCreate()            ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        initViews();
        initApi();
        loadDataFromIntent();
        displayData();
        setupListeners();
    }

    private void initViews() {
        Log.d(TAG, "▶ initViews()");

        // Tournament Info
        tvTournamentName = findViewById(R.id.tvTournamentName);
        tvTournamentDate = findViewById(R.id.tvTournamentDate);
        tvTournamentLocation = findViewById(R.id.tvTournamentLocation);

        // Player Info
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvPlayerPhone = findViewById(R.id.tvPlayerPhone);
        tvPlayerGender = findViewById(R.id.tvPlayerGender);
        tvRegistrationFee = findViewById(R.id.tvRegistrationFee);

        // Total & Confirm
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        Log.d(TAG, "  ✓ Views initialized");
    }

    private void initApi() {
        Log.d(TAG, "▶ initApi()");

        tokenManager = TokenManager.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        tournamentApiService = ApiClient.createService(
                ServiceHost.TOURNAMENT_SERVICE,
                TournamentApiService.class
        );

        Log.d(TAG, "  ✓ API initialized");
    }

    private void loadDataFromIntent() {
        Log.d(TAG, "▶ loadDataFromIntent()");

        // Tournament Info
        tournamentId = getIntent().getStringExtra("tournamentId");
        tournamentDetailId = getIntent().getStringExtra("tournamentDetailId");
        String tournamentName = getIntent().getStringExtra("tournamentName");
        String tournamentDate = getIntent().getStringExtra("tournamentDate");
        String tournamentLocation = getIntent().getStringExtra("tournamentLocation");

        // Match Type Info
        matchTypeId = getIntent().getStringExtra("matchTypeId");
        matchTypeName = getIntent().getStringExtra("matchTypeName");

        // Registration Fee
        String feeString = getIntent().getStringExtra("registrationFee");
        try {
            registrationFee = Double.parseDouble(feeString != null ? feeString : "0");
        } catch (NumberFormatException e) {
            registrationFee = 0;
            Log.e(TAG, "  ✗ Error parsing registration fee", e);
        }

        // Player Info
        playerName = getIntent().getStringExtra("playerName");
        playerPhone = getIntent().getStringExtra("playerPhone");
        playerEmail = getIntent().getStringExtra("playerEmail");
        playerGender = getIntent().getStringExtra("playerGender");
        playerDateOfBirth = getIntent().getStringExtra("playerDateOfBirth");

        // Dynamic Form Data
        dynamicFormDataJson = getIntent().getStringExtra("dynamicFormData");

        // Registration data from API
        orderId = getIntent().getStringExtra("orderId"); // ✅ IMPORTANT

        Log.d(TAG, "  Tournament ID: " + tournamentId);
        Log.d(TAG, "  Order ID: " + orderId);
        Log.d(TAG, "  Tournament Detail ID: " + tournamentDetailId);
        Log.d(TAG, "  Tournament Name: " + tournamentName);
        Log.d(TAG, "  Match Type: " + matchTypeName);
        Log.d(TAG, "  Match Type ID: " + matchTypeId);
        Log.d(TAG, "  Player Name: " + playerName);
        Log.d(TAG, "  Player DOB: " + playerDateOfBirth);
        Log.d(TAG, "  Registration Fee: " + registrationFee);
        Log.d(TAG, "  Dynamic Form Data: " + (dynamicFormDataJson != null ? "Present" : "Null"));
    }

    private void displayData() {
        Log.d(TAG, "▶ displayData()");

        // Tournament Info
        String tournamentName = getIntent().getStringExtra("tournamentName");
        String tournamentDate = getIntent().getStringExtra("tournamentDate");
        String tournamentLocation = getIntent().getStringExtra("tournamentLocation");

        tvTournamentName.setText(tournamentName != null ? tournamentName : "Giải đấu");
        tvTournamentDate.setText(tournamentDate != null ? tournamentDate : "Đang cập nhật");
        tvTournamentLocation.setText(tournamentLocation != null ? tournamentLocation : "Đang cập nhật");

        // Player Info
        tvPlayerName.setText(playerName != null ? playerName : "N/A");
        tvPlayerPhone.setText(playerPhone != null ? playerPhone : "N/A");
        tvPlayerGender.setText(playerGender != null ? playerGender : "N/A");

        // Registration Fee
        String formattedFee = formatCurrency(registrationFee);
        tvRegistrationFee.setText(formattedFee);

        // Total Amount (same as registration fee for now)
        tvTotalAmount.setText(formatCurrencyWithoutSymbol(registrationFee));

        Log.d(TAG, "  ✓ Data displayed successfully");

        // Log dynamic form data
        if (dynamicFormDataJson != null) {
            logDynamicFormData();
        }
    }

    private void logDynamicFormData() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║            Dynamic Form Data Received                      ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        try {
            JSONArray jsonArray = new JSONArray(dynamicFormDataJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject fieldObj = jsonArray.getJSONObject(i);
                String label = fieldObj.optString("label", "");
                String value = fieldObj.optString("value", "");
                Log.d(TAG, "  " + label + ": " + value);
            }
        } catch (JSONException e) {
            Log.e(TAG, "  ✗ Error parsing dynamic form data", e);
        }
    }

    private void setupListeners() {
        Log.d(TAG, "▶ setupListeners()");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "  Back button clicked");
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            Log.d(TAG, "  Confirm button clicked");
            handleConfirm();
        });
    }

    private void handleConfirm() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                 handleConfirm() START                      ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        if (orderId == null || orderId.isEmpty()) {
            showError("Lỗi: Không tìm thấy mã đơn hàng");
            Log.e(TAG, "  ✗ orderId is null or empty");
            return;
        }

        Log.d(TAG, "  Order ID: " + orderId);
        Log.d(TAG, "  Registration Fee: " + registrationFee);

        // Call tourney-reg-init API to get payment URL
        callTourneyRegInitApi();
    }

    /**
     * Call tourney-reg-init API
     * - Get payment URL from VNPay
     * - Open browser to payment page
     */
    private void callTourneyRegInitApi() {
        Log.d(TAG, "▶ callTourneyRegInitApi()");
        Log.d(TAG, "  orderId: " + orderId);

        showLoading(true);

        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showError("Không tìm thấy thông tin người dùng");
            showLoading(false);
            return;
        }

        TourneyRegInitRequest request = new TourneyRegInitRequest();
        request.setUserId(userId);
        request.setOrderId(orderId);
        request.setTotalAmount(String.valueOf((int) registrationFee));
        request.setPaymentMethod("VNPGW");
        request.setOrderDescription("Thanh toán đăng ký giải đấu " + tournamentId);

        Call<BaseResponse<PaymentUrlResponse>> call = tournamentApiService.initTournamentPayment(request);

        call.enqueue(new Callback<BaseResponse<PaymentUrlResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<PaymentUrlResponse>> call,
                                   Response<BaseResponse<PaymentUrlResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PaymentUrlResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        PaymentUrlResponse data = baseResponse.getData();
                        String paymentUrl = data.getPaymentUrl();

                        Log.d(TAG, "  ✓ Payment URL received");
                        Log.d(TAG, "  Payment URL: " + paymentUrl);

                        // Save orderId to SharedPreferences (for PaymentResultActivity)
                        sharedPrefManager.saveBookingFacilityId(orderId);

                        // Open payment URL in browser
                        openPaymentUrl(paymentUrl);
                    } else {
                        String errorMsg = baseResponse.getMessage();
                        Log.e(TAG, "  ✗ Get payment URL failed: " + errorMsg);
                        showError(errorMsg != null ? errorMsg : "Không thể tạo link thanh toán");
                    }
                } else {
                    Log.e(TAG, "  ✗ Response unsuccessful: " + response.code());
                    showError("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PaymentUrlResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "  ✗ API call failed", t);
                showError("Lỗi kết nối: " + t.getMessage());
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
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);

            Toast.makeText(this, "Đang chuyển đến trang thanh toán VNPay...", Toast.LENGTH_LONG).show();

            // Finish activity
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error opening payment URL", e);
            Toast.makeText(this, "Lỗi mở trang thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnConfirm.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Format currency with VND symbol
     */
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    /**
     * Format currency without symbol (for total amount)
     */
    private String formatCurrencyWithoutSymbol(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}