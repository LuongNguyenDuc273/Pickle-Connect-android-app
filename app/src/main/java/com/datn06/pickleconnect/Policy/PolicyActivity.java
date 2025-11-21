package com.datn06.pickleconnect.Policy;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.datn06.pickleconnect.API.ApiClient;
import com.datn06.pickleconnect.API.AuthApiService;
import com.datn06.pickleconnect.API.ServiceHost;
import com.datn06.pickleconnect.Common.BaseResponse;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Utils.AlertHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PolicyActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private FrameLayout loadingContainer;
    private ProgressBar progressBar;
    private LinearLayout errorContainer;
    private TextView tvErrorMessage;
    private MaterialButton btnRetry;
    private CardView contentCard;
    private TextView tvPolicyContent;
    private TextView tvLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);

        initViews();
        setupToolbar();
        loadPolicy();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        loadingContainer = findViewById(R.id.loadingContainer);
        progressBar = findViewById(R.id.progressBar);
        errorContainer = findViewById(R.id.errorContainer);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
        contentCard = findViewById(R.id.contentCard);
        tvPolicyContent = findViewById(R.id.tvPolicyContent);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);

        btnRetry.setOnClickListener(v -> loadPolicy());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadPolicy() {
        showLoading();

        PolicyRequest request = new PolicyRequest();
        
        AuthApiService authService = ApiClient.createService(ServiceHost.AUTH_SERVICE, AuthApiService.class);
        authService.getPolicy(request).enqueue(new Callback<BaseResponse<PolicyResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<PolicyResponse>> call, Response<BaseResponse<PolicyResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PolicyResponse> baseResponse = response.body();
                    
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        String policyText = baseResponse.getData().getPolicyDescription();
                        showContent(policyText);
                    } else {
                        showError(baseResponse.getMessage() != null ? baseResponse.getMessage() : "Không thể tải chính sách");
                    }
                } else {
                    String errorMessage = "Lỗi kết nối (Code: " + response.code() + ")";
                    if (response.code() == 404) {
                        errorMessage = "Không tìm thấy chính sách";
                    } else if (response.code() == 500) {
                        errorMessage = "Lỗi server. Vui lòng thử lại sau.";
                    }
                    showError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PolicyResponse>> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
        contentCard.setVisibility(View.GONE);
        tvLastUpdated.setVisibility(View.GONE);
    }

    private void showContent(String content) {
        loadingContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        contentCard.setVisibility(View.VISIBLE);
        tvLastUpdated.setVisibility(View.VISIBLE);

        // Display policy content
        if (content != null && !content.isEmpty()) {
            tvPolicyContent.setText(content);
        } else {
            tvPolicyContent.setText("Nội dung chính sách đang được cập nhật.");
        }

        // Set last updated date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        tvLastUpdated.setText("Cập nhật lần cuối: " + currentDate);
    }

    private void showError(String errorMessage) {
        loadingContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        contentCard.setVisibility(View.GONE);
        tvLastUpdated.setVisibility(View.GONE);

        tvErrorMessage.setText(errorMessage);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
