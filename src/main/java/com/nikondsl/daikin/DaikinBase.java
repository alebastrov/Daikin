package com.nikondsl.daikin;

import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.enums.Timer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public abstract class DaikinBase {
	
	public static final int COMFORT_TEMPERATURE = 22;
	public static final int DEFAUL_HTTP_PORT = 80;
	@Getter
    protected String host;
    @Getter
    @Setter
    protected double targetTemperature = COMFORT_TEMPERATURE;
    @Getter
    @Setter
    protected boolean on = false;
    @Getter
    @Setter
    protected int targetHumidity = 0;
    @Getter
    @Setter
    protected Mode mode = Mode.Auto;
    @Getter
    @Setter
    protected Fan fan = Fan.Silent;
    @Getter
    @Setter
    protected FanDirection fanDirection = FanDirection.Off;
    @Getter
    protected Timer timer = Timer.None;
    @Getter
    protected double insideTemperature = 0;
    @Getter
    protected double insideHumidity = 0;
    @Getter
    protected double outsideTemperature = 0;
    @Getter
    protected int port = DEFAUL_HTTP_PORT;

    public DaikinBase(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    protected abstract String getTypeOfUnit();
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getTypeOfUnit());
        result.append("Daikin unit [ ");

        result.append("\n  Host: ");
        result.append(host);

        result.append("\n  Power: ");
        result.append(on ? "ON" : "OFF");

        result.append("\n  Mode: ");
        result.append(mode);

        result.append("\n  Fan: ");
        result.append(fan);

        result.append("\n  Fan direction: ");
        result.append(fanDirection);

        result.append("\n  Target humidity: ");
        result.append(targetHumidity);

        result.append("\n  Target temperature: ");
        result.append(targetTemperature);

        result.append("\n  Inside temperature: ");
        result.append(insideTemperature);

        result.append("\n  Outside temperature: ");
        result.append(outsideTemperature);

        result.append("\n]");

        return result.toString();
    }

    public abstract void updateDaikinState() throws IOException;

    public abstract void readDaikinState() throws IOException;
}
