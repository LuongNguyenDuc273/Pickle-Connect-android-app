package com.datn06.pickleconnect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.FacilityServiceDTO;
import com.datn06.pickleconnect.R;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<FacilityServiceDTO> services;

    public ServiceAdapter(List<FacilityServiceDTO> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        FacilityServiceDTO service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public void updateData(List<FacilityServiceDTO> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceName;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
        }

        public void bind(FacilityServiceDTO service) {
            if (service == null) return;

            String displayText = service.getServiceName();

            // Add price if available and not free
            if (service.getPrice() != null && service.getPrice().intValue() > 0) {
                displayText += " (" + service.getFormattedPrice() + ")";
            }

            tvServiceName.setText(displayText);
        }
    }
}
