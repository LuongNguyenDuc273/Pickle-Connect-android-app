package com.datn06.pickleconnect.Menu;

import android.content.Context;
import android.content.Intent;

import com.datn06.pickleconnect.MainActivity;
import com.datn06.pickleconnect.R;
import com.datn06.pickleconnect.Home.HomeActivity;
import com.datn06.pickleconnect.Search.SearchedActivity;
// Import các Activity khác khi đã tạo
// import com.datn06.pickleconnect.Booking.BookingActivity;
// import com.datn06.pickleconnect.Account.AccountActivity;

public class MenuNavigation {
    private Context context;

    public MenuNavigation(Context context) {
        this.context = context;
    }

    public void navigateTo(int destinationID) {
        if (destinationID == R.id.nav_home) {
            // Chuyển đến Home
            Intent intent = new Intent(context, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else if (destinationID == R.id.nav_booking) {
            // Chuyển đến Booking (Đặt sân)
             Intent intent = new Intent(context, SearchedActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
             context.startActivity(intent);
        }
        else if (destinationID == R.id.nav_account) {
            // Chuyển đến Account (Tài khoản)
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}