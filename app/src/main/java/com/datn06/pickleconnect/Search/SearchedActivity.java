package com.datn06.pickleconnect.Search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.Adapter.FacilityAdapter;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchedActivity extends AppCompatActivity {

    private static final String TAG = "SearchedActivity";

    // Views
    private ImageView icArrowLeft;
    private ImageView icSearch;
    private EditText searchInput;
    private ImageView icFilter;
    private RecyclerView recyclerViewVenues;

    // Adapter
    private FacilityAdapter facilityAdapter;

    // API Service
    private ApiService apiService;

    // Data
    private String currentKeyword;
    private Double userLat;
    private Double userLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched);

        initViews();
        initApiService();
        setupRecyclerView();
        loadDataFromIntent();
        setupListeners();
        performSearch();
    }

    private void initViews() {
        icArrowLeft = findViewById(R.id.ic_arrow_left);
        icSearch = findViewById(R.id.ic_search);
        searchInput = findViewById(R.id.search_input);
        icFilter = findViewById(R.id.ic_filter);
        recyclerViewVenues = findViewById(R.id.recycler_view_venues);
    }

    private void initApiService() {
        apiService = ApiClient.getApiService();
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
        // Lấy keyword từ Intent
        currentKeyword = getIntent().getStringExtra("keyword");
        userLat = getIntent().getDoubleExtra("userLat", 21.0285);
        userLng = getIntent().getDoubleExtra("userLng", 105.8542);

        // Hiển thị keyword trong EditText
        if (currentKeyword != null && !currentKeyword.isEmpty()) {
            searchInput.setText(currentKeyword);
            searchInput.setSelection(currentKeyword.length()); // Đặt cursor ở cuối
        }

        Log.d(TAG, "Loaded from intent: keyword=" + currentKeyword +
                ", lat=" + userLat + ", lng=" + userLng);
    }

    private void setupListeners() {
        // Nút Back
        icArrowLeft.setOnClickListener(v -> finish());

        // Nút Filter (TODO: Implement filter feature)
        icFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng lọc đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // ✅ QUAN TRỌNG: Click vào EditText → Chuyển sang SearchActivity
        searchInput.setOnClickListener(v -> openSearchActivity());

        // ✅ Tắt focus để tránh bàn phím hiện ra
        searchInput.setFocusable(false);
        searchInput.setClickable(true);

        // ✅ OPTIONAL: Nếu muốn giữ chức năng Enter để search lại
        // (Nhưng thường thì click vào EditText sẽ chuyển sang SearchActivity)
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                String newKeyword = searchInput.getText().toString().trim();
                if (!newKeyword.isEmpty()) {
                    currentKeyword = newKeyword;
                    performSearch();

                    // Ẩn bàn phím
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        // ✅ Click vào icon search cũng chuyển sang SearchActivity
        if (icSearch != null) {
            icSearch.setOnClickListener(v -> openSearchActivity());
        }
    }

    /**
     * ✅ Mở SearchActivity với keyword hiện tại được pre-fill
     */
    private void openSearchActivity() {
        Intent intent = new Intent(SearchedActivity.this, SearchActivity.class);

        // Truyền vị trí user
        intent.putExtra("userLat", userLat);
        intent.putExtra("userLng", userLng);

        // ✅ Truyền keyword hiện tại để pre-fill vào SearchActivity
        if (currentKeyword != null && !currentKeyword.isEmpty()) {
            intent.putExtra("prefilledKeyword", currentKeyword);
        }

        Log.d(TAG, "Opening SearchActivity with keyword: " + currentKeyword);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // ✅ Finish activity hiện tại để tránh stack quá nhiều
        finish();
    }

    private void performSearch() {
        if (currentKeyword == null || currentKeyword.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Searching for: " + currentKeyword);

        // Hiển thị loading (có thể thêm ProgressBar trong layout)
        // showLoading();

        // Call API
        Call<SearchResponse> call = apiService.searchFacilities(
                currentKeyword.trim(),
                userLat,
                userLng,
                null, // maxDistanceKm
                50    // limit - lấy nhiều kết quả hơn cho trang kết quả
        );

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                // hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    SearchResponse searchResponse = response.body();

                    Log.d(TAG, "API Response code: " + searchResponse.getCode());

                    if ("200".equals(searchResponse.getCode()) && searchResponse.getData() != null) {
                        List<FacilityDTO> facilities = searchResponse.getData().getFacilities();

                        if (facilities != null && !facilities.isEmpty()) {
                            // Cập nhật RecyclerView
                            facilityAdapter.setFacilityList(facilities);

                            Log.d(TAG, "Found " + facilities.size() + " facilities");
                            Toast.makeText(SearchedActivity.this,
                                    "Tìm thấy " + facilities.size() + " sân",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Không có kết quả
                            facilityAdapter.setFacilityList(new ArrayList<>());
                            Toast.makeText(SearchedActivity.this,
                                    "Không tìm thấy sân nào",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        facilityAdapter.setFacilityList(new ArrayList<>());
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
                // hideLoading();

                Log.e(TAG, "Search failed: " + t.getMessage(), t);
                Toast.makeText(SearchedActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFacilityDetail(FacilityDTO facility) {
        Toast.makeText(this, "Xem chi tiết: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();

        // TODO: Navigate sang FacilityDetailActivity
        // Intent intent = new Intent(this, FacilityDetailActivity.class);
        // intent.putExtra("facilityId", facility.getFacilityId());
        // startActivity(intent);
    }

    private void bookFacility(FacilityDTO facility) {
        Toast.makeText(this, "Đặt sân: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();

        // TODO: Navigate sang BookingActivity
        // Intent intent = new Intent(this, BookingActivity.class);
        // intent.putExtra("facilityId", facility.getFacilityId());
        // startActivity(intent);
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}