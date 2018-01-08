package net.jonathangiles.daikin;

import net.jonathangiles.daikin.enums.Fan;
import net.jonathangiles.daikin.enums.FanDirection;
import net.jonathangiles.daikin.enums.Mode;
import net.jonathangiles.daikin.enums.Timer;

public interface IDaikin {
    String getHost();

    void setOn(boolean on);

    boolean isOn();

    void setMode(Mode mode);

    Mode getMode();

    void setTargetTemperature(double temperature);

    double getTargetTemperature();

    void setTargetHumidity(int humidity);

    int getTargetHumidity();

    void setFan(Fan fanRate);

    Fan getFan();

    void setFanDirection(FanDirection fanDirection);

    FanDirection getFanDirection();

    Timer getTimer();

    double getInsideTemperature();

    double getOutsideTemperature();

    void updateDaikinState(boolean verboseOutput);

    void readDaikinState(boolean verboseOutput, boolean restAssuranceOnly);
}
