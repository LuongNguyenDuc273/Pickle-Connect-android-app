package com.datn06.pickleconnect.Profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.MemberApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.MemberInfoRequest;
import com.datn06.pickleconnect.Models.MemberInfoResponse;
import com.datn06.pickleconnect.Models.UpdateMemberRequest;
import com.datn06.pickleconnect.Models.UpdateMemberResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.AlertHelper;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImageEdit;
    private CardView editProfileImage;
    private TextInputEditText etFullName;
    private TextInputEditText etPhone;
    private TextInputEditText etDateOfBirth;
    private AutoCompleteTextView spinnerGender;
    private Button btnConfirm;

    private TokenManager tokenManager;
    private LoadingDialog loadingDialog;
    private MemberApiService memberApiService;

    // Date formatters
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat apiFormatWithDash = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // API actual format
    private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tokenManager = TokenManager.getInstance(requireContext());
        loadingDialog = new LoadingDialog(requireContext());

        memberApiService = ApiClient.createService(ServiceHost.MEMBER_SERVICE, MemberApiService.class);

        initViews(view);
        setupGenderSpinner();
        setupDatePicker();
        setupListeners();
        loadProfileDataFromApi();

        return view;
    }

    private void initViews(View view) {
        profileImageEdit = view.findViewById(R.id.profileImageEdit);
        editProfileImage = view.findViewById(R.id.editProfileImage);
        etFullName = view.findViewById(R.id.etFullName);
        etPhone = view.findViewById(R.id.etPhone);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        btnConfirm = view.findViewById(R.id.btnConfirm);
    }

    private void setupGenderSpinner() {
        String[] genders = {"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                genders
        );
        spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            String currentDate = etDateOfBirth.getText().toString().trim();
            if (!currentDate.isEmpty()) {
                try {
                    Date date = displayFormat.parse(currentDate);
                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (ParseException e) {
                    Log.e("ProfileFragment", "Error parsing date: " + e.getMessage());
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Display format dd/MM/yyyy
                        String dateDisplay = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        etDateOfBirth.setText(dateDisplay);
                    },
                    year, month, day
            );

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        editProfileImage.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
        });

        btnConfirm.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfileToApi();
            }
        });
    }

    private void loadProfileDataFromApi() {
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();

        String currentUserId = tokenManager.getUserId();
        String currentEmail = tokenManager.getEmail();
        String currentPhone = tokenManager.getPhoneNumber();

        Log.d("ProfileFragment", "Loading profile for userId: " + currentUserId);

        MemberInfoRequest request = new MemberInfoRequest(
                currentUserId,
                currentEmail,
                currentPhone
        );

        memberApiService.getMemberInfo(request).enqueue(new Callback<BaseResponse<MemberInfoResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<MemberInfoResponse>> call,
                                   Response<BaseResponse<MemberInfoResponse>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<MemberInfoResponse> baseResponse = response.body();

                    Log.d("ProfileFragment", "Response code: " + baseResponse.getCode());

                    if ("00".equals(baseResponse.getCode())) {
                        MemberInfoResponse data = baseResponse.getData();
                        Log.d("ProfileFragment", "DOB from API: " + data.getDateOfBirth());
                        Log.d("ProfileFragment", "Gender from API: " + data.getGender());
                        populateFields(data);
                    } else {
                        Toast.makeText(requireContext(),
                                baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(requireContext(),
                            "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(),
                            "Không thể tải thông tin: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberInfoResponse>> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e("ProfileFragment", "API call failed: " + t.getMessage(), t);
                Toast.makeText(requireContext(),
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(MemberInfoResponse data) {
        if (data != null) {
            // Set full name
            if (etFullName != null && data.getFullName() != null) {
                etFullName.setText(data.getFullName());
            }

            // Set phone number
            if (etPhone != null && data.getPhoneNumber() != null) {
                etPhone.setText(data.getPhoneNumber());
            }

            // Set date of birth - handle multiple formats
            if (etDateOfBirth != null && data.getDateOfBirth() != null && !data.getDateOfBirth().isEmpty()) {
                String dobString = data.getDateOfBirth();
                Date date = null;

                // Try parsing with different formats
                // 1. Try dd-MM-yyyy (API returns this format: "27-10-2003")
                try {
                    date = apiFormatWithDash.parse(dobString);
                    Log.d("ProfileFragment", "✅ Parsed with dd-MM-yyyy");
                } catch (ParseException e) {
                    // 2. Try yyyy-MM-dd HH:mm:ss.SSS (database format)
                    try {
                        date = dbFormat.parse(dobString);
                        Log.d("ProfileFragment", "✅ Parsed with yyyy-MM-dd HH:mm:ss.SSS");
                    } catch (ParseException e2) {
                        // 3. Try yyyy-MM-dd (simple date)
                        try {
                            date = apiFormat.parse(dobString);
                            Log.d("ProfileFragment", "✅ Parsed with yyyy-MM-dd");
                        } catch (ParseException e3) {
                            Log.e("ProfileFragment", "❌ All date formats failed for: " + dobString);
                        }
                    }
                }

                // Display the parsed date
                if (date != null) {
                    String displayDate = displayFormat.format(date);
                    etDateOfBirth.setText(displayDate);
                    Log.d("ProfileFragment", "Parsed DOB: " + dobString + " -> " + displayDate);
                } else {
                    etDateOfBirth.setText(dobString);
                }
            }

            // Handle gender as Integer or String
            if (spinnerGender != null && data.getGender() != null) {
                String genderDisplay = convertGenderToDisplay(data.getGender());
                spinnerGender.setText(genderDisplay, false);
            }
        }
    }

    private String convertGenderToDisplay(String gender) {
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
            // If not numeric, treat as string (MALE, FEMALE, OTHER)
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

    private String convertGenderToApi(String displayGender) {
        switch (displayGender) {
            case "Nam":
                return "1";  // API: 1 = Nam
            case "Nữ":
                return "0";  // API: 0 = Nữ (NOT 2!)
            default:
                return "2";  // API: 2 = Khác (fallback)
        }
    }

    private void updateProfileToApi() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dateOfBirthDisplay = etDateOfBirth.getText().toString().trim();
        String genderDisplay = spinnerGender.getText().toString().trim();

        Log.d("ProfileFragment", "=== UPDATE START ===");
        Log.d("ProfileFragment", "Display date: " + dateOfBirthDisplay);

        // Convert date from dd/MM/yyyy (display) to dd-MM-yyyy (API format)
        String dateOfBirthApi = "";
        if (!dateOfBirthDisplay.isEmpty()) {
            // Simple conversion: replace "/" with "-"
            dateOfBirthApi = dateOfBirthDisplay.replace("/", "-");
            Log.d("ProfileFragment", "Date conversion: " + dateOfBirthDisplay + " -> " + dateOfBirthApi);
        }

        // Convert gender to API format
        String gender = convertGenderToApi(genderDisplay);

        Log.d("ProfileFragment", "Final API date: " + dateOfBirthApi);
        Log.d("ProfileFragment", "Final API gender: " + gender);

        loadingDialog.show();

        String userId = tokenManager.getUserId();
        UpdateMemberRequest request = new UpdateMemberRequest(userId);

        request.setFullName(fullName);
        if (!dateOfBirthApi.isEmpty()) {
            request.setDateOfBirth(dateOfBirthApi);
        }
        request.setGender(gender);

        Log.d("ProfileFragment", "Updating - userId: " + userId + ", gender: " + gender + ", dob: " + dateOfBirthApi);

        memberApiService.updateMember(request).enqueue(new Callback<BaseResponse<UpdateMemberResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<UpdateMemberResponse>> call,
                                   Response<BaseResponse<UpdateMemberResponse>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<UpdateMemberResponse> baseResponse = response.body();

                    if ("00".equals(baseResponse.getCode())) {
                        tokenManager.saveUserInfo(
                                userId,
                                tokenManager.getUsername(),
                                fullName,
                                tokenManager.getEmail(),
                                phone
                        );

                        AlertHelper.showSuccess(requireActivity(), "Cập nhật thành công");

                        new android.os.Handler().postDelayed(() -> {
                            loadProfileDataFromApi();
                        }, 1500);

                    } else {
                        AlertHelper.showError(requireActivity(),
                                baseResponse.getMessage() != null ?
                                        baseResponse.getMessage() : "Cập nhật thất bại");
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(requireContext(),
                            "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(),
                            "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UpdateMemberResponse>> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e("ProfileFragment", "Update failed: " + t.getMessage(), t);
                AlertHelper.showError(requireActivity(),
                        "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private boolean validateInputs() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 2) {
            etFullName.setError("Họ và tên quá ngắn");
            etFullName.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            etPhone.setError("Số điện thoại không hợp lệ (ít nhất 10 số)");
            etPhone.requestFocus();
            return false;
        }

        if (!phone.matches("\\d+")) {
            etPhone.setError("Số điện thoại chỉ được chứa chữ số");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    public void updateProfileData(String name, String phone, String dob, String gender) {
        if (etFullName != null && name != null) {
            etFullName.setText(name);
        }
        if (etPhone != null && phone != null) {
            etPhone.setText(phone);
        }
        if (etDateOfBirth != null && dob != null && !dob.isEmpty()) {
            Date date = null;

            // Try parsing with different formats
            try {
                date = apiFormatWithDash.parse(dob);
            } catch (ParseException e) {
                try {
                    date = dbFormat.parse(dob);
                } catch (ParseException e2) {
                    try {
                        date = apiFormat.parse(dob);
                    } catch (ParseException e3) {
                        Log.e("ProfileFragment", "Error parsing date: " + e3.getMessage());
                    }
                }
            }

            if (date != null) {
                String displayDate = displayFormat.format(date);
                etDateOfBirth.setText(displayDate);
            } else {
                etDateOfBirth.setText(dob);
            }
        }
        if (spinnerGender != null && gender != null && !gender.isEmpty()) {
            spinnerGender.setText(gender, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tokenManager.isLoggedIn()) {
            loadProfileDataFromApi();
        }
    }
}