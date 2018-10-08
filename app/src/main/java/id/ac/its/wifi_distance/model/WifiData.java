package id.ac.its.wifi_distance.model;

import android.net.wifi.ScanResult;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;

public class WifiData {
    private String BSSID;
    private String SSID;
    private int frequency;
    private Deque<Integer> dbmList = new LinkedList<>();

    private WifiData() {
    }

    public static WifiData from(ScanResult result) {
        WifiData data = new WifiData();
        data.BSSID = result.BSSID;
        data.SSID = result.SSID;
        data.frequency = result.frequency;
        data.dbmList.add(result.level);
        return data;
    }

    public void updateDbm(ScanResult result) {
        dbmList.addLast(result.level);
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getSummary() {
        return String.format(
                Locale.getDefault(),
                "SSID: %s\nFrequency: %d MHz\nAvg. Level: %.2f dBm\nDistance: %.2f m\n\n",
                SSID, frequency, getAverageDbm(), getDistance()
        );
    }

    public double getAverageDbm() {
        if (dbmList.isEmpty()) {
            return Double.NaN;
        } else {
            int sum = 0;
            int count = 0;
            for (int dBm : dbmList) {
                sum += dBm;
                ++count;
            }
            return (double) sum / count;
        }
    }

    private Double getDistance() {
        double averageDbm = getAverageDbm();
        double exp = (27.55 - (20.0 * Math.log10(frequency)) + Math.abs(averageDbm)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
