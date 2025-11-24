package com.datn06.pickleconnect.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.datn06.pickleconnect.Profile.BookingHistoryFragment;
import com.datn06.pickleconnect.Profile.MediaLibraryFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BookingHistoryFragment(); // Tab "Lịch sử đặt sân"
            case 1:
                return new MediaLibraryFragment(); // Tab "Thư viện" (ảnh/video)
            default:
                return new BookingHistoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}
