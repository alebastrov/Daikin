package com.nikondsl.daikin.enums;

import com.nikondsl.daikin.ConsoleCommandParser;
import lombok.Getter;

public enum Mode {

    // only these options are available for wireless daikins
    Auto("0", "Auto"),
    Dry("2", "Dry"),
    Cool("3", "Cool"),
    Heat("4", "Heat"),
    Fan("6", "Fan"),

    // the non-wireless daikins also support the following:
    OnlyFun("0", "OnlyFun"),
    Night("0", "Night");

    private String wirelessCommand;
    private String wiredCommand;

    Mode(String wirelessCommand, String wiredCommand) {
        this.wirelessCommand = wirelessCommand;
        this.wiredCommand = wiredCommand;
    }
    
    @Getter
    private static ConsoleCommandParser<Mode> parser = new ConsoleCommandParser<Mode>() {
        @Override
        public Mode parseCommand(String consoleCommand) {
            if ("Auto".equalsIgnoreCase(consoleCommand)) return Auto;
            if ("Dry".equalsIgnoreCase(consoleCommand)) return Dry;
            if ("Cool".equalsIgnoreCase(consoleCommand)) return Cool;
            if ("Heat".equalsIgnoreCase(consoleCommand)) return Heat;
            if ("Fan".equalsIgnoreCase(consoleCommand)) return Fan;
            return OnlyFun;
        }
    };
	
	public String getModeCommandForWired() {
        return wiredCommand;
    }

    public String getModeCommandForWireless() {
        return wirelessCommand;
    }

    public static Mode valueOfWired(String value) {
        for (Mode mode : values()) {
            if (mode.getModeCommandForWired().equalsIgnoreCase(value)) return mode;
        }
        return Mode.Auto;
    }

    public static Mode valueOfWireless(String value) {
        for (Mode mode : values()) {
            if (mode.getModeCommandForWireless().equalsIgnoreCase(value)) return mode;
        }
        return Mode.Auto;
    }
}