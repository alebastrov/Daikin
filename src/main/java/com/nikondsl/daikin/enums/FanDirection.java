package com.nikondsl.daikin.enums;

import com.nikondsl.daikin.ConsoleCommandParser;
import lombok.Getter;

public enum FanDirection {

    Off("0", ""),
    Vertical("1", "UD"),
    Horizontal("2", ""),
    VerticalAndHorizontal("3", "");
	
    FanDirection(String wirelessCommand, String wiredCommand) {
    	this.wirelessCommand = wirelessCommand;
    	this.wiredCommand =wiredCommand;
	}
	
	@Getter
	private static ConsoleCommandParser<FanDirection> parser = new ConsoleCommandParser<FanDirection>() {
		@Override
		public FanDirection parseCommand(String consoleCommand) {
			if ("v".equalsIgnoreCase(consoleCommand)) return Vertical;
			if ("h".equalsIgnoreCase(consoleCommand)) return Horizontal;
			if ("hv".equalsIgnoreCase(consoleCommand)) return VerticalAndHorizontal;
			if ("vh".equalsIgnoreCase(consoleCommand)) return VerticalAndHorizontal;
			return Off;
		}
	};
	
	@Getter
	private String wiredCommand;
	@Getter
	private String wirelessCommand;
	
	public static FanDirection valueOfWirelessCommand(String value) {
		for (FanDirection fanDirection : values()) {
			if (fanDirection.getWiredCommand().equalsIgnoreCase(value)) return fanDirection;
		}
		return Off;
	}
	
	public static FanDirection valueOfWiredCommand(String value) {
		for (FanDirection fanDirection : values()) {
			if (fanDirection.getWirelessCommand().equalsIgnoreCase(value)) return fanDirection;
		}
		return Off;
	}
	
	public static FanDirection parseConsoleCommand(String fanDirection) {
		return parser.parseCommand(fanDirection);
	}
}