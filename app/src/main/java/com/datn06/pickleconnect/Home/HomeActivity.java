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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Adapter.BannerAdapter;
import com.datn06.pickleconnect.Adapter.FacilityAdapter;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.Home.HomeResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
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
    private View progressBar;
    private Button btnFindNearby;
    private androidx.cardview.widget.CardView mapCard;

    private BannerAdapter bannerAdapter;
    private FacilityAdapter facilityAdapter;
    private ApiService apiService;

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    Log.d(TAG, "Permission granted");
                    getCurrentLocationAndLoad();
                } else {
                    Log.w(TAG, "Permission denied");
                    Toast.makeText(this, "Quyền truy cập vị trí bị từ chối. Sử dụng vị trí mặc định (Hà Nội)",
                            Toast.LENGTH_LONG).show();
                    currentLat = DEFAULT_LAT;
                    currentLng = DEFAULT_LNG;
                    loadHomeData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerViews();
        setupApiService();
        setupLocationClient();
        requestLocationAndLoadData();
    }

    private void initViews() {
        rvBanner = findViewById(R.id.rvBanner);
        rvSportsVenues = findViewById(R.id.rvSportsVenues);
        progressBar = findViewById(R.id.progressBar);
        btnFindNearby = findViewById(R.id.btnFindNearby);
        mapCard = findViewById(R.id.mapCard);

        if (btnFindNearby != null) {
            btnFindNearby.setOnClickListener(v -> openMapActivity());
        }

        if (mapCard != null) {
            mapCard.setOnClickListener(v -> openMapActivity());
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

        facilityAdapter = new FacilityAdapter(this);
        LinearLayoutManager facilityLayoutManager = new LinearLayoutManager(this);
        rvSportsVenues.setLayoutManager(facilityLayoutManager);
        rvSportsVenues.setAdapter(facilityAdapter);

        facilityAdapter.setOnFacilityClickListener(new FacilityAdapter.OnFacilityClickListener() {
            @Override
            public void onFacilityClick(FacilityDTO facility) {
                openFacilityDetail(facility);
            }

            @Override
            public void onBookClick(FacilityDTO facility) {
                bookFacility(facility);
            }
        });
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
            loadHomeData();
            return;
        }

        try {
            showLoading();

            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            ).addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                    Log.d(TAG, "GPS Location: " + currentLat + ", " + currentLng);
                    Toast.makeText(this, "Đã lấy vị trí của bạn", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Location is null, using default");
                    currentLat = DEFAULT_LAT;
                    currentLng = DEFAULT_LNG;
                    Toast.makeText(this, "Sử dụng vị trí mặc định (Hà Nội)", Toast.LENGTH_SHORT).show();
                }
                loadHomeData();

            }).addOnFailureListener(this, e -> {
                Log.e(TAG, "Failed to get location: " + e.getMessage(), e);
                Toast.makeText(this, "Không thể lấy vị trí. Sử dụng vị trí mặc định", Toast.LENGTH_SHORT).show();
                currentLat = DEFAULT_LAT;
                currentLng = DEFAULT_LNG;
                loadHomeData();
            });

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage(), e);
            currentLat = DEFAULT_LAT;
            currentLng = DEFAULT_LNG;
            loadHomeData();
        }
    }

    private void loadHomeData() {
        showLoading();
        Log.d(TAG, "Loading home data with location: " + currentLat + ", " + currentLng);

        apiService.getHomePageData(currentLat, currentLng)
                .enqueue(new Callback<HomeResponse>() {
                    @Override
                    public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                        hideLoading();

                        if (response.isSuccessful() && response.body() != null) {
                            HomeResponse homeResponse = response.body();

                            if ("200".equals(homeResponse.getCode()) && homeResponse.getData() != null) {
                                if (homeResponse.getData().getBanners() != null) {
                                    bannerAdapter.setBannerList(homeResponse.getData().getBanners());
                                    Log.d(TAG, "Loaded " + homeResponse.getData().getBanners().size() + " banners");
                                }

                                if (homeResponse.getData().getFeaturedFacilities() != null) {
                                    facilityAdapter.setFacilityList(homeResponse.getData().getFeaturedFacilities());
                                    Log.d(TAG, "Loaded " + homeResponse.getData().getFeaturedFacilities().size() + " facilities");
                                }
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
                        hideLoading();
                        showError("Lỗi kết nối: " + t.getMessage());
                        Log.e(TAG, "API call failed", t);
                    }
                });
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
        Toast.makeText(this, "Xem chi tiết: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
    }

    private void bookFacility(FacilityDTO facility) {
        Toast.makeText(this, "Đặt sân: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
    }

    private void openMapActivity() {
        Intent intent = new Intent(HomeActivity.this, com.datn06.pickleconnect.Map.MapActivity.class);
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
    }
}