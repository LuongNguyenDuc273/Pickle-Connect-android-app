package com.datn06.pickleconnect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Model.FacilityReviewDTO;
import com.datn06.pickleconnect.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<FacilityReviewDTO> reviews;

    public ReviewAdapter(List<FacilityReviewDTO> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        FacilityReviewDTO review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public void updateData(List<FacilityReviewDTO> newReviews) {
        this.reviews = newReviews;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private TextView tvReviewerName;
        private RatingBar ratingBar;
        private TextView tvReviewComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
        }

        public void bind(FacilityReviewDTO review) {
            if (review == null) return;

            // Set reviewer name (with fallback to "Ẩn danh")
            tvReviewerName.setText(review.getDisplayName());

            // Set rating
            ratingBar.setRating(review.getRatingStars());

            // Set comment
            String comment = review.getComment();
            if (comment != null && !comment.isEmpty()) {
                tvReviewComment.setText(comment);
                tvReviewComment.setVisibility(View.VISIBLE);
            } else {
                tvReviewComment.setVisibility(View.GONE);
            }

            // TODO: Load avatar image using Glide or Picasso
            // For now, it will use the default drawable
            imgAvatar.setImageResource(R.drawable.avartar_placeholder);
        }
    }
}
