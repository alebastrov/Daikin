package com.nikondsl.daikin;

import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;

public class WirelessTest {

    public static void main(String[] args) {
        final String host = "http://192.168.1.162";
        DaikinBase daikin = DaikinFactory.createWirelessDaikin(host, 80);
        daikin.setTargetTemperature(22);
        daikin.setMode(Mode.Heat);
        daikin.setOn(true);
        daikin.setFan(Fan.F1);
        daikin.setFanDirection(FanDirection.Off);
        System.out.println(daikin);
    }
}
