package com.datn06.pickleconnect.Court;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Adapter.CourtImageAdapter;
import com.datn06.pickleconnect.Adapter.ReviewAdapter;
import com.datn06.pickleconnect.Adapter.ServiceAdapter;
import com.datn06.pickleconnect.Booking.FieldSelectionActivity;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.FacilityReviewDTO;
import com.datn06.pickleconnect.Model.FacilityServiceDTO;
import com.datn06.pickleconnect.Model.FieldPriceDTO;
import com.datn06.pickleconnect.Models.CourtDetailRequest;
import com.datn06.pickleconnect.Models.CourtDetailResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Utils.TokenManager; // ✅ CHANGED: Import TokenManager


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtDetailActivity extends AppCompatActivity {

    private static final String TAG = "CourtDetailActivity";

    // Intent extras
    public static final String EXTRA_FACILITY_ID = "facility_id";

    // Views
    private Toolbar toolbar;
    private TextView tvFieldName;
    private TextView tvAddress;
    private TextView tvDescription;
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicator;
    private CourtApiService apiService;
    private RecyclerView recyclerViewServices;
    private RecyclerView recyclerViewReviews;
    private TableLayout tableLayoutPrices;
    private TextView tvOverallRating;

    // Adapters
    private CourtImageAdapter imageAdapter;
    private ServiceAdapter serviceAdapter;
    private ReviewAdapter reviewAdapter;

    // Data
    private CourtDetailResponse courtDetail;
    private Long facilityId;
    private TokenManager tokenManager; // ✅ CHANGED: Use TokenManager instead of SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

        // ✅ CHANGED: Initialize TokenManager
        tokenManager = TokenManager.getInstance(this);
        Log.e(TAG, "Court detail user id load from token: " + tokenManager.getUserId());

        // Get facility ID from intent
        facilityId = getIntent().getLongExtra("facilityId", 0);
        if (facilityId == -1L) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sân", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        setupApiService();
        loadCourtDetail();
    }

    private void initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Text views
        tvFieldName = findViewById(R.id.tvFieldName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDescription = findViewById(R.id.tvDescription);
        tvOverallRating = findViewById(R.id.tvOverallRating);

        // ViewPager
        viewPagerImages = findViewById(R.id.viewPagerImages);
        layoutIndicator = findViewById(R.id.layoutIndicator);

        // RecyclerViews
        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);

        // TableLayout
        tableLayoutPrices = findViewById(R.id.tableLayoutPrices);
    }

    private void setupRecyclerViews() {
        // Services RecyclerView
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceAdapter(new ArrayList<>());
        recyclerViewServices.setAdapter(serviceAdapter);

        // Reviews RecyclerView
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        recyclerViewReviews.setAdapter(reviewAdapter);
    }

    private void loadCourtDetail() {
        // ✅ CHANGED: Get userId from TokenManager
        String userIdStr = tokenManager.getUserId();
        if (userIdStr == null || userIdStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.e(TAG, "userid: " + userIdStr);



        apiService.getCourtDetail(userIdStr, facilityId).enqueue(new Callback<BaseResponse<CourtDetailResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<CourtDetailResponse>> call,
                                   Response<BaseResponse<CourtDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<CourtDetailResponse> baseResponse = response.body();
                    if ("00".equals(baseResponse.getCode()) && baseResponse.getData() != null) {
                        courtDetail = baseResponse.getData();
                        displayCourtDetail();
                    } else {
                        Toast.makeText(CourtDetailActivity.this,
                                baseResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CourtDetailActivity.this,
                            "Lỗi khi tải thông tin sân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<CourtDetailResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading court detail", t);
                Toast.makeText(CourtDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCourtDetail() {
        if (courtDetail == null) return;

        // Hiển thị thông tin cơ bản
        tvFieldName.setText(courtDetail.getFacilityName());
        tvAddress.setText(courtDetail.getFullAddress());

        // Hiển thị mô tả (sử dụng mô tả của sân đầu tiên nếu có)
        if (courtDetail.getFields() != null && !courtDetail.getFields().isEmpty()) {
            String description = courtDetail.getFields().get(0).getDescription();
            if (description != null && !description.isEmpty()) {
                tvDescription.setText(description);
            } else {
                tvDescription.setText("Không có mô tả");
            }
        } else {
            tvDescription.setText("Không có mô tả"); // Thêm trường hợp không có fields
        }

        // Hiển thị hình ảnh
        //setupImagePager();

        // Hiển thị dịch vụ
        if (courtDetail.getServices() != null) {
            serviceAdapter.updateData(courtDetail.getServices());
            // ✅ THÊM: Thông báo cho Adapter để cập nhật RecyclerView
            serviceAdapter.notifyDataSetChanged();
        } else {
            serviceAdapter.updateData(new ArrayList<>());
            serviceAdapter.notifyDataSetChanged();
        }

        // Hiển thị bảng giá
        if (courtDetail.getPrices() != null && !courtDetail.getPrices().isEmpty()) {
            displayPriceTable();
        } else {
            // ✅ THÊM: Xóa bảng nếu không có giá (chỉ giữ lại header)
            int childCount = tableLayoutPrices.getChildCount();
            if (childCount > 1) {
                tableLayoutPrices.removeViews(1, childCount - 1);
            }
        }

        // Hiển thị đánh giá
        if (courtDetail.getReviews() != null) {
            reviewAdapter.updateData(courtDetail.getReviews());
            // ✅ THÊM: Thông báo cho Adapter để cập nhật RecyclerView
            reviewAdapter.notifyDataSetChanged();
            displayOverallRating();
        } else {
            reviewAdapter.updateData(new ArrayList<>());
            reviewAdapter.notifyDataSetChanged();
            tvOverallRating.setText("N/A"); // Đặt rating là N/A nếu không có review
        }
    }

    private void setupImagePager() {
        // Placeholder images - replace with actual image URLs from your backend
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(""); // Add your image URLs here

        if (imageUrls.isEmpty()) {
            // Add placeholder if no images
            imageUrls.add("placeholder");
        }

        imageAdapter = new CourtImageAdapter(imageUrls);
        viewPagerImages.setAdapter(imageAdapter);

        // Setup indicator
        setupIndicators(imageUrls.size());
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
            }
        });
    }

    private void setupIndicators(int count) {
        layoutIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    16, 16
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_inactive);
            layoutIndicator.addView(indicator);
        }
        if (count > 0) {
            layoutIndicator.getChildAt(0).setBackgroundResource(R.drawable.indicator_active);
        }
    }

    private void updateIndicator(int position) {
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View indicator = layoutIndicator.getChildAt(i);
            indicator.setBackgroundResource(
                    i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive
            );
        }
    }

    private void displayPriceTable() {
        // Clear existing rows except header
        int childCount = tableLayoutPrices.getChildCount();
        if (childCount > 1) {
            tableLayoutPrices.removeViews(1, childCount - 1);
        }

        // Group prices by weekday
        Map<String, List<FieldPriceDTO>> groupedPrices = groupPricesByWeekday();

        // Add rows for each group
        for (Map.Entry<String, List<FieldPriceDTO>> entry : groupedPrices.entrySet()) {
            String weekdayLabel = entry.getKey();
            List<FieldPriceDTO> prices = entry.getValue();

            for (FieldPriceDTO price : prices) {
                TableRow row = createPriceRow(weekdayLabel, price);
                tableLayoutPrices.addView(row);
                weekdayLabel = ""; // Only show weekday label for first row
            }
        }
    }

    private Map<String, List<FieldPriceDTO>> groupPricesByWeekday() {
        Map<String, List<FieldPriceDTO>> grouped = new HashMap<>();

        for (FieldPriceDTO price : courtDetail.getPrices()) {
            String key = getWeekdayGroupLabel(price.getWeekday());
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<>());
            }
            grouped.get(key).add(price);
        }

        return grouped;
    }


    private String getWeekdayGroupLabel(String weekday) {
        if (weekday == null || weekday.isEmpty()) return "Không xác định";

        weekday = weekday.trim().toLowerCase();

        // Nếu là khoảng: "monday-saturday" hoặc "t2-t7"
        if (weekday.contains("-")) {
            String[] parts = weekday.split("-");
            if (parts.length == 2) {
                int from = convertToDayNumber(parts[0].trim());
                int to = convertToDayNumber(parts[1].trim());

                if (from == -1 || to == -1) return "Khác";

                // Nếu khoảng nằm trong T2-T6
                if (from >= 1 && to <= 5) return "T2 - T6";

                // Nếu gồm T7 hoặc CN
                if (to == 6 || to == 0) return "T7 - CN";

                return "Khác";
            }
            return "Khác";
        }

        // --- Trường hợp chỉ 1 ngày ---
        int dayNumber = convertToDayNumber(weekday);
        if (dayNumber == -1) return "Khác";

        if (dayNumber >= 1 && dayNumber <= 5) return "T2 - T6";
        if (dayNumber == 6 || dayNumber == 0) return "T7 - CN";

        return "Khác";
    }

    private int convertToDayNumber(String weekday) {
        switch (weekday) {
            // English full
            case "monday": return 1;
            case "tuesday": return 2;
            case "wednesday": return 3;
            case "thursday": return 4;
            case "friday": return 5;
            case "saturday": return 6;
            case "sunday": return 0;

            // English short
            case "mon": return 1;
            case "tue": return 2;
            case "wed": return 3;
            case "thu": return 4;
            case "fri": return 5;
            case "sat": return 6;
            case "sun": return 0;

            // Vietnamese
            case "t2": return 1;
            case "t3": return 2;
            case "t4": return 3;
            case "t5": return 4;
            case "t6": return 5;
            case "t7": return 6;
            case "cn": return 0;

            default:
                return -1;
        }
    }



    private TableRow createPriceRow(String weekdayLabel, FieldPriceDTO price) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        row.setLayoutParams(params);
        row.setPadding(32, 32, 32, 32);

        // Weekday column
        TextView tvWeekday = createTableCell(weekdayLabel);
        row.addView(tvWeekday);

        // Time slot column
        TextView tvTimeSlot = createTableCell(price.getTimeSlotLabel());
        row.addView(tvTimeSlot);

        // Fixed price column
        TextView tvFixedPrice = createTableCell(price.getFormattedFixedPrice());
        row.addView(tvFixedPrice);

        // Walk-in price column
        TextView tvWalkinPrice = createTableCell(price.getFormattedWalkinPrice());
        row.addView(tvWalkinPrice);

        return row;
    }

    private TextView createTableCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tv.setTextSize(12);
        tv.setPadding(8, 8, 8, 8);
        return tv;
    }

    private void displayOverallRating() {
        double avgRating = courtDetail.getAverageRating();
        if (tvOverallRating != null) {
            tvOverallRating.setText(String.format("%.1f", avgRating));
        }
    }

    private void setupApiService() {
        apiService = ApiClient.createService(
                ServiceHost.COURT_SERVICE,
                CourtApiService.class
        );
    }


}