package com.datn06.pickleconnect.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.Model.BookingHistoryDTO;
import com.datn06.pickleconnect.Model.FieldTimeSlotDTO;
import com.datn06.pickleconnect.R;
import com.google.android.flexbox.FlexboxLayout;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder> {

    private Context context;
    private List<BookingHistoryDTO> bookingList = new ArrayList<>();
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onDetailClick(BookingHistoryDTO booking);
        void onRebookClick(BookingHistoryDTO booking);
    }

    public BookingHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setBookingList(List<BookingHistoryDTO> bookingList) {
        this.bookingList = bookingList != null ? bookingList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_history, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingHistoryDTO booking = bookingList.get(position);

        // Facility name
        holder.tvFacilityName.setText(booking.getFacilityName() != null ? booking.getFacilityName() : "Pickle Connect Logo");
        
        // Booking code
        holder.tvBookingCode.setText("(Mã phiếu: " + (booking.getBookingCode() != null ? booking.getBookingCode() : "N/A") + ")");

        // Address
        String address = booking.getFacilityName() != null ? booking.getFacilityName() : "";
        address += "\n" + booking.getFullAddress();
        holder.tvAddress.setText(address);

        // User name
        holder.tvUserName.setText(booking.getUserName() != null ? booking.getUserName() : "N/A");

        // Booking date - format from "2025-04-22T10:30:00" to "22/04/2025"
        if (booking.getBookingDate() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(booking.getBookingDate());
                holder.tvBookingDate.setText(outputFormat.format(date));
            } catch (ParseException e) {
                holder.tvBookingDate.setText(booking.getBookingDate());
            }
        } else {
            holder.tvBookingDate.setText("N/A");
        }

        // Ticket type - extract from booking code (e.g., "EVENT-xxx" -> "EVENT")
        String ticketType = "N/A";
        if (booking.getBookingCode() != null && booking.getBookingCode().contains("-")) {
            ticketType = booking.getBookingCode().split("-")[0];
        }
        holder.tvTicketType.setText(ticketType);

        // Time slots - group by field name
        holder.flexSlotsSan1.removeAllViews();
        if (booking.getSlots() != null && !booking.getSlots().isEmpty()) {
            // Group slots by fieldName
            Map<String, List<FieldTimeSlotDTO>> slotsByField = new LinkedHashMap<>();
            for (FieldTimeSlotDTO slot : booking.getSlots()) {
                String fieldName = slot.getFieldName() != null ? slot.getFieldName() : "Sân N/A";
                if (!slotsByField.containsKey(fieldName)) {
                    slotsByField.put(fieldName, new ArrayList<>());
                }
                slotsByField.get(fieldName).add(slot);
            }
            
            // Display each field group
            for (Map.Entry<String, List<FieldTimeSlotDTO>> entry : slotsByField.entrySet()) {
                String fieldName = entry.getKey();
                List<FieldTimeSlotDTO> slots = entry.getValue();
                
                // Add field name label
                TextView fieldLabel = new TextView(context);
                fieldLabel.setText(fieldName + ":");
                fieldLabel.setTextSize(13);
                fieldLabel.setTextColor(context.getResources().getColor(R.color.text_dark, null));
                fieldLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                fieldLabel.setPadding(0, 16, 0, 8);
                
                FlexboxLayout.LayoutParams labelParams = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.MATCH_PARENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                );
                fieldLabel.setLayoutParams(labelParams);
                holder.flexSlotsSan1.addView(fieldLabel);
                
                // Add time slots for this field
                for (FieldTimeSlotDTO slot : slots) {
                    TextView slotView = new TextView(context);
                    slotView.setText(slot.getTimeSlotDisplay());
                    slotView.setTextSize(12);
                    slotView.setTextColor(context.getResources().getColor(R.color.white, null));
                    slotView.setBackground(context.getDrawable(R.drawable.bg_time_slot_selected));
                    slotView.setPadding(16, 12, 16, 12);
                    
                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 8, 8, 8);
                    slotView.setLayoutParams(params);
                    
                    holder.flexSlotsSan1.addView(slotView);
                }
            }
        }

        // Total hours
        if (booking.getTotalHours() != null) {
            holder.tvTotalHours.setText(booking.getTotalHours().intValue() + "h");
        } else {
            holder.tvTotalHours.setText("");
        }

        // Total price
        if (booking.getTotalPrice() != null) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvTotalPrice.setText(formatter.format(booking.getTotalPrice()) + " VND");
        } else {
            holder.tvTotalPrice.setText("0 VND");
        }

        // Note - for now, leave empty or add later
        holder.tvNote.setText("");

        // Button listeners
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(booking);
            }
        });

        holder.btnRebook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRebookClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvFacilityName, tvBookingCode, tvAddress;
        TextView tvUserName, tvBookingDate, tvTicketType;
        FlexboxLayout flexSlotsSan1;
        TextView tvTotalHours, tvTotalPrice, tvNote;
        Button btnDetail, btnRebook;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvFacilityName = itemView.findViewById(R.id.tvFacilityName);
            tvBookingCode = itemView.findViewById(R.id.tvBookingCode);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvTicketType = itemView.findViewById(R.id.tvTicketType);
            flexSlotsSan1 = itemView.findViewById(R.id.flexSlotsSan1);
            tvTotalHours = itemView.findViewById(R.id.tvTotalHours);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvNote = itemView.findViewById(R.id.tvNote);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnRebook = itemView.findViewById(R.id.btnRebook);
        }
    }
}
