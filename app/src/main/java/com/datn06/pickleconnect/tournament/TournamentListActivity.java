package com.datn06.pickleconnect.tournament;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.API.TournamentApiService;
import com.datn06.pickleconnect.Adapter.TournamentAdapter;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Home.HomeActivity;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.datn06.pickleconnect.Models.Tournament.TournamentConstants;
import com.datn06.pickleconnect.Models.Tournament.TourneyListRequest;
import com.datn06.pickleconnect.Models.Tournament.TourneyListResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TournamentListActivity extends AppCompatActivity {

    private static final String TAG = "TournamentListActivity";

    // Views
    private ImageButton btnBack;
    private ImageButton btnSearch;
    private Chip chipOngoing;
    private Chip chipUpcoming;
    private Chip chipCompleted;
    private RecyclerView recyclerViewTournaments;
    private BottomNavigationView bottomNavigation;

    // Adapter
    private TournamentAdapter tournamentAdapter;

    // API
    private TournamentApiService tournamentApiService;

    // Data
    private String currentUserId;
    private String currentSearchType = TournamentConstants.SEARCH_TYPE_ONGOING;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Token Manager & Menu Navigation
    private TokenManager tokenManager;
    private MenuNavigation menuNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║           TournamentListActivity onCreate()                ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        setContentView(R.layout.activity_tourament_list);

        initViews();
        initData();
        setupRecyclerView();
        setupChipFilters();
        setupListeners();
        setupBottomNavigation();

        // Load initial data
        loadTournaments(true);
    }

    private void initViews() {
        Log.d(TAG, "▶ initViews()");

        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        chipOngoing = findViewById(R.id.chipOngoing);
        chipUpcoming = findViewById(R.id.chipUpcoming);
        chipCompleted = findViewById(R.id.chipCompleted);
        recyclerViewTournaments = findViewById(R.id.recyclerViewTournaments);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        Log.d(TAG, "  Views found:");
        Log.d(TAG, "    btnBack: " + (btnBack != null));
        Log.d(TAG, "    btnSearch: " + (btnSearch != null));
        Log.d(TAG, "    chipOngoing: " + (chipOngoing != null));
        Log.d(TAG, "    chipUpcoming: " + (chipUpcoming != null));
        Log.d(TAG, "    chipCompleted: " + (chipCompleted != null));
        Log.d(TAG, "    recyclerViewTournaments: " + (recyclerViewTournaments != null));
        Log.d(TAG, "    bottomNavigation: " + (bottomNavigation != null));
    }

    private void initData() {
        Log.d(TAG, "▶ initData()");

        // Initialize TokenManager
        tokenManager = TokenManager.getInstance(this);

        // Initialize MenuNavigation
        menuNavigation = new MenuNavigation(this);

        // Get current user ID from TokenManager
        currentUserId = tokenManager.getUserId();

        Log.d(TAG, "  User ID: " + currentUserId);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "  ERROR: User ID is null or empty!");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize API service using ServiceHost
        setupApiService();
    }

    private void setupApiService() {
        Log.d(TAG, "▶ setupApiService()");

        tournamentApiService = ApiClient.createService(
                ServiceHost.TOURNAMENT_SERVICE,
                TournamentApiService.class
        );

        Log.d(TAG, "  API Service initialized: " + (tournamentApiService != null));
    }

    private void setupRecyclerView() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              setupRecyclerView()                           ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        // Create adapter
        tournamentAdapter = new TournamentAdapter(this);
        Log.d(TAG, "  Adapter created: " + (tournamentAdapter != null));

        // Create LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        Log.d(TAG, "  LayoutManager created: " + (layoutManager != null));

        // Set LayoutManager
        recyclerViewTournaments.setLayoutManager(layoutManager);
        Log.d(TAG, "  LayoutManager set to RecyclerView");

        // Set Adapter
        recyclerViewTournaments.setAdapter(tournamentAdapter);
        Log.d(TAG, "  Adapter set to RecyclerView");

        // Check RecyclerView info
        Log.d(TAG, "  RecyclerView info BEFORE layout:");
        Log.d(TAG, "    Visibility: " + recyclerViewTournaments.getVisibility());
        Log.d(TAG, "    Width: " + recyclerViewTournaments.getWidth());
        Log.d(TAG, "    Height: " + recyclerViewTournaments.getHeight());
        Log.d(TAG, "    Adapter: " + recyclerViewTournaments.getAdapter());
        Log.d(TAG, "    LayoutManager: " + recyclerViewTournaments.getLayoutManager());

        // Post to get actual dimensions after layout
        recyclerViewTournaments.post(() -> {
            Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
            Log.d(TAG, "║         RecyclerView AFTER Layout (post)                   ║");
            Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");
            Log.d(TAG, "  Width: " + recyclerViewTournaments.getWidth());
            Log.d(TAG, "  Height: " + recyclerViewTournaments.getHeight());
            Log.d(TAG, "  MeasuredWidth: " + recyclerViewTournaments.getMeasuredWidth());
            Log.d(TAG, "  MeasuredHeight: " + recyclerViewTournaments.getMeasuredHeight());
            Log.d(TAG, "  Child count: " + recyclerViewTournaments.getChildCount());

            if (recyclerViewTournaments.getAdapter() != null) {
                Log.d(TAG, "  Adapter item count: " + recyclerViewTournaments.getAdapter().getItemCount());
            } else {
                Log.e(TAG, "  ERROR: Adapter is null!");
            }

            if (recyclerViewTournaments.getWidth() == 0 || recyclerViewTournaments.getHeight() == 0) {
                Log.e(TAG, "  ⚠️ WARNING: RecyclerView has zero width or height!");
                Log.e(TAG, "  ⚠️ Check your layout XML!");
            }
        });

        // Pagination
        recyclerViewTournaments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        Log.d(TAG, "▶ Pagination triggered - loading more tournaments");
                        loadTournaments(false);
                    }
                }
            }
        });

        // Item click listener
        tournamentAdapter.setOnTournamentClickListener(tournament -> {
            Log.d(TAG, "Tournament clicked: " + tournament.getTournamentName());
            Intent intent = new Intent(TournamentListActivity.this, HomeActivity.class);
            intent.putExtra("tournamentId", tournament.getTournamentId());
            startActivity(intent);
        });
    }

    private void setupChipFilters() {
        Log.d(TAG, "▶ setupChipFilters()");

        // Set initial selected chip
        updateChipSelection(chipOngoing);

        chipOngoing.setOnClickListener(v -> {
            if (!currentSearchType.equals(TournamentConstants.SEARCH_TYPE_ONGOING)) {
                Log.d(TAG, "▶ Chip ONGOING clicked");
                currentSearchType = TournamentConstants.SEARCH_TYPE_ONGOING;
                updateChipSelection(chipOngoing);
                resetAndLoadTournaments();
            }
        });

        chipUpcoming.setOnClickListener(v -> {
            if (!currentSearchType.equals(TournamentConstants.SEARCH_TYPE_UPCOMING)) {
                Log.d(TAG, "▶ Chip UPCOMING clicked");
                currentSearchType = TournamentConstants.SEARCH_TYPE_UPCOMING;
                updateChipSelection(chipUpcoming);
                resetAndLoadTournaments();
            }
        });

        chipCompleted.setOnClickListener(v -> {
            if (!currentSearchType.equals(TournamentConstants.SEARCH_TYPE_PAST)) {
                Log.d(TAG, "▶ Chip COMPLETED clicked");
                currentSearchType = TournamentConstants.SEARCH_TYPE_PAST;
                updateChipSelection(chipCompleted);
                resetAndLoadTournaments();
            }
        });
    }

    private void updateChipSelection(Chip selectedChip) {
        // Reset all chips
        resetChip(chipOngoing);
        resetChip(chipUpcoming);
        resetChip(chipCompleted);

        // Set selected chip
        selectedChip.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
        selectedChip.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void resetChip(Chip chip) {
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void setupListeners() {
        Log.d(TAG, "▶ setupListeners()");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            Log.d(TAG, "Search button clicked");
            Toast.makeText(this, "Tìm kiếm đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        Log.d(TAG, "▶ setupBottomNavigation()");

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_tournaments);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_tournaments) {
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
            bottomNavigation.setSelectedItemId(R.id.nav_tournaments);
        }
    }

    private void resetAndLoadTournaments() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║            resetAndLoadTournaments()                       ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        currentPage = 1;
        isLastPage = false;

        Log.d(TAG, "  Clearing adapter...");
        tournamentAdapter.clear();

        Log.d(TAG, "  Loading tournaments with search type: " + currentSearchType);
        loadTournaments(true);
    }

    private void loadTournaments(boolean isFirstPage) {
        if (isLoading) {
            Log.w(TAG, "Already loading, skipping...");
            return;
        }

        isLoading = true;

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              loadTournaments() START                       ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");
        Log.d(TAG, "  isFirstPage: " + isFirstPage);
        Log.d(TAG, "  currentPage: " + currentPage);
        Log.d(TAG, "  currentSearchType: " + currentSearchType);
        Log.d(TAG, "  currentUserId: " + currentUserId);

        // Create request
        TourneyListRequest request = new TourneyListRequest(currentUserId, currentSearchType);
        request.setPage(currentPage);
        request.setSize(PAGE_SIZE);

        Log.d(TAG, "  Request created - Making API call...");

        // Make API call
        Call<BaseResponse<List<TourneyListResponse>>> call =
                tournamentApiService.getTourneyList(request);

        call.enqueue(new Callback<BaseResponse<List<TourneyListResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<TourneyListResponse>>> call,
                                   Response<BaseResponse<List<TourneyListResponse>>> response) {
                isLoading = false;

                Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.d(TAG, "║              API RESPONSE RECEIVED                         ║");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");
                Log.d(TAG, "  HTTP Code: " + response.code());
                Log.d(TAG, "  isSuccessful: " + response.isSuccessful());
                Log.d(TAG, "  Body is null: " + (response.body() == null));

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<List<TourneyListResponse>> baseResponse = response.body();

                    Log.d(TAG, "  Response code: " + baseResponse.getCode());
                    Log.d(TAG, "  Response message: " + baseResponse.getMessage());

                    if ("00".equals(baseResponse.getCode())) {
                        List<TourneyListResponse> tournaments = baseResponse.getData();

                        Log.d(TAG, "  Data is null: " + (tournaments == null));
                        Log.d(TAG, "  Data size: " + (tournaments != null ? tournaments.size() : 0));

                        if (tournaments != null && !tournaments.isEmpty()) {
                            // Log từng tournament
                            Log.d(TAG, "  ── Tournament List ──");
                            for (int i = 0; i < tournaments.size(); i++) {
                                TourneyListResponse t = tournaments.get(i);
                                Log.d(TAG, "    [" + i + "] " + t.getTournamentName() +
                                        " (ID: " + t.getTournamentId() + ")");
                            }

                            Log.d(TAG, "  Adapter size BEFORE update: " + tournamentAdapter.getItemCount());

                            if (isFirstPage) {
                                Log.d(TAG, "  ▶ Calling setTournaments() for first page");
                                tournamentAdapter.setTournaments(tournaments);
                            } else {
                                Log.d(TAG, "  ▶ Calling addTournaments() for pagination");
                                tournamentAdapter.addTournaments(tournaments);
                            }

                            Log.d(TAG, "  Adapter size AFTER update: " + tournamentAdapter.getItemCount());

                            currentPage++;
                            Log.d(TAG, "  Next page will be: " + currentPage);

                            if (tournaments.size() < PAGE_SIZE) {
                                isLastPage = true;
                                Log.d(TAG, "  ✓ Reached last page (size < PAGE_SIZE)");
                            }
                        } else {
                            Log.w(TAG, "  ⚠️ No tournaments in response");
                            if (isFirstPage) {
                                tournamentAdapter.clear();
                                showEmptyState();
                            }
                            isLastPage = true;
                        }
                    } else {
                        Log.e(TAG, "  ✗ API Error - Code: " + baseResponse.getCode() +
                                ", Message: " + baseResponse.getMessage());
                        showError("Lỗi: " + baseResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "  ✗ Response not successful or body is null");
                    Log.e(TAG, "  HTTP Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "  Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "  Error reading error body", e);
                    }
                    showError("Không thể tải danh sách giải đấu");
                }

                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");
            }

            @Override
            public void onFailure(Call<BaseResponse<List<TourneyListResponse>>> call, Throwable t) {
                isLoading = false;

                Log.e(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.e(TAG, "║              API CALL FAILED                               ║");
                Log.e(TAG, "╚════════════════════════════════════════════════════════════╝");
                Log.e(TAG, "  Error: " + t.getMessage(), t);

                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        String message;
        switch (currentSearchType) {
            case TournamentConstants.SEARCH_TYPE_ONGOING:
                message = "Không có giải đấu đang diễn ra";
                break;
            case TournamentConstants.SEARCH_TYPE_UPCOMING:
                message = "Không có giải đấu sắp tới";
                break;
            case TournamentConstants.SEARCH_TYPE_PAST:
                message = "Không có giải đấu đã kết thúc";
                break;
            default:
                message = "Không có giải đấu";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Empty state: " + message);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }
}