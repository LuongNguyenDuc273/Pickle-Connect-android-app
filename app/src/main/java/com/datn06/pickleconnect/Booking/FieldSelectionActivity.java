package com.datn06.pickleconnect.Booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.CourtApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.Event.EventDetailActivity;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for selecting fields and time slots for booking - Grid Layout Version
 * Updated: Merged event slots + click to navigate to event booking
 */
public class FieldSelectionActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private TextView tvHeaderTitle;
    private TabLayout tabLayout;
    private ImageButton btnPrevDate, btnNextDate;
    private TextView tvSelectedDate;
    private LinearLayout layoutCalendarGrid, layoutTimeHeader, layoutFieldRows;
    private TextView tvSelectedCount, tvTotalPrice;
    private MaterialButton btnContinue;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private ImageButton btnZoomIn, btnZoomOut;

    // Data
    private Long facilityId;
    private String facilityName;
    private Calendar selectedDate;
    private FieldBookingResponse fieldBookingData;
    private final List<SelectedSlotDTO> selectedSlots = new ArrayList<>();
    private List<String> uniqueTimeSlots = new ArrayList<>();

    // Cell dimensions - now variable for zoom
    private int cellWidthDp = 80;
    private int cellHeightDp = 50;
    private static final int FIELD_NAME_WIDTH_DP = 100;
    private static final int MIN_CELL_WIDTH = 60;
    private static final int MAX_CELL_WIDTH = 120;
    private static final int ZOOM_STEP = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_selection_grid);

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
        layoutCalendarGrid = findViewById(R.id.layoutCalendarGrid);
        layoutTimeHeader = findViewById(R.id.layoutTimeHeader);
        layoutFieldRows = findViewById(R.id.layoutFieldRows);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnContinue = findViewById(R.id.btnContinue);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);

        // Set header title
        if (facilityName != null) {
            tvHeaderTitle.setText(facilityName);
        }
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

        // Zoom buttons
        btnZoomIn.setOnClickListener(v -> zoomGrid(true));
        btnZoomOut.setOnClickListener(v -> zoomGrid(false));

        // Tab layout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    Intent intent = new Intent(FieldSelectionActivity.this, EventsActivity.class);
                    intent.putExtra("facilityId", facilityId);
                    intent.putExtra("facilityName", facilityName);
                    startActivity(intent);
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

        TokenManager tokenManager = TokenManager.getInstance(this);
        String userId = tokenManager.getUserId();
        String dateStr = formatDateForApi(selectedDate);

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
                                buildCalendarGrid();
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
     * Build the calendar grid view
     */
    private void buildCalendarGrid() {
        if (fieldBookingData == null || fieldBookingData.getFields() == null
                || fieldBookingData.getFields().isEmpty()) {
            showEmptyState();
            return;
        }

        hideEmptyState();
        updateDateLabel();

        // Clear previous views
        layoutTimeHeader.removeAllViews();
        layoutFieldRows.removeAllViews();

        // Get all unique time slots
        extractUniqueTimeSlots();

        if (uniqueTimeSlots.isEmpty()) {
            showEmptyState();
            return;
        }

        // Build time header
        buildTimeHeader();

        // Build field rows
        buildFieldRows();
    }

    /**
     * Extract all unique time slots from all fields
     */
    private void extractUniqueTimeSlots() {
        Set<String> timeSet = new LinkedHashSet<>();

        for (FieldAvailabilityDTO field : fieldBookingData.getFields()) {
            if (field.getTimeSlots() != null) {
                for (TimeSlotDTO slot : field.getTimeSlots()) {
                    timeSet.add(slot.getStartTime());
                }
            }
        }

        uniqueTimeSlots = new ArrayList<>(timeSet);
    }

    /**
     * Build time header row
     */
    private void buildTimeHeader() {
        // Add empty cell for field names column
        TextView emptyCell = createHeaderCell("");
        emptyCell.setLayoutParams(new LinearLayout.LayoutParams(
                dpToPx(FIELD_NAME_WIDTH_DP),
                dpToPx(cellHeightDp)
        ));
        emptyCell.setBackgroundColor(Color.parseColor("#E0E0E0"));
        layoutTimeHeader.addView(emptyCell);

        // Add time headers
        for (String timeSlot : uniqueTimeSlots) {
            TextView timeCell = createHeaderCell(formatTimeLabel(timeSlot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(cellWidthDp),
                    dpToPx(cellHeightDp)
            );
            params.setMargins(dpToPx(1), 0, 0, 0);
            timeCell.setLayoutParams(params);
            layoutTimeHeader.addView(timeCell);
        }
    }

    /**
     * Build field rows with merged event slots
     */
    private void buildFieldRows() {
        for (FieldAvailabilityDTO field : fieldBookingData.getFields()) {
            LinearLayout fieldRow = new LinearLayout(this);
            fieldRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, dpToPx(1), 0, 0);
            fieldRow.setLayoutParams(rowParams);

            // Add field name cell
            TextView fieldNameCell = createFieldNameCell(field.getFieldName());
            fieldRow.addView(fieldNameCell);

            // Track event slots to merge
            int i = 0;
            while (i < uniqueTimeSlots.size()) {
                String timeSlot = uniqueTimeSlots.get(i);
                TimeSlotDTO slot = findSlotByTime(field, timeSlot);

                // Check if this is an event slot
                if (slot != null && slot.getEventId() != null && !slot.getEventId().equals("null")) {
                    // Find consecutive event slots with same eventId
                    int spanCount = 1;
                    String eventId = slot.getEventId();

                    for (int j = i + 1; j < uniqueTimeSlots.size(); j++) {
                        TimeSlotDTO nextSlot = findSlotByTime(field, uniqueTimeSlots.get(j));
                        if (nextSlot != null && eventId.equals(nextSlot.getEventId())) {
                            spanCount++;
                        } else {
                            break;
                        }
                    }

                    // Create merged event cell
                    TextView eventCell = createMergedEventCell(slot, field, spanCount);
                    fieldRow.addView(eventCell);

                    // Skip the merged slots
                    i += spanCount;
                } else {
                    // Regular slot (available, booked, or empty)
                    TextView slotCell = createSlotCell(slot, field);
                    fieldRow.addView(slotCell);
                    i++;
                }
            }

            layoutFieldRows.addView(fieldRow);
        }
    }

    /**
     * Create merged event cell (spans multiple time slots)
     */
    private TextView createMergedEventCell(TimeSlotDTO slot, FieldAvailabilityDTO field, int spanCount) {
        TextView cell = new TextView(this);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(getSlotTextSize());

        // Calculate width for merged cell
        int totalWidth = dpToPx(cellWidthDp * spanCount + (spanCount - 1)); // Include margins
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                totalWidth,
                dpToPx(cellHeightDp)
        );
        params.setMargins(dpToPx(1), 0, 0, 0);
        cell.setLayoutParams(params);

        // ✅ THÊM PADDING GIỐNG CÁC CELL KHÁC
        cell.setPadding(dpToPx(2), dpToPx(4), dpToPx(2), dpToPx(4));

        // ✅ SET BORDER TRƯỚC KHI SET BACKGROUND COLOR
        cell.setBackground(ContextCompat.getDrawable(this, R.drawable.cell_border));

        // Event style - Pink background (set sau khi có border)
        cell.setBackgroundColor(Color.parseColor("#FF4081"));
        cell.setTextColor(Color.WHITE);
        cell.setText("Sự kiện\n" + formatCurrency(slot.getTicketPrice()));
        cell.setMaxLines(2);

        // ✅ Click to navigate to EVENT DETAIL instead of EventsActivity
        cell.setOnClickListener(v -> navigateToEventDetail(slot.getEventId()));

        return cell;
    }

    /**
     * Navigate to event booking screen
     */
    private void navigateToEventBooking(String eventId) {
        Intent intent = new Intent(this, EventsActivity.class);
        intent.putExtra("facilityId", facilityId);
        intent.putExtra("facilityName", facilityName);
        intent.putExtra("eventId", eventId); // Pass eventId to auto-select
        intent.putExtra("bookingDate", formatDateForApi(selectedDate));
        startActivity(intent);
    }

    /**
     * Find time slot by start time
     */
    private TimeSlotDTO findSlotByTime(FieldAvailabilityDTO field, String startTime) {
        if (field.getTimeSlots() == null) return null;

        for (TimeSlotDTO slot : field.getTimeSlots()) {
            if (slot.getStartTime().equals(startTime)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Create header cell
     */
    private TextView createHeaderCell(String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(getHeaderTextSize());
        cell.setTextColor(Color.BLACK);
        cell.setBackgroundColor(Color.parseColor("#F5F5F5"));
        cell.setPadding(dpToPx(2), dpToPx(8), dpToPx(2), dpToPx(8));
        cell.setBackground(ContextCompat.getDrawable(this, R.drawable.cell_border));
        return cell;
    }

    /**
     * Create field name cell
     */
    private TextView createFieldNameCell(String fieldName) {
        TextView cell = new TextView(this);
        cell.setText(fieldName);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(getFieldNameTextSize());
        cell.setTextColor(Color.BLACK);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        cell.setLayoutParams(new LinearLayout.LayoutParams(
                dpToPx(FIELD_NAME_WIDTH_DP),
                dpToPx(cellHeightDp)
        ));
        cell.setBackgroundColor(Color.parseColor("#E8F5E9"));
        cell.setPadding(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8));
        cell.setBackground(ContextCompat.getDrawable(this, R.drawable.cell_border));
        cell.setBackgroundColor(Color.parseColor("#E8F5E9"));
        return cell;
    }

    /**
     * Create time slot cell (for regular slots, not events)
     */
    private TextView createSlotCell(TimeSlotDTO slot, FieldAvailabilityDTO field) {
        TextView cell = new TextView(this);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(getSlotTextSize());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(cellWidthDp),
                dpToPx(cellHeightDp)
        );
        params.setMargins(dpToPx(1), 0, 0, 0);
        cell.setLayoutParams(params);
        cell.setPadding(dpToPx(2), dpToPx(4), dpToPx(2), dpToPx(4));

        if (slot == null) {
            // Empty slot
            cell.setBackgroundColor(Color.parseColor("#F5F5F5"));
            cell.setEnabled(false);
            return cell;
        }

        // Set initial appearance based on status
        updateSlotCellAppearance(cell, slot, field);

        // Set click listener (only for available slots)
        if (slot.getIsAvailable() && (slot.getEventId() == null || slot.getEventId().equals("null"))) {
            cell.setOnClickListener(v -> onSlotCellClicked(cell, slot, field));
        }

        return cell;
    }

    /**
     * Update slot cell appearance
     */
    private void updateSlotCellAppearance(TextView cell, TimeSlotDTO slot, FieldAvailabilityDTO field) {
        boolean isSelected = isSlotSelected(slot.getSlotId());

        if (isSelected) {
            // Selected - Green
            cell.setBackgroundColor(Color.parseColor("#4CAF50"));
            cell.setTextColor(Color.WHITE);
            cell.setText(formatCurrency(slot.getFixedPrice()));
        } else if (!slot.getIsAvailable()) {
            // Booked - Gray
            cell.setBackgroundColor(Color.parseColor("#9E9E9E"));
            cell.setTextColor(Color.WHITE);
            cell.setText("Đã đặt");
            cell.setEnabled(false);
        } else {
            // Available - Cyan
            cell.setBackgroundColor(Color.parseColor("#00BCD4"));
            cell.setTextColor(Color.WHITE);
            cell.setText(formatCurrency(slot.getFixedPrice()));
        }
    }

    /**
     * Handle slot cell click
     */
    private void onSlotCellClicked(TextView cell, TimeSlotDTO slot, FieldAvailabilityDTO field) {
        if (!slot.getIsAvailable()) {
            Toast.makeText(this, "Slot này đã được đặt", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCurrentlySelected = isSlotSelected(slot.getSlotId());

        if (isCurrentlySelected) {
            // Remove from selection
            selectedSlots.removeIf(s -> s.getSlotId() != null &&
                    s.getSlotId().toString().equals(slot.getSlotId()));
        } else {
            // Add to selection
            SelectedSlotDTO selectedSlot = SelectedSlotDTO.fromTimeSlot(slot, field.getFieldId());
            selectedSlot.setFieldName(field.getFieldName());
            selectedSlots.add(selectedSlot);
        }

        // Update cell appearance
        updateSlotCellAppearance(cell, slot, field);

        // Update summary
        updateSummary();
    }

    /**
     * Check if slot is selected
     */
    private boolean isSlotSelected(String slotId) {
        return selectedSlots.stream()
                .anyMatch(s -> s.getSlotId() != null && s.getSlotId().toString().equals(slotId));
    }

    /**
     * Update summary (count and total price)
     */
    private void updateSummary() {
        tvSelectedCount.setText(String.valueOf(selectedSlots.size()));

        BigDecimal total = selectedSlots.stream()
                .map(SelectedSlotDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tvTotalPrice.setText(formatCurrency(total));
        btnContinue.setEnabled(!selectedSlots.isEmpty());
    }

    /**
     * Navigate to booking confirmation
     */
    private void onContinueClicked() {
        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một slot", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal totalAmount = selectedSlots.stream()
                .map(SelectedSlotDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalHours = selectedSlots.size() * 0.5;

        Intent intent = new Intent(this, BookingConfirmActivity.class);
        intent.putExtra("selectedSlots", new Gson().toJson(selectedSlots));
        intent.putExtra("facilityId", facilityId);
        intent.putExtra("facilityName", facilityName);
        intent.putExtra("bookingDate", formatDateForApi(selectedDate));
        intent.putExtra("totalAmount", totalAmount.toString());
        intent.putExtra("totalHours", totalHours);

        startActivity(intent);
    }

    /**
     * Zoom grid in/out
     */
    private void zoomGrid(boolean zoomIn) {
        if (zoomIn) {
            if (cellWidthDp < MAX_CELL_WIDTH) {
                cellWidthDp += ZOOM_STEP;
                cellHeightDp = (int) (cellWidthDp * 0.625);
                rebuildGrid();
            } else {
                Toast.makeText(this, "Đã phóng to tối đa", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (cellWidthDp > MIN_CELL_WIDTH) {
                cellWidthDp -= ZOOM_STEP;
                cellHeightDp = (int) (cellWidthDp * 0.625);
                rebuildGrid();
            } else {
                Toast.makeText(this, "Đã thu nhỏ tối đa", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Rebuild grid with new cell sizes
     */
    private void rebuildGrid() {
        if (fieldBookingData != null) {
            buildCalendarGrid();
        }
    }

    // ========== Date Navigation ==========

    private void navigateDate(int offsetDays) {
        selectedDate.add(Calendar.DAY_OF_MONTH, offsetDays);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        if (selectedDate.before(today)) {
            selectedDate = today;
            Toast.makeText(this, "Không thể chọn ngày quá khứ", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedSlots.clear();
        updateSummary();
        loadFieldAvailability();
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    selectedSlots.clear();
                    updateSummary();
                    loadFieldAvailability();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d - dd/MM/yyyy",
                new Locale("vi", "VN"));
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    // ========== Helper Methods ==========

    private String formatDateForApi(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date.getTime());
    }

    private String formatTimeLabel(String startTime) {
        try {
            String[] parts = startTime.split(":");
            int startHour = Integer.parseInt(parts[0]);
            int startMinute = Integer.parseInt(parts[1]);

            int endHour = startHour;
            int endMinute = startMinute + 30;

            if (endMinute >= 60) {
                endHour++;
                endMinute -= 60;
            }

            String start = startHour + ":" + String.format("%02d", startMinute);
            String end = endHour + ":" + String.format("%02d", endMinute);

            return start + "-" + end;
        } catch (Exception e) {
            return startTime;
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }

        long value = amount.longValue();
        if (value >= 1000) {
            return (value / 1000) + "k";
        }
        return String.valueOf(value);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutCalendarGrid.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showEmptyState() {
        layoutEmptyState.setVisibility(View.VISIBLE);
        layoutCalendarGrid.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmptyState.setVisibility(View.GONE);
        layoutCalendarGrid.setVisibility(View.VISIBLE);
    }

    private int getHeaderTextSize() {
        return Math.round(8 + (cellWidthDp - MIN_CELL_WIDTH) * 6f / (MAX_CELL_WIDTH - MIN_CELL_WIDTH));
    }

    private int getSlotTextSize() {
        return Math.round(8 + (cellWidthDp - MIN_CELL_WIDTH) * 4f / (MAX_CELL_WIDTH - MIN_CELL_WIDTH));
    }

    private int getFieldNameTextSize() {
        return Math.round(12 + (cellWidthDp - MIN_CELL_WIDTH) * 4f / (MAX_CELL_WIDTH - MIN_CELL_WIDTH));
    }

    private void navigateToEventDetail(String eventId) {
        if (eventId == null || eventId.equals("null")) {
            Toast.makeText(this, "Không tìm thấy thông tin sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Replace with your actual EventDetailActivity
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("facilityId", facilityId);
        intent.putExtra("facilityName", facilityName);
        intent.putExtra("bookingDate", formatDateForApi(selectedDate));
        startActivity(intent);

        // Alternative: If you want to open EventsActivity with auto-selected event
        // Intent intent = new Intent(this, EventsActivity.class);
        // intent.putExtra("facilityId", facilityId);
        // intent.putExtra("facilityName", facilityName);
        // intent.putExtra("selectedEventId", eventId); // Auto select this event
        // intent.putExtra("bookingDate", formatDateForApi(selectedDate));
        // startActivity(intent);
    }
}