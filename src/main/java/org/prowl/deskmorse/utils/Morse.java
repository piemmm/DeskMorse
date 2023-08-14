package org.prowl.deskmorse.utils;

/**
 * An enum of the Morse code alphabet, numbers and punctuation.
 */
public enum Morse {

    A(".-", "A"),
    B("-...", "B"),
    C("-.-.", "C"),
    D("-..", "D"),
    E(".", "E"),
    F("..-.", "F"),
    G("--.", "G"),
    H("....", "H"),
    I("..", "I"),
    J(".---", "J"),
    K("-.-", "K"),
    L(".-..", "L"),
    M("--", "M"),
    N("-.", "N"),
    O("---", "O"),
    P(".--.", "P"),
    Q("--.-", "Q"),
    R(".-.", "R"),
    S("...", "S"),
    T("-", "T"),
    U("..-", "U"),
    V("...-", "V"),
    W(".--", "W"),
    X("-..-", "X"),
    Y("-.--", "Y"),
    Z("--..", "Z"),
    ZERO("-----", "0"),
    ONE(".----", "1"),
    TWO("..---", "2"),
    THREE("...--", "3"),
    FOUR("....-", "4"),
    FIVE(".....", "5"),
    SIX("-....", "6"),
    SEVEN("--...", "7"),
    EIGHT("---..", "8"),
    NINE("----.", "9"),
    PERIOD(".-.-.-", "."),
    COMMA("--..--", ","),
    QUESTION("..--..", "?"),
    APOSTROPHE(".----.", "'"),
    EXCLAMATION("-.-.--", "!"),
    SLASH("-..-.", "/"),
    PARENTHESIS_OPEN("-.--.", "("),
    PARENTHESIS_CLOSE("-.--.-", ")"),
    AMPERSAND(".-...", "&"),
    COLON("---...", ":"),
    SEMICOLON("-.-.-.", ";"),
    EQUALS("-...-", "="),
    PLUS(".-.-.", "+"),
    MINUS("-....-", "-"),
    UNDERSCORE("..--.-", "_"),
    QUOTATION_MARK(".-..-.", "\""),
    DOLLAR("...-..-", "$"),
    AT(".--.-.", "@"),
    SPACE(" ", " "),
    ERROR("........", "ERROR");

    private String morse;
    private String character;

    Morse(String morse, String character) {
        this.morse = morse;
        this.character = character;
    }

    public String getMorse() {
        return morse;
    }

    public String getCharacter() {
        return character;
    }

    /**
     * Get the Morse object for a given Morse code string.
     * @param morse
     * @return
     */
    public static Morse getMorseFromCode(String morse) {
        for (Morse m : Morse.values()) {
            if (m.getMorse().equals(morse)) {
                return m;
            }
        }
        return SPACE;
    }

    /**
     * Get the Morse code object for a given character.
     * @param character
     * @return
     */
    public static Morse getMorseFromCharacter(char character) {
        for (Morse m : Morse.values()) {
            if (m.getCharacter().charAt(0) == character) {
                return m;
            }
        }
        return SPACE;
    }



}
