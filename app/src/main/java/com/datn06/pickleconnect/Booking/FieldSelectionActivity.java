package com.datn06.pickleconnect.Booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Event.EventsActivity;
import com.datn06.pickleconnect.Model.FieldAvailabilityDTO;
import com.datn06.pickleconnect.Model.FieldBookingResponse;
import com.datn06.pickleconnect.Model.SelectedSlotDTO;
import com.datn06.pickleconnect.Model.TimeSlotDTO;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for selecting fields and time slots for booking
 */
public class FieldSelectionActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton btnBack;
    private TextView tvHeaderTitle;
    private TabLayout tabLayout;
    private ImageButton btnPrevDate, btnNextDate;
    private TextView tvSelectedDate;
    private RecyclerView rvFieldTabs, rvTimeSlots;
    private TextView tvSelectedCount, tvTotalPrice;
    private MaterialButton btnContinue;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    
    // Data
    private Long facilityId;
    private String facilityName;
    private Calendar selectedDate;
    private FieldBookingResponse fieldBookingData;
    private int selectedFieldIndex = 0;
    private final List<SelectedSlotDTO> selectedSlots = new ArrayList<>();
    
    // Adapters
    private FieldTabAdapter fieldTabAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_selection);
        
        // Get intent data
        facilityId = getIntent().getLongExtra("facilityId", 0);
        facilityName = getIntent().getStringExtra("facilityName");
        
        if (facilityId == 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy cơ sở", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupListeners();
        
        // Initialize date to today
        selectedDate = Calendar.getInstance();
        
        // Initial load
        loadFieldAvailability();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tabLayout = findViewById(R.id.tabLayout);
        btnPrevDate = findViewById(R.id.btnPrevDate);
        btnNextDate = findViewById(R.id.btnNextDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rvFieldTabs = findViewById(R.id.rvFieldTabs);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnContinue = findViewById(R.id.btnContinue);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        
        // Set header title
        if (facilityName != null) {
            tvHeaderTitle.setText(facilityName);
        }
        
        // Setup RecyclerViews
        rvFieldTabs.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Date navigation
        btnPrevDate.setOnClickListener(v -> navigateDate(-1));
        btnNextDate.setOnClickListener(v -> navigateDate(1));

        // Date picker
        tvSelectedDate.setOnClickListener(v -> showDatePicker());

        // Continue button
        btnContinue.setOnClickListener(v -> onContinueClicked());

        // Tab layout (Dịch kèo sẵn / Đặt kèo tự tạo)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab 0 = Dịch kèo sẵn (hiện tại)
                // Tab 1 = Đặt kèo tự tạo (chuyển sang EventsActivity)
                if (tab.getPosition() == 1) {
                    // Chuyển sang EventsActivity
                    Intent intent = new Intent(FieldSelectionActivity.this, EventsActivity.class);
                    intent.putExtra("facilityId", facilityId);
                    intent.putExtra("facilityName", facilityName);
                    startActivity(intent);
                    // Đóng Activity hiện tại để tránh stack quá nhiều
                    finish();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    /**
     * Load field availability from API
     */
    private void loadFieldAvailability() {
        showLoading(true);
        
        // Get user info from TokenManager (Login lưu vào TokenManager)
        TokenManager tokenManager = TokenManager.getInstance(this);
        
        // Get userId for query parameter
        String userId = tokenManager.getUserId();
        
        String dateStr = formatDateForApi(selectedDate);  // "2025-11-17"
        
        CourtApiService courtService = ApiClient.createService(
            ServiceHost.COURT_SERVICE, 
            CourtApiService.class
        );
        
        courtService.getFieldAvailability(userId, facilityId, dateStr)
            .enqueue(new Callback<BaseResponse<FieldBookingResponse>>() {
                @Override
                public void onResponse(Call<BaseResponse<FieldBookingResponse>> call, 
                                     Response<BaseResponse<FieldBookingResponse>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<FieldBookingResponse> baseResponse = response.body();
                        
                        if (baseResponse.isSuccess()) {
                            fieldBookingData = baseResponse.getData();
                            displayFieldsAndSlots();
                        } else {
                            showError(baseResponse.getErrorMessage());
                        }
                    } else {
                        showError("Lỗi kết nối server");
                    }
                }
                
                @Override
                public void onFailure(Call<BaseResponse<FieldBookingResponse>> call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi kết nối: " + t.getMessage());
                }
            });
    }
    
    /**
     * Display fields and time slots
     */
    private void displayFieldsAndSlots() {
        if (fieldBookingData == null || fieldBookingData.getFields() == null 
            || fieldBookingData.getFields().isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        
        // Update date label
        updateDateLabel();
        
        // Display field tabs
        fieldTabAdapter = new FieldTabAdapter(
            fieldBookingData.getFields(),
            selectedFieldIndex,
            this::onFieldSelected
        );
        rvFieldTabs.setAdapter(fieldTabAdapter);
        
        // Display time slots for first field
        displayTimeSlotsForField(0);
    }
    
    /**
     * Display time slots for a specific field
     */
    private void displayTimeSlotsForField(int fieldIndex) {
        if (fieldBookingData == null || fieldBookingData.getFields() == null) {
            return;
        }
        
        if (fieldIndex < 0 || fieldIndex >= fieldBookingData.getFields().size()) {
            return;
        }
        
        selectedFieldIndex = fieldIndex;
        
        // Update field tab selection
        if (fieldTabAdapter != null) {
            fieldTabAdapter.setSelectedPosition(fieldIndex);
        }
        
        // Get slots grouped by period
        Map<String, List<TimeSlotDTO>> groupedSlots = 
            fieldBookingData.getSlotsByPeriod(fieldIndex);
        
        // Create and set adapter
        timeSlotAdapter = new TimeSlotAdapter(
            groupedSlots,
            selectedSlots,
            this::onSlotClicked
        );
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }
    
    /**
     * Handle field tab selection
     */
    private void onFieldSelected(int position) {
        displayTimeSlotsForField(position);
    }
    
    /**
     * Handle slot click
     */
    private void onSlotClicked(TimeSlotDTO slot, boolean isSelected) {
        if (!slot.getIsAvailable()) {
            Toast.makeText(this, "Slot này đã được đặt", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isSelected) {
            // Add to selection
            FieldAvailabilityDTO currentField = 
                fieldBookingData.getFields().get(selectedFieldIndex);
            
            SelectedSlotDTO selectedSlot = SelectedSlotDTO.fromTimeSlot(
                slot, 
                currentField.getFieldId()
            );
            
            // Set field name for display
            selectedSlot.setFieldName(currentField.getFieldName());
            
            selectedSlots.add(selectedSlot);
        } else {
            // Remove from selection
            selectedSlots.removeIf(s -> s.getSlotId().equals(slot.getSlotId()));
        }
        
        updateSummary();
        
        // Notify adapter to refresh colors
        if (timeSlotAdapter != null) {
            timeSlotAdapter.notifySelectionChanged();
        }
    }
    
    /**
     * Update summary (count and total price)
     */
    private void updateSummary() {
        // Update count
        tvSelectedCount.setText(String.valueOf(selectedSlots.size()));
        
        // Calculate total price
        BigDecimal total = selectedSlots.stream()
            .map(SelectedSlotDTO::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        tvTotalPrice.setText(formatCurrency(total));
        
        // Enable/disable continue button
        btnContinue.setEnabled(!selectedSlots.isEmpty());
    }
    
    /**
     * Navigate to next screen (booking confirmation)
     */
    private void onContinueClicked() {
        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một slot", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calculate total
        BigDecimal totalAmount = selectedSlots.stream()
            .map(SelectedSlotDTO::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Navigate to confirmation screen
        Intent intent = new Intent(this, BookingConfirmActivity.class);
        intent.putExtra("selectedSlots", new Gson().toJson(selectedSlots));
        intent.putExtra("facilityId", facilityId);
        intent.putExtra("facilityName", facilityName);
        intent.putExtra("bookingDate", formatDateForApi(selectedDate));
        intent.putExtra("totalAmount", totalAmount.toString());
        intent.putExtra("totalHours", selectedSlots.size());
        
        startActivity(intent);
    }
    
    // ========== Date Navigation ==========
    
    /**
     * Navigate date by offset days
     */
    private void navigateDate(int offsetDays) {
        selectedDate.add(Calendar.DAY_OF_MONTH, offsetDays);
        
        // Don't allow past dates
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        
        if (selectedDate.before(today)) {
            selectedDate = today;
            Toast.makeText(this, "Không thể chọn ngày quá khứ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clear previous selections
        selectedSlots.clear();
        updateSummary();
        
        // Reload data
        loadFieldAvailability();
    }
    
    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                
                // Clear selections and reload
                selectedSlots.clear();
                updateSummary();
                loadFieldAvailability();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set min date to today
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }
    
    /**
     * Update date label
     */
    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d - dd/MM/yyyy", 
            new Locale("vi", "VN"));
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Format date for API (yyyy-MM-dd)
     */
    private String formatDateForApi(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date.getTime());
    }
    
    /**
     * Format currency
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        
        return String.format(Locale.US, "%,d VND", amount.intValue());
    }
    
    /**
     * Show loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvTimeSlots.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Show empty state
     */
    private void showEmptyState() {
        layoutEmptyState.setVisibility(View.VISIBLE);
        rvTimeSlots.setVisibility(View.GONE);
        rvFieldTabs.setVisibility(View.GONE);
    }
    
    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        layoutEmptyState.setVisibility(View.GONE);
        rvTimeSlots.setVisibility(View.VISIBLE);
        rvFieldTabs.setVisibility(View.VISIBLE);
    }
}
