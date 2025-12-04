package com.datn06.pickleconnect.Home;

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

import com.bumptech.glide.Glide;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.API.AppConfig;
import com.datn06.pickleconnect.API.MemberApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Booking.FieldSelectionActivity;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Court.CourtDetailActivity;
import com.datn06.pickleconnect.Models.MemberInfoRequest;
import com.datn06.pickleconnect.Models.MemberInfoResponse;
import com.datn06.pickleconnect.Profile.ProfileActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Adapter.BannerAdapter;
import com.datn06.pickleconnect.Adapter.FacilityAdapter;
import com.datn06.pickleconnect.Adapter.FacilityGroupAdapter;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.Home.HomeResponse;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.hdodenhof.circleimageview.CircleImageView;

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
    private CircleImageView ivProfile;  // ✅ Profile avatar
    private int currentFacilityGroupPosition = 0;

    private BannerAdapter bannerAdapter;
    private FacilityGroupAdapter facilityGroupAdapter;
    private ApiService apiService;
    private MemberApiService memberApiService;  // ✅ For loading profile data

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    private LoadingDialog loadingDialog;
    private MenuNavigation menuNavigation;
    private TokenManager tokenManager;  // ✅ Token manager
    private int currentBannerPosition = 0;

    private boolean isDataLoaded = false;

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

        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // ✅ Initialize TokenManager and MemberApiService
        tokenManager = TokenManager.getInstance(this);
        memberApiService = ApiClient.createService(ServiceHost.MEMBER_SERVICE, MemberApiService.class);

        initViews();
        setupSearchListeners();
        setupRecyclerViews();
        setupApiService();
        setupLocationClient();
        setupBottomNavigation();
        dotsContainer = findViewById(R.id.dotsContainer);

        loadingDialog = new LoadingDialog(this);
        menuNavigation = new MenuNavigation(this);

        if (savedInstanceState == null) {
            requestLocationAndLoadData();
        } else {
            isDataLoaded = true;
        }

        // ✅ Load user avatar
        loadUserAvatar();
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
        ivProfile = findViewById(R.id.ivProfile);  // ✅ Profile avatar

        if (btnFindNearby != null) {
            btnFindNearby.setOnClickListener(v -> openMapActivity());
        }

        if (mapCard != null) {
            mapCard.setOnClickListener(v -> openMapActivity());
        }

        findViewById(R.id.ivSeeMore).setOnClickListener(v -> openCourtList());

        // ✅ Profile avatar click -> Open ProfileActivity
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> openProfileActivity());
        }
    }

    private void setupSearchListeners() {
        if (etSearch != null) {
            etSearch.setOnClickListener(v -> openSearchActivity());
            etSearch.setFocusable(false);
            etSearch.setClickable(true);
        }

        if (searchCard != null) {
            searchCard.setOnClickListener(v -> openSearchActivity());
        }
    }

    private void openSearchActivity() {
        Intent intent = new Intent(HomeActivity.this, com.datn06.pickleconnect.Search.SearchActivity.class);
        intent.putExtra("userLat", currentLat);
        intent.putExtra("userLng", currentLng);
        Log.d(TAG, "Opening SearchActivity with location: " + currentLat + ", " + currentLng);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        return true;
                    } else {
                        menuNavigation.navigateTo(itemId);
                        return true;
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // ✅ Reload avatar when returning to HomeActivity
        loadUserAvatar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d(TAG, "onNewIntent called - Activity reused, not reloading data");

        boolean forceRefresh = intent.getBooleanExtra("forceRefresh", false);
        if (forceRefresh) {
            Log.d(TAG, "Force refresh requested");
            loadHomeData();
            loadUserAvatar();  // ✅ Reload avatar
        }
    }

    // ✅ NEW: Load user avatar from database
    private void loadUserAvatar() {
        if (!tokenManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, using default avatar");
            return;
        }

        String currentUserId = tokenManager.getUserId();
        String currentEmail = tokenManager.getEmail();
        String currentPhone = tokenManager.getPhoneNumber();

        Log.d(TAG, "Loading avatar for userId: " + currentUserId);

        MemberInfoRequest request = new MemberInfoRequest(
                currentUserId,
                currentEmail,
                currentPhone
        );

        memberApiService.getMemberInfo(request).enqueue(new Callback<BaseResponse<MemberInfoResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberInfoResponse>> call,
                                   Response<BaseResponse<MemberInfoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<MemberInfoResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        MemberInfoResponse data = baseResponse.getData();
                        updateProfileAvatar(data.getAvatarUrl());
                    } else {
                        Log.w(TAG, "Failed to load avatar: " + baseResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "Avatar API response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberInfoResponse>> call, Throwable t) {
                Log.e(TAG, "Avatar API call failed: " + t.getMessage(), t);
            }
        });
    }

    // ✅ NEW: Update profile avatar using Glide
    private void updateProfileAvatar(String avatarUrl) {
        if (ivProfile != null && avatarUrl != null && !avatarUrl.isEmpty()) {
            // Convert localhost MinIO URL to public ngrok URL
            String imageUrl = AppConfig.fixImageUrl(avatarUrl);
            Log.d(TAG, "Loading avatar from: " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.avartar_placeholder)
                    .error(R.drawable.avartar_placeholder)
                    .into(ivProfile);
        } else {
            Log.w(TAG, "Avatar URL is null or empty");
        }
    }

    // ✅ NEW: Open ProfileActivity when clicking avatar
    private void openProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvBanner);

        rvBanner.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(bannerLayoutManager);
                    if (centerView != null) {
                        int snapPosition = bannerLayoutManager.getPosition(centerView);
                        if (currentBannerPosition != snapPosition) {
                            currentBannerPosition = snapPosition;
                            updateDotIndicator();
                        }
                    }
                }
            }
        });

        facilityGroupAdapter = new FacilityGroupAdapter(this, new FacilityAdapter.OnFacilityClickListener() {
            @Override
            public void onFacilityClick(FacilityDTO facility) {
                openCourtDetail(facility);
            }

            @Override
            public void onBookClick(FacilityDTO facility) {
                openFieldSelection(facility);
            }
        });

        LinearLayoutManager facilityLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);

        rvSportsVenues.setLayoutManager(facilityLayoutManager);
        rvSportsVenues.setAdapter(facilityGroupAdapter);

        PagerSnapHelper facilitySnapHelper = new PagerSnapHelper();
        facilitySnapHelper.attachToRecyclerView(rvSportsVenues);

        rvSportsVenues.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = facilitySnapHelper.findSnapView(facilityLayoutManager);
                    if (centerView != null) {
                        int snapPosition = facilityLayoutManager.getPosition(centerView);
                        if (currentFacilityGroupPosition != snapPosition) {
                            currentFacilityGroupPosition = snapPosition;
                            updateFacilityDotIndicator();
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

    private void setupDotIndicator(int dotCount) {
        if (dotsContainer == null) return;

        dotsContainer.removeAllViews();

        for (int i = 0; i < dotCount; i++) {
            View dot = new View(this);
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
            dotsContainer.addView(dot);
        }

        currentBannerPosition = 0;
        updateDotIndicator();
    }

    private void updateDotIndicator() {
        int dotCount = dotsContainer.getChildCount();
        if (dotCount == 0) return;

        for (int i = 0; i < dotCount; i++) {
            View dot = dotsContainer.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();

            if (i == currentBannerPosition) {
                dot.setBackgroundResource(R.drawable.dot_active);
                params.width = (int) getResources().getDimension(R.dimen.dot_active_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_active_size);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
                params.width = (int) getResources().getDimension(R.dimen.dot_inactive_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_inactive_size);
            }
            dot.setLayoutParams(params);
        }
    }

    private void setupFacilityDotIndicator(int dotCount) {
        if (rvFacilitiesDotsContainer == null) return;

        rvFacilitiesDotsContainer.removeAllViews();

        for (int i = 0; i < dotCount; i++) {
            View dot = new View(this);
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

        currentFacilityGroupPosition = 0;
        updateFacilityDotIndicator();
    }

    private void updateFacilityDotIndicator() {
        if (rvFacilitiesDotsContainer == null) return;
        int dotCount = rvFacilitiesDotsContainer.getChildCount();
        if (dotCount == 0) return;

        for (int i = 0; i < dotCount; i++) {
            View dot = rvFacilitiesDotsContainer.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();

            if (i == currentFacilityGroupPosition) {
                dot.setBackgroundResource(R.drawable.dot_active);
                params.width = (int) getResources().getDimension(R.dimen.dot_active_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_active_size);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
                params.width = (int) getResources().getDimension(R.dimen.dot_inactive_size);
                params.height = (int) getResources().getDimension(R.dimen.dot_inactive_size);
            }
            dot.setLayoutParams(params);
        }
    }

    private void setupApiService() {
        apiService = ApiClient.createService(ServiceHost.API_SERVICE, ApiService.class);
        Log.d(TAG, "API Service initialized for port 9003");
    }

    private void setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cancellationTokenSource = new CancellationTokenSource();
    }

    private void requestLocationAndLoadData() {
        if (isDataLoaded) {
            Log.d(TAG, "Data already loaded, skipping");
            return;
        }

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
                                    List<List<FacilityDTO>> facilityGroups = groupFacilities(facilities, 3);
                                    facilityGroupAdapter.setFacilityGroupList(facilityGroups);
                                    setupFacilityDotIndicator(facilityGroups.size());
                                    Log.d(TAG, "Loaded " + facilities.size() + " facilities, grouped into " + facilityGroups.size() + " pages.");
                                }

                                isDataLoaded = true;

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

    private void openCourtDetail(FacilityDTO facility) {
        Intent intent = new Intent(HomeActivity.this, CourtDetailActivity.class);
        intent.putExtra("facilityId", facility.getFacilityId());

        Log.d(TAG, "Opening CourtDetailActivity for facility: " + facility.getFacilityName());

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openFieldSelection(FacilityDTO facility) {
        Intent intent = new Intent(HomeActivity.this, FieldSelectionActivity.class);
        intent.putExtra("facilityId", facility.getFacilityId());
        intent.putExtra("facilityName", facility.getFacilityName());

        Log.d(TAG, "Opening FieldSelectionActivity for facility: " + facility.getFacilityName());

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openMapActivity() {
        Intent intent = new Intent(HomeActivity.this, com.datn06.pickleconnect.Map.MapActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openCourtList() {
        Intent intent = new Intent(HomeActivity.this, com.datn06.pickleconnect.Court.CourtListActivity.class);
        intent.putExtra("userLatitude", currentLat);
        intent.putExtra("userLongitude", currentLng);
        startActivity(intent);
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