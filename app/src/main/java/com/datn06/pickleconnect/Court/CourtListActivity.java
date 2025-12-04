package com.datn06.pickleconnect.Court;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Booking.FieldSelectionActivity;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.Model.FacilitySearchResponse;
import com.datn06.pickleconnect.Model.SearchCourtRequest;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtListActivity extends AppCompatActivity {
    private static final String TAG = "CourtListActivity";

    private ImageButton btnBack, btnMap, btnHistory, btnFilter;
    private EditText etSearch;
    private Chip chipNearby, chipSaved;
    private RecyclerView rvCourts;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private BottomNavigationView bottomNavigation;

    private CourtAdapter courtAdapter;
    private List<FacilityDTO> courtList = new ArrayList<>();

    private SearchCourtRequest currentSearchRequest;
    private boolean isShowingSaved = false;

    // Vị trí người dùng
    private double userLatitude = 21.0285;  // Mặc định Hà Nội
    private double userLongitude = 105.8542;

    private MenuNavigation menuNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_list);

        Log.d(TAG, "=== CourtListActivity onCreate START ===");

        // Lấy vị trí từ Intent
        userLatitude = getIntent().getDoubleExtra("userLatitude", 21.0285);
        userLongitude = getIntent().getDoubleExtra("userLongitude", 105.8542);

        Log.d(TAG, "User location: " + userLatitude + ", " + userLongitude);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupBottomNavigation();

        menuNavigation = new MenuNavigation(this);

        // Initial load - ALL courts (no filter)
        loadAllCourts();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMap = findViewById(R.id.btnMap);
        btnHistory = findViewById(R.id.btnHistory);
        btnFilter = findViewById(R.id.btnFilter);
        etSearch = findViewById(R.id.etSearch);
        chipNearby = findViewById(R.id.chipNearby);
        chipSaved = findViewById(R.id.chipSaved);
        rvCourts = findViewById(R.id.rvCourts);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        // ✅ UPDATED: Tách biệt 2 loại click giống HomeActivity
        courtAdapter = new CourtAdapter(courtList, new CourtAdapter.OnCourtClickListener() {
            @Override
            public void onCourtClick(FacilityDTO facility) {
                // ✅ Click vào card (không phải nút) -> mở CourtDetailActivity
                openCourtDetail(facility);
            }

            @Override
            public void onBookNowClick(FacilityDTO facility) {
                // ✅ Click vào nút "ĐẶT SÂN" -> mở FieldSelectionActivity
                openFieldSelection(facility);
            }
        }, this);

        rvCourts.setLayoutManager(new LinearLayoutManager(this));
        rvCourts.setAdapter(courtAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Giữ nguyên nút Map ở top bar
        btnMap.setOnClickListener(v -> openMapActivity());

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.datn06.pickleconnect.Booking.BookingHistoryActivity.class);
            startActivity(intent);
        });

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        // Search text change
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    searchCourtsByName(s.toString());
                } else {
                    loadAllCourts();
                }
            }
        });

        // Chip nearby
        chipNearby.setOnClickListener(v -> {
            isShowingSaved = false;
            chipNearby.setChecked(true);
            chipSaved.setChecked(false);
            searchNearbyCourts();
        });

        // Chip saved
        chipSaved.setOnClickListener(v -> {
            isShowingSaved = true;
            chipNearby.setChecked(false);
            chipSaved.setChecked(true);
            loadSavedCourts();
        });
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_booking);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_booking) {
                        return true;
                    } else {
                        menuNavigation.navigateTo(itemId);
                        return true;
                    }
                }
            });
        }
    }

    private void openMapActivity() {
        Intent intent = new Intent(CourtListActivity.this, com.datn06.pickleconnect.Map.MapActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Load ALL courts (no filter) - Màn hình mặc định
     */
    private void loadAllCourts() {
        Log.d(TAG, "loadAllCourts called");

        currentSearchRequest = SearchCourtRequest.builder()
                .page(0)
                .size(100)
                .build();

        Log.d(TAG, "Request: Load ALL courts (no filters)");
        performSearch(currentSearchRequest);
    }

    /**
     * Search nearby courts - Khi ấn chip "Gần tôi"
     */
    private void searchNearbyCourts() {
        Log.d(TAG, "searchNearbyCourts called");

        currentSearchRequest = SearchCourtRequest.builder()
                .userLatitude(roundToDecimal(userLatitude, 6))
                .userLongitude(roundToDecimal(userLongitude, 6))
                .maxDistanceKm(50.0)
                .page(0)
                .size(20)
                .build();

        Log.d(TAG, "Request: lat=" + userLatitude + ", lng=" + userLongitude + ", maxDist=50km");
        performSearch(currentSearchRequest);
    }

    /**
     * Search theo tên sân - CHỈ tìm theo tên, KHÔNG filter location
     */
    private void searchCourtsByName(String name) {
        Log.d(TAG, "searchCourtsByName: " + name);

        currentSearchRequest = SearchCourtRequest.builder()
                .facilityName(name)
                .page(0)
                .size(50)
                .build();

        performSearch(currentSearchRequest);
    }

    private void performSearch(SearchCourtRequest request) {
        Log.d(TAG, "performSearch START");
        showLoading(true);

        CourtApiService apiService = ApiClient.createService(ServiceHost.COURT_SERVICE, CourtApiService.class);
        Log.d(TAG, "API Service URL: " + ServiceHost.COURT_SERVICE);

        apiService.searchCourts(request).enqueue(new Callback<FacilitySearchResponse>() {
            @Override
            public void onResponse(Call<FacilitySearchResponse> call, Response<FacilitySearchResponse> response) {
                showLoading(false);
                Log.d(TAG, "API Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    FacilitySearchResponse searchResponse = response.body();
                    Log.d(TAG, "Response success! Facilities count: " +
                            (searchResponse.getFacilities() != null ? searchResponse.getFacilities().size() : 0));

                    if (searchResponse.getFacilities() != null && !searchResponse.getFacilities().isEmpty()) {
                        courtList.clear();
                        courtList.addAll(searchResponse.getFacilities());
                        courtAdapter.notifyDataSetChanged();
                        showNoResults(false);
                        Log.d(TAG, "Updated RecyclerView with " + courtList.size() + " courts");
                    } else {
                        courtList.clear();
                        courtAdapter.notifyDataSetChanged();
                        showNoResults(true);
                        Log.w(TAG, "No facilities found");
                    }
                } else {
                    Log.e(TAG, "API Response failed: code=" + response.code());
                    Toast.makeText(CourtListActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FacilitySearchResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call FAILED: " + t.getMessage(), t);
                Toast.makeText(CourtListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSavedCourts() {
        Intent intent = new Intent(this, SavedCourtsActivity.class);
        startActivity(intent);
    }

    private void showFilterBottomSheet() {
        FilterBottomSheet filterBottomSheet = FilterBottomSheet.newInstance(userLatitude, userLongitude);
        filterBottomSheet.setOnFilterApplyListener(request -> {
            currentSearchRequest = request;
            performSearch(request);
        });
        filterBottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
    }

    // ✅ NEW: Open CourtDetailActivity (click vào card)
    private void openCourtDetail(FacilityDTO facility) {
        Intent intent = new Intent(CourtListActivity.this, CourtDetailActivity.class);
        intent.putExtra("facilityId", facility.getFacilityId());

        Log.d(TAG, "Opening CourtDetailActivity for facility: " + facility.getFacilityName());

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ✅ NEW: Open FieldSelectionActivity (click nút "ĐẶT SÂN")
    private void openFieldSelection(FacilityDTO facility) {
        Intent intent = new Intent(CourtListActivity.this, FieldSelectionActivity.class);
        intent.putExtra("facilityId", facility.getFacilityId());
        intent.putExtra("facilityName", facility.getFacilityName());

        Log.d(TAG, "Opening FieldSelectionActivity for facility: " + facility.getFacilityName());

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvCourts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showNoResults(boolean show) {
        tvNoResults.setVisibility(show ? View.VISIBLE : View.GONE);
        rvCourts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private BigDecimal roundToDecimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }
}