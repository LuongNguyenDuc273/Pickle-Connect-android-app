package com.datn06.pickleconnect.Utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.datn06.pickleconnect.R;

public class AlertHelper {

    public enum AlertType {
        SUCCESS, ERROR, WARNING, INFO
    }

    public static void showAlert(Activity activity, String message, AlertType type) {
        if (activity == null || activity.isFinishing()) return;

        ViewGroup rootView = activity.findViewById(android.R.id.content);
        View alertView = LayoutInflater.from(activity).inflate(R.layout.custom_alert, rootView, false);

        CardView cardView = alertView.findViewById(R.id.alertCard);
        ImageView ivIcon = alertView.findViewById(R.id.ivAlertIcon);
        TextView tvMessage = alertView.findViewById(R.id.tvAlertMessage);

        tvMessage.setText(message);

        switch (type) {
            case SUCCESS:
                ivIcon.setImageResource(R.drawable.ic_success);
                cardView.setCardBackgroundColor(0xFF4CAF50);
                break;
            case ERROR:
                ivIcon.setImageResource(R.drawable.ic_error);
                cardView.setCardBackgroundColor(0xFFF44336);
                break;
            case WARNING:
                ivIcon.setImageResource(R.drawable.ic_warning);
                cardView.setCardBackgroundColor(0xFFFF9800);
                break;
            case INFO:
                ivIcon.setImageResource(R.drawable.ic_info);
                cardView.setCardBackgroundColor(0xFF2196F3);
                break;
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP;
        params.topMargin = 50;
        params.leftMargin = 16;
        params.rightMargin = 16;
        alertView.setLayoutParams(params);

        rootView.addView(alertView);

        TranslateAnimation slideDown = new TranslateAnimation(0, 0, -500, 0);
        slideDown.setDuration(300);
        alertView.startAnimation(slideDown);

        alertView.postDelayed(() -> {
            TranslateAnimation slideUp = new TranslateAnimation(0, 0, 0, -500);
            slideUp.setDuration(300);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    rootView.removeView(alertView);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            alertView.startAnimation(slideUp);
        }, 3000);
    }

    public static void showSuccess(Activity activity, String message) {
        showAlert(activity, message, AlertType.SUCCESS);
    }

    public static void showError(Activity activity, String message) {
        showAlert(activity, message, AlertType.ERROR);
    }

    public static void showWarning(Activity activity, String message) {
        showAlert(activity, message, AlertType.WARNING);
    }

    public static void showInfo(Activity activity, String message) {
        showAlert(activity, message, AlertType.INFO);
    }
}