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
    protected final String host;
    @Getter
    @Setter
    protected int targetTemperature = 22;
    @Getter
    @Setter
    protected boolean on = false;
    @Getter
    @Setter
    protected int targetHumidity = 0;
    @Getter
    @Setter
    protected Mode mode = Mode.None;
    @Getter
    @Setter
    protected Fan fan = Fan.None;
    @Getter
    @Setter
    protected FanDirection fanDirection = FanDirection.None;
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
        StringBuilder sb = new StringBuilder();

        boolean isWirelessUnit = this instanceof WirelessDaikin;

        sb.append(isWirelessUnit ? "Wireless " : "Wired ");
        sb.append("Daikin unit [ ");

        sb.append("\n  Host: ");
        sb.append(host);

        sb.append("\n  Power: ");
        sb.append(on ? "ON" : "OFF");

        sb.append("\n  Mode: ");
        sb.append(mode);

        sb.append("\n  Fan: ");
        sb.append(fan);

        sb.append("\n  Fan direction: ");
        sb.append(fanDirection);

        sb.append("\n  Target humidity: ");
        sb.append(targetHumidity);

        sb.append("\n  Target temperature: ");
        sb.append(targetTemperature);

        sb.append("\n  Inside temperature: ");
        sb.append(insideTemperature);

        sb.append("\n  Outside temperature: ");
        sb.append(outsideTemperature);

        sb.append("\n]");

        return sb.toString();
    }

    public abstract void updateDaikinState(boolean isVerboseOutput);

    public abstract void readDaikinState(boolean verboseOutput, boolean restAssuranceOnly);
}
