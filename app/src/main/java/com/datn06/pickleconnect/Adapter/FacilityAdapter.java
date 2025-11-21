package com.datn06.pickleconnect.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.datn06.pickleconnect.Booking.FieldSelectionActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Model.FacilityDTO;
import java.util.ArrayList;
import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder> {

    private Context context;
    private List<FacilityDTO> facilityList;
    private OnFacilityClickListener listener;

    // Interface for click listeners
    public interface OnFacilityClickListener {
        void onFacilityClick(FacilityDTO facility);
        void onBookClick(FacilityDTO facility);
    }

    public FacilityAdapter(Context context) {
        this.context = context;
        this.facilityList = new ArrayList<>();
    }

    public void setOnFacilityClickListener(OnFacilityClickListener listener) {
        this.listener = listener;
    }

    public void setFacilityList(List<FacilityDTO> facilityList) {
        this.facilityList = facilityList != null ? facilityList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sports_venue, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        FacilityDTO facility = facilityList.get(position);

        // Load venue image
        String imageUrl = facility.getFirstImageUrl();
        Glide.with(context)
                .load(imageUrl)
                .apply(new RequestOptions()
                        .transform(new RoundedCorners(16))
                        .placeholder(R.drawable.banner_placeholder)
                        .error(R.drawable.banner_error))
                .into(holder.ivVenue);

        // Set venue info
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
            // Navigate to FieldSelectionActivity with facilityId
            Intent intent = new Intent(context, FieldSelectionActivity.class);
            intent.putExtra("facilityId", facility.getFacilityId());
            intent.putExtra("facilityName", facility.getFacilityName());
            // bookDate sẽ được lấy là ngày hiện tại trong FieldSelectionActivity
            context.startActivity(intent);
            
            // Gọi callback nếu có (để HomeActivity biết)
            if (listener != null) {
                listener.onBookClick(facility);
            }
        });
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    static class FacilityViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVenue;
        TextView tvVenueName;
        TextView tvVenueAddress;
        TextView tvVenuePhone;
        Button btnBookVenue;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVenue = itemView.findViewById(R.id.ivVenue);
            tvVenueName = itemView.findViewById(R.id.tvVenueName);
            tvVenueAddress = itemView.findViewById(R.id.tvVenueAddress);
            tvVenuePhone = itemView.findViewById(R.id.tvVenuePhone);
            btnBookVenue = itemView.findViewById(R.id.btnBookVenue);
        }
    }
}
