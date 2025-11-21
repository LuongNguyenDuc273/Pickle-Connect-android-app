package com.datn06.pickleconnect.Court;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.FacilityDTO;
import com.datn06.pickleconnect.R;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private final List<FacilityDTO> courtList;
    private final OnCourtClickListener listener;

    public interface OnCourtClickListener {
        void onCourtClick(FacilityDTO facility);
        void onBookNowClick(FacilityDTO facility);
    }

    public CourtAdapter(List<FacilityDTO> courtList, OnCourtClickListener listener) {
        this.courtList = courtList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        FacilityDTO facility = courtList.get(position);
        holder.bind(facility, listener);
    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCourtImage;
        private final TextView tvCourtName, tvAddress, tvPrice, tvServices, tvDistance;
        private final RatingBar ratingBar;
        private final MaterialButton btnBookNow;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourtImage = itemView.findViewById(R.id.ivCourtImage);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvServices = itemView.findViewById(R.id.tvServices);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnBookNow = itemView.findViewById(R.id.btnBookNow);
        }

        public void bind(FacilityDTO facility, OnCourtClickListener listener) {
            tvCourtName.setText(facility.getFacilityName());
            tvAddress.setText(facility.getFullAddress());
            
            // Format price - sử dụng priceRangeMin và priceRangeMax
            if (facility.getPriceRangeMin() != null && facility.getPriceRangeMax() != null) {
                tvPrice.setText(facility.getFormattedPriceRange());
            } else {
                tvPrice.setText("Liên hệ");
            }
            
            // Services
            if (facility.getServices() != null && !facility.getServices().isEmpty()) {
                tvServices.setText(String.join(", ", facility.getServices()));
                tvServices.setVisibility(View.VISIBLE);
            } else {
                tvServices.setVisibility(View.GONE);
            }
            
            // Distance - sử dụng helper method
            if (facility.getDistanceKm() != null) {
                tvDistance.setText(facility.getFormattedDistance());
                tvDistance.setVisibility(View.VISIBLE);
            } else {
                tvDistance.setVisibility(View.GONE);
            }
            
            // Rating
            if (facility.getRating() != null) {
                ratingBar.setRating(facility.getRating().floatValue());
                ratingBar.setVisibility(View.VISIBLE);
            } else {
                ratingBar.setVisibility(View.GONE);
            }
            
            // Load image with Glide
            if (facility.getFirstImageUrl() != null) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(facility.getFirstImageUrl())
                    .placeholder(R.drawable.court_placeholder)
                    .error(R.drawable.court_placeholder)
                    .centerCrop()
                    .into(ivCourtImage);
            } else {
                ivCourtImage.setImageResource(R.drawable.court_placeholder);
            }
            
            itemView.setOnClickListener(v -> listener.onCourtClick(facility));
            btnBookNow.setOnClickListener(v -> listener.onBookNowClick(facility));
        }
    }
}
