package com.nikondsl.daikin.enums;

import lombok.Getter;

public enum Fan {

    Auto("A","FAuto"),
    Silent("B","Silent"),
    F1("3","Fun1"),
    F2("4","Fun2"),
    F3("5","Fun3"),
    F4("6","Fun4"),
    F5("7","Fun5");

    @Getter
    private String wiredCommand;
    @Getter
    private String wirelessCommand;

    Fan(String wirelessCommand, String wiredCommand) {
        this.wiredCommand = wiredCommand;
        this.wirelessCommand = wirelessCommand;
    }

    public static Fan valueOfWiredCommand(String wiredCommand) {
        for (Fan fan : values()) {
            if (fan.wiredCommand.equalsIgnoreCase(wiredCommand)) return fan;
        }
        if (wiredCommand.equalsIgnoreCase("FA")) return Auto;
        if (wiredCommand.equalsIgnoreCase("F1")) return F1;
        if (wiredCommand.equalsIgnoreCase("F2")) return F2;
        if (wiredCommand.equalsIgnoreCase("F3")) return F3;
        if (wiredCommand.equalsIgnoreCase("F4")) return F4;
        if (wiredCommand.equalsIgnoreCase("F5")) return F5;

        return Silent;
    }

    public static Fan valueOfWirelessCommand(String wirelessCommand) {
        for (Fan fan : values()) {
            if (fan.wirelessCommand.equalsIgnoreCase(wirelessCommand)) return fan;
        }
        if (wirelessCommand.equals("1")) return F1;
        if (wirelessCommand.equals("2")) return F1;

        return Silent;
    }
    
	public static Fan parseFanConsoleCommand(String fan) {
		if ("silent".equalsIgnoreCase(fan)) return Silent;
		if ("1".equals(fan)) return F1;
		if ("2".equals(fan)) return F2;
		if ("3".equals(fan)) return F3;
		if ("4".equals(fan)) return F4;
		if ("5".equals(fan)) return F5;
		return Auto;
	}
	
}