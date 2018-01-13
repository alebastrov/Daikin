package com.nikondsl.daikin;

import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.enums.Timer;
import com.nikondsl.daikin.wireless.WirelessDaikin;
import lombok.Getter;
import lombok.Setter;

public abstract class DaikinBase {

    @Getter
    protected String host;
    @Getter
    @Setter
    protected double targetTemperature = 22;
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
    protected int port = 80;

    public DaikinBase(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        boolean isWirelessUnit = this instanceof WirelessDaikin;

        result.append(isWirelessUnit ? "Wireless " : "Wired ");
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

    public abstract void updateDaikinState(boolean isVerboseOutput);

    public abstract void readDaikinState(boolean verboseOutput);
}
