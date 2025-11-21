package com.datn06.pickleconnect.Home;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.Event.EventsActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Adapter.BannerAdapter;
import com.datn06.pickleconnect.Adapter.FacilityAdapter;
import com.datn06.pickleconnect.Adapter.FacilityGroupAdapter;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.Home.HomeResponse;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final double DEFAULT_LAT = 21.0285;
    private static final double DEFAULT_LNG = 105.8542;

    private double currentLat = DEFAULT_LAT;
    private double currentLng = DEFAULT_LNG;

    private RecyclerView rvBanner;
    private RecyclerView rvSportsVenues;
    private EditText etSearch;
    private androidx.cardview.widget.CardView searchCard;
    private View progressBar;
    private Button btnFindNearby;
    private androidx.cardview.widget.CardView mapCard;
    private BottomNavigationView bottomNavigation;
    private LinearLayout dotsContainer;
    private LinearLayout rvFacilitiesDotsContainer;
    private int currentFacilityGroupPosition = 0;

    private BannerAdapter bannerAdapter;
    private FacilityGroupAdapter facilityGroupAdapter;
    private ApiService apiService;

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    private LoadingDialog loadingDialog;
    private MenuNavigation menuNavigation;
    private int currentBannerPosition = 0;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    Log.d(TAG, "Permission granted");
                    showLoadingDialog("Đang lấy vị trí của bạn...");
                    getCurrentLocationAndLoad();
                } else {
                    Log.w(TAG, "Permission denied");
                    Toast.makeText(this, "Quyền truy cập vị trí bị từ chối. Sử dụng vị trí mặc định (Hà Nội)",
                            Toast.LENGTH_LONG).show();
                    currentLat = DEFAULT_LAT;
                    currentLng = DEFAULT_LNG;
                    showLoadingDialog("Đang tải dữ liệu...");
                    loadHomeData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BỎ EdgeToEdge.enable(this); - Đây là nguyên nhân gây khoảng trống

        setContentView(R.layout.activity_home);

        // SỬA LẠI: Chỉ apply window insets cho main layout, KHÔNG cho bottomNav
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Chỉ set padding cho top, KHÔNG set cho bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });


        initViews();
        setupSearchListeners();
        setupRecyclerViews();
        setupApiService();
        setupLocationClient();
        setupBottomNavigation();
        dotsContainer = findViewById(R.id.dotsContainer);

        loadingDialog = new LoadingDialog(this);
        menuNavigation = new MenuNavigation(this);

        requestLocationAndLoadData();
    }

    private void initViews() {
        rvBanner = findViewById(R.id.rvBanner);
        rvSportsVenues = findViewById(R.id.rvSportsVenues);
        progressBar = findViewById(R.id.progressBar);
        btnFindNearby = findViewById(R.id.btnFindNearby);
        mapCard = findViewById(R.id.mapCard);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvFacilitiesDotsContainer = findViewById(R.id.rvFacilitiesDotsContainer);
        etSearch = findViewById(R.id.etSearch);
        searchCard = findViewById(R.id.searchCard);

        if (btnFindNearby != null) {
            btnFindNearby.setOnClickListener(v -> openMapActivity());
        }

        if (mapCard != null) {
            mapCard.setOnClickListener(v -> openMapActivity());
        }
    }

    private void setupSearchListeners() {
        // Click vào EditText
        if (etSearch != null) {
            etSearch.setOnClickListener(v -> openSearchActivity());
            etSearch.setFocusable(false); // Không cho focus để tránh bàn phím hiện ra ở HomeActivity
            etSearch.setClickable(true);
        }

        // Click vào cả CardView chứa thanh search
        if (searchCard != null) {
            searchCard.setOnClickListener(v -> openSearchActivity());
        }
    }

    private void openSearchActivity() {
        Intent intent = new Intent(HomeActivity.this, com.datn06.pickleconnect.Search.SearchActivity.class);

        // Truyền vị trí hiện tại của user
        intent.putExtra("userLat", currentLat);
        intent.putExtra("userLng", currentLng);

        Log.d(TAG, "Opening SearchActivity with location: " + currentLat + ", " + currentLng);

        startActivity(intent);
        // Có thể thêm animation chuyển màn hình
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            // Set Home là item được chọn mặc định (vì đang ở HomeActivity)
            bottomNavigation.setSelectedItemId(R.id.nav_home);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        // Đang ở Home rồi, không làm gì
                        return true;
                    } else {
                        // Chuyển đến trang khác thông qua MenuNavigation
                        menuNavigation.navigateTo(itemId);
                        // Không finish() Activity hiện tại để giữ lại trong back stack
                        return true;
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo Home luôn được highlight khi quay lại Activity này
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupRecyclerViews() {
        bannerAdapter = new BannerAdapter(this);
        LinearLayoutManager bannerLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        rvBanner.setLayoutManager(bannerLayoutManager);
        rvBanner.setAdapter(bannerAdapter);

        bannerAdapter.setOnBannerClickListener(banner -> {
            if (banner.getLinkUrl() != null && !banner.getLinkUrl().isEmpty()) {
                openUrl(banner.getLinkUrl());
            }
        });

        // 1. Dùng PagerSnapHelper để cuộn từng item một (Quan trọng cho dot indicator)
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvBanner);

        // 2. Thêm OnScrollListener để cập nhật dot
        rvBanner.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Chỉ kiểm tra khi cuộn đã dừng
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(bannerLayoutManager);
                    if (centerView != null) {
                        int snapPosition = bannerLayoutManager.getPosition(centerView);
                        if (currentBannerPosition != snapPosition) {
                            currentBannerPosition = snapPosition;
                            updateDotIndicator(); // <--- Gọi hàm cập nhật dot
                        }
                    }
                }
            }
        });

        // THIẾT LẬP SPORTS VENUES
        // 1. Dùng FacilityGroupAdapter mới
        facilityGroupAdapter = new FacilityGroupAdapter(this, new FacilityAdapter.OnFacilityClickListener() {
            @Override
            public void onFacilityClick(FacilityDTO facility) {
                openFacilityDetail(facility);
            }

            @Override
            public void onBookClick(FacilityDTO facility) {
                bookFacility(facility);
            }
        });

        // 2. Đổi LayoutManager sang HOẠT ĐỘNG NGANG (HORIZONTAL)
        LinearLayoutManager facilityLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);

        rvSportsVenues.setLayoutManager(facilityLayoutManager);
        rvSportsVenues.setAdapter(facilityGroupAdapter); // Sử dụng adapter nhóm mới

        // 3. Thêm PagerSnapHelper
        PagerSnapHelper facilitySnapHelper = new PagerSnapHelper();
        facilitySnapHelper.attachToRecyclerView(rvSportsVenues);

        rvSportsVenues.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Chỉ kiểm tra khi cuộn đã dừng
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = facilitySnapHelper.findSnapView(facilityLayoutManager);
                    if (centerView != null) {
                        int snapPosition = facilityLayoutManager.getPosition(centerView);
                        if (currentFacilityGroupPosition != snapPosition) {
                            currentFacilityGroupPosition = snapPosition;
                            updateFacilityDotIndicator(); // <--- Gọi hàm cập nhật dot mới
                        }
                    }
                }
            }
        });
    }

    private List<List<FacilityDTO>> groupFacilities(List<FacilityDTO> facilities, int groupSize) {
        List<List<FacilityDTO>> groupedList = new ArrayList<>();
        if (facilities == null || facilities.isEmpty()) {
            return groupedList;
        }

        for (int i = 0; i < facilities.size(); i += groupSize) {
            int endIndex = Math.min(i + groupSize, facilities.size());
            groupedList.add(facilities.subList(i, endIndex));
        }
        return groupedList;
    }

    /**
     * Tạo các dot indicator dựa trên số lượng banner.
     */
    private void setupDotIndicator(int dotCount) {
        if (dotsContainer == null) return;

        dotsContainer.removeAllViews(); // Xóa các dot cũ

        for (int i = 0; i < dotCount; i++) {
            View dot = new View(this);
            // Kích thước của dot inactive (8dp)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.dot_inactive_size),
                    (int) getResources().getDimension(R.dimen.dot_inactive_size));
            params.setMargins(
                    (int) getResources().getDimension(R.dimen.dot_margin),
                    0,
                    (int) getResources().getDimension(R.dimen.dot_margin),
                    0);

            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive); // dot_inactive là màu xám/trắng
            dotsContainer.addView(dot);
        }

        // Cập nhật trạng thái ban đầu
        currentBannerPosition = 0;
        updateDotIndicator();
    }

    /**
     * Cập nhật trạng thái của dot (Active/Inactive)
     */
    private void updateDotIndicator() {
        int dotCount = dotsContainer.getChildCount();
        if (dotCount == 0) return;

        for (int i = 0; i < dotCount; i++) {
            View dot = dotsContainer.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();

            if (i == currentBannerPosition) {
                // Dot Active: Kích thước lớn hơn (10dp) và màu active
                dot.setBackgroundResource(R.drawable.dot_active); // dot_active là màu xanh
                params.width = (int) getResources().getDimension(R.dimen.dot_active_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_active_size);
            } else {
                // Dot Inactive: Kích thước nhỏ hơn (8dp) và màu inactive
                dot.setBackgroundResource(R.drawable.dot_inactive);
                params.width = (int) getResources().getDimension(R.dimen.dot_inactive_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_inactive_size);
            }
            dot.setLayoutParams(params);
        }
    }

    /**
     * Tạo các dot indicator dựa trên số lượng NHÓM (trang) sân.
     */
    private void setupFacilityDotIndicator(int dotCount) {
        if (rvFacilitiesDotsContainer == null) return;

        rvFacilitiesDotsContainer.removeAllViews(); // Xóa các dot cũ

        for (int i = 0; i < dotCount; i++) {
            View dot = new View(this);
            // Kích thước của dot inactive (8dp)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.dot_inactive_size),
                    (int) getResources().getDimension(R.dimen.dot_inactive_size));
            params.setMargins(
                    (int) getResources().getDimension(R.dimen.dot_margin),
                    0,
                    (int) getResources().getDimension(R.dimen.dot_margin),
                    0);

            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            rvFacilitiesDotsContainer.addView(dot);
        }

        // Cập nhật trạng thái ban đầu
        currentFacilityGroupPosition = 0;
        updateFacilityDotIndicator();
    }

    /**
     * Cập nhật trạng thái của dot (Active/Inactive) cho Sân
     */
    private void updateFacilityDotIndicator() {
        if (rvFacilitiesDotsContainer == null) return;
        int dotCount = rvFacilitiesDotsContainer.getChildCount();
        if (dotCount == 0) return;

        for (int i = 0; i < dotCount; i++) {
            View dot = rvFacilitiesDotsContainer.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();

            if (i == currentFacilityGroupPosition) {
                // Dot Active
                dot.setBackgroundResource(R.drawable.dot_active);
                params.width = (int) getResources().getDimension(R.dimen.dot_active_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_active_size);
            } else {
                // Dot Inactive
                dot.setBackgroundResource(R.drawable.dot_inactive);
                params.width = (int) getResources().getDimension(R.dimen.dot_inactive_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_inactive_size);
            }
            dot.setLayoutParams(params);
        }
    }

    private void setupApiService() {
        apiService = ApiClient.getApiService();
    }

    private void setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cancellationTokenSource = new CancellationTokenSource();
    }

    private void requestLocationAndLoadData() {
        if (checkLocationPermission()) {
            Log.d(TAG, "Permission already granted");
            showLoadingDialog("Đang lấy vị trí của bạn...");
            getCurrentLocationAndLoad();
        } else {
            Log.d(TAG, "Requesting permission");
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void getCurrentLocationAndLoad() {
        if (!checkLocationPermission()) {
            currentLat = DEFAULT_LAT;
            currentLng = DEFAULT_LNG;
            updateLoadingMessage("Đang tải dữ liệu...");
            loadHomeData();
            return;
        }

        try {
            updateLoadingMessage("Đang xác định vị trí...");

            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            ).addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                    Log.d(TAG, "GPS Location: " + currentLat + ", " + currentLng);
                    updateLoadingMessage("Đang tải dữ liệu gần bạn...");
                } else {
                    Log.w(TAG, "Location is null, using default");
                    currentLat = DEFAULT_LAT;
                    currentLng = DEFAULT_LNG;
                    updateLoadingMessage("Đang tải dữ liệu...");
                }
                // Tiếp tục load data, loading dialog vẫn hiển thị
                loadHomeData();

            }).addOnFailureListener(this, e -> {
                Log.e(TAG, "Failed to get location: " + e.getMessage(), e);
                currentLat = DEFAULT_LAT;
                currentLng = DEFAULT_LNG;
                updateLoadingMessage("Đang tải dữ liệu...");
                loadHomeData();
            });

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage(), e);
            currentLat = DEFAULT_LAT;
            currentLng = DEFAULT_LNG;
            updateLoadingMessage("Đang tải dữ liệu...");
            loadHomeData();
        }
    }

    private void loadHomeData() {
        Log.d(TAG, "Loading home data with location: " + currentLat + ", " + currentLng);

        apiService.getHomePageData(currentLat, currentLng)
                .enqueue(new Callback<HomeResponse>() {
                    @Override
                    public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                        hideLoadingDialog();

                        if (response.isSuccessful() && response.body() != null) {
                            HomeResponse homeResponse = response.body();

                            if ("200".equals(homeResponse.getCode()) && homeResponse.getData() != null) {
                                if (homeResponse.getData().getBanners() != null) {
                                    bannerAdapter.setBannerList(homeResponse.getData().getBanners());
                                    setupDotIndicator(homeResponse.getData().getBanners().size());
                                    Log.d(TAG, "Loaded " + homeResponse.getData().getBanners().size() + " banners");
                                }

                                if (homeResponse.getData().getFeaturedFacilities() != null) {
                                    List<FacilityDTO> facilities = homeResponse.getData().getFeaturedFacilities();

                                    // **BƯỚC CHÍNH: NHÓM DỮ LIỆU SÂN**
                                    List<List<FacilityDTO>> facilityGroups = groupFacilities(facilities, 3);

                                    // Cập nhật adapter nhóm mới
                                    facilityGroupAdapter.setFacilityGroupList(facilityGroups);

                                    // Cập nhật dot indicator cho sân (đã được sửa trong yêu cầu trước)
                                    setupFacilityDotIndicator(facilityGroups.size());

                                    Log.d(TAG, "Loaded " + facilities.size() + " facilities, grouped into " + facilityGroups.size() + " pages.");
                                }

                                Toast.makeText(HomeActivity.this, "Đã tải xong!", Toast.LENGTH_SHORT).show();
                            } else {
                                showError("Lỗi: " + homeResponse.getMessage());
                            }
                        } else {
                            showError("Không thể tải dữ liệu trang chủ");
                            Log.e(TAG, "Response not successful: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<HomeResponse> call, Throwable t) {
                        hideLoadingDialog();
                        showError("Lỗi kết nối: " + t.getMessage());
                        Log.e(TAG, "API call failed", t);
                    }
                });
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null) {
            try {
                loadingDialog.show();
                Log.d(TAG, "Loading: " + message);
            } catch (Exception e) {
                Log.e(TAG, "Error showing loading dialog", e);
            }
        }
    }

    private void updateLoadingMessage(String message) {
        Log.d(TAG, "Loading: " + message);
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            try {
                loadingDialog.dismiss();
                Log.d(TAG, "Loading dismissed");
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing loading dialog", e);
            }
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening URL", e);
        }
    }

    private void openFacilityDetail(FacilityDTO facility) {
        // Chuyển sang EventsActivity và truyền facilityId
        Intent intent = new Intent(HomeActivity.this, EventsActivity.class);
        intent.putExtra("facilityId", facility.getFacilityId());
        intent.putExtra("facilityName", facility.getFacilityName());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void bookFacility(FacilityDTO facility) {
        Toast.makeText(this, "Đặt sân: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
    }

    private void openMapActivity() {
        Intent intent = new Intent(HomeActivity.this, com.datn06.pickleconnect.Map.MapActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
        if (loadingDialog != null) {
            try {
                loadingDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing dialog in onDestroy", e);
            }
        }
    }


}