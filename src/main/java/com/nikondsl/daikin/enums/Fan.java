package com.nikondsl.daikin.enums;

import com.nikondsl.daikin.ConsoleCommandParser;
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
    private static ConsoleCommandParser<Fan> parser = new ConsoleCommandParser<Fan>() {
        @Override
        public Fan parseCommand(String consoleCommand) {
            if ("silent".equalsIgnoreCase(consoleCommand)) return Silent;
            if ("1".equals(consoleCommand)) return F1;
            if ("2".equals(consoleCommand)) return F2;
            if ("3".equals(consoleCommand)) return F3;
            if ("4".equals(consoleCommand)) return F4;
            if ("5".equals(consoleCommand)) return F5;
            return Auto;
        }
    };

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
	
}