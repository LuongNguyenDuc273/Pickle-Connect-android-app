package com.datn06.pickleconnect.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.BookingApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Adapter.BookingHistoryAdapter;
import com.datn06.pickleconnect.Booking.BookingHistoryRequest;
import com.datn06.pickleconnect.Model.BookingHistoryDTO;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị lịch sử đặt sân
 * Sử dụng logic từ BookingHistoryActivity
 */
public class BookingHistoryFragment extends Fragment {

    private static final String TAG = "BookingHistoryFragment";

    private RecyclerView bookingHistoryRecyclerView;
    private View emptyStateLayout;
    private ProgressBar progressBar;
    
    private BookingHistoryAdapter adapter;
    private List<BookingHistoryDTO> allBookings = new ArrayList<>();
    private TokenManager tokenManager;
    private SimpleDateFormat apiDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_history, container, false);

        tokenManager = TokenManager.getInstance(requireContext());
        
        initViews(view);
        setupRecyclerView();
        loadBookingHistory();

        return view;
    }

    private void initViews(View view) {
        bookingHistoryRecyclerView = view.findViewById(R.id.bookingHistoryRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Thêm ProgressBar nếu chưa có trong layout
        if (progressBar == null) {
            progressBar = new ProgressBar(requireContext());
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        adapter = new BookingHistoryAdapter(requireContext());
        adapter.setBookingList(allBookings);
        adapter.setOnBookingClickListener(new BookingHistoryAdapter.OnBookingClickListener() {
            @Override
            public void onDetailClick(BookingHistoryDTO booking) {
                Toast.makeText(requireContext(), 
                    "Đánh giá: " + booking.getBookingCode(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRebookClick(BookingHistoryDTO booking) {
                Toast.makeText(requireContext(), 
                    "Tái phiếu: " + booking.getBookingCode(), Toast.LENGTH_SHORT).show();
            }
        });

        bookingHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingHistoryRecyclerView.setAdapter(adapter);
    }

    private void loadBookingHistory() {
        if (!tokenManager.isLoggedIn()) {
            showEmptyState(true);
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        bookingHistoryRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        // Get userId from TokenManager
        String userIdStr = tokenManager.getUserId();
        long userId = -1L;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid userId: " + userIdStr);
        }

        if (userId == -1L) {
            showEmptyState(true);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        // Default: last 30 days, status "00" (Đã thanh toán)
        Calendar toCalendar = Calendar.getInstance();
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.DAY_OF_MONTH, -30);

        String fromDate = apiDateFormatter.format(fromCalendar.getTime());
        String toDate = apiDateFormatter.format(toCalendar.getTime());

        BookingHistoryRequest request = new BookingHistoryRequest(
            userId,
            "00", // Đã thanh toán
            fromDate,
            toDate
        );

        Log.d(TAG, "Loading booking history: userId=" + userId + ", from=" + fromDate + ", to=" + toDate);

        BookingApiService apiService = ApiClient.createService(
            ServiceHost.TXN_SERVICE, 
            BookingApiService.class
        );

        apiService.getBookingHistory(request).enqueue(new Callback<List<BookingHistoryDTO>>() {
            @Override
            public void onResponse(Call<List<BookingHistoryDTO>> call, Response<List<BookingHistoryDTO>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<BookingHistoryDTO> bookings = response.body();
                    
                    if (bookings.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        allBookings = bookings;
                        adapter.setBookingList(allBookings);
                        bookingHistoryRecyclerView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Loaded " + bookings.size() + " bookings");
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<List<BookingHistoryDTO>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                showEmptyState(true);
                Log.e(TAG, "API call failed", t);
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        if (tokenManager.isLoggedIn()) {
            loadBookingHistory();
        }
    }
}
