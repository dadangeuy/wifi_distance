package id.ac.its.wifi_distance.model;

import android.net.wifi.ScanResult;

import java.util.LinkedList;
import java.util.List;

public class WifiData {
    private String BSSID;
    private String SSID;
    private int frequency;
    private List<Integer> dBmList = new LinkedList<>();

    private WifiData() {
    }

    public static WifiData from(ScanResult result) {
        WifiData data = new WifiData();
        data.BSSID = result.BSSID;
        data.SSID = result.SSID;
        data.frequency = result.frequency;
        data.dBmList.add(result.level);
        return data;
    }

    public void updateDbm(ScanResult result) {
        dBmList.add(result.level);
    }

    public void resetDbm() {
        dBmList.clear();
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

    public double getAverageDistance() {
        double averageDbm = getAverageDbm();

        if (averageDbm == Double.NaN) {
            return Double.NaN;
        } else {
            double exp = (27.55 - (20.0 * Math.log10(frequency)) + Math.abs(averageDbm)) / 20.0;
            return Math.pow(10.0, exp);
        }
    }

    public double getAverageDbm() {
        int sum = 0;
        int count = dBmList.size();

        if (count == 0) {
            return Double.NaN;
        } else {
            for (int dBm : dBmList) {
                sum += dBm;
            }
            return (double) sum / count;
        }
    }
}
