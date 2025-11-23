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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Model.FacilityDTO;
import java.util.ArrayList;
import java.util.List;

public class FacilityGroupAdapter extends RecyclerView.Adapter<FacilityGroupAdapter.GroupViewHolder> {

    private Context context;
    // Data là List của các nhóm, mỗi nhóm chứa tối đa 3 FacilityDTO
    private List<List<FacilityDTO>> facilityGroupList;
    private FacilityAdapter.OnFacilityClickListener listener;

    public FacilityGroupAdapter(Context context, FacilityAdapter.OnFacilityClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.facilityGroupList = new ArrayList<>();
    }

    public void setFacilityGroupList(List<List<FacilityDTO>> facilityGroupList) {
        this.facilityGroupList = facilityGroupList != null ? facilityGroupList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout mới chứa 3 item sân
        View view = LayoutInflater.from(context).inflate(R.layout.item_facility_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        List<FacilityDTO> group = facilityGroupList.get(position);

        // Bind dữ liệu cho 3 item sân trong nhóm
        bindFacilityItem(holder.facilityHolder1, group.size() > 0 ? group.get(0) : null);
        bindFacilityItem(holder.facilityHolder2, group.size() > 1 ? group.get(1) : null);
        bindFacilityItem(holder.facilityHolder3, group.size() > 2 ? group.get(2) : null);
    }

    private void bindFacilityItem(GroupViewHolder.FacilityHolder holder, FacilityDTO facility) {
        if (facility == null) {
            holder.itemView.setVisibility(View.GONE);
            return;
        }

        holder.itemView.setVisibility(View.VISIBLE);

        // Load image
        String imageUrl = facility.getFirstImageUrl();
        Glide.with(context)
                .load(imageUrl)
                .apply(new RequestOptions()
                        .transform(new RoundedCorners(16))
                        .placeholder(R.drawable.banner_placeholder)
                        .error(R.drawable.banner_error))
                .into(holder.ivVenue);

        // Set info
        holder.tvVenueName.setText(facility.getFacilityName());
        holder.tvVenueAddress.setText(facility.getFullAddress());
        holder.tvVenuePhone.setText("SĐT: " + facility.getContactInfo());

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFacilityClick(facility);
            }
        });

        holder.btnBookVenue.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(facility);
            }
        });
    }

    @Override
    public int getItemCount() {
        return facilityGroupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        // ViewHolder cho mỗi item sân (được include)
        static class FacilityHolder {
            View itemView;
            ImageView ivVenue;
            TextView tvVenueName;
            TextView tvVenueAddress;
            TextView tvVenuePhone;
            Button btnBookVenue;

            public FacilityHolder(View itemView) {
                this.itemView = itemView;
                ivVenue = itemView.findViewById(R.id.ivVenue);
                tvVenueName = itemView.findViewById(R.id.tvVenueName);
                tvVenueAddress = itemView.findViewById(R.id.tvVenueAddress);
                tvVenuePhone = itemView.findViewById(R.id.tvVenuePhone);
                btnBookVenue = itemView.findViewById(R.id.btnBookVenue);
            }
        }

        // Các holder cho 3 sân trong nhóm
        FacilityHolder facilityHolder1;
        FacilityHolder facilityHolder2;
        FacilityHolder facilityHolder3;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ các item include
            View facility1View = itemView.findViewById(R.id.facility_1);
            View facility2View = itemView.findViewById(R.id.facility_2);
            View facility3View = itemView.findViewById(R.id.facility_3);

            facilityHolder1 = new FacilityHolder(facility1View);
            facilityHolder2 = new FacilityHolder(facility2View);
            facilityHolder3 = new FacilityHolder(facility3View);
        }
    }
}