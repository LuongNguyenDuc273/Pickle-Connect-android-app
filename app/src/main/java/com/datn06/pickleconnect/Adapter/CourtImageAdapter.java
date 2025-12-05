package com.datn06.pickleconnect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.datn06.pickleconnect.API.AppConfig;
import com.datn06.pickleconnect.R;

import java.util.List;

public class CourtImageAdapter extends RecyclerView.Adapter<CourtImageAdapter.ImageViewHolder> {

    private List<String> imageUrls;

    public CourtImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        holder.bind(imageUrl);
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public void updateData(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(String imageUrl) {
            if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("placeholder")) {
                // Load placeholder image
                imageView.setImageResource(R.drawable.banner_placeholder);
            } else {
                // ✅ Fix localhost URLs for emulator
                String fixedUrl = AppConfig.fixImageUrl(imageUrl);
                
                // Load image from URL using Glide
                Glide.with(itemView.getContext())
                        .load(fixedUrl)
                        .placeholder(R.drawable.banner_placeholder)
                        .error(R.drawable.banner_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageView);
            }
        }
    }
}
