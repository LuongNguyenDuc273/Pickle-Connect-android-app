package com.datn06.pickleconnect.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.datn06.pickleconnect.R;

/**
 * Fragment hiển thị thư viện ảnh/video
 * TODO: Implement danh sách ảnh/video sau
 */
public class MediaLibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Tạo layout đơn giản tạm thời
        View view = inflater.inflate(R.layout.fragment_media_library_temp, container, false);
        return view;
    }
}
