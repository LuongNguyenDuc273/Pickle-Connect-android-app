package com.datn06.pickleconnect.Booking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.SelectedSlotDTO;
import com.datn06.pickleconnect.Model.TimeSlotDTO;
import com.datn06.pickleconnect.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying time slots with section headers (Sáng, Chiều, Tối)
 * Supports selection/deselection of available slots
 */
public class TimeSlotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SLOT_ROW = 1;
    
    private final List<Object> items;  // Mix of String (headers) and List<TimeSlotDTO> (slot rows)
    private final List<SelectedSlotDTO> selectedSlots;
    private final OnSlotClickListener listener;
    private static final int SLOTS_PER_ROW = 4;
    
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
        this.items = new ArrayList<>();
        
        // Flatten grouped slots with headers
        // Each section has: Header (String) + Rows of slots (List<TimeSlotDTO>)
        for (Map.Entry<String, List<TimeSlotDTO>> entry : groupedSlots.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                // Add header
                items.add(entry.getKey() + ":");  // "Sáng:", "Chiều:", "Tối:"
                
                // Add slots in rows of 4
                List<TimeSlotDTO> slots = entry.getValue();
                for (int i = 0; i < slots.size(); i += SLOTS_PER_ROW) {
                    int end = Math.min(i + SLOTS_PER_ROW, slots.size());
                    items.add(slots.subList(i, end));
                }
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String 
            ? VIEW_TYPE_HEADER 
            : VIEW_TYPE_SLOT_ROW;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            // Create a horizontal LinearLayout for the row
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot_row, parent, false);
            return new SlotRowViewHolder(view, listener, selectedSlots);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof SlotRowViewHolder) {
            @SuppressWarnings("unchecked")
            List<TimeSlotDTO> slotRow = (List<TimeSlotDTO>) items.get(position);
            ((SlotRowViewHolder) holder).bind(slotRow);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Notify adapter that selection changed
     */
    public void notifySelectionChanged() {
        notifyDataSetChanged();
    }
    
    // ========== ViewHolders ==========
    
    /**
     * ViewHolder for section headers (Sáng:, Chiều:, Tối:)
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHeader;
        
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
        
        void bind(String header) {
            tvHeader.setText(header);
        }
    }
    
    /**
     * ViewHolder for a row of time slots (up to 4 slots)
     */
    static class SlotRowViewHolder extends RecyclerView.ViewHolder {
        private final ViewGroup container;
        private final OnSlotClickListener listener;
        private final List<SelectedSlotDTO> selectedSlots;
        
        SlotRowViewHolder(View itemView, OnSlotClickListener listener, 
                         List<SelectedSlotDTO> selectedSlots) {
            super(itemView);
            this.container = (ViewGroup) itemView;
            this.listener = listener;
            this.selectedSlots = selectedSlots;
        }
        
        void bind(List<TimeSlotDTO> slots) {
            container.removeAllViews();
            
            for (TimeSlotDTO slot : slots) {
                View slotView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_time_slot, container, false);
                
                bindSlot(slotView, slot);
                container.addView(slotView);
            }
            
            // Fill remaining space if less than 4 slots
            while (container.getChildCount() < 4) {
                View emptyView = new View(container.getContext());
                android.widget.LinearLayout.LayoutParams params = 
                    new android.widget.LinearLayout.LayoutParams(0, 1);
                params.weight = 1;
                emptyView.setLayoutParams(params);
                container.addView(emptyView);
            }
        }
        
        private void bindSlot(View slotView, TimeSlotDTO slot) {
            MaterialCardView cardSlot = slotView.findViewById(R.id.cardSlot);
            TextView tvTimeRange = slotView.findViewById(R.id.tvTimeRange);
            TextView tvPrice = slotView.findViewById(R.id.tvPrice);
            
            // Set time label
            tvTimeRange.setText(slot.getSlotLabel());
            
            // Set price
            tvPrice.setText(slot.getFormattedPrice());
            
            // Check if selected
            boolean isSelected = selectedSlots.stream()
                .anyMatch(s -> s.getSlotId().equals(slot.getSlotId()));
            
            // Set card background color based on status
            int bgColor;
            boolean isClickable;
            
            if (!slot.getIsAvailable()) {
                // Booked - Gray
                bgColor = Color.parseColor("#E0E0E0");
                isClickable = false;
                tvTimeRange.setTextColor(Color.parseColor("#757575"));
                tvPrice.setTextColor(Color.parseColor("#757575"));
            } else if (isSelected) {
                // Selected - Green
                bgColor = Color.parseColor("#4CAF50");
                isClickable = true;
                tvTimeRange.setTextColor(Color.WHITE);
                tvPrice.setTextColor(Color.WHITE);
            } else {
                // Available - Cyan
                bgColor = Color.parseColor("#00BCD4");
                isClickable = true;
                tvTimeRange.setTextColor(Color.WHITE);
                tvPrice.setTextColor(Color.WHITE);
            }
            
            cardSlot.setCardBackgroundColor(bgColor);
            cardSlot.setClickable(isClickable);
            
            // Click handler
            if (isClickable && listener != null) {
                cardSlot.setOnClickListener(v -> {
                    listener.onSlotClick(slot, !isSelected);
                });
            }
        }
    }
}
