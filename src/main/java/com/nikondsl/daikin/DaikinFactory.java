package com.nikondsl.daikin;

import com.nikondsl.daikin.wired.WiredDaikin;
import com.nikondsl.daikin.wireless.WirelessDaikin;

public class DaikinFactory {

    public static DaikinBase createWiredDaikin(String host) {
        return new WiredDaikin(host);
    }

    public static DaikinBase createWirelessDaikin(String host) {
        return new WirelessDaikin(host);
    }
}
