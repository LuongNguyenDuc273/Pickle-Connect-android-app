package com.datn06.pickleconnect.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import com.datn06.pickleconnect.R;

public class LoadingDialog {
    private Dialog dialog;
    private Context context;

    public LoadingDialog(Context context) {
        this.context = context;
        initDialog();
    }
    
    private void initDialog() {
        if (context == null) return;
        
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCancelable(false);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            try {
                dialog.show();
            } catch (Exception e) {
                // Catch WindowManager$BadTokenException if activity is finishing
                e.printStackTrace();
            }
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                // Catch IllegalArgumentException if already dismissed
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cleanup dialog resources
     */
    public void destroy() {
        dismiss();
        dialog = null;
        context = null;
    }
}
