package org.prowl.deskmorse.config;

public enum MorseCodeType {

    Farnsworth("Farnsworth"),
    Hand("Hand"),
    Computer("Computer");

    private String name;

    MorseCodeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MorseCodeType fromName(String name) {
        for (MorseCodeType type : MorseCodeType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
