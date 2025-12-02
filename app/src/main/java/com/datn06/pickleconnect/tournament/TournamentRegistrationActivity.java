package com.datn06.pickleconnect.tournament;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.API.MemberApiService;
import com.datn06.pickleconnect.API.TournamentApiService;
import com.datn06.pickleconnect.Adapter.DynamicFormAdapter;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.MemberInfoRequest;
import com.datn06.pickleconnect.Models.MemberInfoResponse;
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

    // Views - Fixed Player Information
    private TextView tvPlayerInfoLabel;
    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;

    // Views - Dynamic Form
    private RecyclerView rvDynamicForm;
    private DynamicFormAdapter dynamicFormAdapter;

    // Views - Terms & Submit
    private CheckBox cbTerms;
    private TextView tvTerms;
    private Button btnSubmit;
    private ProgressBar progressBar;

    // API
    private TournamentApiService tournamentApiService;
    private MemberApiService memberApiService;

    // Data
    private String tournamentId;
    private String tournamentName;
    private String tournamentDetailId;
    private String currentUserId;
    private TokenManager tokenManager;

    private List<TourneyDetailResponse.MatchType> matchTypeList = new ArrayList<>();
    private List<TourneyRegConfigResponse> formFields = new ArrayList<>();
    private double registrationFee;

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
        setupTermsAndConditions();

        if (tournamentId != null && !tournamentId.isEmpty()) {
            // ✅ Load user info first, then load match types
            loadUserInfo();
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

        // Fixed Player Information
        tvPlayerInfoLabel = findViewById(R.id.tvPlayerInfoLabel);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);

        // Dynamic Form
        rvDynamicForm = findViewById(R.id.rvDynamicForm);

        // Terms & Submit
        cbTerms = findViewById(R.id.cbTerms);
        tvTerms = findViewById(R.id.tvTerms);
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
        String registrationFeeStr = getIntent().getStringExtra("registrationFee");


        tokenManager = TokenManager.getInstance(this);
        currentUserId = tokenManager.getUserId();

        Log.d(TAG, "  Tournament ID: " + tournamentId);
        Log.d(TAG, "  Tournament Name: " + tournamentName);
        Log.d(TAG, "  Registration Fee: " + registrationFee);

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
        memberApiService = ApiClient.createService(
                ServiceHost.MEMBER_SERVICE,
                MemberApiService.class
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
                if (position >= 0 && position < matchTypeList.size()) {
                    TourneyDetailResponse.MatchType selectedType = matchTypeList.get(position);
                    String matchTypeCode = selectedType.getMatchTypeCode();

                    Log.d(TAG, "  Match type selected: " + selectedType.getMatchTypeName());
                    Log.d(TAG, "  Match type code: " + matchTypeCode);

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

    /**
     * ✅ Setup Terms and Conditions với clickable text
     */
    private void setupTermsAndConditions() {
        Log.d(TAG, "▶ setupTermsAndConditions()");

        String fullText = "Tôi xác nhận đồng ý với Chính sách chia sẻ dữ liệu và bảo mật của Pickle Connect";
        SpannableString spannableString = new SpannableString(fullText);

        // Tìm vị trí của text cần highlight
        String highlightText = "Chính sách chia sẻ dữ liệu và bảo mật";
        int startIndex = fullText.indexOf(highlightText);
        int endIndex = startIndex + highlightText.length();

        if (startIndex >= 0) {
            // Set màu xanh cho text
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#00BFA5"));
            spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Thêm clickable span
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // TODO: Mở trang chính sách
                    Toast.makeText(TournamentRegistrationActivity.this,
                            "Mở trang Chính sách chia sẻ dữ liệu và bảo mật",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "  Terms link clicked");
                }
            };
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvTerms.setText(spannableString);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerms.setHighlightColor(Color.TRANSPARENT); // Bỏ highlight khi click

        Log.d(TAG, "  ✓ Terms and Conditions setup complete");
    }

    // ============================================
    // API CALLS - USER INFO
    // ============================================

    /**
     * ✅ Load user info from MemberInfo API to auto-fill fixed fields
     */
    private void loadUserInfo() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              loadUserInfo() START                          ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);

        String currentEmail = tokenManager.getEmail();
        String currentPhone = tokenManager.getPhoneNumber();

        Log.d(TAG, "  User ID: " + currentUserId);
        Log.d(TAG, "  Email: " + currentEmail);
        Log.d(TAG, "  Phone: " + currentPhone);

        MemberInfoRequest request = new MemberInfoRequest(
                currentUserId,
                currentEmail,
                currentPhone
        );

        memberApiService.getMemberInfo(request).enqueue(new Callback<BaseResponse<MemberInfoResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberInfoResponse>> call,
                                   Response<BaseResponse<MemberInfoResponse>> response) {
                showLoading(false);

                Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.d(TAG, "║          MEMBER INFO API RESPONSE RECEIVED                 ║");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<MemberInfoResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        MemberInfoResponse data = baseResponse.getData();

                        Log.d(TAG, "  ✓ User info loaded successfully");
                        Log.d(TAG, "    Full Name: " + data.getFullName());
                        Log.d(TAG, "    Phone: " + data.getPhoneNumber());
                        Log.d(TAG, "    Email: " + data.getEmail());
                        Log.d(TAG, "    Gender: " + data.getGender());

                        // ✅ Auto-fill fixed fields
                        populateFixedFields(data);

                        // ✅ Then load match types
                        loadMatchTypes();

                    } else {
                        showError("Lỗi: " + baseResponse.getMessage());
                        Log.e(TAG, "  ✗ Response code: " + baseResponse.getCode());
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(TournamentRegistrationActivity.this,
                            "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showError("Không thể tải thông tin người dùng: " + response.code());
                    Log.e(TAG, "  ✗ Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberInfoResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "  ✗ API call failed: " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * ✅ Auto-fill fixed fields with user data
     */
    private void populateFixedFields(MemberInfoResponse data) {
        if (data == null) return;

        Log.d(TAG, "▶ populateFixedFields()");

        // Set full name
        if (data.getFullName() != null && !data.getFullName().isEmpty()) {
            etFullName.setText(data.getFullName());
            etFullName.setEnabled(false); // Make read-only
            Log.d(TAG, "  ✓ Full Name: " + data.getFullName());
        }

        // Set phone number
        if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
            etPhone.setText(data.getPhoneNumber());
            etPhone.setEnabled(false); // Make read-only
            Log.d(TAG, "  ✓ Phone: " + data.getPhoneNumber());
        }

        // Set email
        if (data.getEmail() != null && !data.getEmail().isEmpty()) {
            etEmail.setText(data.getEmail());
            etEmail.setEnabled(false); // Make read-only
            Log.d(TAG, "  ✓ Email: " + data.getEmail());
        }

        // Set gender
        if (data.getGender() != null) {
            String genderDisplay = convertGenderToDisplay(data.getGender());

            if ("Nam".equals(genderDisplay)) {
                rbMale.setChecked(true);
            } else if ("Nữ".equals(genderDisplay)) {
                rbFemale.setChecked(true);
            }

            // Make radio group read-only
            rgGender.setEnabled(false);
            rbMale.setEnabled(false);
            rbFemale.setEnabled(false);

            Log.d(TAG, "  ✓ Gender: " + genderDisplay);
        }

        Log.d(TAG, "  ✓ Fixed fields populated successfully");
    }

    /**
     * ✅ Convert gender from API format to display format
     */
    private String convertGenderToDisplay(String gender) {
        if (gender == null) {
            return "Nam"; // Default
        }

        // Check if gender is numeric (1, 0, 2)
        try {
            int genderInt = Integer.parseInt(gender);
            switch (genderInt) {
                case 1:
                    return "Nam";   // API: 1 = Nam
                case 0:
                    return "Nữ";    // API: 0 = Nữ
                default:
                    return "Khác";  // API: 2 = Khác
            }
        } catch (NumberFormatException e) {
            // If not numeric, treat as string
            switch (gender.toUpperCase()) {
                case "MALE":
                    return "Nam";
                case "FEMALE":
                    return "Nữ";
                default:
                    return "Khác";
            }
        }
    }

    // ============================================
    // API CALLS - TOURNAMENT
    // ============================================

    private void loadMatchTypes() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              loadMatchTypes() START                        ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        showLoading(true);

        TourneyDetailRequest request = new TourneyDetailRequest(
                currentUserId,
                tournamentId,
                "2"
        );

        Call<BaseResponse<TourneyDetailResponse>> call =
                tournamentApiService.getTourneyDetail(request);

        call.enqueue(new Callback<BaseResponse<TourneyDetailResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<TourneyDetailResponse>> call,
                                   Response<BaseResponse<TourneyDetailResponse>> response) {
                showLoading(false);

                Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
                Log.d(TAG, "║              API RESPONSE RECEIVED                         ║");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<TourneyDetailResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        TourneyDetailResponse data = baseResponse.getData();

                        if (data != null && data.getMatchTypes() != null && !data.getMatchTypes().isEmpty()) {
                            matchTypeList = data.getMatchTypes();

                            Log.d(TAG, "  ✓ Loaded " + matchTypeList.size() + " match types");

                            for (TourneyDetailResponse.MatchType mt : matchTypeList) {
                                Log.d(TAG, "    - " + mt.getMatchTypeName() +
                                        " (" + mt.getMatchTypeCode() + "): " +
                                        mt.getNumberOfParticipant() + "/" + mt.getMaxParticipants());
                            }

                            setupMatchTypeSpinner();
                        } else {
                            showError("Không có nội dung thi đấu");
                            Log.e(TAG, "  ✗ matchTypes is null or empty");
                        }
                    } else {
                        showError("Lỗi: " + baseResponse.getMessage());
                        Log.e(TAG, "  ✗ Response code: " + baseResponse.getCode());
                    }
                } else {
                    showError("Không thể tải nội dung thi đấu");
                    Log.e(TAG, "  ✗ Response not successful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "  Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "  Cannot read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<TourneyDetailResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "  ✗ API call failed: " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void setupMatchTypeSpinner() {
        Log.d(TAG, "▶ setupMatchTypeSpinner()");

        List<String> matchTypeNames = new ArrayList<>();
        for (TourneyDetailResponse.MatchType type : matchTypeList) {
            String displayText = type.getMatchTypeName() +
                    " (" + type.getNumberOfParticipant() + "/" + type.getMaxParticipants() + ")";

            if (type.isFull()) {
                displayText += " [ĐẦY]";
            }

            matchTypeNames.add(displayText);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                matchTypeNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegType.setAdapter(adapter);

        Log.d(TAG, "  ✓ Spinner populated with " + matchTypeNames.size() + " match types");
    }

    private void loadFormConfig() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║              loadFormConfig() START                        ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        showLoading(true);

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

                            dynamicFormAdapter.updateFormFields(formFields);

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

        // Validate dynamic form fields
        if (!validateForm()) {
            return;
        }

        // Validate terms checkbox
        if (!cbTerms.isChecked()) {
            Toast.makeText(this,
                    "Vui lòng đồng ý với chính sách chia sẻ dữ liệu và bảo mật",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if match type is full
        int selectedPosition = spinnerRegType.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= matchTypeList.size()) {
            Toast.makeText(this, "Vui lòng chọn nội dung thi đấu", Toast.LENGTH_SHORT).show();
            return;
        }

        TourneyDetailResponse.MatchType selectedType = matchTypeList.get(selectedPosition);

        if (selectedType.isFull()) {
            Toast.makeText(this,
                    "Nội dung thi đấu này đã đầy. Vui lòng chọn nội dung khác.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "  Selected match type: " + selectedType.getMatchTypeName());
        Log.d(TAG, "  Match type code: " + selectedType.getMatchTypeCode());

        // Show fixed form data (from API)
        Log.d(TAG, "  Fixed Form Data (from API):");
        Log.d(TAG, "    Full Name: " + etFullName.getText().toString().trim());
        Log.d(TAG, "    Phone: " + etPhone.getText().toString().trim());
        Log.d(TAG, "    Email: " + etEmail.getText().toString().trim());
        Log.d(TAG, "    Gender: " + (rbMale.isChecked() ? "Nam" : "Nữ"));

        // Show dynamic form data
        Log.d(TAG, "  Dynamic Form Data:");
        for (TourneyRegConfigResponse field : formFields) {
            Log.d(TAG, "    " + field.getFieldName() + " = " + field.getValue());
        }

        // ✅ Navigate to Confirmation Activity
        navigateToConfirmation(selectedType);
    }

    /**
     * ✅ Navigate to TournamentConfirmationActivity với data
     */
    private void navigateToConfirmation(TourneyDetailResponse.MatchType selectedMatchType) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║            navigateToConfirmation()                        ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        Intent intent = new Intent(this, TournamentConfirmationActivity.class);

        // Tournament Info
        intent.putExtra("tournamentId", tournamentId);
        intent.putExtra("tournamentName", tournamentName);
        intent.putExtra("tournamentDate", getIntent().getStringExtra("tournamentDate"));
        intent.putExtra("tournamentLocation", getIntent().getStringExtra("tournamentLocation"));

        // Match Type Info
        //intent.putExtra("matchTypeId", selectedMatchType.getTournamentDetailId());
        intent.putExtra("matchTypeName", selectedMatchType.getMatchTypeName());
        intent.putExtra("matchTypeCode", selectedMatchType.getMatchTypeCode());

        // ✅ Ưu tiên lấy từ matchType, nếu không có thì dùng từ participationConditions
        String feeToUse = getIntent().getStringExtra("registrationFee");
        if (feeToUse == null || feeToUse.isEmpty() || "0".equals(feeToUse)) {
            feeToUse = String.valueOf(registrationFee);
        }
        intent.putExtra("registrationFee", feeToUse);
        Log.d(TAG, "  ✓ Registration Fee: " + feeToUse);

        // Player Info (Fixed Fields)
        intent.putExtra("playerName", etFullName.getText().toString().trim());
        intent.putExtra("playerPhone", etPhone.getText().toString().trim());
        intent.putExtra("playerEmail", etEmail.getText().toString().trim());
        intent.putExtra("playerGender", rbMale.isChecked() ? "Nam" : "Nữ");

        // Dynamic Form Data - Convert to JSON string
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray();
            for (TourneyRegConfigResponse field : formFields) {
                org.json.JSONObject fieldObj = new org.json.JSONObject();
                fieldObj.put("fieldName", field.getFieldName());
                fieldObj.put("label", field.getLabel());
                fieldObj.put("value", field.getValue());
                fieldObj.put("fieldType", field.getFieldType());
                jsonArray.put(fieldObj);
            }
            intent.putExtra("dynamicFormData", jsonArray.toString());
            Log.d(TAG, "  ✓ Dynamic form data serialized: " + jsonArray.length() + " fields");
        } catch (org.json.JSONException e) {
            Log.e(TAG, "  ✗ Error serializing dynamic form data", e);
        }

        Log.d(TAG, "  ✓ Starting TournamentConfirmationActivity");
        startActivity(intent);
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