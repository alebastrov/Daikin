package com.nikondsl.daikin.wireless;

import com.nikondsl.daikin.CommandMode;
import com.nikondsl.daikin.DaikinBase;
import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.util.RestConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WirelessDaikin extends DaikinBase {
	private final Logger LOG = LogManager.getLogger(WirelessDaikin.class);

    private static final String GET_CONTROL_INFO = "/aircon/get_control_info";
    private static final String SET_CONTROL_INFO = "/aircon/set_control_info";
    private static final String GET_SENSOR_INFO = "/aircon/get_sensor_info";
    
    public WirelessDaikin(String host, int port) {
        super(host, port);
    }
	
	@Override
	protected String getTypeOfUnit() {
		return "Wireless ";
	}
	
	@Override
    public void updateDaikinState() throws IOException {
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
        command.append(fanDirection.getWirelessCommand());
        parameters.put("f_dir", fanDirection.getWirelessCommand());

        command.append("&shum=");
        command.append(getTargetHumidity());
        parameters.put("shum", String.valueOf(getTargetHumidity()));

        RestConnector.submitPost(this, SET_CONTROL_INFO, command.toString(), parameters);
    }

    @Override
    public void readDaikinState() throws IOException {
        // this returns a CSV line of properties and their values, which we 
        // then parse and store as properties on this Daikin instance
        List<String> strings = readFromAdapter(GET_CONTROL_INFO);
        if (strings.isEmpty()) {
            LOG.warn("Could not read anything from unit " + GET_CONTROL_INFO);
            return;
        }
        String controlInfo = strings.get(0);
        LOG.debug("Got response for (" + GET_CONTROL_INFO + "): " + controlInfo);
		parseControlInfoResponse(controlInfo);

        // we also read in the sensor values that we care about
		strings = readFromAdapter(GET_SENSOR_INFO);
		if (strings.isEmpty()) {
			LOG.warn("Could not read anything from unit " + GET_SENSOR_INFO);
			return;
		}
		String sensorInfo = strings.get(0);
		LOG.debug("Got response for (" + GET_SENSOR_INFO + "): " + sensorInfo);
		parseSensorResponse(sensorInfo);
    }
	
	private void parseSensorResponse(String sensorInfo) {
		Map<String, String> properties;
		properties = createMap(sensorInfo);
		
		for (Entry<String, String> property : properties.entrySet()) {
			String key = property.getKey();
			String value = property.getValue();
			LOG.trace("Parsing sensor response: " + key + "=" + value);

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
					LOG.warn("Ignoring unknown sensor response parameter, got " + key + "=" + value);
			}
		}
	}
	
	private void parseControlInfoResponse(String controlInfo) {
		Map<String, String> properties = createMap(controlInfo);
		
		for (Entry<String, String> property : properties.entrySet()) {
			String key = property.getKey();
			String value = property.getValue();
			LOG.trace("Parsing control info: " + key + "=" + value);

			switch (key) {
				case "ret":
					if (!"ok".equalsIgnoreCase(value))
						throw new IllegalStateException("Invalid response, ret=" + value);
					break;
				case "pow":
					on = "1".equals(value);
					break;
				case "mode":
					mode = Mode.valueOfWireless(value);
					break;
				case "stemp":
					targetTemperature = parseDouble(value);
					break;
				case "shum":
					targetHumidity = parseInt(value);
					break;
				case "f_rate":
					fan = Fan.valueOfWirelessCommand(value);
					break;
				case "f_dir":
					fanDirection = FanDirection.valueOfWirelessCommand(value);
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
					LOG.warn("Ignoring unknown control response parameter, got " + key + "=" + value);
			}
		}
	}
	
	List<String> readFromAdapter(String pathToApi) throws IOException {
        return RestConnector.submitGet(this, pathToApi, CommandMode.COMMAND);
    }
    
    private Map<String, String> createMap(String controlInfo) {
        Map<String, String> properties = new HashMap<>();
        String[] splitString = controlInfo.split(",");
        for (String property : splitString) {
            String[] pair = property.split("=");
            if (pair.length != 2) {
            	LOG.warn("Could not parse " + property + " as key=value");
            	continue;
			}
            String key = pair[0];
            String value = pair[1];
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
