package com.nikondsl.daikin;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import lombok.Getter;
import lombok.Setter;

import static com.nikondsl.daikin.enums.Fan.F1;
import static com.nikondsl.daikin.enums.Fan.F2;
import static com.nikondsl.daikin.enums.Fan.F3;
import static com.nikondsl.daikin.enums.Fan.F4;
import static com.nikondsl.daikin.enums.Fan.F5;
import static com.nikondsl.daikin.enums.Fan.Silent;

@Parameters(separators = "=")
public class CommandParser {
    @Parameter(names = "-protocol")
    @Getter
    @Setter
    private String protocol = "http";

    @Parameter(names = "-host")
    @Getter
    private String host = "192.168.1.165";

    @Parameter(names = "-port")
    @Getter
    private String port = "80";

    @Parameter(names = "-power")
    @Getter
    private String power = "on";

    @Parameter(names = "-mode")
    private String mode = "";

    @Parameter(names = "-temp")
    @Getter
    private String targetTemperature = "";

    @Parameter(names = "-humid")
    @Getter
    private String targetHumudity = "";

    @Parameter(names = "-fan")
    private String fan = "silent";

    @Parameter(names = "-direction")
    private String fanDirection = "";

    @Parameter(names = "-timeoutConnection.connection")
    @Setter
    private String timeoutConnection = "";

    @Parameter(names = "-timeoutConnection.socket")
    @Setter
    private String timeoutSocket = "";

    @Parameter(names = "-verbose")
    private String verboseOutput = "";

    @Parameter(names = "-file")
    private String writeToFile;

    @Parameter(names = "-check.every")
    @Getter
    private String checkEvery;

    public boolean isVerboseOutput() {
        return verboseOutput != null && verboseOutput.length() > 0;
    }

    public String getWriteToFile() {
        return writeToFile;
    }

    public String getCheckEvery() {
        return checkEvery;
    }

    public Mode parseModeConsoleCommand() {
        if ("Auto".equalsIgnoreCase(mode)) return Mode.Auto;
        if ("Dry".equalsIgnoreCase(mode)) return Mode.Dry;
        if ("Cool".equalsIgnoreCase(mode)) return Mode.Cool;
        if ("Heat".equalsIgnoreCase(mode)) return Mode.Heat;
        if ("Fan".equalsIgnoreCase(mode)) return Mode.Fan;
        return Mode.OnlyFun;
    }
	
	public FanDirection parseFanDirectionConsoleCommand() {
		if ("v".equalsIgnoreCase(fanDirection)) return FanDirection.Vertical;
		if ("h".equalsIgnoreCase(fanDirection)) return FanDirection.Horizontal;
		if ("hv".equalsIgnoreCase(fanDirection)) return FanDirection.VerticalAndHorizontal;
		if ("vh".equalsIgnoreCase(fanDirection)) return FanDirection.VerticalAndHorizontal;
		return FanDirection.Off;
	}

    public Long getTimeoutConnection() {
        try {
            Long timeout = Long.parseLong(this.timeoutConnection);
            return timeout;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Long getTimeoutSocket() {
        try {
            Long timeout = Long.parseLong(this.timeoutSocket);
            return timeout;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
	
	public Fan parseFanConsoleCommand() {
		if ("silent".equalsIgnoreCase(fan)) return Silent;
		if ("1".equals(fan)) return F1;
		if ("2".equals(fan)) return F2;
		if ("3".equals(fan)) return F3;
		if ("4".equals(fan)) return F4;
		if ("5".equals(fan)) return F5;
		return Fan.Auto;
	}
}
