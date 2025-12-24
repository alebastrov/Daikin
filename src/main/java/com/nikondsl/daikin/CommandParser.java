package com.nikondsl.daikin;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Parameters(separators = "=")
public class CommandParser {
    private static final Logger LOG = LogManager.getLogger(CommandParser.class);

    @Parameter(names = "-protocol", description = "http/https protocol to use REST API")
    @Getter
    @Setter
    private String protocol = "http";

    @Parameter(names = "-host", description = "Host IPv4 address")
    @Getter
    private String host = "192.168.1.165";

    @Parameter(names = "-port", description = "http port to use REST API")
    @Getter
    private String port = "80";

    @Parameter(names = "-power", description = "Switch power on/off")
    @Getter
    private String power = "on";

    @Parameter(names = "-mode", description = "AC unit mode: auto, cool, heat, etc.")
    @Getter
    private String mode = "";

    @Parameter(names = "-temp", description = "Target temperature in celsius")
    @Getter
    private String targetTemperature = "";

    @Parameter(names = "-humid", description = "Target humidity level")
    @Getter
    private String targetHumudity = "";

    @Parameter(names = "-fan", description = "Fan speed")
	@Getter
    private String fan = "silent";

    @Parameter(names = "-direction", description = "Wings direction")
    @Getter
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
    
    public Long getTimeoutConnection() {
        try {
            return Long.parseLong(timeoutConnection);
        } catch (NumberFormatException ex) {
            LOG.warn("Cannot parse [" + timeoutConnection + "] as long, ignoring.");
            return null;
        }
    }

    public Long getTimeoutSocket() {
        try {
            return Long.parseLong(timeoutSocket);
        } catch (NumberFormatException ex) {
            LOG.warn("Cannot parse [" + timeoutSocket + "] as long.ignoring.");
            return null;
        }
    }
    
}
