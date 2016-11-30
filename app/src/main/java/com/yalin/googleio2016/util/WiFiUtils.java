package com.yalin.googleio2016.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.settings.SettingsUtils;

import java.util.List;

/**
 * YaLin
 * 2016/11/30.
 */

public class WiFiUtils {
    private static final String TAG = "WiFiUtils";

    public static void installConferenceWiFi(final Context context) {
        // Create conferenceWifiConfig
        WifiConfiguration conferenceWifiConfig = getConferenceWifiConfig();

        // Store conferenceWifiConfig.
        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int netId = wifiManager.addNetwork(conferenceWifiConfig);
        if (netId != -1) {
            wifiManager.enableNetwork(netId, false);
            boolean result = wifiManager.saveConfiguration();
            if (!result) {
                Log.e(TAG, "Unknown error while calling WiFiManager.saveConfiguration()");
                Toast.makeText(context,
                        context.getResources().getString(R.string.wifi_install_error_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Unknown error while calling WiFiManager.addNetwork()");
            Toast.makeText(context,
                    context.getResources().getString(R.string.wifi_install_error_message),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns whether we should or should not offer to set up wifi. If asCard == true
     * this will decide whether or not to offer wifi setup actively (as a card, for instance).
     * If asCard == false, this will return whether or not to offer wifi setup passively
     * (in the overflow menu, for instance).
     */
    public static boolean shouldOfferToSetupWifi(final Context context, boolean actively) {
        long now = TimeUtils.getCurrentTime(context);
        if (now < Config.WIFI_SETUP_OFFER_START) {
            LogUtil.d(TAG, "Too early to offer wifi");
            return false;
        }
        if (now > Config.CONFERENCE_END_MILLIS) {
            LogUtil.d(TAG, "Too late to offer wifi");
            return false;
        }
        if (!WiFiUtils.isWiFiEnabled(context)) {
            LogUtil.d(TAG, "Wifi isn't enabled");
            return false;
        }
        if (!SettingsUtils.isAttendeeAtVenue(context)) {
            LogUtil.d(TAG, "Attendee isn't on-site so wifi wouldn't matter");
            return false;
        }
        if (WiFiUtils.isWiFiApConfigured(context)) {
            LogUtil.d(TAG, "Attendee is already setup for wifi.");
            return false;
        }
        if (actively && SettingsUtils.hasDeclinedWifiSetup(context)) {
            LogUtil.d(TAG, "Attendee opted out of wifi.");
            return false;
        }
        return true;
    }

    public static boolean isWiFiEnabled(final Context context) {
        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean isWiFiApConfigured(final Context context) {
        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();

        if (configs == null) return false;

        // Check for existing APs.
        final String conferenceSSID = getConferenceWifiConfig().SSID;
        for (WifiConfiguration config : configs) {
            if (conferenceSSID.equalsIgnoreCase(config.SSID)) return true;
        }
        return false;
    }

    private static WifiConfiguration getConferenceWifiConfig() {
        WifiConfiguration conferenceConfig = new WifiConfiguration();

        // Must be in double quotes to tell system this is an ASCII SSID and passphrase.
        conferenceConfig.SSID = String.format("\"%s\"", BuildConfig.WIFI_SSID);
        conferenceConfig.preSharedKey = String.format("\"%s\"", BuildConfig.WIFI_PASSPHRASE);

        return conferenceConfig;
    }
}
