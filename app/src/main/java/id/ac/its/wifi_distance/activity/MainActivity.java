package id.ac.its.wifi_distance.activity;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import id.ac.its.wifi_distance.R;
import id.ac.its.wifi_distance.activity.callback.OnPermissionGrantedCallback;
import id.ac.its.wifi_distance.activity.callback.PermissionActivity;
import id.ac.its.wifi_distance.model.WifiData;
import id.ac.its.wifi_distance.model.WifiDataAdapter;

public class MainActivity extends PermissionActivity implements OnPermissionGrantedCallback {
    // Non-Android Component
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final BroadcastReceiver wifiScanReceiver;
    private final IntentFilter wifiScanFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private final Map<String, WifiData> wifiDataMap = new ConcurrentHashMap<>();
    private final List<WifiData> wifiDataList = new LinkedList<>();

    // Service Component
    private WifiManager wifiManager;

    // UI Component
    private WifiDataAdapter wifiDataAdapter;

    public MainActivity() {
        super(
                permission.ACCESS_WIFI_STATE,
                permission.CHANGE_WIFI_STATE,
                permission.ACCESS_COARSE_LOCATION
        );
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processWifiScanResults();
            }
        };
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUiComponent();
        initServiceComponent();
    }

    private void initUiComponent() {
        Button resetButton = findViewById(R.id.ResetButton);
        resetButton.setOnClickListener(this::onClickReset);

        RecyclerView wifiRecyclerView = findViewById(R.id.WifiRecyclerView);
        wifiDataAdapter = new WifiDataAdapter(wifiDataList);
        wifiRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wifiRecyclerView.setAdapter(wifiDataAdapter);
    }

    private void onClickReset(View view) {
        wifiDataMap.clear();
        wifiDataList.clear();
        wifiDataAdapter.notifyDataSetChanged();
    }

    private void initServiceComponent() {
        wifiManager = getSystemService(WifiManager.class);
    }

    @Override
    public void onPermissionGranted() {
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
        showWifiDataSummary();
    }

    private void updateWifiDataMap(List<ScanResult> scanResults) {
        scanResults.forEach(this::updateWifiDataMap);
    }

    private void updateWifiDataMap(ScanResult result) {
        boolean dataExist = wifiDataMap.containsKey(result.BSSID);
        if (dataExist) {
            wifiDataMap
                    .get(result.BSSID)
                    .addDbm(result);
        } else {
            wifiDataMap.put(
                    result.BSSID,
                    WifiData.from(result)
            );
        }
    }

    private synchronized void showWifiDataSummary() {
        wifiDataList.clear();
        wifiDataList.addAll(wifiDataMap.values());
        wifiDataAdapter.notifyDataSetChanged();
    }
}
