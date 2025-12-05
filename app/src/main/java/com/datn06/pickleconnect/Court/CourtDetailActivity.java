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

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Adapter.ReviewAdapter;
import com.datn06.pickleconnect.Adapter.ServiceAdapter;
import com.datn06.pickleconnect.Booking.FieldSelectionActivity;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.FieldPriceDTO;
import com.datn06.pickleconnect.Models.CourtDetailResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;

import java.util.ArrayList;

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
    private CourtApiService apiService;
    private RecyclerView recyclerViewServices;
    private RecyclerView recyclerViewReviews;
    private TableLayout tableLayoutPrices;
    private TextView tvOverallRating;
    private Button btnBook;
    private LinearLayout layoutStars;
    private ImageView btnFavorite;

    // Adapters
    private ServiceAdapter serviceAdapter;
    private ReviewAdapter reviewAdapter;

    // Data
    private CourtDetailResponse courtDetail;
    private Long facilityId;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

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

        // Hiển thị mô tả
        if (courtDetail.getFields() != null && !courtDetail.getFields().isEmpty()) {
            String description = courtDetail.getFields().get(0).getDescription();
            if (description != null && !description.isEmpty()) {
                tvDescription.setText(description);
            } else {
                tvDescription.setText("Không có mô tả");
            }
        } else {
            tvDescription.setText("Không có mô tả");
        }

        // Hiển thị dịch vụ
        if (courtDetail.getServices() != null) {
            serviceAdapter.updateData(courtDetail.getServices());
            serviceAdapter.notifyDataSetChanged();
        } else {
            serviceAdapter.updateData(new ArrayList<>());
            serviceAdapter.notifyDataSetChanged();
        }

        // Hiển thị bảng giá
        if (courtDetail.getPrices() != null && !courtDetail.getPrices().isEmpty()) {
            displayPriceTable();
        } else {
            int childCount = tableLayoutPrices.getChildCount();
            if (childCount > 1) {
                tableLayoutPrices.removeViews(1, childCount - 1);
            }
        }

        // Hiển thị reviews
        if (courtDetail.getReviews() != null && !courtDetail.getReviews().isEmpty()) {
            reviewAdapter.updateData(courtDetail.getReviews());
            reviewAdapter.notifyDataSetChanged();
            displayOverallRating();
        } else {
            reviewAdapter.updateData(new ArrayList<>());
            reviewAdapter.notifyDataSetChanged();
            tvOverallRating.setText("N/A");

            if (layoutStars != null) {
                layoutStars.removeAllViews();
                for (int i = 0; i < 5; i++) {
                    layoutStars.addView(createStarImage(R.drawable.ic_star_empty));
                }
            }
        }
    }

    private void displayPriceTable() {
        // Clear existing rows except header
        int childCount = tableLayoutPrices.getChildCount();
        if (childCount > 1) {
            tableLayoutPrices.removeViews(1, childCount - 1);
        }

        if (courtDetail.getPrices() != null) {
            for (FieldPriceDTO price : courtDetail.getPrices()) {
                String weekdayLabel = formatWeekdayLabel(price.getWeekday());
                TableRow row = createPriceRow(weekdayLabel, price);
                tableLayoutPrices.addView(row);
            }
        }
    }

    private String formatWeekdayLabel(String weekday) {
        if (weekday == null || weekday.isEmpty()) {
            return "Không xác định";
        }

        weekday = weekday.trim();

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

        return convertDayToVietnamese(weekday);
    }

    private String convertDayToVietnamese(String day) {
        if (day == null || day.isEmpty()) {
            return "?";
        }

        switch (day.toLowerCase()) {
            case "monday":
            case "mon":
                return "T2";
            case "tuesday":
            case "tue":
                return "T3";
            case "wednesday":
            case "wed":
                return "T4";
            case "thursday":
            case "thu":
                return "T5";
            case "friday":
            case "fri":
                return "T6";
            case "saturday":
            case "sat":
                return "T7";
            case "sunday":
            case "sun":
                return "CN";
            case "t2":
            case "t3":
            case "t4":
            case "t5":
            case "t6":
            case "t7":
            case "cn":
                return day.toUpperCase();
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

        TextView tvWeekday = createTableCell(weekdayLabel);
        row.addView(tvWeekday);

        TextView tvTimeSlot = createTableCell(price.getTimeSlotLabel());
        row.addView(tvTimeSlot);

        TextView tvFixedPrice = createTableCell(price.getFormattedFixedPrice());
        row.addView(tvFixedPrice);

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

        if (layoutStars != null) {
            displayStarRating(avgRating);
        }
    }

    private void displayStarRating(double rating) {
        layoutStars.removeAllViews();

        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        for (int i = 0; i < fullStars; i++) {
            layoutStars.addView(createStarImage(R.drawable.ic_star_filled));
        }

        if (hasHalfStar) {
            layoutStars.addView(createStarImage(R.drawable.ic_star_half));
        }

        for (int i = 0; i < emptyStars; i++) {
            layoutStars.addView(createStarImage(R.drawable.ic_star_empty));
        }
    }

    private ImageView createStarImage(int drawableRes) {
        ImageView star = new ImageView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(20),
                dpToPx(20)
        );
        params.setMarginStart(dpToPx(4));
        star.setLayoutParams(params);

        star.setImageResource(drawableRes);

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