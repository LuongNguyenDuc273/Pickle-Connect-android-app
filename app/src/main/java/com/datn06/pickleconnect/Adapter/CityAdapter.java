package com.datn06.pickleconnect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.CityDTO;
import com.datn06.pickleconnect.R;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Tỉnh/Thành phố
 * Cho phép chọn 1 item bằng RadioButton
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private List<CityDTO> cityList;
    private CityDTO selectedCity;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(CityDTO city);
    }

    public CityAdapter(List<CityDTO> cityList, OnCityClickListener listener) {
        this.cityList = cityList;
        this.listener = listener;
    }

    public void setSelectedCity(CityDTO city) {
        this.selectedCity = city;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityDTO city = cityList.get(position);
        holder.tvCityName.setText(city.getName());
        holder.radioButton.setChecked(city.equals(selectedCity));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        RadioButton radioButton;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
