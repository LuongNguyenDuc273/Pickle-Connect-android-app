# 🎾 Luồng Chọn Sân Đặt - Chi Tiết Technical Flow

## 📱 Overview từ UI của bạn

Dựa trên hình ảnh, màn hình có cấu trúc:

```
┌─────────────────────────────┐
│ < Chọn sân                  │ ← Header
├─────────────────────────────┤
│ [Dịch kèo sẵn Booking][Đặt kèo tự tạo] │ ← Tabs
├─────────────────────────────┤
│ Chọn ngày                   │
│ 📅 Thu 7 - 22/04/2025  ◀ ▶  │ ← Date Picker
├─────────────────────────────┤
│ Chọn sân                    │
│ [Sân 1][Sân 2][Sân 3][Sân 4][Sân 5][Sân 6] │ ← Field Tabs
│         Xem tất cả          │
├─────────────────────────────┤
│ Chọn khung giờ              │
│ ☐ Đang chọn ☑ Chỗ trống ☐ Đã đặt │ ← Legend
│                             │
│ Sáng:                       │
│ [7:00-8:00][7:30-8:30][8:00-9:00][8:30-9:30] │
│   80k        50k      60k       50k    │
│                             │
│ [9:00-10:00][9:30-10:30][10:00-11:00][10:30-11:30] │
│   80k         50k        150k        50k     │
│                             │
│ [11:00-12:00][12:00-13:00]  │
│    150k         50k         │
│                             │
│ Chiều:                      │
│ [12:30-13:30][13:00-14:00][13:30-14:30][14:00-15:00] │
│   100k         50k         60k         50k     │
│                             │
│ [15:00-16:00][15:30-16:30][16:00-17:00]      │
│    50k         50k        120k                │
│                             │
│ [17:00-18:00][17:30-18:00][17:30-18:00][18:00-19:00] │
│   120k        120k         120k        120k    │
│                             │
│ Tối:                        │
│ [20:30-21:30][21:00-22:00][21:30-22:30][21:30-23:00] │
│    120k        120k        120k        120k    │
│                             │
│ [21:30-22:30]               │
│    50k                      │
├─────────────────────────────┤
│ Số sân      1               │
│ Tổng tiền   1,000,000 VND   │ ← Summary
│         [Tiếp tục] →        │ ← Action Button
└─────────────────────────────┘
```

---

## 🔄 Luồng Sự Kiện Khi User Chọn Sân

### **BƯỚC 1: User vào màn hình Chọn Sân**

#### Trigger:
- User từ màn Home/Danh sách cơ sở → Click vào 1 facility → Navigate to `FieldSelectionActivity`

#### Input Data:
```java
Intent intent = new Intent(context, FieldSelectionActivity.class);
intent.putExtra("facilityId", facilityId);  // e.g., 12345
intent.putExtra("facilityName", "Sân Pickle ABC");
intent.putExtra("facilityAddress", "123 Đường XYZ");
startActivity(intent);
```

---

### **BƯỚC 2: Load dữ liệu sân khả dụng**

#### API Call:
```http
GET /api/v1/booking/fields/availability?facilityId=12345&bookingDate=2025-04-22
Headers:
  X-Userinfo: {userId}
```

#### Backend Flow (`BookingController` → `BookingService` → `FieldBookingRepository`):

**Step 2.1: Controller nhận request**
```java
// BookingController.java line 42-77
@GetMapping("/fields/availability")
public ResponseEntity<BaseResponse<FieldBookingResponse>> getFieldAvailability(
    @RequestHeader("X-Userinfo") String xUser,
    @RequestParam BigInteger facilityId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate
) {
    // Validate date must be >= today
    if (bookingDate.isBefore(LocalDate.now())) {
        return ResponseEntity.badRequest().build();
    }
    
    FieldBookingRequest request = FieldBookingRequest.builder()
        .facilityId(facilityId)
        .bookingDate(bookingDate)
        .build();
    
    BaseResponse<FieldBookingResponse> response = 
        bookingService.getFieldAvailability(xUser, request);
    
    return ResponseEntity.ok(response);
}
```

**Step 2.2: Service xử lý**
```java
// BookingService.java line 43-60
public BaseResponse<FieldBookingResponse> getFieldAvailability(
    String xUser, 
    FieldBookingRequest request
) {
    String requestId = UUID.randomUUID().toString();
    BaseResponse<FieldBookingResponse> response = new BaseResponse<>();
    
    try {
        // Validate user exists
        if (!memberRepository.validateUser(xUser, userId, response)) {
            return response;  // User không tồn tại
        }
        
        // Get field availability data
        response.data = fieldBookingRepository.getFieldAvailability(request);
        response.setCode("00");
        response.setMessage("Success");
        
    } catch (Exception ex) {
        response.setCode("99");
        response.setMessage("System error");
    }
    
    return response;
}
```

**Step 2.3: Repository query database**
```java
// FieldBookingRepository.java line 25-46
@ReadOnlyConnection
public FieldBookingResponse getFieldAvailability(FieldBookingRequest request) {
    
    // 1. Get facility info
    Record facilityRecord = dsl.select(
        CAT_FACILITIES.FACILITY_ID,
        CAT_FACILITIES.FACILITY_NAME
    )
    .from(CAT_FACILITIES)
    .where(CAT_FACILITIES.FACILITY_ID.eq(request.getFacilityId()))
    .and(CAT_FACILITIES.STATUS.eq(1))  // Active only
    .fetchOne();
    
    if (facilityRecord == null) {
        return null;  // Facility không tồn tại hoặc inactive
    }
    
    // 2. Get all fields with their time slots
    List<FieldAvailabilityDTO> fields = 
        getFieldsWithAvailability(request.getFacilityId(), request.getBookingDate());
    
    // 3. Build response
    return FieldBookingResponse.builder()
        .facilityId(request.getFacilityId())
        .facilityName(facilityRecord.get(FACILITY_NAME))
        .bookingDate(request.getBookingDate())
        .fields(fields)  // List of fields with slots
        .build();
}
```

**Step 2.4: Get Fields & Time Slots**
```java
// FieldBookingRepository.java line 48-76
private List<FieldAvailabilityDTO> getFieldsWithAvailability(
    BigInteger facilityId, 
    LocalDate bookingDate
) {
    // 1. Get all active fields for facility
    Result<Record> fieldRecords = dsl.select()
        .from(CAT_FIELDS)
        .where(CAT_FIELDS.FACILITY_ID.eq(facilityId))
        .and(CAT_FIELDS.STATUS.eq(1))  // Active
        .orderBy(CAT_FIELDS.FIELD_NAME)
        .fetch();
    
    List<BigInteger> fieldIds = fieldRecords.stream()
        .map(r -> r.get(CAT_FIELDS.FIELD_ID))
        .toList();
    
    // 2. Get all time slots for these fields
    Map<Long, List<TimeSlotDTO>> fieldTimeSlots = 
        getTimeSlotsByField(fieldIds, bookingDate);
    
    // 3. Build FieldAvailabilityDTO for each field
    return fieldRecords.stream()
        .map(fieldRecord -> {
            Long fieldId = fieldRecord.get(CAT_FIELDS.FIELD_ID).longValue();
            
            return FieldAvailabilityDTO.builder()
                .fieldId(fieldId)
                .fieldName(fieldRecord.get(CAT_FIELDS.FIELD_NAME))  // "Sân 1", "Sân 2"
                .description(fieldRecord.get(CAT_FIELDS.DESCRIPTION))
                .status(fieldRecord.get(CAT_FIELDS.STATUS))
                .timeSlots(fieldTimeSlots.getOrDefault(fieldId, new ArrayList<>()))
                .build();
        })
        .toList();
}
```

**Step 2.5: Get Time Slots by Field**
```java
// FieldBookingRepository.java line 78-122
private Map<Long, List<TimeSlotDTO>> getTimeSlotsByField(
    List<BigInteger> fieldIds, 
    LocalDate bookingDate
) {
    // 1. Convert date to timestamp range
    LocalDateTime startOfDay = bookingDate.atStartOfDay();  // 2025-04-22 00:00:00
    LocalDateTime endOfDay = bookingDate.atTime(23, 59, 59);  // 2025-04-22 23:59:59
    
    // 2. Query all time slots for these fields on this date
    Result<Record> timeSlotRecords = dsl.select()
        .from(CAT_FIELD_TIME_SLOTS)
        .where(CAT_FIELD_TIME_SLOTS.FIELD_ID.in(fieldIds.toArray()))
        .and(CAT_FIELD_TIME_SLOTS.SLOT_DATE.between(startOfDay, endOfDay))
        .and(CAT_FIELD_TIME_SLOTS.STATUS.eq(1))  // Active slots only
        .orderBy(CAT_FIELD_TIME_SLOTS.FIELD_ID, CAT_FIELD_TIME_SLOTS.START_TIME)
        .fetch();
    
    // 3. Get booked slot IDs (slots already booked by others)
    Set<Long> bookedSlotIds = getBookedSlotIds(bookingDate);
    
    // 4. Get pricing for all fields
    Map<Long, List<Record>> fieldPricing = getPricingByField(fieldIds, bookingDate);
    
    // 5. Group slots by field and map to DTO
    return timeSlotRecords.stream()
        .collect(Collectors.groupingBy(
            record -> record.get(CAT_FIELD_TIME_SLOTS.FIELD_ID).longValue(),
            Collectors.mapping(
                record -> {
                    Long slotId = record.get(CAT_FIELD_TIME_SLOTS.SLOT_ID).longValue();
                    Long fieldId = record.get(CAT_FIELD_TIME_SLOTS.FIELD_ID).longValue();
                    LocalTime startTime = record.get(CAT_FIELD_TIME_SLOTS.START_TIME);
                    LocalTime endTime = record.get(CAT_FIELD_TIME_SLOTS.END_TIME);
                    
                    // Calculate price for this slot
                    BigDecimal[] prices = calculateSlotPricing(
                        fieldPricing.get(fieldId), 
                        startTime, 
                        bookingDate
                    );
                    
                    return TimeSlotDTO.builder()
                        .slotId(slotId)
                        .startTime(startTime)          // 07:00
                        .endTime(endTime)              // 08:00
                        .slotLabel("7:00-8:00")
                        .slotStatus(bookedSlotIds.contains(slotId) 
                            ? SlotStatus.BOOKED      // Đã đặt (gray)
                            : SlotStatus.AVAILABLE)  // Còn trống (cyan)
                        .fixedPrice(prices[0])       // 80,000
                        .walkinPrice(prices[1])      // 100,000
                        .isAvailable(!bookedSlotIds.contains(slotId))
                        .build();
                },
                Collectors.toList()
            )
        ));
}
```

**Step 2.6: Get Booked Slot IDs**
```java
// FieldBookingRepository.java line 124-145
private Set<Long> getBookedSlotIds(LocalDate bookingDate) {
    LocalDateTime startOfDay = bookingDate.atStartOfDay();
    LocalDateTime endOfDay = bookingDate.atTime(23, 59, 59);
    
    // Query slots that are already booked (status = SUCCESS or INIT)
    return dsl.selectDistinct(TXN_BOOKING_SLOTS.SLOT_ID)
        .from(TXN_BOOKING_SLOTS)
        .join(TXN_BOOKINGS)
            .on(TXN_BOOKING_SLOTS.BOOKING_ID.eq(TXN_BOOKINGS.BOOKING_ID))
        .join(CAT_FIELD_TIME_SLOTS)
            .on(TXN_BOOKING_SLOTS.SLOT_ID.eq(CAT_FIELD_TIME_SLOTS.SLOT_ID))
        .where(CAT_FIELD_TIME_SLOTS.SLOT_DATE.between(startOfDay, endOfDay))
        .and(TXN_BOOKINGS.STATUS.in(
            BookingStatus.SUCCESS.getCode(),  // Paid & confirmed
            BookingStatus.INIT.getCode()      // Reserved (payment pending)
        ))
        .fetch()
        .stream()
        .map(record -> record.get(TXN_BOOKING_SLOTS.SLOT_ID).longValue())
        .collect(Collectors.toSet());
}
```

**Step 2.7: Calculate Slot Pricing**
```java
// FieldBookingRepository.java line 147-162 & 164-182
private Map<Long, List<Record>> getPricingByField(
    List<BigInteger> fieldIds, 
    LocalDate bookingDate
) {
    // Get day of week (e.g., "THURSDAY")
    String dayOfWeek = bookingDate.getDayOfWeek()
        .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        .toUpperCase();
    
    // Query pricing rules for these fields
    Result<Record> pricingRecords = dsl.select()
        .from(CAT_FIELD_PRICES)
        .where(CAT_FIELD_PRICES.FIELD_ID.in(fieldIds.toArray()))
        .and(CAT_FIELD_PRICES.STATUS.eq(1))
        .and(
            CAT_FIELD_PRICES.WEEKDAY.eq(dayOfWeek)  // Specific weekday pricing
            .or(CAT_FIELD_PRICES.WEEKDAY.isNull())  // Or default pricing
        )
        .orderBy(CAT_FIELD_PRICES.FIELD_ID, CAT_FIELD_PRICES.START_TIME)
        .fetch();
    
    // Group by field_id
    return pricingRecords.stream()
        .collect(Collectors.groupingBy(
            record -> record.get(CAT_FIELD_PRICES.FIELD_ID).longValue()
        ));
}

private BigDecimal[] calculateSlotPricing(
    List<Record> fieldPricing, 
    LocalTime slotTime,  // e.g., 07:00
    LocalDate bookingDate
) {
    // Find pricing rule that covers this time
    Record applicablePricing = fieldPricing.stream()
        .filter(record -> {
            LocalTime startTime = record.get(CAT_FIELD_PRICES.START_TIME);
            LocalTime endTime = record.get(CAT_FIELD_PRICES.END_TIME);
            
            // Example: 
            // Rule: 06:00 - 12:00 = 80k
            // Slot: 07:00 → matches!
            return !slotTime.isBefore(startTime) && slotTime.isBefore(endTime);
        })
        .findFirst()
        .orElse(fieldPricing.getFirst());  // Fallback to default
    
    BigDecimal fixedPrice = applicablePricing.get(CAT_FIELD_PRICES.FIXED_PRICE);
    BigDecimal walkinPrice = applicablePricing.get(CAT_FIELD_PRICES.WALKIN_PRICE);
    
    return new BigDecimal[]{fixedPrice, walkinPrice};
}
```

---

### **BƯỚC 3: Response trả về Android**

#### Response Structure:
```json
{
  "code": "00",
  "message": "Success",
  "requestId": "abc-123-def",
  "data": {
    "facilityId": 12345,
    "facilityName": "Sân Pickle ABC",
    "bookingDate": "2025-04-22",
    "fields": [
      {
        "fieldId": 1,
        "fieldName": "Sân 1",
        "description": "Sân standard",
        "status": 1,
        "timeSlots": [
          {
            "slotId": 101,
            "startTime": "07:00:00",
            "endTime": "08:00:00",
            "slotLabel": "7:00-8:00",
            "slotStatus": "AVAILABLE",
            "fixedPrice": 80000,
            "walkinPrice": 100000,
            "isAvailable": true
          },
          {
            "slotId": 102,
            "startTime": "07:30:00",
            "endTime": "08:30:00",
            "slotLabel": "7:30-8:30",
            "slotStatus": "BOOKED",
            "fixedPrice": 50000,
            "walkinPrice": 70000,
            "isAvailable": false
          },
          {
            "slotId": 103,
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "slotLabel": "8:00-9:00",
            "slotStatus": "AVAILABLE",
            "fixedPrice": 60000,
            "walkinPrice": 80000,
            "isAvailable": true
          }
          // ... more slots
        ]
      },
      {
        "fieldId": 2,
        "fieldName": "Sân 2",
        "timeSlots": [...]
      }
      // ... more fields (Sân 3, 4, 5, 6...)
    ]
  }
}
```

---

### **BƯỚC 4: Android hiển thị UI**

#### Activity Code:
```java
public class FieldSelectionActivity extends AppCompatActivity {
    
    // UI Components
    private TextView tvFacilityName;
    private TextView tvSelectedDate;
    private ImageButton btnPrevDate, btnNextDate;
    private RecyclerView rvFieldTabs;
    private RecyclerView rvTimeSlots;
    private TextView tvSelectedCount;
    private TextView tvTotalPrice;
    private MaterialButton btnContinue;
    
    // Data
    private BigInteger facilityId;
    private String facilityName;
    private LocalDate selectedDate = LocalDate.now();
    private FieldBookingResponse fieldBookingData;
    private int selectedFieldIndex = 0;  // Currently viewing field
    private List<SelectedSlotDTO> selectedSlots = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_selection);
        
        // Get intent data
        facilityId = (BigInteger) getIntent().getSerializableExtra("facilityId");
        facilityName = getIntent().getStringExtra("facilityName");
        
        initViews();
        setupDatePicker();
        setupFieldTabs();
        
        // Initial load
        loadFieldAvailability(selectedDate);
    }
    
    private void loadFieldAvailability(LocalDate date) {
        showLoading(true);
        
        String userId = SharedPrefManager.getUserId().toString();
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);  // "2025-04-22"
        
        CourtApiService courtService = ApiClient.createService(
            ServiceHost.COURT_SERVICE, 
            CourtApiService.class
        );
        
        courtService.getFieldAvailability(userId, facilityId, dateStr)
            .enqueue(new Callback<BaseResponse<FieldBookingResponse>>() {
                @Override
                public void onResponse(Call call, Response<BaseResponse<FieldBookingResponse>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<FieldBookingResponse> baseResponse = response.body();
                        
                        if ("00".equals(baseResponse.getCode())) {
                            fieldBookingData = baseResponse.getData();
                            displayFieldsAndSlots();
                        } else {
                            showError(baseResponse.getMessage());
                        }
                    }
                }
                
                @Override
                public void onFailure(Call call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi kết nối: " + t.getMessage());
                }
            });
    }
    
    private void displayFieldsAndSlots() {
        // Update facility name
        tvFacilityName.setText(fieldBookingData.getFacilityName());
        
        // Update date label
        tvSelectedDate.setText(formatDate(selectedDate));  // "Thu 7 - 22/04/2025"
        
        // Display field tabs (Sân 1, Sân 2, Sân 3...)
        FieldTabAdapter fieldTabAdapter = new FieldTabAdapter(
            fieldBookingData.getFields(),
            selectedFieldIndex,
            position -> {
                selectedFieldIndex = position;
                displayTimeSlotsForField(position);
            }
        );
        rvFieldTabs.setAdapter(fieldTabAdapter);
        
        // Display time slots for first field
        displayTimeSlotsForField(0);
    }
    
    private void displayTimeSlotsForField(int fieldIndex) {
        FieldAvailabilityDTO field = fieldBookingData.getFields().get(fieldIndex);
        List<TimeSlotDTO> timeSlots = field.getTimeSlots();
        
        // Group slots by time period (Sáng, Chiều, Tối)
        Map<String, List<TimeSlotDTO>> groupedSlots = groupSlotsByPeriod(timeSlots);
        
        // Display in RecyclerView with section headers
        TimeSlotAdapter adapter = new TimeSlotAdapter(
            groupedSlots,
            selectedSlots,
            this::onSlotClicked
        );
        rvTimeSlots.setAdapter(adapter);
    }
    
    private Map<String, List<TimeSlotDTO>> groupSlotsByPeriod(List<TimeSlotDTO> slots) {
        Map<String, List<TimeSlotDTO>> grouped = new LinkedHashMap<>();
        
        grouped.put("Sáng", new ArrayList<>());
        grouped.put("Chiều", new ArrayList<>());
        grouped.put("Tối", new ArrayList<>());
        
        for (TimeSlotDTO slot : slots) {
            int hour = slot.getStartTime().getHour();
            
            if (hour >= 6 && hour < 12) {
                grouped.get("Sáng").add(slot);
            } else if (hour >= 12 && hour < 18) {
                grouped.get("Chiều").add(slot);
            } else {
                grouped.get("Tối").add(slot);
            }
        }
        
        return grouped;
    }
    
    private void onSlotClicked(TimeSlotDTO slot, boolean isSelected) {
        // Check if slot is available
        if (!slot.isAvailable()) {
            Toast.makeText(this, "Slot này đã được đặt", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isSelected) {
            // Add to selection
            SelectedSlotDTO selectedSlot = new SelectedSlotDTO();
            selectedSlot.setSlotId(BigInteger.valueOf(slot.getSlotId()));
            selectedSlot.setFieldId(BigInteger.valueOf(
                fieldBookingData.getFields().get(selectedFieldIndex).getFieldId()
            ));
            selectedSlot.setPrice(slot.getFixedPrice());
            
            selectedSlots.add(selectedSlot);
        } else {
            // Remove from selection
            selectedSlots.removeIf(s -> 
                s.getSlotId().longValue() == slot.getSlotId()
            );
        }
        
        updateSummary();
    }
    
    private void updateSummary() {
        // Update count
        tvSelectedCount.setText(selectedSlots.size() + " sân");
        
        // Calculate total price
        BigDecimal total = selectedSlots.stream()
            .map(SelectedSlotDTO::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        tvTotalPrice.setText(formatCurrency(total));
        
        // Enable/disable continue button
        btnContinue.setEnabled(!selectedSlots.isEmpty());
    }
    
    private void setupDatePicker() {
        btnPrevDate.setOnClickListener(v -> {
            selectedDate = selectedDate.minusDays(1);
            if (selectedDate.isBefore(LocalDate.now())) {
                selectedDate = LocalDate.now();
                Toast.makeText(this, "Không thể chọn ngày quá khứ", Toast.LENGTH_SHORT).show();
                return;
            }
            loadFieldAvailability(selectedDate);
        });
        
        btnNextDate.setOnClickListener(v -> {
            selectedDate = selectedDate.plusDays(1);
            loadFieldAvailability(selectedDate);
        });
    }
    
    private void onContinueClicked() {
        if (selectedSlots.isEmpty()) {
            showError("Vui lòng chọn ít nhất một slot");
            return;
        }
        
        // Navigate to booking confirmation
        Intent intent = new Intent(this, BookingConfirmActivity.class);
        intent.putExtra("selectedSlots", new Gson().toJson(selectedSlots));
        intent.putExtra("facilityId", facilityId);
        intent.putExtra("facilityName", facilityName);
        intent.putExtra("bookingDate", selectedDate.toString());
        
        BigDecimal totalAmount = selectedSlots.stream()
            .map(SelectedSlotDTO::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        intent.putExtra("totalAmount", totalAmount);
        intent.putExtra("totalHours", selectedSlots.size());
        
        startActivity(intent);
    }
}
```

---

## 🎨 UI Implementation Details

### Layout XML (`activity_field_selection.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Scrollable Content -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginBottom="80dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Header -->
            <TextView
                android:id="@+id/tvFacilityName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Chọn sân"
                android:textSize="20sp"
                android:textStyle="bold"/>

            <!-- Tabs (Dịch kèo sẵn / Đặt kèo tự tạo) -->
            <com.google.android.material.tabs.TabLayout
                android:id="@+id/tabLayout"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                app:tabMode="fixed"
                app:tabGravity="fill">
                
                <com.google.android.material.tabs.TabItem
                    android:text="Dịch kèo sẵn Booking"/>
                
                <com.google.android.material.tabs.TabItem
                    android:text="Đặt kèo tự tạo"/>
            </com.google.android.material.tabs.TabLayout>

            <!-- Date Picker -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Chọn ngày"
                android:textStyle="bold"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:orientation="horizontal"
                android:gravity="center_vertical">

                <ImageButton
                    android:id="@+id/btnPrevDate"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:src="@drawable/ic_chevron_left"
                    android:background="?attr/selectableItemBackgroundBorderless"/>

                <TextView
                    android:id="@+id/tvSelectedDate"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Thu 7 - 22/04/2025"
                    android:textAlignment="center"
                    android:textSize="16sp"
                    android:drawableStart="@drawable/ic_calendar"
                    android:drawablePadding="8dp"/>

                <ImageButton
                    android:id="@+id/btnNextDate"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:src="@drawable/ic_chevron_right"
                    android:background="?attr/selectableItemBackgroundBorderless"/>
            </LinearLayout>

            <!-- Field Tabs (Sân 1, 2, 3...) -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Chọn sân"
                android:textStyle="bold"/>

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rvFieldTabs"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:orientation="horizontal"
                app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="Xem tất cả"
                android:textColor="@color/primary"
                android:textStyle="bold"/>

            <!-- Legend -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Chọn khung giờ"
                android:textStyle="bold"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="☐ Đang chọn"
                    android:layout_marginEnd="16dp"/>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="☑ Chỗ trống"
                    android:textColor="#00BCD4"
                    android:layout_marginEnd="16dp"/>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="☐ Đã đặt"
                    android:textColor="#9E9E9E"/>
            </LinearLayout>

            <!-- Time Slots Grid -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rvTimeSlots"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:nestedScrollingEnabled="false"/>

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>

    <!-- Bottom Summary Bar -->
    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        app:cardElevation="8dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp"
            android:gravity="center_vertical">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Số sân"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"/>

                <TextView
                    android:id="@+id/tvSelectedCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="0"
                    android:textSize="16sp"
                    android:textStyle="bold"/>
            </LinearLayout>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Tổng tiền"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"/>

                <TextView
                    android:id="@+id/tvTotalPrice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="0 VND"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/primary"/>
            </LinearLayout>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnContinue"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Tiếp tục"
                android:enabled="false"/>

        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

---

## 📦 Adapter cho Time Slots

### TimeSlotAdapter.java:

```java
public class TimeSlotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SLOT = 1;
    
    private List<Object> items;  // Mix of String (headers) and TimeSlotDTO
    private List<SelectedSlotDTO> selectedSlots;
    private OnSlotClickListener listener;
    
    public interface OnSlotClickListener {
        void onSlotClick(TimeSlotDTO slot, boolean isSelected);
    }
    
    public TimeSlotAdapter(
        Map<String, List<TimeSlotDTO>> groupedSlots,
        List<SelectedSlotDTO> selectedSlots,
        OnSlotClickListener listener
    ) {
        this.selectedSlots = selectedSlots;
        this.listener = listener;
        
        // Flatten grouped slots with headers
        this.items = new ArrayList<>();
        for (Map.Entry<String, List<TimeSlotDTO>> entry : groupedSlots.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                items.add(entry.getKey());  // Header (Sáng, Chiều, Tối)
                items.addAll(entry.getValue());  // Slots
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String 
            ? VIEW_TYPE_HEADER 
            : VIEW_TYPE_SLOT;
    }
    
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
            return new SlotViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else {
            TimeSlotDTO slot = (TimeSlotDTO) items.get(position);
            boolean isSelected = selectedSlots.stream()
                .anyMatch(s -> s.getSlotId().longValue() == slot.getSlotId());
            ((SlotViewHolder) holder).bind(slot, isSelected, listener);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    // Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
        
        void bind(String header) {
            tvHeader.setText(header);  // "Sáng:", "Chiều:", "Tối:"
        }
    }
    
    // Slot ViewHolder
    static class SlotViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSlot;
        TextView tvTimeRange;
        TextView tvPrice;
        
        SlotViewHolder(View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
        
        void bind(TimeSlotDTO slot, boolean isSelected, OnSlotClickListener listener) {
            // Set time label
            tvTimeRange.setText(slot.getSlotLabel());  // "7:00-8:00"
            
            // Set price
            tvPrice.setText(formatPrice(slot.getFixedPrice()));  // "80k"
            
            // Set card background color based on status
            int bgColor;
            boolean isClickable;
            
            if (!slot.isAvailable()) {
                // Booked - Gray
                bgColor = Color.parseColor("#E0E0E0");
                isClickable = false;
            } else if (isSelected) {
                // Selected - Green highlight
                bgColor = Color.parseColor("#4CAF50");
                isClickable = true;
            } else {
                // Available - Cyan
                bgColor = Color.parseColor("#00BCD4");
                isClickable = true;
            }
            
            cardSlot.setCardBackgroundColor(bgColor);
            cardSlot.setClickable(isClickable);
            
            // Click handler
            if (isClickable) {
                cardSlot.setOnClickListener(v -> {
                    listener.onSlotClick(slot, !isSelected);
                });
            }
        }
        
        private String formatPrice(BigDecimal price) {
            if (price == null) return "-";
            
            long value = price.longValue();
            if (value >= 1000) {
                return (value / 1000) + "k";
            }
            return value + "";
        }
    }
}
```

### item_time_slot.xml:
```xml
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardSlot"
    android:layout_width="0dp"
    android:layout_height="60dp"
    android:layout_weight="1"
    android:layout_margin="4dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="8dp">

        <TextView
            android:id="@+id/tvTimeRange"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="7:00-8:00"
            android:textSize="12sp"
            android:textColor="@android:color/white"
            android:textStyle="bold"/>

        <TextView
            android:id="@+id/tvPrice"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="80k"
            android:textSize="10sp"
            android:textColor="@android:color/white"
            android:layout_marginTop="4dp"/>
    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

---

## 📊 Database Schema Understanding

### Tables Involved:

**1. `cat_facilities`** - Danh sách cơ sở
```sql
facility_id BIGINT PK
facility_name VARCHAR
address TEXT
status INT (1=Active, 0=Inactive)
```

**2. `cat_fields`** - Danh sách sân trong cơ sở
```sql
field_id BIGINT PK
facility_id BIGINT FK
field_name VARCHAR  ("Sân 1", "Sân 2")
description TEXT
status INT
```

**3. `cat_field_time_slots`** - Khung giờ của sân
```sql
slot_id BIGINT PK
field_id BIGINT FK
slot_date TIMESTAMP  (2025-04-22 07:00:00)
start_time TIME  (07:00:00)
end_time TIME  (08:00:00)
slot_label VARCHAR  ("7:00-8:00")
status INT
```

**4. `cat_field_prices`** - Bảng giá theo khung giờ
```sql
price_id BIGINT PK
field_id BIGINT FK
weekday VARCHAR  ("MONDAY", "TUESDAY", NULL=all days)
start_time TIME  (06:00:00)
end_time TIME  (12:00:00)
fixed_price DECIMAL  (80000.00)
walkin_price DECIMAL  (100000.00)
status INT
```

**5. `txn_bookings`** - Đơn đặt sân
```sql
booking_id BIGINT PK
booking_code VARCHAR  ("BOOK-xxx")
user_id BIGINT
booking_date TIMESTAMP
status VARCHAR  ("INIT", "SUCCESS", "FAILED")
total_price DECIMAL
```

**6. `txn_booking_slots`** - Slots đã được đặt
```sql
booking_slot_id BIGINT PK
booking_id BIGINT FK
slot_id BIGINT FK
field_id BIGINT FK
price DECIMAL
```

---

## 🔑 Key Business Logic

### 1. Slot Availability Check:
```sql
-- Slot là BOOKED nếu:
SELECT DISTINCT slot_id 
FROM txn_booking_slots
JOIN txn_bookings ON txn_booking_slots.booking_id = txn_bookings.booking_id
WHERE slot_id IN (...)
AND booking_date = '2025-04-22'
AND status IN ('INIT', 'SUCCESS');  -- Cả pending và confirmed
```

### 2. Pricing Logic:
- Mỗi sân có nhiều pricing rules
- Rule có thể specify weekday hoặc apply cho tất cả ngày
- Rule define time range (06:00-12:00) và price
- Slot's price = find matching rule based on slot's start_time

### 3. Slot Status:
- **AVAILABLE** (Cyan/Blue): `isAvailable = true`, chưa có trong `txn_booking_slots`
- **BOOKED** (Gray): `isAvailable = false`, đã có booking với status INIT/SUCCESS
- **SELECTED** (Green): User đã click chọn trong UI

---

## ✅ Summary - Luồng Hoàn Chỉnh

```
1. User vào màn FieldSelectionActivity
   ↓
2. Activity call API: GET /fields/availability?facilityId=X&date=Y
   ↓
3. Backend query:
   - Get facility info
   - Get all fields of facility
   - Get all time slots for each field on that date
   - Check which slots are already booked
   - Calculate price for each slot
   ↓
4. Return FieldBookingResponse với fields[] và timeSlots[]
   ↓
5. Android display:
   - Field tabs (Sân 1, 2, 3...)
   - Time slots grid grouped by period (Sáng, Chiều, Tối)
   - Each slot shows: time, price, status (Available/Booked)
   ↓
6. User select slots → Add to selectedSlots[]
   ↓
7. Update summary: count, total price
   ↓
8. Click "Tiếp tục" → Navigate to BookingConfirmActivity
```

---

## 🎯 Next Implementation Steps

1. ✅ Create DTOs (FieldBookingResponse, TimeSlotDTO, etc.)
2. ✅ Create CourtApiService.getFieldAvailability()
3. ✅ Design layout XML
4. ✅ Create TimeSlotAdapter
5. ✅ Implement FieldSelectionActivity logic
6. Test with real backend API

Bạn muốn tôi tạo code cho phần nào trước? 🚀
