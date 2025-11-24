package com.datn06.pickleconnect.Profile;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.datn06.pickleconnect.R;

/**
 * Activity riêng để chỉnh sửa hồ sơ
 * Sử dụng ProfileFragment bên trong
 */
public class ProfileEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Setup back button
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // Load ProfileFragment
        if (savedInstanceState == null) {
            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, profileFragment);
            transaction.commit();
        }
    }
}
