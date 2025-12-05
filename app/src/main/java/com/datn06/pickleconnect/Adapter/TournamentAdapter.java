package com.datn06.pickleconnect.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.datn06.pickleconnect.API.AppConfig;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Models.Tournament.TourneyListResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder> {

    private static final String TAG = "TournamentAdapter";

    private Context context;
    private List<TourneyListResponse> tournamentList;
    private OnTournamentClickListener listener;

    private static final SimpleDateFormat INPUT_DATE_FORMAT =
            new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnTournamentClickListener {
        void onTournamentClick(TourneyListResponse tournament);
    }

    public TournamentAdapter(Context context) {
        this.context = context;
        this.tournamentList = new ArrayList<>();
        Log.d(TAG, "Constructor called");
    }

    public void setOnTournamentClickListener(OnTournamentClickListener listener) {
        this.listener = listener;
    }

    public void setTournaments(List<TourneyListResponse> tournaments) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "setTournaments() CALLED");
        Log.d(TAG, "========================================");
        Log.d(TAG, "Current list size: " + this.tournamentList.size());
        Log.d(TAG, "New list size: " + (tournaments != null ? tournaments.size() : "null"));

        this.tournamentList.clear();
        if (tournaments != null) {
            this.tournamentList.addAll(tournaments);

            // ✅ Sắp xếp theo startDate - Gần nhất lên đầu
            sortByStartDate();

            // Log chi tiết từng tournament
            for (int i = 0; i < this.tournamentList.size(); i++) {
                TourneyListResponse t = this.tournamentList.get(i);
                Log.d(TAG, "  [" + i + "] " + t.getTournamentName() +
                        " (ID: " + t.getTournamentId() + ", StartDate: " + t.getStartDate() + ")");
            }
        }

        Log.d(TAG, "Calling notifyDataSetChanged()...");
        notifyDataSetChanged();
        Log.d(TAG, "Final list size: " + this.tournamentList.size());
        Log.d(TAG, "========================================");
    }

    public void addTournaments(List<TourneyListResponse> tournaments) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "addTournaments() CALLED (Pagination)");
        Log.d(TAG, "========================================");

        if (tournaments != null && !tournaments.isEmpty()) {
            int startPosition = this.tournamentList.size();
            this.tournamentList.addAll(tournaments);

            // ✅ Sắp xếp lại toàn bộ danh sách theo startDate
            sortByStartDate();

            Log.d(TAG, "Added " + tournaments.size() + " items");
            Log.d(TAG, "Total size after sort: " + this.tournamentList.size());

            // Gọi notifyDataSetChanged vì thứ tự có thể thay đổi
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "addTournaments called with null or empty list");
        }
        Log.d(TAG, "========================================");
    }

    /**
     * Sắp xếp danh sách giải đấu theo startDate
     * Giải đấu có startDate GẦN NHẤT (sắp diễn ra sớm nhất) sẽ ở đầu danh sách
     */
    private void sortByStartDate() {
        Collections.sort(this.tournamentList, new Comparator<TourneyListResponse>() {
            @Override
            public int compare(TourneyListResponse t1, TourneyListResponse t2) {
                try {
                    // Parse startDate của cả 2 tournament
                    Date date1 = parseDate(t1.getStartDate());
                    Date date2 = parseDate(t2.getStartDate());

                    // Nếu cả 2 đều null, coi như bằng nhau
                    if (date1 == null && date2 == null) return 0;

                    // Nếu date1 null, đẩy xuống cuối
                    if (date1 == null) return 1;

                    // Nếu date2 null, đẩy xuống cuối
                    if (date2 == null) return -1;

                    // ✅ Sắp xếp TĂNG DẦN (ngày gần nhất lên đầu)
                    // date1.compareTo(date2) -> ngày nhỏ hơn (gần hơn) lên trước
                    return date1.compareTo(date2);

                } catch (Exception e) {
                    Log.e(TAG, "Error comparing dates", e);
                    return 0;
                }
            }
        });

        Log.d(TAG, "✓ Sorted by startDate (nearest first)");
    }

    /**
     * Parse date từ String sang Date
     * Hỗ trợ nhiều format khác nhau
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // Thử parse với format "dd-MM-yyyy HH:mm:ss"
        try {
            return INPUT_DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse with INPUT_DATE_FORMAT: " + dateStr);
        }

        // Thử parse với format ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return isoFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse with ISO format: " + dateStr);
        }

        // Thử parse với format khác (yyyy-MM-dd HH:mm:ss)
        try {
            SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return altFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse with alternative format: " + dateStr);
        }

        Log.e(TAG, "Could not parse date: " + dateStr);
        return null;
    }

    public void clear() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "clear() CALLED");
        Log.d(TAG, "========================================");

        int oldSize = this.tournamentList.size();
        this.tournamentList.clear();

        Log.d(TAG, "Cleared " + oldSize + " items");
        Log.d(TAG, "Calling notifyDataSetChanged()");

        notifyDataSetChanged();

        Log.d(TAG, "New size: " + this.tournamentList.size());
        Log.d(TAG, "========================================");
    }

    @Override
    public int getItemCount() {
        int count = tournamentList.size();
        Log.d(TAG, "getItemCount() called -> returning " + count);
        return count;
    }

    @NonNull
    @Override
    public TournamentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "onCreateViewHolder() CALLED");
        Log.d(TAG, "========================================");
        Log.d(TAG, "Parent: " + parent.getClass().getSimpleName());
        Log.d(TAG, "Parent width: " + parent.getWidth());
        Log.d(TAG, "Parent height: " + parent.getHeight());

        View view = LayoutInflater.from(context).inflate(R.layout.item_tournament, parent, false);

        Log.d(TAG, "Item view inflated: " + view.getClass().getSimpleName());

        // Log kích thước sau khi layout
        view.post(() -> {
            Log.d(TAG, "Item view measured:");
            Log.d(TAG, "  Width: " + view.getWidth());
            Log.d(TAG, "  Height: " + view.getHeight());
            Log.d(TAG, "  MeasuredWidth: " + view.getMeasuredWidth());
            Log.d(TAG, "  MeasuredHeight: " + view.getMeasuredHeight());
        });

        Log.d(TAG, "========================================");
        return new TournamentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TournamentViewHolder holder, int position) {
        Log.d(TAG, "----------------------------------------");
        Log.d(TAG, "onBindViewHolder() called for position " + position);

        if (position < 0 || position >= tournamentList.size()) {
            Log.e(TAG, "ERROR: Invalid position " + position + " (list size: " + tournamentList.size() + ")");
            return;
        }

        TourneyListResponse tournament = tournamentList.get(position);
        Log.d(TAG, "Binding tournament: " + tournament.getTournamentName());

        holder.bind(tournament);

        Log.d(TAG, "----------------------------------------");
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(TAG, "========================================");
        Log.d(TAG, "Adapter ATTACHED to RecyclerView");
        Log.d(TAG, "RecyclerView: " + recyclerView.getClass().getSimpleName());
        Log.d(TAG, "========================================");
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Log.d(TAG, "Adapter DETACHED from RecyclerView");
    }

    class TournamentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTournamentImage;
        private ImageView ivOrganizerLogo;
        private TextView tvOrganizerName;
        private TextView tvTournamentTitle;
        private TextView tvLocation;
        private TextView tvDate;

        public TournamentViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "ViewHolder constructor called");

            ivTournamentImage = itemView.findViewById(R.id.ivTournamentImage);
            ivOrganizerLogo = itemView.findViewById(R.id.ivOrganizerLogo);
            tvOrganizerName = itemView.findViewById(R.id.tvOrganizerName);
            tvTournamentTitle = itemView.findViewById(R.id.tvTournamentTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);

            // Kiểm tra views
            Log.d(TAG, "Views found:");
            Log.d(TAG, "  ivTournamentImage: " + (ivTournamentImage != null));
            Log.d(TAG, "  ivOrganizerLogo: " + (ivOrganizerLogo != null));
            Log.d(TAG, "  tvOrganizerName: " + (tvOrganizerName != null));
            Log.d(TAG, "  tvTournamentTitle: " + (tvTournamentTitle != null));
            Log.d(TAG, "  tvLocation: " + (tvLocation != null));
            Log.d(TAG, "  tvDate: " + (tvDate != null));

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Log.d(TAG, "Item clicked at position: " + position);
                    listener.onTournamentClick(tournamentList.get(position));
                }
            });
        }

        public void bind(TourneyListResponse tournament) {
            Log.d(TAG, "  Binding data to views:");

            // Tournament title
            tvTournamentTitle.setText(tournament.getTournamentName());
            Log.d(TAG, "    Title: " + tournament.getTournamentName());

            // Organizer name
            if (tournament.getOrganizerName() != null && !tournament.getOrganizerName().isEmpty()) {
                tvOrganizerName.setText(tournament.getOrganizerName());
                Log.d(TAG, "    Organizer: " + tournament.getOrganizerName());
            } else {
                tvOrganizerName.setText("Organizer");
                Log.d(TAG, "    Organizer: (default)");
            }

            // Location
            if (tournament.getTournamentLocation() != null && !tournament.getTournamentLocation().isEmpty()) {
                tvLocation.setText(tournament.getTournamentLocation());
                Log.d(TAG, "    Location: " + tournament.getTournamentLocation());
            } else {
                tvLocation.setText("Chưa có địa điểm");
                Log.d(TAG, "    Location: (default)");
            }

            // Date range
            String dateRange = formatDateRange(tournament.getStartDate(), tournament.getEndDate());
            tvDate.setText(dateRange);
            Log.d(TAG, "    Date: " + dateRange);

            // Load tournament image (get first image from array)
            String tournamentImageUrl = tournament.getFirstTournamentImageUrl();
            loadImage(ivTournamentImage, tournamentImageUrl, R.drawable.banner_placeholder);
            Log.d(TAG, "    Tournament image URL: " + tournamentImageUrl);

            // Load organizer logo
            loadImage(ivOrganizerLogo, tournament.getOrganizerLogo(), R.drawable.logo_atp);
            Log.d(TAG, "    Organizer logo URL: " + tournament.getOrganizerLogo());
        }

        private String formatDateRange(String startDate, String endDate) {
            try {
                String formattedStart = "N/A";
                String formattedEnd = "N/A";

                if (startDate != null && !startDate.isEmpty()) {
                    Date start = INPUT_DATE_FORMAT.parse(startDate);
                    if (start != null) {
                        formattedStart = OUTPUT_DATE_FORMAT.format(start);
                    }
                }

                if (endDate != null && !endDate.isEmpty()) {
                    Date end = INPUT_DATE_FORMAT.parse(endDate);
                    if (end != null) {
                        formattedEnd = OUTPUT_DATE_FORMAT.format(end);
                    }
                }

                return formattedStart + " - " + formattedEnd;
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date", e);
                return "Chưa có lịch thi đấu";
            }
        }

        private void loadImage(ImageView imageView, String imageUrl, int placeholder) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // ✅ Fix localhost URLs for emulator
                String fixedUrl = AppConfig.fixImageUrl(imageUrl);
                Log.d(TAG, "      Loading image from URL: " + fixedUrl);
                Glide.with(context)
                        .load(fixedUrl)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);
            } else {
                Log.d(TAG, "      Using placeholder image");
                imageView.setImageResource(placeholder);
            }
        }
    }
}