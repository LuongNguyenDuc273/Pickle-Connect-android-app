package com.datn06.pickleconnect.Search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.Adapter.FacilityAdapter;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchedActivity extends AppCompatActivity {

    private static final String TAG = "SearchedActivity";

    // Sort options
    private static final int SORT_DEFAULT = 0;
    private static final int SORT_RATING_HIGH = 1;
    private static final int SORT_RATING_LOW = 2;
    private static final int SORT_DISTANCE_NEAR = 3;
    private static final int SORT_DISTANCE_FAR = 4;

    // Views
    private ImageView icArrowLeft;
    private ImageView icSearch;
    private EditText searchInput;
    private ImageView icFilter;
    private RecyclerView recyclerViewVenues;
    private BottomNavigationView bottomNavigation;

    // Adapter
    private FacilityAdapter facilityAdapter;

    // API Service
    private ApiService apiService;

    // Navigation
    private MenuNavigation menuNavigation;

    // Data
    private String currentKeyword;
    private Double userLat;
    private Double userLng;
    private List<FacilityDTO> currentFacilities = new ArrayList<>();
    private int currentSortOption = SORT_DEFAULT;

    // PopupWindow
    private PopupWindow filterPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched);

        initViews();
        initApiService();
        setupRecyclerView();
        loadDataFromIntent();
        setupListeners();
        setupBottomNavigation();
        performSearch();
    }

    private void initViews() {
        icArrowLeft = findViewById(R.id.ic_arrow_left);
        icSearch = findViewById(R.id.ic_search);
        searchInput = findViewById(R.id.search_input);
        icFilter = findViewById(R.id.ic_filter);
        recyclerViewVenues = findViewById(R.id.recycler_view_venues);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void initApiService() {
        apiService = ApiClient.getApiService();
        menuNavigation = new MenuNavigation(this);
    }

    private void setupRecyclerView() {
        facilityAdapter = new FacilityAdapter(this);
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

        recyclerViewVenues.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVenues.setAdapter(facilityAdapter);
    }

    private void loadDataFromIntent() {
        currentKeyword = getIntent().getStringExtra("keyword");
        userLat = getIntent().getDoubleExtra("userLat", 21.0285);
        userLng = getIntent().getDoubleExtra("userLng", 105.8542);

        if (currentKeyword != null && !currentKeyword.isEmpty()) {
            searchInput.setText(currentKeyword);
            searchInput.setSelection(currentKeyword.length());
        }

        Log.d(TAG, "Loaded from intent: keyword=" + currentKeyword +
                ", lat=" + userLat + ", lng=" + userLng);
    }

    private void setupListeners() {
        // Nút Back
        icArrowLeft.setOnClickListener(v -> finish());

        // Nút Filter - Hiển thị popup menu
        icFilter.setOnClickListener(v -> showSortPopup(v));

        // Click vào EditText → Chuyển sang SearchActivity
        searchInput.setOnClickListener(v -> openSearchActivity());

        // Tắt focus để tránh bàn phím hiện ra
        searchInput.setFocusable(false);
        searchInput.setClickable(true);

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                String newKeyword = searchInput.getText().toString().trim();
                if (!newKeyword.isEmpty()) {
                    currentKeyword = newKeyword;
                    performSearch();
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        // Click vào icon search cũng chuyển sang SearchActivity
        if (icSearch != null) {
            icSearch.setOnClickListener(v -> openSearchActivity());
        }
    }

    /**
     * Setup Bottom Navigation
     */
    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            // SearchedActivity không thuộc bottom nav nào, nên không set selected
            // Hoặc có thể set nav_search nếu bạn có menu item cho search
            bottomNavigation.setSelectedItemId(R.id.nav_booking);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    // Chuyển đến trang tương ứng
                    menuNavigation.navigateTo(itemId);

                    // Finish SearchedActivity vì user đang chuyển sang trang khác
                    finish();

                    return true;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SearchedActivity không thuộc bottom nav menu nên không cần set selected
    }

    /**
     * Hiển thị popup sort menu
     */
    private void showSortPopup(View anchorView) {
        // Inflate layout
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_filter_menu, null);

        // Tạo PopupWindow
        filterPopupWindow = new PopupWindow(
                popupView,
                (int) (240 * getResources().getDisplayMetrics().density),
                RecyclerView.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set background để có thể dismiss khi click bên ngoài
        filterPopupWindow.setOutsideTouchable(true);
        filterPopupWindow.setFocusable(true);

        // Get views from popup
        RadioGroup radioGroupSort = popupView.findViewById(R.id.radio_group_sort);
        Button btnApply = popupView.findViewById(R.id.btn_apply_sort);

        // Set current selected option
        setCurrentSortSelection(radioGroupSort);

        // Apply button
        btnApply.setOnClickListener(v -> {
            applySorting(radioGroupSort);
            filterPopupWindow.dismiss();
        });

        // Hiển thị popup bên dưới icon filter
        filterPopupWindow.showAsDropDown(anchorView, -180, 10, Gravity.END);
    }

    /**
     * Set selected radio button dựa trên sort hiện tại
     */
    private void setCurrentSortSelection(RadioGroup radioGroupSort) {
        switch (currentSortOption) {
            case SORT_DEFAULT:
                radioGroupSort.check(R.id.radio_sort_default);
                break;
            case SORT_RATING_HIGH:
                radioGroupSort.check(R.id.radio_sort_rating_high);
                break;
            case SORT_RATING_LOW:
                radioGroupSort.check(R.id.radio_sort_rating_low);
                break;
            case SORT_DISTANCE_NEAR:
                radioGroupSort.check(R.id.radio_sort_distance_near);
                break;
            case SORT_DISTANCE_FAR:
                radioGroupSort.check(R.id.radio_sort_distance_far);
                break;
        }
    }

    /**
     * Áp dụng sorting
     */
    private void applySorting(RadioGroup radioGroupSort) {
        int selectedId = radioGroupSort.getCheckedRadioButtonId();

        if (selectedId == R.id.radio_sort_default) {
            currentSortOption = SORT_DEFAULT;
        } else if (selectedId == R.id.radio_sort_rating_high) {
            currentSortOption = SORT_RATING_HIGH;
        } else if (selectedId == R.id.radio_sort_rating_low) {
            currentSortOption = SORT_RATING_LOW;
        } else if (selectedId == R.id.radio_sort_distance_near) {
            currentSortOption = SORT_DISTANCE_NEAR;
        } else if (selectedId == R.id.radio_sort_distance_far) {
            currentSortOption = SORT_DISTANCE_FAR;
        }

        Log.d(TAG, "Sort applied: " + currentSortOption);

        // Sắp xếp lại danh sách hiện tại
        sortFacilities();

        Toast.makeText(this, "Đã áp dụng sắp xếp", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sắp xếp danh sách facilities theo option đã chọn
     */
    private void sortFacilities() {
        if (currentFacilities == null || currentFacilities.isEmpty()) {
            return;
        }

        List<FacilityDTO> sortedList = new ArrayList<>(currentFacilities);

        switch (currentSortOption) {
            case SORT_RATING_HIGH:
                // Sắp xếp rating từ cao đến thấp
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double rating1 = f1.getRating() != null ? f1.getRating() : 0.0;
                        Double rating2 = f2.getRating() != null ? f2.getRating() : 0.0;
                        return rating2.compareTo(rating1); // Giảm dần
                    }
                });
                break;

            case SORT_RATING_LOW:
                // Sắp xếp rating từ thấp đến cao
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double rating1 = f1.getRating() != null ? f1.getRating() : 0.0;
                        Double rating2 = f2.getRating() != null ? f2.getRating() : 0.0;
                        return rating1.compareTo(rating2); // Tăng dần
                    }
                });
                break;

            case SORT_DISTANCE_NEAR:
                // Sắp xếp khoảng cách từ gần đến xa
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double distance1 = f1.getDistanceKm() != null ? f1.getDistanceKm() : Double.MAX_VALUE;
                        Double distance2 = f2.getDistanceKm() != null ? f2.getDistanceKm() : Double.MAX_VALUE;
                        return distance1.compareTo(distance2); // Tăng dần
                    }
                });
                break;

            case SORT_DISTANCE_FAR:
                // Sắp xếp khoảng cách từ xa đến gần
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double distance1 = f1.getDistanceKm() != null ? f1.getDistanceKm() : 0.0;
                        Double distance2 = f2.getDistanceKm() != null ? f2.getDistanceKm() : 0.0;
                        return distance2.compareTo(distance1); // Giảm dần
                    }
                });
                break;

            case SORT_DEFAULT:
            default:
                // Giữ nguyên thứ tự từ API
                break;
        }

        // Cập nhật RecyclerView
        facilityAdapter.setFacilityList(sortedList);
    }

    private void openSearchActivity() {
        Intent intent = new Intent(SearchedActivity.this, SearchActivity.class);
        intent.putExtra("userLat", userLat);
        intent.putExtra("userLng", userLng);

        if (currentKeyword != null && !currentKeyword.isEmpty()) {
            intent.putExtra("prefilledKeyword", currentKeyword);
        }

        Log.d(TAG, "Opening SearchActivity with keyword: " + currentKeyword);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void performSearch() {
        // ✅ BỎ VALIDATION keyword - cho phép search tất cả
        // Nếu không có keyword → lấy tất cả sân

        if (currentKeyword != null && !currentKeyword.trim().isEmpty()) {
            Log.d(TAG, "Searching for: '" + currentKeyword + "' with location: " + userLat + ", " + userLng);
        } else {
            Log.d(TAG, "Getting ALL facilities with location: " + userLat + ", " + userLng);
        }

        // Call API - truyền keyword (có thể null)
        Call<SearchResponse> call = apiService.searchFacilities(
                currentKeyword != null && !currentKeyword.trim().isEmpty() ? currentKeyword.trim() : null,  // ✅ null nếu empty
                userLat,  // Vị trí hiện tại của user
                userLng,  // Vị trí hiện tại của user
                null,     // maxDistanceKm - không giới hạn
                50
        );

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SearchResponse searchResponse = response.body();

                    Log.d(TAG, "API Response code: " + searchResponse.getCode());

                    if ("200".equals(searchResponse.getCode()) && searchResponse.getData() != null) {
                        List<FacilityDTO> facilities = searchResponse.getData().getFacilities();

                        if (facilities != null && !facilities.isEmpty()) {
                            // Lưu danh sách hiện tại
                            currentFacilities = new ArrayList<>(facilities);

                            // Log khoảng cách để kiểm tra
                            for (FacilityDTO facility : currentFacilities) {
                                Log.d(TAG, facility.getFacilityName() +
                                        " - Distance: " + facility.getDistanceKm() + " km" +
                                        " - Rating: " + facility.getRating());
                            }

                            // Áp dụng sorting nếu có
                            sortFacilities();

                            Log.d(TAG, "Found " + facilities.size() + " facilities");

                            // ✅ Toast message phù hợp
                            String toastMessage;
                            if (currentKeyword != null && !currentKeyword.trim().isEmpty()) {
                                toastMessage = "Tìm thấy " + facilities.size() + " sân với '" + currentKeyword + "'";
                            } else {
                                toastMessage = "Hiển thị " + facilities.size() + " sân";
                            }
                            Toast.makeText(SearchedActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            currentFacilities = new ArrayList<>();
                            facilityAdapter.setFacilityList(currentFacilities);
                            Toast.makeText(SearchedActivity.this,
                                    "Không tìm thấy sân nào",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        currentFacilities = new ArrayList<>();
                        facilityAdapter.setFacilityList(currentFacilities);
                        Toast.makeText(SearchedActivity.this,
                                "Lỗi: " + searchResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Code: " + response.code());
                    Toast.makeText(SearchedActivity.this,
                            "Lỗi khi tìm kiếm (Code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e(TAG, "Search failed: " + t.getMessage(), t);
                Toast.makeText(SearchedActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFacilityDetail(FacilityDTO facility) {
        Toast.makeText(this, "Xem chi tiết: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
    }

    private void bookFacility(FacilityDTO facility) {
        Toast.makeText(this, "Đặt sân: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
        }
    }
}