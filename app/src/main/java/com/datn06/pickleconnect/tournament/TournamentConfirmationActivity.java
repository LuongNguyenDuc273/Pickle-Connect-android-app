package com.datn06.pickleconnect.tournament;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.datn06.pickleconnect.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class TournamentConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "TournamentConfirm";

    // Views - Tournament Info
    private TextView tvTournamentName;
    private TextView tvTournamentDate;
    private TextView tvTournamentLocation;

    // Views - Player Info
    private TextView tvPlayerName;
    private TextView tvPlayerPhone;
    private TextView tvPlayerGender;
    private TextView tvRegistrationFee;

    // Views - Total & Confirm
    private TextView tvTotalAmount;
    private Button btnConfirm;
    private ImageView btnBack;

    // Data
    private String tournamentId;
    private String matchTypeId;
    private String playerName;
    private String playerPhone;
    private String playerEmail;
    private String playerGender;
    private String dynamicFormDataJson;
    private double registrationFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_confirmation);

        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║       TournamentConfirmationActivity onCreate()            ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        initViews();
        loadDataFromIntent();
        displayData();
        setupListeners();
    }

    private void initViews() {
        Log.d(TAG, "▶ initViews()");

        // Tournament Info
        tvTournamentName = findViewById(R.id.tvTournamentName);
        tvTournamentDate = findViewById(R.id.tvTournamentDate);
        tvTournamentLocation = findViewById(R.id.tvTournamentLocation);

        // Player Info
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvPlayerPhone = findViewById(R.id.tvPlayerPhone);
        tvPlayerGender = findViewById(R.id.tvPlayerGender);
        tvRegistrationFee = findViewById(R.id.tvRegistrationFee);

        // Total & Confirm
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        Log.d(TAG, "  ✓ Views initialized");
    }

    private void loadDataFromIntent() {
        Log.d(TAG, "▶ loadDataFromIntent()");

        // Tournament Info
        tournamentId = getIntent().getStringExtra("tournamentId");
        String tournamentName = getIntent().getStringExtra("tournamentName");
        String tournamentDate = getIntent().getStringExtra("tournamentDate");
        String tournamentLocation = getIntent().getStringExtra("tournamentLocation");

        // Match Type Info
        matchTypeId = getIntent().getStringExtra("matchTypeId");
        String matchTypeName = getIntent().getStringExtra("matchTypeName");

        // Registration Fee
        String feeString = getIntent().getStringExtra("registrationFee");
        try {
            registrationFee = Double.parseDouble(feeString != null ? feeString : "0");
        } catch (NumberFormatException e) {
            registrationFee = 0;
            Log.e(TAG, "  ✗ Error parsing registration fee", e);
        }

        // Player Info
        playerName = getIntent().getStringExtra("playerName");
        playerPhone = getIntent().getStringExtra("playerPhone");
        playerEmail = getIntent().getStringExtra("playerEmail");
        playerGender = getIntent().getStringExtra("playerGender");

        // Dynamic Form Data
        dynamicFormDataJson = getIntent().getStringExtra("dynamicFormData");

        Log.d(TAG, "  Tournament ID: " + tournamentId);
        Log.d(TAG, "  Tournament Name: " + tournamentName);
        Log.d(TAG, "  Match Type: " + matchTypeName);
        Log.d(TAG, "  Player Name: " + playerName);
        Log.d(TAG, "  Registration Fee: " + registrationFee);
        Log.d(TAG, "  Dynamic Form Data: " + (dynamicFormDataJson != null ? "Present" : "Null"));
    }

    private void displayData() {
        Log.d(TAG, "▶ displayData()");

        // Tournament Info
        String tournamentName = getIntent().getStringExtra("tournamentName");
        String tournamentDate = getIntent().getStringExtra("tournamentDate");
        String tournamentLocation = getIntent().getStringExtra("tournamentLocation");

        tvTournamentName.setText(tournamentName != null ? tournamentName : "Giải đấu");
        tvTournamentDate.setText(tournamentDate != null ? tournamentDate : "Đang cập nhật");
        tvTournamentLocation.setText(tournamentLocation != null ? tournamentLocation : "Đang cập nhật");

        // Player Info
        tvPlayerName.setText(playerName != null ? playerName : "N/A");
        tvPlayerPhone.setText(playerPhone != null ? playerPhone : "N/A");
        tvPlayerGender.setText(playerGender != null ? playerGender : "N/A");

        // Registration Fee
        String formattedFee = formatCurrency(registrationFee);
        tvRegistrationFee.setText(formattedFee);

        // Total Amount (same as registration fee for now)
        tvTotalAmount.setText(formatCurrencyWithoutSymbol(registrationFee));

        Log.d(TAG, "  ✓ Data displayed successfully");

        // Log dynamic form data
        if (dynamicFormDataJson != null) {
            logDynamicFormData();
        }
    }

    private void logDynamicFormData() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║            Dynamic Form Data Received                      ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        try {
            JSONArray jsonArray = new JSONArray(dynamicFormDataJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject fieldObj = jsonArray.getJSONObject(i);
                String label = fieldObj.optString("label", "");
                String value = fieldObj.optString("value", "");
                Log.d(TAG, "  " + label + ": " + value);
            }
        } catch (JSONException e) {
            Log.e(TAG, "  ✗ Error parsing dynamic form data", e);
        }
    }

    private void setupListeners() {
        Log.d(TAG, "▶ setupListeners()");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "  Back button clicked");
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            Log.d(TAG, "  Confirm button clicked");
            handleConfirm();
        });
    }

    private void handleConfirm() {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════╗");
        Log.d(TAG, "║                 handleConfirm() START                      ║");
        Log.d(TAG, "╚════════════════════════════════════════════════════════════╝");

        // TODO: Implement payment flow
        // For now, just show success message

        Log.d(TAG, "  Tournament ID: " + tournamentId);
        Log.d(TAG, "  Match Type ID: " + matchTypeId);
        Log.d(TAG, "  Player Name: " + playerName);
        Log.d(TAG, "  Player Phone: " + playerPhone);
        Log.d(TAG, "  Player Email: " + playerEmail);
        Log.d(TAG, "  Player Gender: " + playerGender);
        Log.d(TAG, "  Registration Fee: " + registrationFee);

        // Log dynamic form data for registration API
        if (dynamicFormDataJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(dynamicFormDataJson);
                Log.d(TAG, "  Dynamic Form Data for API:");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject fieldObj = jsonArray.getJSONObject(i);
                    String fieldName = fieldObj.optString("fieldName", "");
                    String value = fieldObj.optString("value", "");
                    Log.d(TAG, "    " + fieldName + " = " + value);
                }
            } catch (JSONException e) {
                Log.e(TAG, "  ✗ Error parsing dynamic form data", e);
            }
        }

        // TODO: Call registration API here
        Toast.makeText(this,
                "Xác nhận thành công!\nChức năng thanh toán đang được phát triển...",
                Toast.LENGTH_LONG).show();

        // TODO: Navigate to payment gateway (VNPAY)
        // For now, just finish
        // finish();
    }

    /**
     * Format currency with VND symbol
     */
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    /**
     * Format currency without symbol (for total amount)
     */
    private String formatCurrencyWithoutSymbol(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}