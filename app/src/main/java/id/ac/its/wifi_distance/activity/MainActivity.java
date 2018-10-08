package id.ac.its.wifi_distance.activity;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
    private ScheduledExecutorService executor;
    private BroadcastReceiver wifiScanReceiver;
    private IntentFilter wifiScanFilter = new IntentFilter();

    private Button resetButton;
    private TextView outputView;
    private WifiManager wifiManager;
    private Map<String, WifiData> wifiDataMap = new ConcurrentHashMap<>();

    public MainActivity() {
        executor = Executors.newSingleThreadScheduledExecutor();
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String summary = getWifiDataSummary();
                outputView.setText(summary);
            }
        };
        wifiScanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resetButton = findViewById(R.id.ResetButton);
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
        resetButton.setOnClickListener(v -> {
            wifiDataMap.clear();
            outputView.setText("empty");
        });
        initPeriodicWifiScan();
    }

    private void initPeriodicWifiScan() {
        executor.scheduleAtFixedRate(this::initWifiScan, 0, 2, TimeUnit.SECONDS);
    }

    private void initWifiScan() {
        registerReceiver(wifiScanReceiver, wifiScanFilter);
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
                int frequency = value.getFrequency();
                double averageDbm = value.getAverageDbm();
                double distance = dbmToMeter(frequency, averageDbm);
                String summary = String.format(
                        Locale.getDefault(),
                        "SSID: %s\nFrequency: %d\nLevel: %.2f dBm\nDistance: %.2f m\n\n",
                        value.getSSID(),
                        frequency,
                        averageDbm,
                        distance
                );
                outputBuilder.append(summary);
            });
            return outputBuilder.toString().trim();
        }
    }

    private Double dbmToMeter(int frequency, double dBm) {
        double exp = (27.55 - (20.0 * Math.log10(frequency)) + Math.abs(dBm)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
