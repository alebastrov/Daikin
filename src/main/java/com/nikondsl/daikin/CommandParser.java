package com.nikondsl.daikin;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;
import lombok.Setter;


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
    private String fan = ""; //0-5

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

    public String getMode() {
        if (mode.equalsIgnoreCase("Auto")) return "0";
        if (mode.equalsIgnoreCase("Dry")) return "2";
        if (mode.equalsIgnoreCase("Cool")) return "3";
        if (mode.equalsIgnoreCase("Heat")) return "4";
        if (mode.equalsIgnoreCase("Fan")) return "6";
        return "";
    }

    public String getFan() {
        if ("0".equals(fan)) return "1";
        if ("1".equals(fan)) return "3";
        if ("2".equals(fan)) return "4";
        if ("3".equals(fan)) return "5";
        if ("4".equals(fan)) return "6";
        if ("5".equals(fan)) return "7";
        if ("a".equalsIgnoreCase(fan)) return "a";
        return fan;
    }

    public String getFanDirection() {
        if ("v".equalsIgnoreCase(fanDirection)) return "1";
        if ("h".equalsIgnoreCase(fanDirection)) return "2";
        if ("hv".equalsIgnoreCase(fanDirection) || "vh".equalsIgnoreCase(fanDirection)) return "3";
        return fanDirection;
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


}
