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

import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Court.FilterBottomSheet;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.datn06.pickleconnect.Model.SearchCourtRequest;
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
        // FIXED: Use API_SERVICE (port 9003) for search
        apiService = ApiClient.createService(ServiceHost.API_SERVICE, ApiService.class);
        Log.d(TAG, "API Service initialized for port 9003 (SearchedActivity)");

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

        // Nút Filter - Hiển thị popup menu với 2 options: Sort và Advanced Filter
        icFilter.setOnClickListener(v -> showFilterOptionsPopup(v));

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
     * ✅ FIXED: Setup Bottom Navigation - Kiểm tra trang hiện tại trước khi navigate
     */
    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_booking);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    // ✅ Kiểm tra nếu đang ở trang hiện tại (Booking/Search)
                    if (itemId == R.id.nav_booking) {
                        return true; // Không làm gì cả, đã ở trang này rồi
                    }

                    // Navigate sang trang khác
                    menuNavigation.navigateTo(itemId);

                    // ✅ REMOVED: Không gọi finish() - để Activity tự quản lý lifecycle
                    return true;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Đảm bảo bottom navigation luôn highlight đúng item khi quay lại
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_booking);
        }
    }

    /**
     * ✅ ADDED: Xử lý khi Activity được gọi lại bằng intent mới (từ bottom navigation)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent mới

        // Load lại data nếu có keyword mới hoặc location mới
        String newKeyword = intent.getStringExtra("keyword");
        Double newLat = intent.getDoubleExtra("userLat", userLat);
        Double newLng = intent.getDoubleExtra("userLng", userLng);

        boolean shouldRefresh = false;

        if (newKeyword != null && !newKeyword.equals(currentKeyword)) {
            currentKeyword = newKeyword;
            searchInput.setText(currentKeyword);
            shouldRefresh = true;
        }

        if (!newLat.equals(userLat) || !newLng.equals(userLng)) {
            userLat = newLat;
            userLng = newLng;
            shouldRefresh = true;
        }

        if (shouldRefresh) {
            performSearch();
        }
    }

    /**
     * MỚI: Hiển thị popup với 2 options: Sort và Advanced Filter
     */
    private void showFilterOptionsPopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_filter_options, null);

        filterPopupWindow = new PopupWindow(
                popupView,
                (int) (240 * getResources().getDisplayMetrics().density),
                RecyclerView.LayoutParams.WRAP_CONTENT,
                true
        );

        filterPopupWindow.setOutsideTouchable(true);
        filterPopupWindow.setFocusable(true);

        Button btnSort = popupView.findViewById(R.id.btn_sort);
        Button btnAdvancedFilter = popupView.findViewById(R.id.btn_advanced_filter);

        // Sort button - show old sort popup
        btnSort.setOnClickListener(v -> {
            filterPopupWindow.dismiss();
            showSortPopup(anchorView);
        });

        // Advanced filter button - show FilterBottomSheet
        btnAdvancedFilter.setOnClickListener(v -> {
            filterPopupWindow.dismiss();
            showFilterBottomSheet();
        });

        filterPopupWindow.showAsDropDown(anchorView, -180, 10, Gravity.END);
    }

    /**
     * Hiển thị popup sort menu (GIỮ NGUYÊN)
     */
    private void showSortPopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_filter_menu, null);

        filterPopupWindow = new PopupWindow(
                popupView,
                (int) (240 * getResources().getDisplayMetrics().density),
                RecyclerView.LayoutParams.WRAP_CONTENT,
                true
        );

        filterPopupWindow.setOutsideTouchable(true);
        filterPopupWindow.setFocusable(true);

        RadioGroup radioGroupSort = popupView.findViewById(R.id.radio_group_sort);
        Button btnApply = popupView.findViewById(R.id.btn_apply_sort);

        setCurrentSortSelection(radioGroupSort);

        btnApply.setOnClickListener(v -> {
            applySorting(radioGroupSort);
            filterPopupWindow.dismiss();
        });

        filterPopupWindow.showAsDropDown(anchorView, -180, 10, Gravity.END);
    }

    /**
     * MỚI: Hiển thị FilterBottomSheet (giống CourtListActivity)
     */
    private void showFilterBottomSheet() {
        FilterBottomSheet filterBottomSheet = FilterBottomSheet.newInstance(userLat, userLng);
        filterBottomSheet.setOnFilterApplyListener(request -> {
            // Khi apply filter từ bottom sheet, gọi performAdvancedSearch
            performAdvancedSearch(request);
        });
        filterBottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
    }

    /**
     * Set selected radio button dựa trên sort hiện tại (GIỮ NGUYÊN)
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
     * Áp dụng sorting (GIỮ NGUYÊN)
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
        sortFacilities();
        Toast.makeText(this, "Đã áp dụng sắp xếp", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sắp xếp danh sách facilities theo option đã chọn (GIỮ NGUYÊN)
     */
    private void sortFacilities() {
        if (currentFacilities == null || currentFacilities.isEmpty()) {
            return;
        }

        List<FacilityDTO> sortedList = new ArrayList<>(currentFacilities);

        switch (currentSortOption) {
            case SORT_RATING_HIGH:
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double rating1 = f1.getRating() != null ? f1.getRating() : 0.0;
                        Double rating2 = f2.getRating() != null ? f2.getRating() : 0.0;
                        return rating2.compareTo(rating1);
                    }
                });
                break;

            case SORT_RATING_LOW:
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double rating1 = f1.getRating() != null ? f1.getRating() : 0.0;
                        Double rating2 = f2.getRating() != null ? f2.getRating() : 0.0;
                        return rating1.compareTo(rating2);
                    }
                });
                break;

            case SORT_DISTANCE_NEAR:
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double distance1 = f1.getDistanceKm() != null ? f1.getDistanceKm() : Double.MAX_VALUE;
                        Double distance2 = f2.getDistanceKm() != null ? f2.getDistanceKm() : Double.MAX_VALUE;
                        return distance1.compareTo(distance2);
                    }
                });
                break;

            case SORT_DISTANCE_FAR:
                Collections.sort(sortedList, new Comparator<FacilityDTO>() {
                    @Override
                    public int compare(FacilityDTO f1, FacilityDTO f2) {
                        Double distance1 = f1.getDistanceKm() != null ? f1.getDistanceKm() : 0.0;
                        Double distance2 = f2.getDistanceKm() != null ? f2.getDistanceKm() : 0.0;
                        return distance2.compareTo(distance1);
                    }
                });
                break;

            case SORT_DEFAULT:
            default:
                break;
        }

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

    /**
     * performSearch - TÌM KIẾM THEO KEYWORD (GIỮ NGUYÊN LOGIC CŨ)
     */
    private void performSearch() {
        if (currentKeyword != null && !currentKeyword.trim().isEmpty()) {
            Log.d(TAG, "Searching for: '" + currentKeyword + "' with location: " + userLat + ", " + userLng);
        } else {
            Log.d(TAG, "Getting ALL facilities with location: " + userLat + ", " + userLng);
        }

        // Call API - truyền keyword (có thể null)
        Call<SearchResponse> call = apiService.searchFacilities(
                currentKeyword != null && !currentKeyword.trim().isEmpty() ? currentKeyword.trim() : null,
                userLat,
                userLng,
                null,
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
                            currentFacilities = new ArrayList<>(facilities);

                            for (FacilityDTO facility : currentFacilities) {
                                Log.d(TAG, facility.getFacilityName() +
                                        " - Distance: " + facility.getDistanceKm() + " km" +
                                        " - Rating: " + facility.getRating());
                            }

                            sortFacilities();

                            Log.d(TAG, "Found " + facilities.size() + " facilities");

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

    /**
     * MỚI: performAdvancedSearch - TÌM KIẾM VỚI FILTER TỪ BOTTOM SHEET
     */
    private void performAdvancedSearch(SearchCourtRequest request) {
        Log.d(TAG, "performAdvancedSearch START");

        Call<SearchResponse> call = apiService.searchFacilities(
                request.getFacilityName(),
                request.getUserLatitude() != null ? request.getUserLatitude().doubleValue() : null,
                request.getUserLongitude() != null ? request.getUserLongitude().doubleValue() : null,
                request.getMaxDistanceKm(),
                request.getSize()
        );

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                Log.d(TAG, "API Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    SearchResponse searchResponse = response.body();

                    Log.d(TAG, "API Response code: " + searchResponse.getCode());

                    if ("200".equals(searchResponse.getCode()) && searchResponse.getData() != null) {
                        List<FacilityDTO> facilities = searchResponse.getData().getFacilities();

                        if (facilities != null && !facilities.isEmpty()) {
                            currentFacilities = new ArrayList<>(facilities);

                            for (FacilityDTO facility : currentFacilities) {
                                Log.d(TAG, facility.getFacilityName() +
                                        " - Distance: " + facility.getDistanceKm() + " km" +
                                        " - Rating: " + facility.getRating());
                            }

                            sortFacilities();

                            Log.d(TAG, "Found " + facilities.size() + " facilities");

                            Toast.makeText(SearchedActivity.this,
                                    "Tìm thấy " + facilities.size() + " sân phù hợp",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            currentFacilities = new ArrayList<>();
                            facilityAdapter.setFacilityList(currentFacilities);
                            Toast.makeText(SearchedActivity.this,
                                    "Không tìm thấy sân nào phù hợp",
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