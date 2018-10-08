package id.ac.its.wifi_distance.activity;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import id.ac.its.wifi_distance.R;
import id.ac.its.wifi_distance.activity.callback.OnPermissionsGrantedCallback;
import id.ac.its.wifi_distance.model.WifiData;

public class MainActivity extends OnPermissionsGrantedCallback {
    // Non-Android Component
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final BroadcastReceiver wifiScanReceiver;
    private final IntentFilter wifiScanFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private final Map<String, WifiData> wifiDataMap = new ConcurrentHashMap<>();

    // Service Component
    private WifiManager wifiManager;

    // UI Component
    private ArrayAdapter<String> wifiList;

    public MainActivity() {
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processWifiScanResults();
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUiComponent();
        initServiceComponent();

        onRequestPermissions(
                permission.ACCESS_WIFI_STATE,
                permission.CHANGE_WIFI_STATE,
                permission.ACCESS_COARSE_LOCATION
        );
    }

    private void initUiComponent() {
        setContentView(R.layout.activity_main);

        Button resetButton = findViewById(R.id.ResetButton);
        resetButton.setOnClickListener(v -> {
            wifiDataMap.clear();
            wifiList.clear();
        });

        ListView wifiListView = findViewById(R.id.WifiListView);
        wifiList = new ArrayAdapter<>(
                this,
                R.layout.layout_wifi_row,
                R.id.SSID
        );
        wifiListView.setAdapter(wifiList);
    }

    private void initServiceComponent() {
        wifiManager = getSystemService(WifiManager.class);
    }

    @Override
    public void onPermissionsGranted() {
        initPeriodicWifiScan();
    }

    private void initPeriodicWifiScan() {
        executor.scheduleAtFixedRate(
                this::initWifiScan,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    private void initWifiScan() {
        registerReceiver(wifiScanReceiver, wifiScanFilter);
        wifiManager.startScan();
    }

    private void processWifiScanResults() {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        updateWifiDataMap(scanResults);
        printWifiDataSummary();
    }

    private void updateWifiDataMap(List<ScanResult> scanResults) {
        scanResults.forEach(result -> {
            boolean dataExist = wifiDataMap.containsKey(result.BSSID);
            if (dataExist) {
                wifiDataMap
                        .get(result.BSSID)
                        .updateDbm(result);
            } else {
                wifiDataMap.put(
                        result.BSSID,
                        WifiData.from(result)
                );
            }
        });
    }

    private void printWifiDataSummary() {
        wifiList.clear();
        wifiDataMap.forEach((BSSID, wifiData) -> {
            String summary = wifiData.getSummary();
            wifiList.add(summary);
        });
    }
}
