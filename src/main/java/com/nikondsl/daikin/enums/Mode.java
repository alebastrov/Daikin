package com.nikondsl.daikin.enums;

public enum Mode {

    // only these options are available for wireless daikins
    Auto,
    Dry,
    Cool,
    Heat,
    Fan,

    // the non-wireless daikins also support the following:
    OnlyFun,
    Night,
    None
}