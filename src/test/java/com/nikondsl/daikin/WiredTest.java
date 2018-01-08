package com.nikondsl.daikin;

public class WiredTest {

    public static void main(String[] args) {
        final String host = "http://daikin";
        DaikinBase daikin = DaikinFactory.createWiredDaikin(host);
//        daikin.setTargetTemperature(20);
//        daikin.setMode(Mode.Auto);
//        daikin.setOn(true);
        System.out.println(daikin);
    }
}
