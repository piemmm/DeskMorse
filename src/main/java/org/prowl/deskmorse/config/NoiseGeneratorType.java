package org.prowl.deskmorse.config;

import org.prowl.deskmorse.output.sound.QRM;
import org.prowl.deskmorse.utils.Tools;

public enum NoiseGeneratorType {

    None("None",null),
    FT8("FT8 Signals","ft8.wav"),
    LocalQRM("Local QRM", "localnoise.wav"),
    RTTY("RTTY Signals", "rtty.wav"),
    SSBVOICE("SSB Voice", "ssbvoice.wav");

    private String name;
    private Object source;

    NoiseGeneratorType(String name, Object source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public Object getSource() {
        return source;
    }

    public static NoiseGeneratorType fromName(String name) {
        for (NoiseGeneratorType type : NoiseGeneratorType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

//    public static void unpack() {
//        for (NoiseGeneratorType type : NoiseGeneratorType.values()) {
//            if (type.getSource() != null && type.getSource() instanceof String) {
//                Tools.unpackFile(QRM.class, (String)type.getSource());
//            }
//        }
//
//    }
}
