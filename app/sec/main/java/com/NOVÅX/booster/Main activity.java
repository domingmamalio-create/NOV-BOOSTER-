package com.novax.booster;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView statusText;
    private static final int PICK_ZIP = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);

        TextView title = new TextView(this);
        title.setText("NOVAX BOOSTER");
        title.setTextSize(26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("no root - no shizuku - user-space");
        sub.setTextSize(12);
        root.addView(sub);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("grant overlay permission");
        btnOverlay.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        root.addView(btnOverlay);

        Button btnWrite = new Button(this);
        btnWrite.setText("grant write-settings (120Hz)");
        btnWrite.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        root.addView(btnWrite);

        Button btnPanel = new Button(this);
        btnPanel.setText("start floating panel");
        btnPanel.setOnClickListener(v -> {
            startForegroundService(new Intent(this, PanelService.class));
        });
        root.addView(btnPanel);

        Button btnImport = new Button(this);
        btnImport.setText("import plugin .zip");
        btnImport.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("application/zip");
            startActivityForResult(i, PICK_ZIP);
        });
        root.addView(btnImport);

        statusText = new TextView(this);
        statusText.setText("ready");
        statusText.setTextSize(11);
        statusText.setPadding(24, 24, 24, 24);
        root.addView(statusText);

        setContentView(root);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            String result = PluginLoader.importZip(this, uri);
            statusText.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
