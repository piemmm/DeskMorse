package org.prowl.deskmorse.config;

import org.prowl.deskmorse.DeskMorse;

/**
 * List of configuration names used in the xml file.
 */
public enum Conf {

    // Enum list of configuration variables with their defaults
    callsign(""),
    speed("12"),
    pitch("800"),
    drive("0.5"),
    handMorseSendingSkill("0.9"),
    qrmGeneratorVolume("0.5"),
    noGgroups("5"),
    groupLength("5"),
    sendingType(SendingType.RandomLetters.name()),
    morseCodeType(MorseCodeType.Farnsworth.name()),
    noiseGeneratorType(NoiseGeneratorType.None.name());

    public Object defaultSetting;

    Conf(Object defaultSetting) {
        this.defaultSetting = defaultSetting;
    }

    public String stringDefault() {
        return String.valueOf(defaultSetting);
    }

    public int intDefault() {
        return Integer.parseInt(String.valueOf(defaultSetting));
    }

    public boolean boolDefault() {
        return Boolean.parseBoolean(String.valueOf(defaultSetting));
    }

    public double doubleDefault() {
        return Double.parseDouble(String.valueOf(defaultSetting));
    }


}
