package com.datn06.pickleconnect.Search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int SEARCH_DELAY_MS = 500; // Delay 500ms trước khi search

    // Views
    private ImageView btnBack;
    private EditText edtSearch;
    private ImageView btnClear;
    private TextView btnCancel;
    private RecyclerView rvSearchSuggestions;

    // Adapter
    private SearchSuggestionAdapter searchAdapter;

    // API Service
    private ApiService apiService;

    // Handler for delayed search
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    // User location
    private Double userLat = null;
    private Double userLng = null;

    // Cache search results for navigation
    private List<FacilityDTO> lastSearchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initApiService();
        setupRecyclerView();
        setupListeners();
        loadUserLocation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);
        btnClear = findViewById(R.id.btnClear);
        btnCancel = findViewById(R.id.btnCancel);
        rvSearchSuggestions = findViewById(R.id.rvSearchSuggestions);

        // Auto focus và hiện bàn phím
        edtSearch.requestFocus();
        edtSearch.post(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(edtSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void initApiService() {
        apiService = ApiClient.getApiService();
    }

    private void setupRecyclerView() {
        searchAdapter = new SearchSuggestionAdapter(suggestion -> {
            // Khi click vào suggestion → Navigate sang SearchedActivity
            navigateToSearchedActivity(suggestion);
        });

        rvSearchSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSearchSuggestions.setAdapter(searchAdapter);
    }

    private void setupListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Nút Clear
        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
            btnClear.setVisibility(View.GONE);
            searchAdapter.updateSuggestions(new ArrayList<>());
            lastSearchResults.clear();
        });

        // Text change listener với debounce
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiện/ẩn nút Clear
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Hủy search trước đó
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Tạo search mới với delay
                if (s.length() > 0) {
                    searchRunnable = () -> performSearch(s.toString());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    // Xóa suggestions khi không có text
                    searchAdapter.updateSuggestions(new ArrayList<>());
                    lastSearchResults.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ✅ QUAN TRỌNG: Xử lý khi nhấn Enter
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                String keyword = edtSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    // Navigate sang SearchedActivity với keyword
                    navigateToSearchedActivity(keyword);
                }
                return true;
            }
            return false;
        });
    }

    private void loadUserLocation() {
        // Lấy vị trí user từ Intent
        userLat = getIntent().getDoubleExtra("userLat", 0.0);
        userLng = getIntent().getDoubleExtra("userLng", 0.0);

        // Hoặc từ SharedPreferences
        if (userLat == 0.0 || userLng == 0.0) {
            try {
                android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String latStr = prefs.getString("userLat", "21.0285");
                String lngStr = prefs.getString("userLng", "105.8542");
                userLat = Double.parseDouble(latStr);
                userLng = Double.parseDouble(lngStr);
            } catch (Exception e) {
                Log.e(TAG, "Error loading user location: " + e.getMessage());
                // Set default location (Hanoi)
                userLat = 21.0285;
                userLng = 105.8542;
            }
        }

        Log.d(TAG, "User location: lat=" + userLat + ", lng=" + userLng);

        // ✅ Kiểm tra xem có keyword được pre-fill không (từ SearchedActivity)
        String prefilledKeyword = getIntent().getStringExtra("prefilledKeyword");
        if (prefilledKeyword != null && !prefilledKeyword.isEmpty()) {
            edtSearch.setText(prefilledKeyword);
            edtSearch.setSelection(prefilledKeyword.length());
            btnClear.setVisibility(View.VISIBLE);
            // Tự động search với keyword đã có
            performSearch(prefilledKeyword);
            Log.d(TAG, "Pre-filled keyword: " + prefilledKeyword);
        }
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        Log.d(TAG, "Searching for: " + keyword);

        // Call API search
        Call<SearchResponse> call = apiService.searchFacilities(
                keyword.trim(),
                userLat,
                userLng,
                null, // maxDistanceKm - không giới hạn
                20    // limit - lấy tối đa 20 kết quả
        );

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SearchResponse searchResponse = response.body();

                    Log.d(TAG, "API Response code: " + searchResponse.getCode());
                    Log.d(TAG, "API Message: " + searchResponse.getMessage());

                    if ("200".equals(searchResponse.getCode()) && searchResponse.getData() != null) {
                        List<FacilityDTO> facilities = searchResponse.getData().getFacilities();

                        if (facilities != null && !facilities.isEmpty()) {
                            // Lưu kết quả để dùng khi navigate
                            lastSearchResults = new ArrayList<>(facilities);

                            // Chuyển đổi thành List<String> để hiển thị suggestions
                            List<String> facilityNames = facilities.stream()
                                    .map(FacilityDTO::getFacilityName)
                                    .collect(Collectors.toList());

                            // Update adapter
                            searchAdapter.updateSuggestions(facilityNames);

                            Log.d(TAG, "Found " + facilities.size() + " facilities");
                        } else {
                            // Không tìm thấy kết quả
                            lastSearchResults.clear();
                            searchAdapter.updateSuggestions(new ArrayList<>());
                            Log.d(TAG, "No facilities found");
                        }
                    } else {
                        Log.w(TAG, "API response code: " + searchResponse.getCode());
                        lastSearchResults.clear();
                        searchAdapter.updateSuggestions(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "Response not successful. Code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(SearchActivity.this,
                            "Lỗi khi tìm kiếm (Code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e(TAG, "Search failed: " + t.getMessage(), t);
                Toast.makeText(SearchActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                lastSearchResults.clear();
                searchAdapter.updateSuggestions(new ArrayList<>());
            }
        });
    }

    /**
     * Navigate sang SearchedActivity với keyword
     */
    private void navigateToSearchedActivity(String keyword) {
        Intent intent = new Intent(SearchActivity.this, SearchedActivity.class);
        intent.putExtra("keyword", keyword);
        intent.putExtra("userLat", userLat);
        intent.putExtra("userLng", userLng);

        Log.d(TAG, "Navigating to SearchedActivity with keyword: " + keyword);

        startActivity(intent);
        // Animation chuyển màn hình
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup handler
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    // ✅ Interface cho callback (phải đặt ngoài inner class)
    interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }

    // ✅ Inner class adapter cho suggestion (non-static)
    private class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {

        private List<String> suggestions = new ArrayList<>();
        private OnSuggestionClickListener listener;

        SearchSuggestionAdapter(OnSuggestionClickListener listener) {
            this.listener = listener;
        }

        void updateSuggestions(List<String> newSuggestions) {
            this.suggestions.clear();
            if (newSuggestions != null) {
                this.suggestions.addAll(newSuggestions);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_suggestion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String suggestion = suggestions.get(position);
            holder.tvSuggestion.setText(suggestion);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSuggestionClick(suggestion);
                }
            });
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }

        // ViewHolder cũng là inner class (không static)
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivSearchIcon;
            TextView tvSuggestion;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivSearchIcon = itemView.findViewById(R.id.ivSearchIcon);
                tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
            }
        }
    }
}