package com.datn06.pickleconnect.Court;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.datn06.pickleconnect.Adapter.FacilityAdapter;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.Model.FacilitySearchResponse;
import com.datn06.pickleconnect.Model.GetSavedCourtsRequest;
import com.datn06.pickleconnect.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity hiển thị danh sách sân đã lưu của user
 * Có chức năng sắp xếp theo thời gian lưu (mới nhất/cũ nhất)
 */
public class SavedCourtsActivity extends AppCompatActivity {

    private static final String TAG = "SavedCourtsActivity";

    // UI Components
    private ImageView ivBack;
    private TextView tvTitle, tvEmptyMessage;
    private MaterialButton btnSortNewest, btnSortOldest;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // Data
    private FacilityAdapter adapter;
    private List<FacilityDTO> savedCourts = new ArrayList<>();
    private CourtApiService apiService;
    private Long userId;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private String currentSortOrder = "DESC"; // DESC = newest first, ASC = oldest first

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_courts);

        initViews();
        getUserId();
        setupRecyclerView();
        setupListeners();
        loadSavedCourts(true);
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnSortNewest = findViewById(R.id.btnSortNewest);
        btnSortOldest = findViewById(R.id.btnSortOldest);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        tvTitle.setText("Sân đã lưu");
    }

    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        userId = prefs.getLong("accountId", -1L);
        if (userId == -1L) {
            Log.e(TAG, "User not logged in - accountId not found in SharedPreferences");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new FacilityAdapter(this);
        adapter.setFacilityList(savedCourts);
        adapter.setOnFacilityClickListener(new FacilityAdapter.OnFacilityClickListener() {
            @Override
            public void onFacilityClick(FacilityDTO facility) {
                // TODO: Navigate to CourtDetailActivity
                Toast.makeText(SavedCourtsActivity.this, 
                    "Xem chi tiết: " + facility.getFacilityName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBookClick(FacilityDTO facility) {
                // Already handled by adapter - navigates to FieldSelectionActivity
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Pagination - load more when scroll to bottom
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMorePages) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadSavedCourts(false);
                    }
                }
            }
        });

        apiService = ApiClient.createService(ServiceHost.COURT_SERVICE, CourtApiService.class);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        // Sort by newest first (DESC)
        btnSortNewest.setOnClickListener(v -> {
            if (!currentSortOrder.equals("DESC")) {
                currentSortOrder = "DESC";
                updateSortButtons();
                resetAndReload();
            }
        });

        // Sort by oldest first (ASC)
        btnSortOldest.setOnClickListener(v -> {
            if (!currentSortOrder.equals("ASC")) {
                currentSortOrder = "ASC";
                updateSortButtons();
                resetAndReload();
            }
        });
    }

    private void updateSortButtons() {
        if (currentSortOrder.equals("DESC")) {
            btnSortNewest.setBackgroundColor(getResources().getColor(R.color.primary, null));
            btnSortNewest.setTextColor(getResources().getColor(R.color.white, null));
            btnSortOldest.setBackgroundColor(getResources().getColor(R.color.white, null));
            btnSortOldest.setTextColor(getResources().getColor(R.color.text_secondary, null));
        } else {
            btnSortOldest.setBackgroundColor(getResources().getColor(R.color.primary, null));
            btnSortOldest.setTextColor(getResources().getColor(R.color.white, null));
            btnSortNewest.setBackgroundColor(getResources().getColor(R.color.white, null));
            btnSortNewest.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void resetAndReload() {
        currentPage = 0;
        hasMorePages = true;
        savedCourts.clear();
        adapter.setFacilityList(savedCourts);
        loadSavedCourts(true);
    }

    /**
     * Load saved courts from API
     * @param showProgress Show loading indicator or not (true for first load, false for pagination)
     */
    private void loadSavedCourts(boolean showProgress) {
        if (isLoading) return;
        isLoading = true;

        if (showProgress) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.GONE);
        }

        GetSavedCourtsRequest request = new GetSavedCourtsRequest();
        request.setUserId(userId);
        request.setPage(currentPage);
        request.setSize(20);
        request.setSortOrder(currentSortOrder);
        request.setRequestId(String.valueOf(System.currentTimeMillis()));

        Log.d(TAG, "Loading saved courts - Page: " + currentPage + ", SortOrder: " + currentSortOrder);

        apiService.getSavedFacilities(request).enqueue(new Callback<BaseResponse<FacilitySearchResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<FacilitySearchResponse>> call,
                                   @NonNull Response<BaseResponse<FacilitySearchResponse>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<FacilitySearchResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode()) && baseResponse.getData() != null) {
                        FacilitySearchResponse data = baseResponse.getData();
                        List<FacilityDTO> newCourts = data.getFacilities();

                        if (newCourts != null && !newCourts.isEmpty()) {
                            savedCourts.addAll(newCourts);
                            adapter.setFacilityList(savedCourts);

                            currentPage++;
                            hasMorePages = currentPage < data.getTotalPages();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvEmptyMessage.setVisibility(View.GONE);

                            Log.d(TAG, "Loaded " + newCourts.size() + " courts. Total: " + savedCourts.size());
                        } else {
                            // No more data
                            hasMorePages = false;
                            if (savedCourts.isEmpty()) {
                                showEmptyState();
                            }
                        }
                    } else if ("02".equals(baseResponse.getCode())) {
                        // NOTFOUND_USER_SAVED
                        hasMorePages = false;
                        if (savedCourts.isEmpty()) {
                            showEmptyState();
                        }
                    } else {
                        Toast.makeText(SavedCourtsActivity.this,
                                "Lỗi: " + baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SavedCourtsActivity.this,
                            "Không thể tải danh sách sân đã lưu", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<FacilitySearchResponse>> call,
                                  @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SavedCourtsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network Error", t);

                if (savedCourts.isEmpty()) {
                    showEmptyState();
                }
            }
        });
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        tvEmptyMessage.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText("Bạn chưa lưu sân nào");
    }
}
