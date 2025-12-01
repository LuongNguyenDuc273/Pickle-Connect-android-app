package com.datn06.pickleconnect.Court;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private Button btnBook;
    private LinearLayout layoutStars;
    private ImageView btnFavorite;

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
        setupButtons();
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

        // Button
        btnBook = findViewById(R.id.btnBook);
        btnFavorite = findViewById(R.id.btnFavorite);

        // RecyclerViews
        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);

        // TableLayout
        tableLayoutPrices = findViewById(R.id.tableLayoutPrices);

        layoutStars = findViewById(R.id.layoutStars);
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

    private void setupButtons() {
        btnBook.setOnClickListener(v -> {
            String userId = tokenManager.getUserId();
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt sân", Toast.LENGTH_SHORT).show();
                return;
            }

            if (facilityId == null || facilityId == 0) {
                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sân", Toast.LENGTH_SHORT).show();
                return;
            }

            String facilityName = courtDetail != null ? courtDetail.getFacilityName() : "";

            Intent intent = new Intent(CourtDetailActivity.this, FieldSelectionActivity.class);
            intent.putExtra("facilityId", facilityId);
            intent.putExtra("facilityName", facilityName);
            startActivity(intent);
        });

        btnFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng yêu thích đang phát triển", Toast.LENGTH_SHORT).show();
        });
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

        if (courtDetail.getReviews() != null && !courtDetail.getReviews().isEmpty()) {
            reviewAdapter.updateData(courtDetail.getReviews());
            reviewAdapter.notifyDataSetChanged();
            displayOverallRating(); // ✅ Hiển thị rating + sao
        } else {
            reviewAdapter.updateData(new ArrayList<>());
            reviewAdapter.notifyDataSetChanged();
            tvOverallRating.setText("N/A");

            // ✅ Hiển thị 0 sao khi không có review
            if (layoutStars != null) {
                layoutStars.removeAllViews();
                for (int i = 0; i < 5; i++) {
                    layoutStars.addView(createStarImage(R.drawable.ic_star_empty));
                }
            }
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

        // ✅ KHÔNG NHÓM - Hiển thị từng dòng với label gốc từ backend
        if (courtDetail.getPrices() != null) {
            for (FieldPriceDTO price : courtDetail.getPrices()) {
                String weekdayLabel = formatWeekdayLabel(price.getWeekday());
                TableRow row = createPriceRow(weekdayLabel, price);
                tableLayoutPrices.addView(row);
            }
        }
    }

    /**
     * Chuyển đổi weekday từ tiếng Anh sang tiếng Việt
     * Hỗ trợ cả ngày đơn lẻ và khoảng ngày
     *
     * Ví dụ:
     * - "Monday" -> "Thứ 2"
     * - "Monday-Friday" -> "T2 - T6"
     * - "Monday-Wednesday" -> "T2 - T4"
     * - "Saturday-Sunday" -> "T7 - CN"
     */
    private String formatWeekdayLabel(String weekday) {
        if (weekday == null || weekday.isEmpty()) {
            return "Không xác định";
        }

        weekday = weekday.trim();

        // ✅ Kiểm tra nếu là khoảng ngày (có dấu "-")
        if (weekday.contains("-")) {
            String[] parts = weekday.split("-");
            if (parts.length == 2) {
                String from = parts[0].trim();
                String to = parts[1].trim();

                String fromVi = convertDayToVietnamese(from);
                String toVi = convertDayToVietnamese(to);

                return fromVi + " - " + toVi;
            }
        }

        // ✅ Nếu chỉ là 1 ngày đơn lẻ
        return convertDayToVietnamese(weekday);
    }

    /**
     * Chuyển đổi tên ngày từ tiếng Anh sang tiếng Việt (viết tắt hoặc đầy đủ)
     */
    private String convertDayToVietnamese(String day) {
        if (day == null || day.isEmpty()) {
            return "?";
        }

        switch (day.toLowerCase()) {
            // Full English names
            case "monday":
                return "T2";
            case "tuesday":
                return "T3";
            case "wednesday":
                return "T4";
            case "thursday":
                return "T5";
            case "friday":
                return "T6";
            case "saturday":
                return "T7";
            case "sunday":
                return "CN";

            // Short English names
            case "mon":
                return "T2";
            case "tue":
                return "T3";
            case "wed":
                return "T4";
            case "thu":
                return "T5";
            case "fri":
                return "T6";
            case "sat":
                return "T7";
            case "sun":
                return "CN";

            // Already Vietnamese
            case "t2":
            case "t3":
            case "t4":
            case "t5":
            case "t6":
            case "t7":
            case "cn":
                return day.toUpperCase();

            default:
                // Trả về nguyên bản nếu không khớp
                return day;
        }
    }

    /**
     * Alternative: Nếu muốn hiển thị tên đầy đủ thay vì viết tắt
     * Ví dụ: "Thứ 2" thay vì "T2"
     */
    private String convertDayToVietnameseFull(String day) {
        if (day == null || day.isEmpty()) {
            return "Không xác định";
        }

        switch (day.toLowerCase()) {
            case "monday":
            case "mon":
                return "Thứ 2";
            case "tuesday":
            case "tue":
                return "Thứ 3";
            case "wednesday":
            case "wed":
                return "Thứ 4";
            case "thursday":
            case "thu":
                return "Thứ 5";
            case "friday":
            case "fri":
                return "Thứ 6";
            case "saturday":
            case "sat":
                return "Thứ 7";
            case "sunday":
            case "sun":
                return "Chủ nhật";
            default:
                return day;
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

        // Hiển thị số rating
        if (tvOverallRating != null) {
            tvOverallRating.setText(String.format("%.1f", avgRating));
        }

        // ✅ Hiển thị sao động
        if (layoutStars != null) {
            displayStarRating(avgRating);
        }
    }

    private void displayStarRating(double rating) {
        // Xóa các sao cũ
        layoutStars.removeAllViews();

        // Tính toán số sao
        int fullStars = (int) rating; // Số sao đầy
        boolean hasHalfStar = (rating - fullStars) >= 0.5; // Có sao nửa không
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0); // Số sao rỗng

        // Thêm sao đầy
        for (int i = 0; i < fullStars; i++) {
            layoutStars.addView(createStarImage(R.drawable.ic_star_filled));
        }

        // Thêm sao nửa (nếu có)
        if (hasHalfStar) {
            layoutStars.addView(createStarImage(R.drawable.ic_star_half));
        }

        // Thêm sao rỗng
        for (int i = 0; i < emptyStars; i++) {
            layoutStars.addView(createStarImage(R.drawable.ic_star_empty));
        }
    }

    private ImageView createStarImage(int drawableRes) {
        ImageView star = new ImageView(this);

        // Set size (20dp x 20dp)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(20),
                dpToPx(20)
        );
        params.setMarginStart(dpToPx(4)); // Margin 4dp giữa các sao
        star.setLayoutParams(params);

        // Set icon
        star.setImageResource(drawableRes);

        // Set tint màu vàng (optional)
        // star.setColorFilter(getResources().getColor(R.color.star_yellow));

        return star;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupApiService() {
        apiService = ApiClient.createService(
                ServiceHost.COURT_SERVICE,
                CourtApiService.class
        );
    }


}