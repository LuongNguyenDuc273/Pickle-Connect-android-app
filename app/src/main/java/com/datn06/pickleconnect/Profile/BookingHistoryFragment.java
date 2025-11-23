package com.datn06.pickleconnect.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.R;

public class BookingHistoryFragment extends Fragment {

    private RecyclerView bookingHistoryRecyclerView;
    private View emptyStateLayout;
    // private BookingHistoryAdapter adapter; // You'll implement this later

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_history, container, false);

        initViews(view);
        setupRecyclerView();

        return view;
    }

    private void initViews(View view) {
        bookingHistoryRecyclerView = view.findViewById(R.id.bookingHistoryRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        bookingHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // TODO: Set your adapter here when you implement it
        // adapter = new BookingHistoryAdapter(bookingList);
        // bookingHistoryRecyclerView.setAdapter(adapter);

        // For now, show empty state
        showEmptyState(true);
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            bookingHistoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            bookingHistoryRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // TODO: Add method to load booking history data
    // public void loadBookingHistory() {
    //     // Fetch data from database or API
    //     // Update adapter
    // }
}
