package id.ac.its.wifi_distance;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class MainActivity extends OnPermissionsGrantedCallback {
    private Button refreshButton;
    private TextView outputView;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshButton = findViewById(R.id.RefreshButton);
        outputView = findViewById(R.id.OutputView);
        wifiManager = getSystemService(WifiManager.class);
        onRequestPermissions(
                permission.ACCESS_WIFI_STATE,
                permission.CHANGE_WIFI_STATE,
                permission.ACCESS_COARSE_LOCATION
        );
    }

    @Override
    public void onPermissionsGranted() {
        refreshButton.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(v -> initWifiScan());
        initWifiScan();
    }

    private void initWifiScan() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                outputView.setText(getWifiData());
                stopLoading();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);

        startLoading();
        wifiManager.startScan();
    }

    private String getWifiData() {
        StringBuilder outputBuilder = new StringBuilder();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults.isEmpty()) {
            outputBuilder.append("empty");
        } else {
            for (ScanResult result : scanResults) {
                double distance = dbmToMeter(
                        result.frequency,
                        result.level
                );
                String formatResult = String.format(
                        Locale.getDefault(),
                        "SSID: %s\nFrequency: %d MHz\nLevel: %d dBm\nDistance: %.2f m\n\n",
                        result.SSID,
                        result.frequency,
                        result.level,
                        distance
                );
                outputBuilder.append(formatResult);
            }
        }
        return outputBuilder.toString().trim();
    }

    private double dbmToMeter(int frequency, int dBm) {
        double exp = (27.55 - (20.0 * Math.log10(frequency)) + Math.abs(dBm)) / 20.0;
        return Math.pow(10.0, exp);
    }

    private void startLoading() {
        refreshButton.setVisibility(View.GONE);
        outputView.setText("Loading...");
    }

    private void stopLoading() {
        refreshButton.setVisibility(View.VISIBLE);
    }
}
