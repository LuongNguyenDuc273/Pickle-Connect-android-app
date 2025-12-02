package com.datn06.pickleconnect.tournament;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.API.TournamentApiService;
import com.datn06.pickleconnect.Adapter.DynamicFormAdapter;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.Tournament.*;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TournamentRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "TournamentReg";

    // Views - Tournament Info (static)
    private ImageView btnBack;
    private ImageView ivTournamentLogo;
    private TextView tvTournamentOrganizer;
    private TextView tvTournamentName;
    private TextView tvTournamentDate;
    private TextView tvTournamentLocation;

    // Views - Registration Type Selection
    private TextView tvRegTypeLabel;
    private Spinner spinnerRegType;

    // Views - Dynamic Form
    private TextView tvPlayerInfoLabel;
    private RecyclerView rvDynamicForm;
    private DynamicFormAdapter dynamicFormAdapter;

    // Views - Submit
    private Button btnSubmit;
    private ProgressBar progressBar;

    // API
    private TournamentApiService tournamentApiService;

    // Data
    private String tournamentId;
    private String tournamentName;
    private String tournamentDetailId; // Selected from spinner
    private String currentUserId;
    private TokenManager tokenManager;

    private List<RegType> regTypeList = new ArrayList<>();
    private List<TourneyRegConfigResponse> formFields = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_registration);

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║       TournamentRegistrationActivity onCreate()            ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        initViews();
        initData();
        setupListeners();
        setupRecyclerView();

        if (tournamentId != null && !tournamentId.isEmpty()) {
            loadRegistrationTypes();
        } else {
            Log.e(TAG, "  ✗ Tournament ID is null!");
            Toast.makeText(this, "Lỗi: Không tìm thấy giải đấu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        Log.d(TAG, "▶ initViews()");

        // Tournament Info
        btnBack = findViewById(R.id.btnBack);
        ivTournamentLogo = findViewById(R.id.ivTournamentLogo);
        tvTournamentOrganizer = findViewById(R.id.tvTournamentOrganizer);
        tvTournamentName = findViewById(R.id.tvTournamentName);
        tvTournamentDate = findViewById(R.id.tvTournamentDate);
        tvTournamentLocation = findViewById(R.id.tvTournamentLocation);

        // Registration Type
        tvRegTypeLabel = findViewById(R.id.tvRegTypeLabel);
        spinnerRegType = findViewById(R.id.spinnerRegType);

        // Dynamic Form
        tvPlayerInfoLabel = findViewById(R.id.tvPlayerInfoLabel);
        rvDynamicForm = findViewById(R.id.rvDynamicForm);

        // Submit
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        Log.d(TAG, "  ✓ Views initialized");
    }

    private void initData() {
        Log.d(TAG, "▶ initData()");

        // Get data from Intent
        tournamentId = getIntent().getStringExtra("tournamentId");
        tournamentName = getIntent().getStringExtra("tournamentName");
        String tournamentDate = getIntent().getStringExtra("tournamentDate");
        String tournamentLocation = getIntent().getStringExtra("tournamentLocation");
        String organizerName = getIntent().getStringExtra("organizerName");

        tokenManager = TokenManager.getInstance(this);
        currentUserId = tokenManager.getUserId();

        Log.d(TAG, "  Tournament ID: " + tournamentId);
        Log.d(TAG, "  Tournament Name: " + tournamentName);

        if (tournamentId == null || tournamentId.isEmpty()) {
            Log.e(TAG, "  ✗ Tournament ID is null!");
            return;
        }

        // Set tournament info
        if (tournamentName != null) {
            tvTournamentName.setText(tournamentName);
        }
        if (tournamentDate != null) {
            tvTournamentDate.setText("Thời gian: " + tournamentDate);
        }
        if (tournamentLocation != null) {
            tvTournamentLocation.setText("Địa điểm: " + tournamentLocation);
        }
        if (organizerName != null) {
            tvTournamentOrganizer.setText(organizerName);
        }

        // Get user info
        tokenManager = TokenManager.getInstance(this);
        currentUserId = tokenManager.getUserId();
        Log.d(TAG, "  User ID: " + currentUserId);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "  ✗ User ID is null!");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupApiService();
    }

    private void setupApiService() {
        Log.d(TAG, "▶ setupApiService()");
        tournamentApiService = ApiClient.createService(
                ServiceHost.TOURNAMENT_SERVICE,
                TournamentApiService.class
        );
        Log.d(TAG, "  ✓ API Service initialized");
    }

    private void setupListeners() {
        Log.d(TAG, "▶ setupListeners()");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "  Back button clicked");
            finish();
        });

        spinnerRegType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < regTypeList.size()) {
                    RegType selectedType = regTypeList.get(position);
                    tournamentDetailId = selectedType.getTournamentDetailId();

                    Log.d(TAG, "  Registration type selected: " + selectedType.getMatchTypeName());
                    Log.d(TAG, "  Tournament Detail ID: " + tournamentDetailId);

                    // Load form config for this type
                    loadFormConfig();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSubmit.setOnClickListener(v -> {
            Log.d(TAG, "  Submit button clicked");
            handleSubmit();
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "▶ setupRecyclerView()");

        rvDynamicForm.setLayoutManager(new LinearLayoutManager(this));

        dynamicFormAdapter = new DynamicFormAdapter(formFields, field -> {
            Log.d(TAG, "  Field changed: " + field.getFieldName() + " = " + field.getValue());
        });

        rvDynamicForm.setAdapter(dynamicFormAdapter);

        Log.d(TAG, "  ✓ RecyclerView setup complete");
    }

    // ============================================
    // API CALLS
    // ============================================

    private void loadRegistrationTypes() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║           loadRegistrationTypes() START                    ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        showLoading(true);

        // ✅ FIX: Sử dụng constructor đơn giản giống TourneyDetailRequest
        TourneyRegTypeRequest request = new TourneyRegTypeRequest(
                currentUserId,
                tournamentId
        );

        Call<BaseResponse<TourneyRegTypeResponse>> call =
                tournamentApiService.getTourneyRegType(request);

        call.enqueue(new Callback<BaseResponse<TourneyRegTypeResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<TourneyRegTypeResponse>> call,
                                   Response<BaseResponse<TourneyRegTypeResponse>> response) {
                showLoading(false);

                Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.d(TAG, "║              API RESPONSE RECEIVED                         ║");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<TourneyRegTypeResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        TourneyRegTypeResponse data = baseResponse.getData();

                        if (data != null && data.getTournamentRegTypes() != null) {
                            regTypeList = data.getTournamentRegTypes();

                            Log.d(TAG, "  ✓ Loaded " + regTypeList.size() + " registration types");

                            setupRegTypeSpinner();
                        } else {
                            showError("Không có nội dung thi đấu");
                        }
                    } else {
                        showError("Lỗi: " + baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải nội dung thi đấu");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<TourneyRegTypeResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "  ✗ API call failed: " + t.getMessage());
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void setupRegTypeSpinner() {
        Log.d(TAG, "▶ setupRegTypeSpinner()");

        List<String> regTypeNames = new ArrayList<>();
        for (RegType type : regTypeList) {
            String displayText = type.getMatchTypeName() +
                    " (" + type.getAvailabilityText() + ")";
            regTypeNames.add(displayText);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                regTypeNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegType.setAdapter(adapter);

        Log.d(TAG, "  ✓ Spinner populated with " + regTypeNames.size() + " items");
    }

    private void loadFormConfig() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              loadFormConfig() START                        ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        showLoading(true);

        // ✅ FIX: Sử dụng constructor đơn giản
        TourneyRegConfigRequest request = new TourneyRegConfigRequest(
                currentUserId,
                tournamentId
        );

        Call<BaseResponse<List<TourneyRegConfigResponse>>> call =
                tournamentApiService.getTourneyRegConfig(request);

        call.enqueue(new Callback<BaseResponse<List<TourneyRegConfigResponse>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<TourneyRegConfigResponse>>> call,
                                   Response<BaseResponse<List<TourneyRegConfigResponse>>> response) {
                showLoading(false);

                Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.d(TAG, "║              API RESPONSE RECEIVED                         ║");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<List<TourneyRegConfigResponse>> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        List<TourneyRegConfigResponse> data = baseResponse.getData();

                        if (data != null && !data.isEmpty()) {
                            formFields = data;

                            Log.d(TAG, "  ✓ Loaded " + formFields.size() + " form fields");

                            // Update RecyclerView
                            dynamicFormAdapter.updateFormFields(formFields);

                            // Show form section
                            tvPlayerInfoLabel.setVisibility(View.VISIBLE);
                            rvDynamicForm.setVisibility(View.VISIBLE);

                        } else {
                            showError("Không có cấu hình form đăng ký");
                        }
                    } else {
                        showError("Lỗi: " + baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải form đăng ký");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<TourneyRegConfigResponse>>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "  ✗ API call failed: " + t.getMessage());
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ============================================
    // FORM VALIDATION & SUBMIT
    // ============================================

    private void handleSubmit() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                 handleSubmit() START                       ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        // Validate all required fields
        if (!validateForm()) {
            return;
        }

        // Show form data for debugging
        Log.d(TAG, "  Form Data:");
        for (TourneyRegConfigResponse field : formFields) {
            Log.d(TAG, "    " + field.getFieldName() + " = " + field.getValue());
        }

        // TODO: Call actual registration API
        Toast.makeText(this,
                "Form hợp lệ!\nChức năng đăng ký đang được phát triển...",
                Toast.LENGTH_LONG).show();

        /*
        // Example: Call register API
        TourneyRegRequest regRequest = buildRegistrationRequest();
        Call<BaseResponse<TourneyRegResponse>> call =
                tournamentApiService.registerTourney(regRequest);

        call.enqueue(new Callback<BaseResponse<TourneyRegResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<TourneyRegResponse>> call,
                                   Response<BaseResponse<TourneyRegResponse>> response) {
                // Handle response
            }

            @Override
            public void onFailure(Call<BaseResponse<TourneyRegResponse>> call, Throwable t) {
                // Handle error
            }
        });
        */
    }

    private boolean validateForm() {
        Log.d(TAG, "▶ validateForm()");

        for (TourneyRegConfigResponse field : formFields) {
            if (field.getIsRequired()) {
                String value = field.getValue();

                if (value == null || value.trim().isEmpty()) {
                    String errorMsg = "Vui lòng nhập: " + field.getLabel();
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "  ✗ Validation failed: " + field.getFieldName() + " is required");
                    return false;
                }
            }
        }

        Log.d(TAG, "  ✓ Form validation passed");
        return true;
    }

    // ============================================
    // UI HELPERS
    // ============================================

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!show);
            spinnerRegType.setEnabled(!show);
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "  Error: " + message);
        });
    }
}