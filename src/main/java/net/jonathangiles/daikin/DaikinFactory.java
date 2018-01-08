package net.jonathangiles.daikin;

import net.jonathangiles.daikin.wired.WiredDaikin;
import net.jonathangiles.daikin.wireless.WirelessDaikin;

public class DaikinFactory {

    public static IDaikin createWiredDaikin(String host) {
        return new WiredDaikin(host);
    }

    public static IDaikin createWirelessDaikin(String host) {
        return new WirelessDaikin(host);
    }
}
