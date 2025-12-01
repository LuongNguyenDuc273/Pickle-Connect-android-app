package com.datn06.pickleconnect.Profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AppConfig;
import com.datn06.pickleconnect.API.MemberApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Adapter.ProfilePagerAdapter;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Login.Login;
import com.datn06.pickleconnect.Models.MemberInfoRequest;
import com.datn06.pickleconnect.Models.MemberInfoResponse;
import com.datn06.pickleconnect.Utils.LoadingDialog;
import com.datn06.pickleconnect.Utils.SharedPrefManager;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.datn06.pickleconnect.Menu.MenuNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import de.hdodenhof.circleimageview.CircleImageView;

import com.datn06.pickleconnect.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private androidx.cardview.widget.CardView editBadge;
    private ImageView ivCopyId;
    private TextView userName;
    private TextView userGender;
    private TextView userId;
    private AppCompatButton logoutButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;
    private BottomNavigationView bottomNavigation;

    private TokenManager tokenManager;
    private LoadingDialog loadingDialog;
    private MemberApiService memberApiService;
    private MenuNavigation menuNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = TokenManager.getInstance(this);
        loadingDialog = new LoadingDialog(this);
        menuNavigation = new MenuNavigation(this);

        memberApiService = ApiClient.createService(ServiceHost.MEMBER_SERVICE, MemberApiService.class);

        initViews();
        setupViewPager();
        setupListeners();
        setupBottomNavigation();
        loadUserDataFromApi();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        editBadge = findViewById(R.id.editBadge);
        ivCopyId = findViewById(R.id.ivCopyId);
        userName = findViewById(R.id.userName);
        userGender = findViewById(R.id.userGender);
        userId = findViewById(R.id.userId);
        logoutButton = findViewById(R.id.logoutButton);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupViewPager() {
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Lịch sử đặt sân");
                            break;
                        case 1:
                            tab.setText("Thư viện");
                            break;
                    }
                }).attach();
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> handleLogout());
        
        // Edit badge - mở ProfileEditActivity (Activity riêng)
        editBadge.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });
        
        ivCopyId.setOnClickListener(v -> copyIdToClipboard());
        userId.setOnClickListener(v -> copyIdToClipboard());
    }

    /**
     * Setup Bottom Navigation
     */
    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            // Set Profile as selected
            bottomNavigation.setSelectedItemId(R.id.nav_account);

            bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    // If already on Profile, do nothing
                    if (itemId == R.id.nav_account) {
                        return true;
                    } else {
                        // Navigate to other pages
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

        // Ensure bottom navigation highlights correct item
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_account);
        }

        if (tokenManager.isLoggedIn()) {
            loadUserDataFromApi();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("ProfileActivity", "onNewIntent called - Activity reused");
    }

    private void copyIdToClipboard() {
        String idText = userId.getText().toString().replace("ID:", "").trim();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User ID", idText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserDataFromApi() {
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        loadingDialog.show();

        String currentUserId = tokenManager.getUserId();
        String currentEmail = tokenManager.getEmail();
        String currentPhone = tokenManager.getPhoneNumber();

        Log.d("ProfileActivity", "Loading profile for userId: " + currentUserId);

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

                    Log.d("ProfileActivity", "Response code: " + baseResponse.getCode());

                    if ("00".equals(baseResponse.getCode())) {
                        MemberInfoResponse data = baseResponse.getData();
                        updateUIWithProfileData(data);
                    } else {
                        Toast.makeText(ProfileActivity.this,
                                baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(ProfileActivity.this,
                            "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                    handleLogout();
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "Không thể tải thông tin: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MemberInfoResponse>> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e("ProfileActivity", "API call failed: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithProfileData(MemberInfoResponse data) {
        if (data != null) {
            // Load avatar image using Glide
            if (data.getAvatarUrl() != null && !data.getAvatarUrl().isEmpty()) {
                // Convert localhost MinIO URL to public ngrok URL
                String imageUrl = AppConfig.fixImageUrl(data.getAvatarUrl());
                // Replace localhost:9000 with ngrok URL
                Log.d("ProfileActivity", "Loading avatar from: " + imageUrl);
                
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(profileImage);
            }

            // Update name
            String fullName = data.getFullName() != null ? data.getFullName().toUpperCase() : "USER";
            userName.setText(fullName);

            // Update user ID
            String accountId = data.getUserId() != null ? data.getUserId() : "N/A";
            userId.setText("ID:" + accountId);

            // Handle gender as Integer or String
            String genderDisplay = convertGenderToDisplay(data.getGender());
            userGender.setText(genderDisplay);

            // Save to TokenManager
            tokenManager.saveUserInfo(
                    data.getUserId(),
                    data.getUsername(),
                    data.getFullName(),
                    data.getEmail(),
                    data.getPhoneNumber()
            );

            // Update fragment if visible
            updateProfileFragmentIfVisible(data);
        }
    }

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

    private void updateProfileFragmentIfVisible(MemberInfoResponse data) {
        if (viewPager.getCurrentItem() == 1) {
            ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + viewPager.getCurrentItem());

            if (fragment != null && fragment.isVisible()) {
                // Use the conversion method
                String genderDisplay = convertGenderToDisplay(data.getGender());

                fragment.updateProfileData(
                        data.getFullName(),
                        data.getPhoneNumber(),
                        data.getDateOfBirth(),
                        genderDisplay
                );
            }
        }
    }

    private void handleLogout() {
        // Clear TokenManager
        tokenManager.clearAll();
        
        // Also clear SharedPrefManager (used by BookingConfirmActivity)
        SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
        prefManager.logout();
        
        // Clear API token
        ApiClient.setAuthToken(null);

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void updateUserInfo(String name, String gender, String id) {
        userName.setText(name);
        userGender.setText(gender);
        userId.setText("ID:" + id);
    }
}