package net.jonathangiles.daikin;

import net.jonathangiles.daikin.enums.Fan;
import net.jonathangiles.daikin.enums.FanDirection;
import net.jonathangiles.daikin.enums.Mode;
import net.jonathangiles.daikin.enums.Timer;
import net.jonathangiles.daikin.wireless.WirelessDaikin;

public abstract class DaikinBase implements IDaikin {

    protected final String host;
    protected double targetTemperature = 0;

    // control info
    protected boolean on = false;
    protected int targetHumidity = 0;
    protected Mode mode = Mode.None;
    protected Fan fan = Fan.None;
    protected FanDirection fanDirection = FanDirection.None;
    protected Timer timer = Timer.None;

    // sensor info
    protected double insideTemperature = 0;
    protected double insideHumidity = 0;
    protected double outsideTemperature = 0;

    public DaikinBase(String host) {
        this.host = host;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void setTargetTemperature(double temperature) {
        this.targetTemperature = temperature;
    }

    @Override
    public double getTargetTemperature() {
        return targetTemperature;
    }

    @Override
    public void setTargetHumidity(int humidity) {
        this.targetHumidity = humidity;
    }

    @Override
    public int getTargetHumidity() {
        return targetHumidity;
    }

    @Override
    public void setFan(Fan fan) {
        this.fan = fan;
    }

    @Override
    public Fan getFan() {
        return fan;
    }

    @Override
    public void setFanDirection(FanDirection fanDirection) {
        this.fanDirection = fanDirection;
    }

    @Override
    public FanDirection getFanDirection() {
        return fanDirection;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public double getInsideTemperature() {
        return insideTemperature;
    }

    @Override
    public double getOutsideTemperature() {
        return outsideTemperature;
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
