package com.datn06.pickleconnect.Court;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Model.CityDTO;
import com.datn06.pickleconnect.Model.DistrictDTO;
import com.datn06.pickleconnect.Model.SearchCourtRequest;
import com.datn06.pickleconnect.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "FilterBottomSheet";

    // UI Components
    private RadioGroup radioGroupFilterMode;
    private RadioButton radioKhuVuc, radioKhoangCach;
    private LinearLayout containerKhuVuc, containerKhoangCach;
    private LinearLayout layoutSelectCity, layoutSelectDistrict;
    private TextView tvSelectedCity, tvSelectedDistrict;
    private TextView tvCurrentLocation, tvDistanceValue;
    private SeekBar seekBarDistance;
    private MaterialButton btnCancel, btnApply;

    // Data
    private List<CityDTO> cityList = new ArrayList<>();
    private List<DistrictDTO> districtList = new ArrayList<>();
    private CityDTO selectedCity;
    private DistrictDTO selectedDistrict;
    private double maxDistance = 10.0;

    // User location
    private double userLatitude;
    private double userLongitude;

    // API
    private CourtApiService courtApiService;

    // Callback
    private OnFilterApplyListener listener;

    public interface OnFilterApplyListener {
        void onApply(SearchCourtRequest request);
    }

    public static FilterBottomSheet newInstance(double userLat, double userLng) {
        FilterBottomSheet fragment = new FilterBottomSheet();
        Bundle args = new Bundle();
        args.putDouble("userLatitude", userLat);
        args.putDouble("userLongitude", userLng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLatitude = getArguments().getDouble("userLatitude", 21.0285);
            userLongitude = getArguments().getDouble("userLongitude", 105.8542);
        }
        courtApiService = ApiClient.createService(ServiceHost.COURT_SERVICE, CourtApiService.class);
    }

    public void setOnFilterApplyListener(OnFilterApplyListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            // Ensure bottom sheet is fully expanded
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        loadCities();
    }

    private void initViews(View view) {
        radioGroupFilterMode = view.findViewById(R.id.radioGroupFilterMode);
        radioKhuVuc = view.findViewById(R.id.radioKhuVuc);
        radioKhoangCach = view.findViewById(R.id.radioKhoangCach);
        
        containerKhuVuc = view.findViewById(R.id.containerKhuVuc);
        containerKhoangCach = view.findViewById(R.id.containerKhoangCach);
        
        layoutSelectCity = view.findViewById(R.id.layoutSelectCity);
        layoutSelectDistrict = view.findViewById(R.id.layoutSelectDistrict);
        tvSelectedCity = view.findViewById(R.id.tvSelectedCity);
        tvSelectedDistrict = view.findViewById(R.id.tvSelectedDistrict);
        
        tvCurrentLocation = view.findViewById(R.id.tvCurrentLocation);
        tvDistanceValue = view.findViewById(R.id.tvDistanceValue);
        seekBarDistance = view.findViewById(R.id.seekBarDistance);
        
        btnCancel = view.findViewById(R.id.btnCancel);
        btnApply = view.findViewById(R.id.btnApply);
    }

    private void setupListeners() {
        // RadioGroup để chuyển đổi giữa 2 chế độ
        radioGroupFilterMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioKhuVuc) {
                // Hiện container Khu vực, ẩn Khoảng cách
                containerKhuVuc.setVisibility(View.VISIBLE);
                containerKhoangCach.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioKhoangCach) {
                // Hiện container Khoảng cách, ẩn Khu vực
                containerKhuVuc.setVisibility(View.GONE);
                containerKhoangCach.setVisibility(View.VISIBLE);
            }
        });

        // Click chọn Tỉnh/TP
        layoutSelectCity.setOnClickListener(v -> showCityDialog());

        // Click chọn Quận/Huyện
        layoutSelectDistrict.setOnClickListener(v -> {
            if (selectedCity != null) {
                showDistrictDialog();
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn Tỉnh/TP trước", Toast.LENGTH_SHORT).show();
            }
        });

        // SeekBar khoảng cách
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxDistance = progress;
                tvDistanceValue.setText("Khoảng cách (" + progress + " km)");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Button Cancel - Reset filters
        btnCancel.setOnClickListener(v -> resetFilters());

        // Button Apply - Áp dụng filter
        btnApply.setOnClickListener(v -> applyFilter());
    }

    /**
     * Load danh sách tỉnh/thành phố từ API
     */
    private void loadCities() {
        courtApiService.getCities().enqueue(new Callback<List<CityDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<CityDTO>> call, @NonNull Response<List<CityDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cityList = response.body();
                    Log.d(TAG, "Loaded " + cityList.size() + " cities");
                } else {
                    Log.e(TAG, "Failed to load cities: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CityDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading cities", t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load danh sách quận/huyện theo city_id
     */
    private void loadDistricts(BigInteger cityId) {
        courtApiService.getDistricts(cityId.toString()).enqueue(new Callback<List<DistrictDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<DistrictDTO>> call, @NonNull Response<List<DistrictDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districtList = response.body();
                    Log.d(TAG, "Loaded " + districtList.size() + " districts for city " + cityId);
                    
                    // Enable district selection
                    layoutSelectDistrict.setEnabled(true);
                    layoutSelectDistrict.setAlpha(1.0f);
                } else {
                    Log.e(TAG, "Failed to load districts: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DistrictDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading districts", t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiện dialog chọn Tỉnh/TP
     */
    private void showCityDialog() {
        if (cityList.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải danh sách...", Toast.LENGTH_SHORT).show();
            return;
        }

        CitySelectionDialog dialog = CitySelectionDialog.newInstance(cityList);
        dialog.setOnCitySelectedListener(city -> {
            selectedCity = city;
            selectedDistrict = null; // Reset district khi đổi city
            districtList.clear();
            
            tvSelectedCity.setText(city.getName());
            tvSelectedCity.setTextColor(getResources().getColor(R.color.black, null));
            
            tvSelectedDistrict.setText("Chọn Quận/Huyện");
            tvSelectedDistrict.setTextColor(getResources().getColor(R.color.text_hint, null));
            
            // Load districts cho city này
            loadDistricts(city.getCityId());
        });
        dialog.show(getParentFragmentManager(), "CitySelectionDialog");
    }

    /**
     * Hiện dialog chọn Quận/Huyện
     */
    private void showDistrictDialog() {
        if (districtList.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải danh sách...", Toast.LENGTH_SHORT).show();
            return;
        }

        DistrictSelectionDialog dialog = DistrictSelectionDialog.newInstance(districtList);
        dialog.setOnDistrictSelectedListener(district -> {
            selectedDistrict = district;
            tvSelectedDistrict.setText(district.getDistrictName());
            tvSelectedDistrict.setTextColor(getResources().getColor(R.color.black, null));
        });
        dialog.show(getParentFragmentManager(), "DistrictSelectionDialog");
    }

    /**
     * Reset tất cả filters và quay về load ALL
     */
    private void resetFilters() {
        selectedCity = null;
        selectedDistrict = null;
        districtList.clear();
        
        tvSelectedCity.setText("Chọn Tỉnh/TP");
        tvSelectedCity.setTextColor(getResources().getColor(R.color.text_hint, null));
        
        tvSelectedDistrict.setText("Chọn Quận/Huyện");
        tvSelectedDistrict.setTextColor(getResources().getColor(R.color.text_hint, null));
        
        layoutSelectDistrict.setEnabled(false);
        layoutSelectDistrict.setAlpha(0.5f);
        
        seekBarDistance.setProgress(10);
        radioKhuVuc.setChecked(true);
        
        // Gọi callback với request rỗng (load ALL)
        if (listener != null) {
            SearchCourtRequest emptyRequest = SearchCourtRequest.builder()
                    .page(0)
                    .size(100)
                    .build();
            listener.onApply(emptyRequest);
        }
        dismiss();
    }

    /**
     * Áp dụng filter và gọi callback
     * KHÔNG validate bắt buộc - cho phép load ALL nếu không chọn gì
     */
    private void applyFilter() {
        SearchCourtRequest request;

        if (radioKhuVuc.isChecked()) {
            // Chế độ Khu vực - tìm kiếm theo Tỉnh/Quận
            if (selectedCity != null) {
                // Có chọn tỉnh - áp dụng filter
                List<String> provinces = new ArrayList<>();
                provinces.add(selectedCity.getName());

                List<String> districts = new ArrayList<>();
                if (selectedDistrict != null) {
                    districts.add(selectedDistrict.getDistrictName());
                }

                request = SearchCourtRequest.builder()
                        .provinces(provinces)
                        .districts(districts.isEmpty() ? null : districts)
                        .page(0)
                        .size(20)
                        .build();

                Log.d(TAG, "Applying AREA filter: Province=" + selectedCity.getName() + 
                      ", District=" + (selectedDistrict != null ? selectedDistrict.getDistrictName() : "All"));
            } else {
                // Không chọn gì - load ALL
                request = SearchCourtRequest.builder()
                        .page(0)
                        .size(100)
                        .build();
                Log.d(TAG, "No area selected - Loading ALL courts");
            }

        } else {
            // Chế độ Khoảng cách - tìm kiếm theo GPS
            request = SearchCourtRequest.builder()
                    .userLatitude(roundToDecimal(userLatitude, 6))
                    .userLongitude(roundToDecimal(userLongitude, 6))
                    .maxDistanceKm(maxDistance)
                    .page(0)
                    .size(20)
                    .build();

            Log.d(TAG, "Applying DISTANCE filter: Distance=" + maxDistance + "km");
        }

        if (listener != null) {
            listener.onApply(request);
        }
        dismiss();
    }

    /**
     * Làm tròn double thành BigDecimal với số chữ số thập phân cố định
     */
    private BigDecimal roundToDecimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }
}

