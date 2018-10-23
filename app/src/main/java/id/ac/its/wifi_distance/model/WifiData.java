package id.ac.its.wifi_distance.model;

import android.net.wifi.ScanResult;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;

public class WifiData {
    private static int MAX_DBM_LIST = 10;
    private static Comparator<Integer> ABS_COMPARATOR = Comparator.comparingInt(Math::abs);

    private final String BSSID;
    private final String SSID;
    private final int frequency;
    private final LinkedList<Integer> dbmList = new LinkedList<>();

    private WifiData(String BSSID, String SSID, int frequency) {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.frequency = frequency;
    }

    public static WifiData from(ScanResult result) {
        WifiData data = new WifiData(result.BSSID, result.SSID, result.frequency);
        data.dbmList.add(result.level);
        return data;
    }

    public void addDbm(ScanResult result) {
        dbmList.addFirst(result.level);
        if (dbmList.size() > MAX_DBM_LIST) {
            dbmList.removeLast();
        }
    }

    public String getSummary() {
        return String.format(
                Locale.getDefault(),
                "BSSID: %s\nSSID: %s\nFrequency: %d\nLevels: %s\nDistance: %.2f m",
                BSSID, SSID, frequency, dbmList.toString(), getDistance()
        );
    }

    private Double getDistance() {
        double dBm = getAverageDbm();
        double exp = (27.55 - (20.0 * Math.log10(frequency)) + Math.abs(dBm)) / 20.0;
        return Math.pow(10.0, exp);
    }

    private double getAverageDbm() {
        return dbmList.stream().mapToDouble(Integer::doubleValue)
                .average()
                .orElse(Double.NaN);
    }

    private int getStrongestDbm() {
        int min = Collections.min(dbmList);
        int max = Collections.max(dbmList);
        int compareMinMax = ABS_COMPARATOR.compare(min, max);
        boolean isMinLessThanMax = compareMinMax < 0;
        return isMinLessThanMax ? min : max;
    }
}
