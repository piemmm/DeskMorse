package org.prowl.deskmorse.config;

public enum NoiseGeneratorType {

    None("None"),
    QRM("QRM"),
    QRMandQSB("QRM and QSB"),
    QSB("QSB");

    private String name;

    NoiseGeneratorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static NoiseGeneratorType fromName(String name) {
        for (NoiseGeneratorType type : NoiseGeneratorType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
