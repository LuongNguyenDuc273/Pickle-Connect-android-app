package com.datn06.pickleconnect.Booking;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.BookingApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Adapter.BookingHistoryAdapter;
import com.datn06.pickleconnect.Model.BookingHistoryDTO;
import com.datn06.pickleconnect.R;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingHistoryActivity extends AppCompatActivity {

    private static final String TAG = "BookingHistoryActivity";

    // UI Components
    private ImageView ivBack;
    private TextView tvTitle;
    private TabLayout tabLayout;
    private EditText etFromDate, etToDate;
    private ImageView ivFilter;
    private RecyclerView rvBookingHistory;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    // Data
    private BookingHistoryAdapter adapter;
    private List<BookingHistoryDTO> allBookings = new ArrayList<>();
    private long userId = -1L;
    private String currentStatus = "00"; // Default: Đã thanh toán
    private Calendar fromCalendar, toCalendar;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        initViews();
        getUserId();
        setupRecyclerView();
        setupTabs();
        setupDatePickers();
        setupListeners();

        // Set default date range (last 30 days)
        toCalendar = Calendar.getInstance();
        fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.DAY_OF_MONTH, -30);

        updateDateFields();
        loadBookingHistory();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tabLayout = findViewById(R.id.tabLayout);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        ivFilter = findViewById(R.id.ivFilter);
        rvBookingHistory = findViewById(R.id.rvBookingHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        userId = prefs.getLong("accountId", -1L);
        
        if (userId == -1L) {
            Log.e(TAG, "User not logged in - accountId not found");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new BookingHistoryAdapter(this);
        adapter.setBookingList(allBookings);
        adapter.setOnBookingClickListener(new BookingHistoryAdapter.OnBookingClickListener() {
            @Override
            public void onDetailClick(BookingHistoryDTO booking) {
                // TODO: Open booking detail or rating screen
                Toast.makeText(BookingHistoryActivity.this, 
                    "Đánh giá: " + booking.getBookingCode(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRebookClick(BookingHistoryDTO booking) {
                // TODO: Navigate to booking screen with pre-filled data
                Toast.makeText(BookingHistoryActivity.this, 
                    "Tái phiếu: " + booking.getBookingCode(), Toast.LENGTH_SHORT).show();
            }
        });

        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBookingHistory.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Đã thanh toán
                        currentStatus = "00";
                        break;
                    case 1: // Đã sử dụng
                        currentStatus = "USED";
                        break;
                    case 2: // Đã hủy
                        currentStatus = "02";
                        break;
                }
                loadBookingHistory();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupDatePickers() {
        etFromDate.setOnClickListener(v -> showDatePicker(true));
        etToDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        ivFilter.setOnClickListener(v -> {
            loadBookingHistory();
        });
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar calendar = isFromDate ? fromCalendar : toCalendar;
        
        DatePickerDialog dialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateFields();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        dialog.show();
    }

    private void updateDateFields() {
        etFromDate.setText(dateFormatter.format(fromCalendar.getTime()));
        etToDate.setText(dateFormatter.format(toCalendar.getTime()));
    }

    private void loadBookingHistory() {
        progressBar.setVisibility(View.VISIBLE);
        rvBookingHistory.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        String fromDate = apiDateFormatter.format(fromCalendar.getTime());
        String toDate = apiDateFormatter.format(toCalendar.getTime());

        BookingHistoryRequest request = new BookingHistoryRequest(
            userId,
            currentStatus,
            fromDate,
            toDate
        );

        Log.d(TAG, "Loading booking history: userId=" + userId + ", status=" + currentStatus + 
              ", from=" + fromDate + ", to=" + toDate);

        BookingApiService apiService = ApiClient.createService(
            ServiceHost.TXN_SERVICE, 
            BookingApiService.class
        );

        apiService.getBookingHistory(request).enqueue(new Callback<List<BookingHistoryDTO>>() {
            @Override
            public void onResponse(Call<List<BookingHistoryDTO>> call, Response<List<BookingHistoryDTO>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<BookingHistoryDTO> bookings = response.body();
                    
                    if (bookings.isEmpty()) {
                        showEmptyState();
                    } else {
                        allBookings = bookings;
                        adapter.setBookingList(allBookings);
                        rvBookingHistory.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Loaded " + bookings.size() + " bookings");
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    showEmptyState();
                    Toast.makeText(BookingHistoryActivity.this, 
                        "Không thể tải lịch sử đặt sân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookingHistoryDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState();
                Log.e(TAG, "API call failed", t);
                Toast.makeText(BookingHistoryActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvBookingHistory.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }
}
