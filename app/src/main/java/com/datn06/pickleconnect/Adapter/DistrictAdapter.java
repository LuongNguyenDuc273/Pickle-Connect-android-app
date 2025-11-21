package com.datn06.pickleconnect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.DistrictDTO;
import com.datn06.pickleconnect.R;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Quận/Huyện
 * Cho phép chọn 1 item bằng RadioButton
 */
public class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.DistrictViewHolder> {

    private List<DistrictDTO> districtList;
    private DistrictDTO selectedDistrict;
    private OnDistrictClickListener listener;

    public interface OnDistrictClickListener {
        void onDistrictClick(DistrictDTO district);
    }

    public DistrictAdapter(List<DistrictDTO> districtList, OnDistrictClickListener listener) {
        this.districtList = districtList;
        this.listener = listener;
    }

    public void setSelectedDistrict(DistrictDTO district) {
        this.selectedDistrict = district;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DistrictViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_district, parent, false);
        return new DistrictViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DistrictViewHolder holder, int position) {
        DistrictDTO district = districtList.get(position);
        holder.tvDistrictName.setText(district.getDistrictName());
        holder.radioButton.setChecked(district.equals(selectedDistrict));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDistrictClick(district);
            }
        });
    }

    @Override
    public int getItemCount() {
        return districtList.size();
    }

    static class DistrictViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistrictName;
        RadioButton radioButton;

        public DistrictViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDistrictName = itemView.findViewById(R.id.tvDistrictName);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
