package com.nikondsl.daikin.wireless;

import com.nikondsl.daikin.DaikinBase;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.util.RestConnector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WirelessDaikin extends DaikinBase {
    private static final String GET_CONTROL_INFO = "/aircon/get_control_info";
    private static final String SET_CONTROL_INFO = "/aircon/set_control_info";
    private static final String GET_SENSOR_INFO = "/aircon/get_sensor_info";

    public WirelessDaikin(String host, int port) {
        super(host, port);
    }

    @Override
    public void updateDaikinState(boolean isVerboseOutput) {
        // posts the state of this object to the Daikin unit, updating it
        Map<String, String> parameters = new LinkedHashMap<>();
        StringBuilder command = new StringBuilder();

        command.append("pow=");
        command.append(isOn() ? "1" : "0");
        parameters.put("pow", isOn() ? "1" : "0");

        command.append("&mode=");
        command.append(getModeCommand());
        parameters.put("mode", getModeCommand());

        command.append("&stemp=");
        command.append(getTargetTemperature());
        parameters.put("stemp", String.valueOf(getTargetTemperature()));

        command.append("&f_rate=");
        command.append(getFanCommand());
        parameters.put("f_rate", getFanCommand());

        command.append("&f_dir=");
        command.append(getFanDirectionCommand());
        parameters.put("f_dir", getFanDirectionCommand());

        command.append("&shum=");
        command.append(getTargetHumidity());
        parameters.put("shum", String.valueOf(getTargetHumidity()));

        RestConnector.submitPost(this, SET_CONTROL_INFO, command.toString(), parameters, isVerboseOutput);
    }

    @Override
    public void readDaikinState(boolean verboseOutput, boolean restAssuranceOnly) {
        // this returns a CSV line of properties and their values, which we 
        // then parse and store as properties on this Daikin instance
        List<String> strings = RestConnector.submitGet(this, GET_CONTROL_INFO, verboseOutput);
        if (strings == null || strings.isEmpty()) return;
        String controlInfo = strings.get(0);
        System.out.println("Got: " + controlInfo);
        Map<String, String> properties = createMap(controlInfo);

        for (Entry<String, String> property : properties.entrySet()) {
            String key = property.getKey();
            String value = property.getValue();

            switch (key) {
                case "ret":
                    if (!"ok".equalsIgnoreCase(value))
                        throw new IllegalStateException("Invalid response, ret=" + value);
                    break;
                case "pow":
                    on = "1".equals(value);
                    break;
                case "mode":
                    mode = parseMode(value);
                    break;
                case "stemp":
                    targetTemperature = parseInt(value);
                    break;
                case "shum":
                    targetHumidity = parseInt(value);
                    break;
                case "f_rate":
                    fan = parseFan(value);
                    break;
                case "f_dir":
                    fanDirection = parseFanDirection(value);
                    break;

                case "adv":
                case "alert":
                case "b_mode":
                case "b_shum":
                case "b_f_rate":
                case "b_f_dir":
                case "b_stemp":
                    // we do not know exactly
                    break;

                case "dt1":
                case "dt2":
                case "dt3":
                case "dt4":
                case "dt5":
                case "dt7":
                    // we do not know exactly (dest temp?)
                    break;

                case "dhh":
                case "dh1":
                case "dh2":
                case "dh3":
                case "dh4":
                case "dh5":
                case "dh7":
                    // dh represents target humidity for each DaikinMode
                    break;

                case "dfrh":
                case "dfr1":
                case "dfr2":
                case "dfr3":
                case "dfr4":
                case "dfr5":
                case "dfr6":
                case "dfr7":
                    // don't know what dfr represents currently,
                    break;

                case "dfdh":
                case "dfd1":
                case "dfd2":
                case "dfd3":
                case "dfd4":
                case "dfd5":
                case "dfd6":
                case "dfd7":
                    // don't know what dfd represents currently,
                    break;

                default:
                    if (verboseOutput) System.err.println("Ignoring got " + key + "=" + value);
            }
        }

        // we also read in the sensor values that we care about
        String sensorInfo = RestConnector.submitGet(this, GET_SENSOR_INFO, verboseOutput).get(0);
        properties = createMap(sensorInfo);

        for (Entry<String, String> property : properties.entrySet()) {
            String key = property.getKey();
            String value = property.getValue();
            //ret=OK,htemp=26.0,hhum=-,otemp=2.5,err=0,cmpfreq=26
            switch (key) {
                case "ret":
                    if (!"ok".equalsIgnoreCase(value))
                        throw new IllegalStateException("Invalid response, ret=" + value);
                    break;

                case "hhum":
                    if (!"-".equals(value)) setTargetHumidity(parseInt(value));
                    break;
                case "htemp":
                    insideTemperature = parseDouble(value);
                    break;
                case "otemp":
                    outsideTemperature = parseDouble(value);
                    break;

                case "err":
                    break;
                case "cmpfreq":
                    break;
                default:
                    if (verboseOutput) System.err.println("Ignoring got " + key + "=" + value);
            }
        }
    }

    private Map<String, String> createMap(String controlInfo) {
        Map<String, String> properties = new HashMap<>();
        String[] splitString = controlInfo.split(",");
        for (String property : splitString) {
            int equalsPos = property.indexOf("=");
            String key = property.substring(0, equalsPos);
            String value = property.substring(equalsPos + 1);
            properties.put(key, value);
        }
        return properties;
    }

    private String getModeCommand() {
        if (mode.equals(Mode.Auto)) return "0";
        if (mode.equals(Mode.Dry)) return "2";
        if (mode.equals(Mode.Cool)) return "3";
        if (mode.equals(Mode.Heat)) return "4";
        if (mode.equals(Mode.Fan)) return "6";
        if (mode.equals(Mode.None)) return "0";

        throw new IllegalArgumentException("Invalid or unsupported mode: " + mode);
    }

    private String getFanCommand() {
        if (fan.equals(Fan.Auto)) return "A";
        if (fan.equals(Fan.F1)) return "3";
        if (fan.equals(Fan.F2)) return "4";
        if (fan.equals(Fan.F3)) return "5";
        if (fan.equals(Fan.F4)) return "6";
        if (fan.equals(Fan.F5)) return "7";
        if (fan.equals(Fan.None)) return "B";

        throw new IllegalArgumentException("Invalid or unsupported fan: " + fan);
    }

    private String getFanDirectionCommand() {
        if (fanDirection.equals(FanDirection.Off)) return "0";
        if (fanDirection.equals(FanDirection.None)) return "0";
        if (fanDirection.equals(FanDirection.Vertical)) return "1";
        if (fanDirection.equals(FanDirection.Horizontal)) return "2";
        if (fanDirection.equals(FanDirection.VerticalAndHorizontal)) return "3";

        throw new IllegalArgumentException("Invalid or unsupported fan direction: " + fanDirection);
    }

    public static Mode parseMode(String value) {
        if (value.equals("0") || value.equals("1") || value.equals("7")) return Mode.Auto;
        if (value.equals("2")) return Mode.Dry;
        if (value.equals("3")) return Mode.Cool;
        if (value.equals("4")) return Mode.Heat;
        if (value.equals("6")) return Mode.Fan;

        return Mode.Auto;
    }

    public static Fan parseFan(String value) {
        if (value.equalsIgnoreCase("A")) return Fan.Auto;
        if (value.equals("1")) return Fan.F1;
        if (value.equals("2")) return Fan.F1;
        if (value.equals("3")) return Fan.F1;
        if (value.equals("4")) return Fan.F2;
        if (value.equals("5")) return Fan.F3;
        if (value.equals("6")) return Fan.F4;
        if (value.equals("7")) return Fan.F5;

        return Fan.None;
    }

    public static FanDirection parseFanDirection(String value) {
        if (value.equals("0")) return FanDirection.None;
        if (value.equals("1")) return FanDirection.Vertical;
        if (value.equals("2")) return FanDirection.Horizontal;
        if (value.equals("3")) return FanDirection.VerticalAndHorizontal;

        return FanDirection.Off;
    }

    public static double parseDouble(String value) {
        if (value == null || value.length() == 0) return 0.0;
        if ("-".equals(value)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            System.err.println("Error: cannot parse value [" + value + "] as a double");
            return 0.0;
        }
    }

    public static int parseInt(String value) {
        if (value == null || "".equals(value)) return 0;
        return Integer.parseInt(value);
    }
}
