package com.datn06.pickleconnect.Booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.SelectedSlotDTO;
import com.datn06.pickleconnect.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter to display selected time slots grouped by field
 */
public class SelectedSlotAdapter extends RecyclerView.Adapter<SelectedSlotAdapter.ViewHolder> {

    private final List<FieldSlotGroup> fieldGroups;

    /**
     * Group slots by field
     */
    public static class FieldSlotGroup {
        private final Long fieldId;
        private final String fieldName;
        private final List<SelectedSlotDTO> slots;

        public FieldSlotGroup(Long fieldId, String fieldName, List<SelectedSlotDTO> slots) {
            this.fieldId = fieldId;
            this.fieldName = fieldName;
            this.slots = slots;
        }

        public Long getFieldId() {
            return fieldId;
        }

        public String getFieldName() {
            return fieldName;
        }

        public List<SelectedSlotDTO> getSlots() {
            return slots;
        }
    }

    public SelectedSlotAdapter(List<SelectedSlotDTO> selectedSlots) {
        this.fieldGroups = groupSlotsByField(selectedSlots);
    }

    /**
     * Group slots by fieldId
     */
    private List<FieldSlotGroup> groupSlotsByField(List<SelectedSlotDTO> selectedSlots) {
        Map<Long, List<SelectedSlotDTO>> grouped = new LinkedHashMap<>();

        for (SelectedSlotDTO slot : selectedSlots) {
            grouped.computeIfAbsent(slot.getFieldId(), k -> new ArrayList<>()).add(slot);
        }

        List<FieldSlotGroup> result = new ArrayList<>();
        for (Map.Entry<Long, List<SelectedSlotDTO>> entry : grouped.entrySet()) {
            // Get field name from first slot
            String fieldName = "Sân " + entry.getKey();
            if (!entry.getValue().isEmpty() && entry.getValue().get(0).getFieldName() != null) {
                fieldName = entry.getValue().get(0).getFieldName();
            }

            result.add(new FieldSlotGroup(entry.getKey(), fieldName, entry.getValue()));
        }

        return result;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_slot_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FieldSlotGroup group = fieldGroups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return fieldGroups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFieldName;
        private final ChipGroup chipGroupTimeSlots;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            chipGroupTimeSlots = itemView.findViewById(R.id.chipGroupTimeSlots);
        }

        public void bind(FieldSlotGroup group) {
            // Set field name
            tvFieldName.setText(group.getFieldName() + ":");

            // Clear previous chips
            chipGroupTimeSlots.removeAllViews();

            // Add chip for each time slot
            for (SelectedSlotDTO slot : group.getSlots()) {
                Chip chip = new Chip(itemView.getContext());

                // ✅ Format time: remove seconds (HH:mm only)
                String startTime = formatTimeWithoutSeconds(slot.getStartTime());
                String endTime = formatTimeWithoutSeconds(slot.getEndTime());
                chip.setText(startTime + " - " + endTime);

                chip.setChipBackgroundColorResource(R.color.chip_selected_background);
                chip.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
                chip.setChipStrokeColorResource(R.color.chip_selected_stroke);
                chip.setChipStrokeWidth(2f);
                chip.setClickable(false);
                chip.setCheckable(false);

                chipGroupTimeSlots.addView(chip);
            }
        }

        /**
         * ✅ Remove seconds from time string
         * Input: "19:00:00" -> Output: "19:00"
         * Input: "19:00" -> Output: "19:00"
         */
        private String formatTimeWithoutSeconds(String time) {
            if (time == null || time.isEmpty()) {
                return "";
            }

            // If format is HH:mm:ss, remove :ss
            if (time.length() == 8 && time.charAt(2) == ':' && time.charAt(5) == ':') {
                return time.substring(0, 5); // Return HH:mm
            }

            // Already in HH:mm format
            return time;
        }
    }
}