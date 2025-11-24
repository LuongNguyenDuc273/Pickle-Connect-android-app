package com.datn06.pickleconnect.Profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.MemberApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Models.Base64UploadRequest;
import com.datn06.pickleconnect.Models.MemberInfoRequest;
import com.datn06.pickleconnect.Models.MemberInfoResponse;
import com.datn06.pickleconnect.Models.UpdateMemberRequest;
import com.datn06.pickleconnect.Models.UpdateMemberResponse;
import com.datn06.pickleconnect.Models.UploadImageResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.AlertHelper;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String selectedImageUrl = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tokenManager = TokenManager.getInstance(requireContext());
        loadingDialog = new LoadingDialog(requireContext());

        memberApiService = ApiClient.createService(ServiceHost.MEMBER_SERVICE, MemberApiService.class);

        setupImagePicker();
        initViews(view);
        setupGenderSpinner();
        setupDatePicker();
        setupListeners();
        loadProfileDataFromApi();

        return view;
    }

    /**
     * Setup image picker activity result launcher
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleSelectedImage(imageUri);
                        }
                    }
                }
        );
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
            openImagePicker();
        });

        btnConfirm.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfileToApi();
            }
        });
    }

    /**
     * Open image picker from gallery
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Handle selected image from gallery
     */
    private void handleSelectedImage(Uri imageUri) {
        try {
            // Show preview using Glide
            Glide.with(this)
                    .load(imageUri)
                    .into(profileImageEdit);

            // Upload to S3
            uploadImageToS3(imageUri);

        } catch (Exception e) {
            Log.e("ProfileFragment", "Error handling image: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Upload image to S3 storage via Member Service (port 9002)
     * Member Service will handle Base64 conversion and call S3 API (port 9023)
     */
    private void uploadImageToS3(Uri imageUri) {
        loadingDialog.show();

        try {
            // Get file from URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Không thể đọc file ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert to byte array
            byte[] imageBytes = getBytes(inputStream);
            inputStream.close();

            // Compress image if needed (max 1MB recommended for Base64)
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Compress to JPEG with quality adjustment
            int quality = 90;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            
            // Further compress if still too large
            while (baos.toByteArray().length > 1024 * 1024 && quality > 20) {
                baos.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            
            byte[] compressedBytes = baos.toByteArray();

            // Get MIME type
            String mimeType = requireContext().getContentResolver().getType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            // Convert to Base64 with data URI format
            String base64String = Base64.encodeToString(compressedBytes, Base64.NO_WRAP);
            String base64WithPrefix = "data:" + mimeType + ";base64," + base64String;

            // Create request parameters
            String userId = tokenManager.getUserId();
            String requestId = "IMG_" + System.currentTimeMillis();
            String requestTime = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                    .format(new Date());

            Base64UploadRequest request = new Base64UploadRequest();
            request.setImageData(base64WithPrefix);
            request.setUserId(userId);
            request.setRequestId(requestId);
            request.setRequestTime(requestTime);
            request.setSubFolder("avatars");

            // Call Member Service upload-avatar API (port 9002)
            memberApiService.uploadAvatar(request).enqueue(new Callback<UploadImageResponse>() {
                @Override
                public void onResponse(Call<UploadImageResponse> call,
                                       Response<UploadImageResponse> response) {
                    loadingDialog.dismiss();

                    if (response.isSuccessful() && response.body() != null) {
                        UploadImageResponse uploadResponse = response.body();

                        if (uploadResponse.isSuccess()) {
                            selectedImageUrl = uploadResponse.getFileUrl();
                            Log.d("ProfileFragment", "✅ Upload success: " + selectedImageUrl);
                            Toast.makeText(requireContext(),
                                    "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();

                            // Reload profile to show new avatar
                            loadProfileDataFromApi();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Upload thất bại: " + uploadResponse.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Lỗi server: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                    loadingDialog.dismiss();
                    Log.e("ProfileFragment", "Upload failed: " + t.getMessage(), t);
                    Toast.makeText(requireContext(),
                            "Lỗi upload: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            loadingDialog.dismiss();
            Log.e("ProfileFragment", "Error reading image: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi đọc file ảnh", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            loadingDialog.dismiss();
            Log.e("ProfileFragment", "Error processing image: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert InputStream to byte array
     */
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    /**
     * Get file name from URI
     */
    private String getFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = requireContext().getContentResolver().query(
                uri, null, null, null, null
        );

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return fileName;
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

            if (data.getAvatarUrl() != null && !data.getAvatarUrl().isEmpty()) {
                // Convert localhost MinIO URL to public ngrok URL
                String imageUrl = data.getAvatarUrl();

                // Replace localhost:9000 with ngrok URL
                // Lấy URL từ: docker logs ngrok-minio
                if (imageUrl.contains("localhost:9000")) {
                    imageUrl = imageUrl.replace("http://localhost:9000", "https://soren-painted-erosely.ngrok-free.dev");
                }
                // Backup: nếu URL trả về đã có domain khác, giữ nguyên
                else if (!imageUrl.startsWith("https://") && !imageUrl.startsWith("http://")) {
                    // URL không hợp lệ, thêm prefix
                    imageUrl = "https://soren-painted-erosely.ngrok-free.dev" + imageUrl;
                }

                Log.d("ProfileFragment", "Loading avatar from: " + imageUrl);

                // Check if Fragment is still attached
                if (isAdded() && getContext() != null && profileImageEdit != null) {
                    // Clear old image first
                    profileImageEdit.setImageDrawable(null);
                    
                    Log.d("ProfileFragment", "🔄 Starting Glide load...");
                    
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .skipMemoryCache(true) // Force skip cache
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // No disk cache
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    Log.e("ProfileFragment", "❌ Glide load failed: " + (e != null ? e.getMessage() : "unknown"));
                                    if (e != null) e.logRootCauses("ProfileFragment");
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    Log.d("ProfileFragment", "✅ Glide load success from: " + dataSource);
                                    Log.d("ProfileFragment", "📦 Resource size: " + resource.getIntrinsicWidth() + "x" + resource.getIntrinsicHeight());
                                    
                                    // Force view update
                                    profileImageEdit.post(() -> {
                                        profileImageEdit.invalidate();
                                        profileImageEdit.requestLayout();
                                        Log.d("ProfileFragment", "🔄 View invalidated and layout requested");
                                    });
                                    
                                    return false;
                                }
                            })
                            .into(profileImageEdit);
                } else {
                    Log.e("ProfileFragment", "❌ Cannot load image - Fragment not attached or view is null");
                }
            }
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