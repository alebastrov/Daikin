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
    public void readDaikinState(boolean verboseOutput) {
        // this returns a CSV line of properties and their values, which we 
        // then parse and store as properties on this Daikin instance
        List<String> strings = readFromAdapter(verboseOutput, GET_CONTROL_INFO);
        if (strings == null || strings.isEmpty()) {
            if (verboseOutput) System.err.println("Could not read anything from adapter");
            return;
        }
        String controlInfo = strings.get(0);
        if (verboseOutput) System.out.println("Got for (" + GET_CONTROL_INFO + "): " + controlInfo);
		parseControlInfoResponse(verboseOutput, controlInfo);

        // we also read in the sensor values that we care about
		strings = readFromAdapter(verboseOutput, GET_SENSOR_INFO);
		if (strings == null || strings.isEmpty()) {
			if (verboseOutput) System.err.println("Could not read anything from adapter");
			return;
		}
		String sensorInfo = strings.get(0);
		if (verboseOutput) System.out.println("Got for (" + GET_SENSOR_INFO + "): " + sensorInfo);
		parseSensorResponse(verboseOutput, sensorInfo);
    }
	
	private void parseSensorResponse(boolean verboseOutput, String sensorInfo) {
		Map<String, String> properties;
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
	
	private void parseControlInfoResponse(boolean verboseOutput, String controlInfo) {
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
					targetTemperature = parseDouble(value);
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
	}
	
	List<String> readFromAdapter(boolean verboseOutput, String pathToApi) {
        return RestConnector.submitGet(this, pathToApi, verboseOutput);
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
        return mode.getModeCommandForWireless();
    }

    private String getFanCommand() {
    	return fan.getWirelessCommand();
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
        return Mode.valueOfWireless(value);
    }

    public static Fan parseFan(String value) {
        return Fan.valueOfWirelessCommand(value);
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
