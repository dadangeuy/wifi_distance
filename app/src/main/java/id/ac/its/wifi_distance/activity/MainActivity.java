package id.ac.its.wifi_distance.activity;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import id.ac.its.wifi_distance.R;
import id.ac.its.wifi_distance.activity.callback.OnPermissionsGrantedCallback;
import id.ac.its.wifi_distance.model.WifiData;

public class MainActivity extends OnPermissionsGrantedCallback {
    private Button refreshButton;
    private TextView outputView;
    private WifiManager wifiManager;
    private Map<String, WifiData> wifiDataMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshButton = findViewById(R.id.RefreshButton);
        outputView = findViewById(R.id.OutputView);
        wifiManager = getSystemService(WifiManager.class);
        executor = Executors.newSingleThreadScheduledExecutor();
        onRequestPermissions(
                permission.ACCESS_WIFI_STATE,
                permission.CHANGE_WIFI_STATE,
                permission.ACCESS_COARSE_LOCATION
        );
    }

    @Override
    public void onPermissionsGranted() {
        refreshButton.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(v -> wifiDataMap.clear());
        initPeriodicWifiScan();
    }

    private void initPeriodicWifiScan() {
        executor.scheduleAtFixedRate(this::initWifiScan, 0, 5, TimeUnit.SECONDS);
    }

    private void initWifiScan() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                outputView.setText(getWifiDataSummary());
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);

        wifiManager.startScan();
    }

    private String getWifiDataSummary() {
        List<ScanResult> scanResults = wifiManager.getScanResults();

        for (ScanResult result : scanResults) {
            boolean dataExist = wifiDataMap.containsKey(result.BSSID);
            WifiData data;
            if (dataExist) {
                data = wifiDataMap.get(result.BSSID);
                data.updateDbm(result);
            } else {
                data = WifiData.from(result);
                wifiDataMap.put(result.BSSID, data);
            }
        }

        if (wifiDataMap.isEmpty()) {
            return "empty";
        } else {
            StringBuilder outputBuilder = new StringBuilder();
            wifiDataMap.forEach((key, value) -> {
                String summary = String.format(
                        Locale.getDefault(),
                        "SSID: %s\nFrequency: %d\nLevel: %.2f dBm\nDistance: %.2f m\n\n",
                        value.getSSID(),
                        value.getFrequency(),
                        value.getAverageDbm(),
                        value.getAverageDistance()
                );
                outputBuilder.append(summary);
            });
            return outputBuilder.toString().trim();
        }
    }
}
