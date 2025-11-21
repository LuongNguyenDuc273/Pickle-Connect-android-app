package com.datn06.pickleconnect.Booking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.FieldAvailabilityDTO;
import com.datn06.pickleconnect.R;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Adapter for horizontal list of field tabs (Sân 1, Sân 2, Sân 3...)
 */
public class FieldTabAdapter extends RecyclerView.Adapter<FieldTabAdapter.FieldTabViewHolder> {
    
    private final List<FieldAvailabilityDTO> fields;
    private int selectedPosition;
    private final OnFieldSelectedListener listener;
    
    public interface OnFieldSelectedListener {
        void onFieldSelected(int position);
    }
    
    public FieldTabAdapter(List<FieldAvailabilityDTO> fields, 
                          int selectedPosition,
                          OnFieldSelectedListener listener) {
        this.fields = fields;
        this.selectedPosition = selectedPosition;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FieldTabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_field_tab, parent, false);
        return new FieldTabViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FieldTabViewHolder holder, int position) {
        FieldAvailabilityDTO field = fields.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(field, isSelected, position, listener);
    }
    
    @Override
    public int getItemCount() {
        return fields != null ? fields.size() : 0;
    }
    
    /**
     * Update selected position
     */
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        
        // Notify both old and new positions
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }
    
    /**
     * ViewHolder for field tab chip
     */
    static class FieldTabViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipField;
        
        FieldTabViewHolder(View itemView) {
            super(itemView);
            chipField = itemView.findViewById(R.id.chipField);
        }
        
        void bind(FieldAvailabilityDTO field, boolean isSelected, 
                 int position, OnFieldSelectedListener listener) {
            
            // Set field name
            chipField.setText(field.getFieldName());
            
            // Set checked state
            chipField.setChecked(isSelected);
            
            // Set colors based on selection
            if (isSelected) {
                chipField.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#00BCD4"))
                );
                chipField.setTextColor(Color.WHITE);
            } else {
                chipField.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(Color.WHITE)
                );
                chipField.setTextColor(Color.BLACK);
            }
            
            // Click listener
            chipField.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFieldSelected(position);
                }
            });
        }
    }
}
