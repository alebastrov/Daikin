package com.nikondsl.daikin;

import com.nikondsl.daikin.wired.WiredDaikin;
import com.nikondsl.daikin.wireless.WirelessDaikin;

public class DaikinFactory {
    private DaikinFactory() {
    }

    public static DaikinBase createWiredDaikin(String host, int port) {
        return new WiredDaikin(host, port);
    }

    public static DaikinBase createWirelessDaikin(String host, int port) {
        return new WirelessDaikin(host, port);
    }
}
