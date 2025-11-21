package com.datn06.pickleconnect.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import com.datn06.pickleconnect.Event.EventDetailActivity;
import com.datn06.pickleconnect.Model.EventListDTO;
import com.datn06.pickleconnect.R;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventListDTO> eventList;
    private Context context;

    public EventAdapter(Context context) {
        this.context = context;
        this.eventList = new ArrayList<>();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventListDTO event = eventList.get(position);
        holder.bind(event, context);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setEventList(List<EventListDTO> events) {
        this.eventList = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addEvents(List<EventListDTO> events) {
        if (events != null && !events.isEmpty()) {
            int startPosition = this.eventList.size();
            this.eventList.addAll(events);
            notifyItemRangeInserted(startPosition, events.size());
        }
    }

    public void clearEvents() {
        this.eventList.clear();
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private View colorIndicator;
        private TextView tvEventTitle;
        private TextView tvEventTime;
        private TextView tvEventLevel;
        private TextView tvBookingStatus;
        private TextView tvPrice;
        private ImageView ivArrowRight;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventLevel = itemView.findViewById(R.id.tvEventLevel);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivArrowRight = itemView.findViewById(R.id.ivArrowRight);
        }

        public void bind(EventListDTO event, Context context) {
            // Set color indicator based on status
            int statusColor = Color.parseColor("#" + Integer.toHexString(event.getStatusColor()));
            colorIndicator.setBackgroundColor(statusColor);

            // Đặt màu nền card là màu trắng/ngà trắng
            MaterialCardView cardView = (MaterialCardView) itemView;
            cardView.setCardBackgroundColor(Color.parseColor("#FFFEF7")); // Màu ngà trắng, dùng "#FFFFFF" cho trắng tinh

            // Màu chữ mặc định là màu tối
            int textColor = Color.parseColor("#212121");

            // Set event title
            String title = event.getEventCode() + ": " + event.getEventName();
            tvEventTitle.setText(title);
            tvEventTitle.setTextColor(textColor);

            // Set time
            String time = "Thời gian: " + event.getStartTime() + " - " + event.getEndTime();
            tvEventTime.setText(time);
            tvEventTime.setTextColor(Color.parseColor("#757575")); // Màu xám cho text phụ

            // Set level/fields info
            if (event.getFields() != null && !event.getFields().isEmpty()) {
                tvEventLevel.setText("Sân: " + event.getFields());
                tvEventLevel.setTextColor(Color.parseColor("#757575"));
                tvEventLevel.setVisibility(View.VISIBLE);
            } else {
                tvEventLevel.setVisibility(View.GONE);
            }

            // Set booking status với màu status nhạt
            tvBookingStatus.setText(event.getBookingStatus());
            tvBookingStatus.setBackgroundColor(adjustColorAlpha(statusColor, 0.15f)); // Alpha thấp hơn
            tvBookingStatus.setTextColor(statusColor); // Text màu status đậm

            // Set price với màu status nhạt
            tvPrice.setText(event.getPriceDisplay());
            tvPrice.setBackgroundColor(adjustColorAlpha(statusColor, 0.15f));
            tvPrice.setTextColor(statusColor);

            // Set arrow color
            ivArrowRight.setColorFilter(Color.parseColor("#9E9E9E"));

            // Click listener
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("eventName", event.getEventName());
                context.startActivity(intent);
            });
        }

        /**
         * Điều chỉnh độ trong suốt (alpha) của màu
         */
        private int adjustColorAlpha(int color, float factor) {
            int alpha = Math.round(255 * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(alpha, red, green, blue);
        }
    }
}