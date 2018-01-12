package com.nikondsl.daikin.enums;

import lombok.Getter;

public enum Fan {

    Auto("A","FAuto"),
    F1("3","Fun1"),
    F2("4","Fun2"),
    F3("5","Fun3"),
    F4("6","Fun4"),
    F5("7","Fun5"),
    None("B","None");

    @Getter
    private String wiredCommand;
    @Getter
    private String wirelessCommand;

    Fan(String wiredCommand, String wirelessCommand) {
        this.wiredCommand = wiredCommand;
        this.wirelessCommand = wirelessCommand;
    }

    public static Fan valueOfWiredCommand(String wiredCommand) {
        for (Fan fan : values()) {
            if (fan.wiredCommand.equalsIgnoreCase(wiredCommand)) return fan;
        }
        if (wiredCommand.equalsIgnoreCase("FA")) return Fan.Auto;
        if (wiredCommand.equalsIgnoreCase("F1")) return Fan.F1;
        if (wiredCommand.equalsIgnoreCase("F2")) return Fan.F2;
        if (wiredCommand.equalsIgnoreCase("F3")) return Fan.F3;
        if (wiredCommand.equalsIgnoreCase("F4")) return Fan.F4;
        if (wiredCommand.equalsIgnoreCase("F5")) return Fan.F5;

        return Fan.None;
    }

    public static Fan valueOfWirelessCommand(String wirelessCommand) {
        for (Fan fan : values()) {
            if (fan.wirelessCommand.equalsIgnoreCase(wirelessCommand)) return fan;
        }
        if (wirelessCommand.equals("1")) return Fan.F1;
        if (wirelessCommand.equals("2")) return Fan.F1;

        return Fan.None;
    }
}