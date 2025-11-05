package com.datn06.pickleconnect.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Model.BannerDTO;
import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<BannerDTO> bannerList;
    private OnBannerClickListener listener;

    // Interface for click listener
    public interface OnBannerClickListener {
        void onBannerClick(BannerDTO banner);
    }

    public BannerAdapter(Context context) {
        this.context = context;
        this.bannerList = new ArrayList<>();
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.listener = listener;
    }

    public void setBannerList(List<BannerDTO> bannerList) {
        this.bannerList = bannerList != null ? bannerList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerDTO banner = bannerList.get(position);

        // Load image using Glide
        Glide.with(context)
                .load(banner.getImageUrl())
                .apply(new RequestOptions()
                        .transform(new RoundedCorners(32))
                        .placeholder(R.drawable.banner_placeholder)
                        .error(R.drawable.banner_error))
                .into(holder.ivBanner);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(banner);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
        }
    }
}
