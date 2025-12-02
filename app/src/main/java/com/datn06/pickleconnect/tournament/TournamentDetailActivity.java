package com.datn06.pickleconnect.tournament;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.API.TournamentApiService;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.Tournament.TourneyDetailRequest;
import com.datn06.pickleconnect.Models.Tournament.TourneyDetailResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TournamentDetailActivity extends AppCompatActivity {

    private static final String TAG = "TournamentDetail";

    // ========== FIX: Thêm constant cho date formats ==========
    // Backend trả về format: "dd-MM-yyyy HH:mm:ss"
    private static final String BACKEND_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    // Format hiển thị: "dd/MM/yyyy"
    private static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";
    // Format để compare date (chỉ lấy ngày, không cần giờ)
    private static final String DATE_ONLY_FORMAT = "dd-MM-yyyy";

    // Views
    private ImageView btnBack;
    private TextView tvTournamentTitle;
    private TextView tvTournamentDate;
    private TextView tvTournamentLocation;
    private TextView tvRegistrationPeriod;
    private Button btnRegister;
    private TextView tvTournamentInfo;
    private TextView tvParticipationConditions;
    private TextView tvPrize;

    // API
    private TournamentApiService tournamentApiService;

    // Data
    private String tournamentId;
    private String currentUserId;
    private TokenManager tokenManager;
    private TourneyDetailResponse tournamentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║         TournamentDetailActivity onCreate()                ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        setContentView(R.layout.activity_tournament_detail);

        initViews();
        initData();
        setupListeners();

        if (tournamentId != null && !tournamentId.isEmpty()) {
            loadTournamentDetail();
        } else {
            Log.e(TAG, "ERROR: Tournament ID is null!");
            Toast.makeText(this, "Không tìm thấy giải đấu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        Log.d(TAG, "▶ initViews()");

        btnBack = findViewById(R.id.btnBack);
        tvTournamentTitle = findViewById(R.id.tvTournamentTitle);
        tvTournamentDate = findViewById(R.id.tvTournamentDate);
        tvTournamentLocation = findViewById(R.id.tvTournamentLocation);
        tvRegistrationPeriod = findViewById(R.id.tvRegistrationPeriod);
        btnRegister = findViewById(R.id.btnRegister);
        tvTournamentInfo = findViewById(R.id.tvTournamentInfo);
        tvParticipationConditions = findViewById(R.id.tvParticipationConditions);
        tvPrize = findViewById(R.id.tvPrize);

        Log.d(TAG, "  Views initialized successfully");
    }

    private void initData() {
        Log.d(TAG, "▶ initData()");

        tournamentId = getIntent().getStringExtra("tournamentId");
        Log.d(TAG, "  Tournament ID from Intent: " + tournamentId);

        tokenManager = TokenManager.getInstance(this);
        currentUserId = tokenManager.getUserId();
        Log.d(TAG, "  User ID: " + currentUserId);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "  ERROR: User ID is null or empty!");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupApiService();
    }

    private void setupApiService() {
        Log.d(TAG, "▶ setupApiService()");

        tournamentApiService = ApiClient.createService(
                ServiceHost.TOURNAMENT_SERVICE,
                TournamentApiService.class
        );

        Log.d(TAG, "  API Service initialized: " + (tournamentApiService != null));
    }

    private void setupListeners() {
        Log.d(TAG, "▶ setupListeners()");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            handleRegisterClick();
        });
    }

    private void loadTournamentDetail() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║            loadTournamentDetail() START                    ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang tải...");

        TourneyDetailRequest request = new TourneyDetailRequest(currentUserId, tournamentId, "1");

        Call<BaseResponse<TourneyDetailResponse>> call =
                tournamentApiService.getTourneyDetail(request);

        call.enqueue(new Callback<BaseResponse<TourneyDetailResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<TourneyDetailResponse>> call,
                                   Response<BaseResponse<TourneyDetailResponse>> response) {
                Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.d(TAG, "║              API RESPONSE RECEIVED                         ║");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<TourneyDetailResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        tournamentDetail = baseResponse.getData();

                        if (tournamentDetail != null) {
                            Log.d(TAG, "  ✓ Tournament detail received");
                            Log.d(TAG, "    Start Date RAW: " + tournamentDetail.getTournamentStartDate());
                            Log.d(TAG, "    End Date RAW: " + tournamentDetail.getTournamentEndDate());
                            Log.d(TAG, "    Reg Start RAW: " + tournamentDetail.getRegStartDate());
                            Log.d(TAG, "    Reg End RAW: " + tournamentDetail.getRegEndDate());

                            updateUI(tournamentDetail);
                        } else {
                            showError("Không tìm thấy thông tin giải đấu");
                        }
                    } else {
                        showError("Lỗi: " + baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải thông tin giải đấu");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<TourneyDetailResponse>> call, Throwable t) {
                Log.e(TAG, "API CALL FAILED: " + t.getMessage());
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void updateUI(TourneyDetailResponse detail) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                    updateUI()                              ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        // 1. Title
        tvTournamentTitle.setText(detail.getTournamentName());
        Log.d(TAG, "  ✓ Title set: " + detail.getTournamentName());

        // 2. Date - FIX: Dùng format mới
        String dateText = formatDateRange(
                detail.getTournamentStartDate(),
                detail.getTournamentEndDate()
        );
        tvTournamentDate.setText("Thời gian: " + dateText);
        Log.d(TAG, "  ✓ Date set: " + dateText);

        // 3. Location
        String location = detail.getTournamentLocation();
        if (location != null && !location.isEmpty()) {
            tvTournamentLocation.setText("Địa điểm: " + location);
        } else {
            tvTournamentLocation.setText("Địa điểm: Đang cập nhật");
        }
        Log.d(TAG, "  ✓ Location set: " + location);

        // 4. Registration Period - FIX: Dùng format mới
        String regPeriod = formatRegistrationPeriod(
                detail.getRegStartDate(),
                detail.getRegEndDate()
        );
        tvRegistrationPeriod.setText(regPeriod);
        Log.d(TAG, "  ✓ Registration period set: " + regPeriod);

        // 5. Update Button
        updateRegisterButton(detail);

        // 6. Description
        String description = detail.getTournamentDescription();
        if (description != null && !description.isEmpty()) {
            // Remove HTML tags if needed
            String cleanDescription = description
                    .replaceAll("<[^>]*>", "")
                    .replaceAll("&nbsp;", " ")
                    .trim();
            tvTournamentInfo.setText(cleanDescription);
            Log.d(TAG, "  ✓ Description set from API");
        } else {
            tvTournamentInfo.setText("Thông tin chi tiết về giải đấu sẽ được cập nhật sớm.");
            Log.d(TAG, "  ⚠ Description empty, using placeholder");
        }

        // 7. Participation Conditions - DYNAMIC FROM API
        String participationConditions = detail.getParticipationConditions();
        if (participationConditions != null && !participationConditions.isEmpty()) {
            tvParticipationConditions.setText(participationConditions);
            Log.d(TAG, "  ✓ Participation conditions set from API");
        } else {
            // Fallback to default conditions
            tvParticipationConditions.setText(
                    "• Người chơi phải đủ 18 tuổi trở lên\n" +
                            "• Có kinh nghiệm chơi Pickleball cơ bản\n" +
                            "• Chấp nhận điều khoản và điều kiện của giải đấu\n" +
                            "• Có đủ sức khỏe để tham gia thi đấu\n" +
                            "• Tuân thủ các quy định về trang phục thi đấu"
            );
            Log.d(TAG, "  ⚠ Participation conditions empty, using default");
        }

        // 8. Prize - TODO: Thêm field này vào backend nếu cần
        tvPrize.setText(
                "🏆 Giải Nhất: 10.000.000 VNĐ\n" +
                        "🥈 Giải Nhì: 5.000.000 VNĐ\n" +
                        "🥉 Giải Ba: 3.000.000 VNĐ\n\n" +
                        "🎁 Các giải khuyến khích\n" +
                        "🎽 Quà tặng tài trợ cho tất cả vận động viên"
        );
        Log.d(TAG, "  ⚠ Prize info using placeholder (TODO: add to backend)");

        Log.d(TAG, "  ✅ UI update completed successfully");
    }

    // ========== FIX: Format date range với format mới ==========
    private String formatDateRange(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return "Đang cập nhật";
        }

        try {
            // Backend format: "dd-MM-yyyy HH:mm:ss"
            SimpleDateFormat backendFormat = new SimpleDateFormat(BACKEND_DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());

            Date start = backendFormat.parse(startDate);
            Date end = backendFormat.parse(endDate);

            if (start != null && end != null) {
                String formattedStart = displayFormat.format(start);
                String formattedEnd = displayFormat.format(end);

                Log.d(TAG, "  Date parsing successful:");
                Log.d(TAG, "    Input start: " + startDate);
                Log.d(TAG, "    Parsed start: " + formattedStart);
                Log.d(TAG, "    Input end: " + endDate);
                Log.d(TAG, "    Parsed end: " + formattedEnd);

                return "Từ " + formattedStart + " - " + formattedEnd;
            }
        } catch (ParseException e) {
            Log.e(TAG, "  ✗ Error parsing tournament dates", e);
            Log.e(TAG, "    Start date: " + startDate);
            Log.e(TAG, "    End date: " + endDate);
        }

        // Fallback
        return "Từ " + startDate + " - " + endDate;
    }

    // ========== FIX: Format registration period với format mới ==========
    private String formatRegistrationPeriod(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return "Thời gian mở đăng ký: Đang cập nhật";
        }

        try {
            // Backend format: "dd-MM-yyyy HH:mm:ss"
            SimpleDateFormat backendFormat = new SimpleDateFormat(BACKEND_DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());

            Date start = backendFormat.parse(startDate);
            Date end = backendFormat.parse(endDate);

            if (start != null && end != null) {
                String formattedStart = displayFormat.format(start);
                String formattedEnd = displayFormat.format(end);

                Log.d(TAG, "  Reg period parsing successful:");
                Log.d(TAG, "    Input start: " + startDate);
                Log.d(TAG, "    Parsed start: " + formattedStart);
                Log.d(TAG, "    Input end: " + endDate);
                Log.d(TAG, "    Parsed end: " + formattedEnd);

                return "Thời gian mở đăng ký: " + formattedStart + " - " + formattedEnd;
            }
        } catch (ParseException e) {
            Log.e(TAG, "  ✗ Error parsing registration dates", e);
            Log.e(TAG, "    Start date: " + startDate);
            Log.e(TAG, "    End date: " + endDate);
        }

        // Fallback
        return "Thời gian mở đăng ký: " + startDate + " - " + endDate;
    }

    private void updateRegisterButton(TourneyDetailResponse detail) {
        Log.d(TAG, "▶ updateRegisterButton()");

        try {
            String currentStr = detail.getCurrentNumberParticipants();
            String maxStr = detail.getMaxParticipants();

            Log.d(TAG, "  Current participants: " + currentStr);
            Log.d(TAG, "  Max participants: " + maxStr);

            if (currentStr != null && maxStr != null) {
                int current = Integer.parseInt(currentStr);
                int max = Integer.parseInt(maxStr);

                // Check if full
                if (current >= max) {
                    btnRegister.setEnabled(false);
                    btnRegister.setText("Đã đủ người");
                    btnRegister.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.darker_gray)
                    );
                    Log.d(TAG, "  ⚠ Tournament is FULL");
                    return;
                }

                // Check registration period - FIX: Dùng format mới
                boolean isOpen = isRegistrationOpen(
                        detail.getRegStartDate(),
                        detail.getRegEndDate()
                );

                Log.d(TAG, "  Registration is open: " + isOpen);

                if (isOpen) {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Đăng ký ngay (" + current + "/" + max + ")");
                    btnRegister.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.holo_green_light)
                    );
                    Log.d(TAG, "  ✓ Button enabled - Registration OPEN");
                } else {
                    btnRegister.setEnabled(false);
                    btnRegister.setText("Hết hạn đăng ký");
                    btnRegister.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.darker_gray)
                    );
                    Log.d(TAG, "  ⚠ Button disabled - Registration CLOSED");
                }
            } else {
                // Default
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký ngay");
                btnRegister.setBackgroundTintList(
                        getResources().getColorStateList(android.R.color.holo_green_light)
                );
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "  ✗ Error parsing participant numbers", e);
            btnRegister.setEnabled(true);
            btnRegister.setText("Đăng ký ngay");
        }
    }

    // ========== FIX: Check registration open với format mới ==========
    private boolean isRegistrationOpen(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            Log.d(TAG, "    Registration dates are null, assuming OPEN");
            return true;
        }

        try {
            // Backend format: "dd-MM-yyyy HH:mm:ss"
            SimpleDateFormat backendFormat = new SimpleDateFormat(BACKEND_DATE_FORMAT, Locale.getDefault());

            Date start = backendFormat.parse(startDate);
            Date end = backendFormat.parse(endDate);
            Date now = new Date();

            if (start != null && end != null) {
                boolean isAfterStart = now.after(start) || now.equals(start);
                boolean isBeforeEnd = now.before(end) || now.equals(end);
                boolean isOpen = isAfterStart && isBeforeEnd;

                SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Log.d(TAG, "    Now: " + logFormat.format(now));
                Log.d(TAG, "    Start: " + logFormat.format(start));
                Log.d(TAG, "    End: " + logFormat.format(end));
                Log.d(TAG, "    Is after start: " + isAfterStart);
                Log.d(TAG, "    Is before end: " + isBeforeEnd);
                Log.d(TAG, "    Is open: " + isOpen);

                return isOpen;
            }
        } catch (ParseException e) {
            Log.e(TAG, "    ✗ Error checking registration period", e);
            Log.e(TAG, "    Start date: " + startDate);
            Log.e(TAG, "    End date: " + endDate);
        }

        // Default to open
        return true;
    }

    private void handleRegisterClick() {
        if (tournamentDetail == null) {
            Toast.makeText(this, "Vui lòng đợi tải xong thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              handleRegisterClick()                         ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");
        Log.d(TAG, "  Tournament ID: " + tournamentId);
        Log.d(TAG, "  Tournament Name: " + tournamentDetail.getTournamentName());

        // Navigate to TournamentRegistrationActivity
        Intent intent = new Intent(this, TournamentRegistrationActivity.class);

        // Pass tournament data
        // Pass tournament data
        intent.putExtra("tournamentId", tournamentId);
        intent.putExtra("tournamentName", tournamentDetail.getTournamentName());
        intent.putExtra("tournamentDate", formatDateRange(
                tournamentDetail.getTournamentStartDate(),
                tournamentDetail.getTournamentEndDate()
        ));
        intent.putExtra("tournamentLocation", tournamentDetail.getTournamentLocation());
        intent.putExtra("organizerName", tournamentDetail.getOrganizerName());

        Log.d(TAG, "  ✓ Starting TournamentRegistrationActivity");
        startActivity(intent);
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            btnRegister.setEnabled(true);
            btnRegister.setText("Đăng ký ngay");
            btnRegister.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.holo_green_light)
            );
        });

        Log.e(TAG, "Error displayed: " + message);
    }
}