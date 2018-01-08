package net.jonathangiles.daikin;

import net.jonathangiles.daikin.enums.Fan;
import net.jonathangiles.daikin.enums.FanDirection;
import net.jonathangiles.daikin.enums.Mode;

public class WirelessTest {

    public static void main(String[] args) {
        final String host = "http://192.168.1.162";
        IDaikin daikin = DaikinFactory.createWirelessDaikin(host);
        daikin.setTargetTemperature(22);
        daikin.setMode(Mode.Heat);
        daikin.setOn(true);
        daikin.setFan(Fan.F1);
        daikin.setFanDirection(FanDirection.Off);
        System.out.println(daikin);
    }
}
