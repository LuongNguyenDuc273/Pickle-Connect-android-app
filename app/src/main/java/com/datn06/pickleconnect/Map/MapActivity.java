package com.datn06.pickleconnect.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.Home.HomeResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final double DEFAULT_LAT = 21.0285;
    private static final double DEFAULT_LNG = 105.8542;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    // ✅ UPDATED: Kept as field for consistency
    private ApiService apiService;

    private LoadingDialog loadingDialog;

    private double currentLat = DEFAULT_LAT;
    private double currentLng = DEFAULT_LNG;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ ADDED: Initialize API Service first
        initApiService();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cancellationTokenSource = new CancellationTokenSource();
        loadingDialog = new LoadingDialog(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ✅ ADDED: Initialize API Service with correct port
    private void initApiService() {
        apiService = ApiClient.createService(ServiceHost.API_SERVICE, ApiService.class);
        Log.d(TAG, "API Service initialized for port 9003 (MapActivity)");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof FacilityDTO) {
                FacilityDTO facility = (FacilityDTO) tag;
                Toast.makeText(this, "Facility ID: " + facility.getFacilityId(), Toast.LENGTH_SHORT).show();
            }
        });

        if (checkLocationPermission()) {
            enableMyLocation();
            getCurrentLocationAndLoadFacilities();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocationAndLoadFacilities();
            } else {
                Toast.makeText(this, "Cần quyền truy cập vị trí để hiển thị bản đồ", Toast.LENGTH_LONG).show();
                loadFacilitiesWithDefaultLocation();
            }
        }
    }

    private void enableMyLocation() {
        if (checkLocationPermission() && mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.e(TAG, "Error enabling my location", e);
            }
        }
    }

    private void getCurrentLocationAndLoadFacilities() {
        if (!checkLocationPermission()) {
            loadFacilitiesWithDefaultLocation();
            return;
        }

        try {
            loadingDialog.show();

            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            ).addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                    Log.d(TAG, "Got location: " + currentLat + ", " + currentLng);
                    addCurrentLocationMarker();
                    loadFacilities();
                } else {
                    Log.w(TAG, "Location is null, using default");
                    loadFacilitiesWithDefaultLocation();
                }
            }).addOnFailureListener(this, e -> {
                Log.e(TAG, "Failed to get location", e);
                loadFacilitiesWithDefaultLocation();
            });

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
            loadFacilitiesWithDefaultLocation();
        }
    }

    private void loadFacilitiesWithDefaultLocation() {
        currentLat = DEFAULT_LAT;
        currentLng = DEFAULT_LNG;
        addCurrentLocationMarker();
        loadFacilities();
    }

    private void addCurrentLocationMarker() {
        if (mMap == null) return;

        LatLng currentLocation = new LatLng(currentLat, currentLng);

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Vị trí của bạn")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13f));
    }

    private void loadFacilities() {
        // ✅ FIXED: Use the initialized apiService with correct port
        apiService.getHomePageData(currentLat, currentLng)
                .enqueue(new Callback<HomeResponse>() {
                    @Override
                    public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                        loadingDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            HomeResponse homeResponse = response.body();

                            if ("200".equals(homeResponse.getCode()) && homeResponse.getData() != null) {
                                List<FacilityDTO> facilities = homeResponse.getData().getFeaturedFacilities();
                                if (facilities != null && !facilities.isEmpty()) {
                                    addFacilityMarkers(facilities);
                                    Log.d(TAG, "Added " + facilities.size() + " facility markers");
                                } else {
                                    Toast.makeText(MapActivity.this,
                                            "Không tìm thấy sân nào gần bạn", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(MapActivity.this,
                                    "Không thể tải dữ liệu sân", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<HomeResponse> call, Throwable t) {
                        loadingDialog.dismiss();
                        Toast.makeText(MapActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "API call failed", t);
                    }
                });
    }

    private void addFacilityMarkers(List<FacilityDTO> facilities) {
        if (mMap == null || facilities == null) return;

        for (FacilityDTO facility : facilities) {
            if (facility.getLatitude() != null && facility.getLongitude() != null) {
                LatLng position = new LatLng(facility.getLatitude(), facility.getLongitude());

                String snippet = facility.getFullAddress();
                if (facility.getDistanceKm() != null) {
                    snippet += "\nCách bạn: " + String.format("%.1f km", facility.getDistanceKm());
                }

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(facility.getFacilityName())
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                marker.setTag(facility);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
    }
}