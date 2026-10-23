package com.novax.booster;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PanelService extends Service {

    private WindowManager wm;
    private View panel;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    "novax", "NOVAX", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "novax")
                    .setContentTitle("NOVAX BOOSTER")
                    .setContentText("Panel active")
                    .setSmallIcon(android.R.drawable.ic_menu_manage)
                    .build();
            startForeground(1, n);
        }

        buildPanel();
    }

    private void buildPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xCC0A0A0A);
        root.setPadding(20, 20, 20, 20);

        TextView handle = new TextView(this);
        handle.setText("NOVAX  =");
        handle.setTextColor(0xFFFFB74D);
        handle.setTextSize(14);
        handle.setPadding(8, 8, 8, 8);
        root.addView(handle);

        TextView status = new TextView(this);
        status.setText("no profile loaded");
        status.setTextColor(0xFFEEEEEE);
        status.setTextSize(10);
        status.setPadding(8, 8, 8, 8);
        status.setBackgroundColor(0x33FFFFFF);
        root.addView(status);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button b120 = new Button(this);
        b120.setText("120Hz");
        b120.setTextSize(10);
        b120.setOnClickListener(v -> {
            boolean ok = applyPeak();
            Toast.makeText(this,
                    ok ? "120 Hz requested" : "WRITE_SETTINGS denied",
                    Toast.LENGTH_SHORT).show();
        });
        row.addView(b120);

        Button bRefresh = new Button(this);
        bRefresh.setText("Refresh");
        bRefresh.setTextSize(10);
        bRefresh.setOnClickListener(v -> {
            status.setText("peak: " + currentPeak() + " Hz");
        });
        row.addView(bRefresh);

        Button bClose = new Button(this);
        bClose.setText("Close");
        bClose.setTextSize(10);
        bClose.setOnClickListener(v -> stopSelf());
        row.addView(bClose);

        root.addView(row);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 40;
        params.y = 200;

        final int[] init = new int[2];
        final float[] touch = new float[2];

        handle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    init[0] = params.x;
                    init[1] = params.y;
                    touch[0] = event.getRawX();
                    touch[1] = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = init[0] + (int) (event.getRawX() - touch[0]);
                    params.y = init[1] + (int) (event.getRawY() - touch[1]);
                    wm.updateViewLayout(panel, params);
                    return true;
            }
            return false;
        });

        panel = root;
        wm.addView(panel, params);
    }

    private boolean applyPeak() {
        try {
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.PEAK_REFRESH_RATE, 120f);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private float currentPeak() {
        try {
            return Settings.System.getFloat(getContentResolver(),
                    Settings.System.PEAK_REFRESH_RATE, 60f);
        } catch (Exception e) {
            return 60f;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (panel != null && wm != null) {
            try { wm.removeView(panel); } catch (Exception ignored) {}
        }
    }
}
