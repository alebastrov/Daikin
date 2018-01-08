package com.nikondsl.daikin;

import com.nikondsl.daikin.enums.Fan;
import com.nikondsl.daikin.enums.FanDirection;
import com.nikondsl.daikin.enums.Mode;
import com.nikondsl.daikin.enums.Timer;

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
