package com.datn06.pickleconnect.tournament;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
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
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private EditText etDateOfBirth;
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

    private TextView tvErrorFullName;
    private TextView tvErrorDateOfBirth;
    private TextView tvErrorPhone;
    private TextView tvErrorEmail;
    private TextView tvErrorGender;
    private TextView tvErrorMatchType;
    private TextView tvErrorTerms;

    // Data
    private String tournamentId;
    private String tournamentName;
    private String tournamentDetailId;
    private String currentUserId;
    private TokenManager tokenManager;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat apiFormatWithDash = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

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
        setupDatePicker();

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
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);

        // ✅ THÊM CÁC ERROR TEXTVIEW
        tvErrorFullName = findViewById(R.id.tvErrorFullName);
        tvErrorDateOfBirth = findViewById(R.id.tvErrorDateOfBirth);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvErrorGender = findViewById(R.id.tvErrorGender);
        tvErrorMatchType = findViewById(R.id.tvErrorMatchType);
        tvErrorTerms = findViewById(R.id.tvErrorTerms);

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

        btnBack.setOnClickListener(v -> finish());

        // ✅ Ẩn lỗi khi người dùng bắt đầu nhập
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideFieldError(tvErrorFullName);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideFieldError(tvErrorDateOfBirth);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideFieldError(tvErrorPhone);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideFieldError(tvErrorEmail);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            hideFieldError(tvErrorGender);
        });

        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideFieldError(tvErrorTerms);
        });

        spinnerRegType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hideFieldError(tvErrorMatchType);
                if (position >= 0 && position < matchTypeList.size()) {
                    TourneyDetailResponse.MatchType selectedType = matchTypeList.get(position);
                    loadFormConfig();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSubmit.setOnClickListener(v -> handleSubmit());
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

    private void setupDatePicker() {
        Log.d(TAG, "▶ setupDatePicker()");

        etDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // Nếu đã có ngày được chọn, sử dụng ngày đó
            String currentDate = etDateOfBirth.getText().toString().trim();
            if (!currentDate.isEmpty()) {
                try {
                    Date date = displayFormat.parse(currentDate);
                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format: dd/MM/yyyy
                        String dateDisplay = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        etDateOfBirth.setText(dateDisplay);
                        Log.d(TAG, "  Date selected: " + dateDisplay);
                    },
                    year, month, day
            );

            // Không cho chọn ngày trong tương lai
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        Log.d(TAG, "  ✓ DatePicker setup complete");
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
            Log.d(TAG, "  ✓ Full Name: " + data.getFullName());
        }

        // Set date of birth - THÊM MỚI
        if (data.getDateOfBirth() != null && !data.getDateOfBirth().isEmpty()) {
            String dobString = data.getDateOfBirth();
            Date date = null;

            Log.d(TAG, "  DOB from API: " + dobString);

            // Try parsing with different formats
            try {
                date = apiFormatWithDash.parse(dobString); // dd-MM-yyyy
                Log.d(TAG, "  ✅ Parsed with dd-MM-yyyy");
            } catch (ParseException e) {
                try {
                    date = dbFormat.parse(dobString); // yyyy-MM-dd HH:mm:ss.SSS
                    Log.d(TAG, "  ✅ Parsed with yyyy-MM-dd HH:mm:ss.SSS");
                } catch (ParseException e2) {
                    try {
                        date = apiFormat.parse(dobString); // yyyy-MM-dd
                        Log.d(TAG, "  ✅ Parsed with yyyy-MM-dd");
                    } catch (ParseException e3) {
                        Log.e(TAG, "  All date formats failed for: " + dobString);
                    }
                }
            }

            if (date != null) {
                String displayDate = displayFormat.format(date); // dd/MM/yyyy
                etDateOfBirth.setText(displayDate);
                Log.d(TAG, "  ✓ Date of Birth: " + displayDate);
            } else {
                etDateOfBirth.setText(dobString);
            }
        }

        // Set phone number
        if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
            etPhone.setText(data.getPhoneNumber());
            Log.d(TAG, "  ✓ Phone: " + data.getPhoneNumber());
        }

        // Set email
        if (data.getEmail() != null && !data.getEmail().isEmpty()) {
            etEmail.setText(data.getEmail());
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

            Log.d(TAG, "  ✓ Gender: " + genderDisplay);
        }

        Log.d(TAG, "  ✓ Fixed fields populated successfully (editable)");
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
            Log.d(TAG, "▶ matchtype:"+matchTypeNames);
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

        // ✅ Ẩn tất cả lỗi trước khi validate
        hideAllErrors();

        // ✅ 1. Validate match type selection
        int selectedPosition = spinnerRegType.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= matchTypeList.size()) {
            showFieldError(tvErrorMatchType, "Vui lòng chọn nội dung thi đấu");
            spinnerRegType.requestFocus();
            return;
        }

        TourneyDetailResponse.MatchType selectedType = matchTypeList.get(selectedPosition);

        // ✅ 2. Check if match type is full
        if (selectedType.isFull()) {
            showFieldError(tvErrorMatchType, "Nội dung thi đấu này đã đầy. Vui lòng chọn nội dung khác.");
            spinnerRegType.requestFocus();
            return;
        }

        // ✅ 3. Validate fixed fields
        if (!validateFixedFields()) {
            return;
        }

        // ✅ 4. Validate giới tính theo match type
        if (!validateGenderByMatchType(selectedType)) {
            return;
        }

        // ✅ 5. Validate độ tuổi theo match type
        if (!validateAgeByMatchType(selectedType)) {
            return;
        }

        // ✅ 6. Validate dynamic form fields
        if (formFields != null && !formFields.isEmpty()) {
            int invalidFieldPosition = dynamicFormAdapter.validateAllFields();

            if (invalidFieldPosition != -1) {
                rvDynamicForm.smoothScrollToPosition(invalidFieldPosition);
                TourneyRegConfigResponse invalidField = dynamicFormAdapter.getFieldAt(invalidFieldPosition);
                if (invalidField != null) {
                    Toast.makeText(this, "Vui lòng nhập: " + invalidField.getLabel(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "  ✗ Validation failed at position " + invalidFieldPosition);
                }
                return;
            }
            Log.d(TAG, "  ✓ All dynamic form fields validated successfully");
        }

        // ✅ 7. Validate terms checkbox
        if (!cbTerms.isChecked()) {
            showFieldError(tvErrorTerms, "Vui lòng đồng ý với chính sách chia sẻ dữ liệu và bảo mật");
            cbTerms.requestFocus();
            return;
        }

        Log.d(TAG, "  ✓ All validations passed");
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
        intent.putExtra("matchTypeName", selectedMatchType.getMatchTypeName());
        intent.putExtra("matchTypeCode", selectedMatchType.getMatchTypeCode());

        // Registration Fee
        String feeToUse = getIntent().getStringExtra("registrationFee");
        if (feeToUse == null || feeToUse.isEmpty() || "0".equals(feeToUse)) {
            feeToUse = String.valueOf(registrationFee);
        }
        intent.putExtra("registrationFee", feeToUse);
        Log.d(TAG, "  ✓ Registration Fee: " + feeToUse);

        // Player Info (Fixed Fields)
        intent.putExtra("playerName", etFullName.getText().toString().trim());
        intent.putExtra("playerDateOfBirth", etDateOfBirth.getText().toString().trim()); // ✅ THÊM MỚI
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

    private boolean validateGenderByMatchType(TourneyDetailResponse.MatchType matchType) {
        String matchTypeName = matchType.getMatchTypeName().toLowerCase();
        boolean isMaleSelected = rbMale.isChecked();
        boolean isFemaleSelected = rbFemale.isChecked();

        Log.d(TAG, "▶ validateGenderByMatchType()");

        if (!isMaleSelected && !isFemaleSelected) {
            showFieldError(tvErrorGender, "Vui lòng chọn giới tính");
            rgGender.requestFocus();
            return false;
        }

        if (matchTypeName.contains("đơn nam") || matchTypeName.contains("nam đơn")) {
            if (!isMaleSelected) {
                showFieldError(tvErrorGender, "Nội dung Đơn Nam yêu cầu giới tính phải là Nam");
                rgGender.requestFocus();
                return false;
            }
        } else if (matchTypeName.contains("đơn nữ") || matchTypeName.contains("nữ đơn")) {
            if (!isFemaleSelected) {
                showFieldError(tvErrorGender, "Nội dung Đơn Nữ yêu cầu giới tính phải là Nữ");
                rgGender.requestFocus();
                return false;
            }
        } else if (matchTypeName.contains("đôi nam") || matchTypeName.contains("nam đôi")) {
            if (!isMaleSelected) {
                showFieldError(tvErrorGender, "Nội dung Đôi Nam yêu cầu giới tính phải là Nam");
                rgGender.requestFocus();
                return false;
            }
        } else if (matchTypeName.contains("đôi nữ") || matchTypeName.contains("nữ đôi")) {
            if (!isFemaleSelected) {
                showFieldError(tvErrorGender, "Nội dung Đôi Nữ yêu cầu giới tính phải là Nữ");
                rgGender.requestFocus();
                return false;
            }
        }

        Log.d(TAG, "  ✓ Gender validation passed");
        return true;
    }

    private boolean validateFixedFields() {
        Log.d(TAG, "▶ validateFixedFields()");

        // Validate Full Name
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            showFieldError(tvErrorFullName, "Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.split("\\s+").length < 2) {
            showFieldError(tvErrorFullName, "Vui lòng nhập họ và tên đầy đủ");
            etFullName.requestFocus();
            return false;
        }

        // Validate Date of Birth
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        if (dateOfBirth.isEmpty()) {
            showFieldError(tvErrorDateOfBirth, "Vui lòng chọn ngày sinh");
            etDateOfBirth.requestFocus();
            return false;
        }

        int age = calculateAge(dateOfBirth);
        if (age < 5) {
            showFieldError(tvErrorDateOfBirth, "Độ tuổi không hợp lệ (quá nhỏ)");
            etDateOfBirth.requestFocus();
            return false;
        }
        if (age > 100) {
            showFieldError(tvErrorDateOfBirth, "Độ tuổi không hợp lệ (quá lớn)");
            etDateOfBirth.requestFocus();
            return false;
        }

        // Validate Phone
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            showFieldError(tvErrorPhone, "Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }

        if (!validatePhoneNumber(phone)) {
            showFieldError(tvErrorPhone, "Số điện thoại không đúng định dạng (VD: 0901234567)");
            etPhone.requestFocus();
            return false;
        }

        // Validate Email
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            showFieldError(tvErrorEmail, "Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!validateEmailFormat(email)) {
            showFieldError(tvErrorEmail, "Email không đúng định dạng (VD: example@gmail.com)");
            etEmail.requestFocus();
            return false;
        }

        Log.d(TAG, "  ✓ Fixed fields validation passed");
        return true;
    }

    private boolean validatePhoneNumber(String phone) {
        Log.d(TAG, "▶ validatePhoneNumber()");

        if (phone == null || phone.isEmpty()) {
            return false;
        }

        // Remove spaces and special characters
        phone = phone.replaceAll("[\\s\\-()]", "");

        // Regex cho số điện thoại Việt Nam:
        // - Bắt đầu bằng 0 hoặc +84 hoặc 84
        // - Theo sau là 9 chữ số
        // Ví dụ: 0901234567, +84901234567, 84901234567
        String phoneRegex = "^(0|\\+84|84)[0-9]{9}$";

        boolean isValid = phone.matches(phoneRegex);

        if (!isValid) {
            Log.d(TAG, "  ✗ Invalid phone format: " + phone);
        } else {
            Log.d(TAG, "  ✓ Valid phone format");
        }

        return isValid;
    }

    private boolean validateEmailFormat(String email) {
        Log.d(TAG, "▶ validateEmailFormat()");

        if (email == null || email.isEmpty()) {
            return false;
        }

        // Sử dụng Android's built-in pattern + thêm regex chi tiết hơn
        boolean isValidPattern = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        // Thêm validation cho các trường hợp đặc biệt
        boolean hasValidFormat = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        // Kiểm tra không có ký tự đặc biệt không hợp lệ
        boolean noInvalidChars = !email.contains("..") &&
                !email.startsWith(".") &&
                !email.endsWith(".");

        boolean isValid = isValidPattern && hasValidFormat && noInvalidChars;

        if (!isValid) {
            Log.d(TAG, "  ✗ Invalid email format: " + email);
        } else {
            Log.d(TAG, "  ✓ Valid email format");
        }

        return isValid;
    }

    private int[] extractAgeRangeFromMatchType(String matchTypeName) {
        Log.d(TAG, "▶ extractAgeRangeFromMatchType()");
        Log.d(TAG, "  Match Type Name: " + matchTypeName);

        try {
            // Regex để tìm pattern U<min>->U<max>
            String agePattern = "U(\\d+)->U(\\d+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(agePattern);
            java.util.regex.Matcher matcher = pattern.matcher(matchTypeName);

            if (matcher.find()) {
                int minAge = Integer.parseInt(matcher.group(1));
                int maxAge = Integer.parseInt(matcher.group(2));

                Log.d(TAG, "  ✓ Age range found: " + minAge + " - " + maxAge);
                return new int[]{minAge, maxAge};
            }

            // Kiểm tra pattern khác: U<age> (chỉ có một độ tuổi)
            String singleAgePattern = "U(\\d+)";
            pattern = java.util.regex.Pattern.compile(singleAgePattern);
            matcher = pattern.matcher(matchTypeName);

            if (matcher.find()) {
                int age = Integer.parseInt(matcher.group(1));
                Log.d(TAG, "  ✓ Single age limit found: U" + age);
                return new int[]{0, age}; // Từ 0 đến age
            }

            Log.d(TAG, "  ℹ No age restriction found");
            return null; // Không có giới hạn độ tuổi

        } catch (Exception e) {
            Log.e(TAG, "  ✗ Error extracting age range: " + e.getMessage());
            return null;
        }
    }

    private int calculateAge(String dateOfBirthStr) {
        Log.d(TAG, "▶ calculateAge()");

        try {
            Date dateOfBirth = displayFormat.parse(dateOfBirthStr); // dd/MM/yyyy

            if (dateOfBirth == null) {
                Log.e(TAG, "  ✗ Cannot parse date: " + dateOfBirthStr);
                return -1;
            }

            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(dateOfBirth);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

            // Kiểm tra nếu chưa đến sinh nhật trong năm nay
            if (today.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }

            Log.d(TAG, "  ✓ Age calculated: " + age);
            return age;

        } catch (ParseException e) {
            Log.e(TAG, "  ✗ Error parsing date: " + e.getMessage());
            return -1;
        }
    }

    private boolean validateAgeByMatchType(TourneyDetailResponse.MatchType matchType) {
        Log.d(TAG, "▶ validateAgeByMatchType()");

        String dateOfBirthStr = etDateOfBirth.getText().toString().trim();

        if (dateOfBirthStr.isEmpty()) {
            showFieldError(tvErrorDateOfBirth, "Vui lòng chọn ngày sinh");
            etDateOfBirth.requestFocus();
            return false;
        }

        int age = calculateAge(dateOfBirthStr);

        if (age < 0) {
            showFieldError(tvErrorDateOfBirth, "Ngày sinh không hợp lệ");
            etDateOfBirth.requestFocus();
            return false;
        }

        int[] ageRange = extractAgeRangeFromMatchType(matchType.getMatchTypeName());

        if (ageRange == null) {
            return true;
        }

        int minAge = ageRange[0];
        int maxAge = ageRange[1];

        if (age < minAge || age > maxAge) {
            String errorMsg = String.format("Độ tuổi của bạn (%d tuổi) không phù hợp (yêu cầu: %d - %d tuổi)",
                    age, minAge, maxAge);
            showFieldError(tvErrorDateOfBirth, errorMsg);
            etDateOfBirth.requestFocus();
            return false;
        }

        Log.d(TAG, "  ✓ Age validation passed");
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

    private void showFieldError(TextView errorTextView, String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    private void hideFieldError(TextView errorTextView) {
        if (errorTextView != null) {
            errorTextView.setVisibility(View.GONE);
        }
    }

    private void hideAllErrors() {
        hideFieldError(tvErrorFullName);
        hideFieldError(tvErrorDateOfBirth);
        hideFieldError(tvErrorPhone);
        hideFieldError(tvErrorEmail);
        hideFieldError(tvErrorGender);
        hideFieldError(tvErrorMatchType);
        hideFieldError(tvErrorTerms);
    }
}