package org.prowl.deskmorse.config;

public enum SendingType {

    RandomWords("Random Words"),
    RandomLetters("Groups of Letters"),
    RandomNumbers("Groups of Numbers"),
    RandomWordsAndNumbers("Random Words and Numbers"),
    RandomLettersAndNumbers("Random Letters and Numbers"),
    RandomWordsLettersAndNumbers("Random Words, Letters and Numbers"),
    QSO("Receive QSO message");

    private String name;

    SendingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SendingType fromName(String name) {
        for (SendingType type : SendingType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }


}
