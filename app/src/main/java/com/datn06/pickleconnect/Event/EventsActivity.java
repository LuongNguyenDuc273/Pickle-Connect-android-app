package com.datn06.pickleconnect.Event;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.ApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Adapter.EventAdapter;
import com.datn06.pickleconnect.Booking.FieldSelectionActivity;
import com.datn06.pickleconnect.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventsActivity extends AppCompatActivity {

    private static final String TAG = "EventSelection";

    // API Service
    private ApiService apiService;

    // UI Components
    private ImageView btnBack;
    private TextView tabProduct;
    private TextView tabEvent;
    private TextView tvDateRange;
    private RecyclerView recyclerViewEvents;
    private View dateSelector;

    // Adapter
    private EventAdapter eventAdapter;

    // Date
    private Calendar startDate;
    private Calendar endDate;
    private SimpleDateFormat dateFormat;

    // Facility Info
    private Long facilityId = null;
    private String facilityName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Initialize API Service first
        initApiService();

        // Get facility data from Intent
        getFacilityIdFromIntent();

        // Initialize views
        initViews();

        // Setup adapter
        setupAdapter();

        // Setup listeners
        setupListeners();

        // Setup default dates
        setupDefaultDates();

        // Load events
        loadEvents();
    }

    /**
     * Initialize API Service
     */
    private void initApiService() {
        apiService = ApiClient.createService(ServiceHost.API_SERVICE, ApiService.class);
        Log.d(TAG, "API Service initialized for port 9003 (EventsActivity)");
    }

    /**
     * Get facility ID and name from Intent
     */
    private void getFacilityIdFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("facilityId")) {
                facilityId = intent.getLongExtra("facilityId", -1L);
                if (facilityId == -1L) {
                    facilityId = null;
                }
            }
            if (intent.hasExtra("facilityName")) {
                facilityName = intent.getStringExtra("facilityName");
            }
        }

        Log.d(TAG, "Received facilityId: " + facilityId);
        Log.d(TAG, "Received facilityName: " + facilityName);
    }

    /**
     * Initialize all views
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabProduct = findViewById(R.id.tabProduct);
        tabEvent = findViewById(R.id.tabEvent);
        tvDateRange = findViewById(R.id.tvDateRange);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        dateSelector = findViewById(R.id.dateSelector);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 7);
    }

    /**
     * Setup RecyclerView adapter
     */
    private void setupAdapter() {
        eventAdapter = new EventAdapter(this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);
    }

    /**
     * Setup all click listeners
     */
    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Tab Product - Switch to FieldSelectionActivity
        tabProduct.setOnClickListener(v -> {
            Intent intent = new Intent(EventsActivity.this, FieldSelectionActivity.class);
            intent.putExtra("facilityId", facilityId != null ? facilityId : 0L);
            intent.putExtra("facilityName", facilityName);
            startActivity(intent);
            // Close current activity
            finish();
        });

        // Tab Event - Already here
        tabEvent.setOnClickListener(v -> {
            // Do nothing, already on this tab
            Toast.makeText(this, "Đang ở tab sự kiện", Toast.LENGTH_SHORT).show();
        });

        // Date selector
        dateSelector.setOnClickListener(v -> showDateRangePicker());
    }

    /**
     * Setup default date range (today + 7 days)
     */
    private void setupDefaultDates() {
        updateDateRangeDisplay();
    }

    /**
     * Update date range display text
     */
    private void updateDateRangeDisplay() {
        String display = dateFormat.format(startDate.getTime()) +
                " - " +
                dateFormat.format(endDate.getTime());
        tvDateRange.setText(display);
    }

    /**
     * Show date range picker dialog
     */
    private void showDateRangePicker() {
        // First pick start date
        DatePickerDialog startPicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startDate.set(year, month, dayOfMonth);
                    showEndDatePicker();
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );

        startPicker.setTitle("Chọn ngày bắt đầu");
        startPicker.show();
    }

    /**
     * Show end date picker dialog
     */
    private void showEndDatePicker() {
        DatePickerDialog endPicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    endDate.set(year, month, dayOfMonth);

                    // Validate end date is after start date
                    if (endDate.before(startDate)) {
                        Toast.makeText(this,
                                "Ngày kết thúc phải sau ngày bắt đầu",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update display and reload events
                    updateDateRangeDisplay();
                    loadEvents();
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        );

        endPicker.setTitle("Chọn ngày kết thúc");
        endPicker.getDatePicker().setMinDate(startDate.getTimeInMillis());
        endPicker.show();
    }

    /**
     * Load events from API
     */
    private void loadEvents() {
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateStr = apiDateFormat.format(startDate.getTime());
        String endDateStr = apiDateFormat.format(endDate.getTime());

        String facilityIdStr = (facilityId != null) ? String.valueOf(facilityId) : null;

        Log.d(TAG, "Loading events from " + startDateStr + " to " + endDateStr +
                " for facilityId: " + facilityIdStr);

        // Call API
        Call<EventResponse> call = apiService.getEventList(
                facilityIdStr,
                startDateStr,
                endDateStr
        );

        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventResponse eventResponse = response.body();

                    if (eventResponse.isSuccess()) {
                        Log.d(TAG, "Loaded " + eventResponse.getData().size() + " events");
                        eventAdapter.setEventList(eventResponse.getData());

                        if (eventResponse.getData().isEmpty()) {
                            String message = facilityId != null ?
                                    "Không có sự kiện nào tại cơ sở này trong khoảng thời gian đã chọn" :
                                    "Không có sự kiện nào trong khoảng thời gian này";
                            Toast.makeText(EventsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EventsActivity.this,
                                "Lỗi: " + eventResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    Toast.makeText(EventsActivity.this,
                            "Không thể tải danh sách sự kiện",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(EventsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}