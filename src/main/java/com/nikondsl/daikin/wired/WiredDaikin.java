package com.nikondsl.daikin.wired;

import com.nikondsl.daikin.DaikinBase;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.enums.Timer;
import com.nikondsl.daikin.util.RestConnector;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.nikondsl.daikin.wireless.WirelessDaikin.parseInt;

/**
 * Code to control a Daikin heat pump via the KKRP01A online controller.
 * <p>
 * - Commands supported:
 * - Power
 * - Mode      Auto, Cool, Dry, Heat, OnlyFun, Night
 * - Temp      between 10C - 32C
 * - Fan       Fun1, Fun2, Fun3, Fun4, Fun5 (speeds), FAuto (auto)
 * - Swing     Ud (up/down), Off
 */
public class WiredDaikin extends DaikinBase {
    private static final String GET_STATE = "/param.csv";
    private static final String SET_STATE = "";

    // the temp values come back with European formatting - i.e. 23,5
    private NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("de"));

    public WiredDaikin(String host, int port) {
        super(host, port);
    }

    @Override
    public void updateDaikinState(boolean isVerboseOutput) {
        // posts the state of this object to the Daikin unit, updating it
        Map<String, String> parameters = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        sb.append("wON=");
        sb.append(isOn() ? "On" : "Off");
        parameters.put("wON", isOn() ? "On" : "Off");

        sb.append("&wMODE=");
        sb.append(getModeCommand());
        parameters.put("wMODE", getModeCommand());

        sb.append("&wTEMP=");
        sb.append(getTargetTemperature());
        sb.append("C");
        parameters.put("wTEMP", getTargetTemperature() + "C");

        sb.append("&wFUN=");
        sb.append(getFanCommand());
        parameters.put("wFUN", getFanCommand());

        sb.append("&wSWNG=");
        sb.append(getFanDirectionCommand());
        parameters.put("wSWNG", getFanDirectionCommand());

        sb.append("&wSETd1=Set");
        parameters.put("wSETd1", "Set");

        RestConnector.submitPost(this, SET_STATE, sb.toString(), parameters, isVerboseOutput);
    }

    @Override
    public void readDaikinState(boolean verboseOutput) {
        // returns a line delimited list of values, with a '.' after each value
        List<String> properties = parseProperties(RestConnector.submitGet(this, GET_STATE, verboseOutput));

        // check the response was OK
        if (!properties.get(0).equals("OK"))
            throw new RuntimeException("Bad connection state received: " + properties.get(0));

        // parse the state values from the response and update our host
        // NOTE: we don't update some of our internal state if Off otherwise
        //       values get cleared and when we switch on we reset state
        on = properties.get(1).equals("ON");
        if (on) {
            mode = Mode.valueOfWired(properties.get(2));
            fan = parseFan(properties.get(4));
            targetTemperature = parseInt(properties.get(3));
        }

        // read-only state
        fanDirection = parseFanDirection(properties.get(5));
        timer = parseTimer(properties.get(7));

        // sensors
        insideTemperature = parseDouble(properties.get(6));
        outsideTemperature = parseDouble(properties.get(14));
        insideHumidity = parseDouble(properties.get(15));
    }

    private List<String> parseProperties(List<String> values) {
        List<String> parsed = new ArrayList<>();
        // the state values come back as a line delimited list, with each value ending in '.'
        for (String value : values) {
            if (value == null || value.length() == 0) {
                value = "";
            } else {
                value = value.substring(0, value.length() - 1);
            }
            parsed.add(value);
        }
        return parsed;
    }

    private String getModeCommand() {
        return mode.getModeCommandForWired();
    }

    private String getFanCommand() {
        return fan.getWiredCommand();
    }

    private String getFanDirectionCommand() {
        if (fanDirection.equals(FanDirection.Off)) return "Off";
        if (fanDirection.equals(FanDirection.None)) return "Silent";
        if (fanDirection.equals(FanDirection.Vertical)) return "Ud";

        throw new IllegalArgumentException("Invalid or unsupported fan direction: " + fanDirection);
    }

    private Fan parseFan(String value) {
        return Fan.valueOfWiredCommand(value);
    }

    private FanDirection parseFanDirection(String value) {
        if (value.equals("UD")) return FanDirection.Vertical;
        if (value.equals("OFF")) return FanDirection.Off;

        return FanDirection.None;
    }

    private Timer parseTimer(String value) {
        if (value.equals("OFF/OFF")) return Timer.OffOff;
        if (value.equals("ON/OFF")) return Timer.OnOff;
        if (value.equals("OFF/ON")) return Timer.OffOn;
        if (value.equals("ON/ON")) return Timer.OnOn;

        return Timer.None;
    }

    private double parseDouble(String value) {
        if (value == null || value.length() == 0) return 0.0;
        if (value.equals("NONE"))
            return 0.0;
        try {
            return numberFormat.parse(value).doubleValue();
        } catch (java.text.ParseException e) {
            System.err.println("Error: cannot parse [" + value + "] as a double");
            return 0.0;
        }
    }
}
