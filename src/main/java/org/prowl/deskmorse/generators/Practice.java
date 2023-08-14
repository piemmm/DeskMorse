package org.prowl.deskmorse.generators;

import com.google.common.io.Resources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.DeskMorse;
import org.prowl.deskmorse.config.MorseCodeType;
import org.prowl.deskmorse.config.NoiseGeneratorType;
import org.prowl.deskmorse.config.SendingType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Practice {

    private static final Log LOG = LogFactory.getLog("Practice");
    private static String[] DICTIONARY = new String[0];

    private String text;
    private NoiseGeneratorType noiseGeneratorType;
    private MorseCodeType morseCodeType;
    private SendingType sendingType;
    private int speed;
    private int noGroups;
    private int groupLength;

    public Practice(SendingType sendingType, NoiseGeneratorType noiseGeneratorType, MorseCodeType morseCodeType, int noGroups, int groupLength, int speed) {
        this.sendingType = sendingType;
        this.noiseGeneratorType = noiseGeneratorType;
        this.morseCodeType = morseCodeType;
        this.noGroups = noGroups;
        this.groupLength = groupLength;
        this.speed = speed;
    }

    public String getText() {
        return text;
    }

    public NoiseGeneratorType getNoiseGeneratorType() {
        return noiseGeneratorType;
    }

    public MorseCodeType getMorseCodeType() {
        return morseCodeType;
    }

    public SendingType getSendingType() {
        return sendingType;
    }

    public int getSpeed() {
        return speed;
    }

    public int getNoGroups() {
        return noGroups;
    }

    public int getGroupLength() {
        return groupLength;
    }

    /**
     * Generate the practice text.
     */
    public void generate() {

        if (sendingType == SendingType.RandomLetters) {
            text = generateRandomLetters();
        } else if (sendingType == SendingType.RandomWords) {
            text = generateRandomWords();
        } else if (sendingType == SendingType.RandomNumbers) {
            text = generateRandomNumbers();
        } else if (sendingType == SendingType.RandomWordsAndNumbers) {
            text = generateRandomWordsAndNumbers();
        } else if (sendingType == SendingType.RandomLettersAndNumbers) {
            text = generateRandomLettersAndNumbers();
        } else if (sendingType == SendingType.RandomWordsLettersAndNumbers) {
            text = generateRandomWordsLettersAndNumbers();
        } else if (sendingType == SendingType.QSO) {
            text = generateQSO();
        }
    }


    /**
     * Generate a random string of letters.
     */
    private String generateRandomLetters() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noGroups; i++) {
            for (int j = 0; j < groupLength; j++) {
                sb.append(letters.charAt((int) (Math.random() * letters.length())));
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Generate a random string of numbers.
     */
    private String generateRandomNumbers() {
        String letters = "1234567890";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noGroups; i++) {
            for (int j = 0; j < groupLength; j++) {
                sb.append(letters.charAt((int) (Math.random() * letters.length())));
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Generate a random string of letters and numbers.
     */
    private String generateRandomLettersAndNumbers() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noGroups; i++) {
            for (int j = 0; j < groupLength; j++) {
                sb.append(letters.charAt((int) (Math.random() * letters.length())));
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Generate a random string of words.
     */
    public String generateRandomWords() {
        if (DICTIONARY.length == 0) {
            try {
                String data = Resources.toString(DeskMorse.class.getResource("/dictionary.txt"), StandardCharsets.UTF_8);
                DICTIONARY = data.split("\n");
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noGroups; i++) {
            for (int j = 0; j < groupLength; j++) {
                sb.append(DICTIONARY[(int) (Math.random() * DICTIONARY.length)].toUpperCase(Locale.ENGLISH));
                sb.append(" ");
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Generate a random string of words and numbers.
     */
    public String generateRandomWordsAndNumbers() {

        String[] words = generateRandomWords().split(" ");
        String[] numbers = generateRandomNumbers().split(" ");

        // Mix the words and numbers together, but randomly.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noGroups; i++) {
            if (Math.random() < 0.5) {
                sb.append(words[(int) (Math.random() * words.length)]);
            } else {
                sb.append(numbers[(int) (Math.random() * numbers.length)]);
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Generate a random string of words, letters and numbers.
     */
    public String generateRandomWordsLettersAndNumbers() {

        String[] words = generateRandomWords().split(" ");
        String[] numbers = generateRandomNumbers().split(" ");
        String[] letters = generateRandomLetters().split(" ");

        // Mix the words and numbers together, but randomly.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noGroups; i++) {
            if (Math.random() < 0.33) {
                sb.append(words[(int) (Math.random() * words.length)]);
            } else if (Math.random() < 0.66) {
                sb.append(numbers[(int) (Math.random() * numbers.length)]);
            } else {
                sb.append(letters[(int) (Math.random() * letters.length)]);
            }
            sb.append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Generate a QSO that you might see as part of a conversation in morse code, together with conventional abbreviations.
     * <p>
     * Conversations will start with a callsign replying to another callsign, maybe something about the weather, location, station details, etc
     * and soemtimes even a joke
     */
    public String generateQSO() {

        StringBuilder sb = new StringBuilder();

        // Generate a callsign
        sb.append(generateITUCallsign());
        sb.append(" DE ");
        sb.append(generateITUCallsign());
        sb.append(" ");

        // Now add about something or exchange something like the WX(weather), QTH(location), rig details, etc
        int info = 0;
        boolean wxDone = false, rigDone = false, qthDone = false;
        while (info < 2) {
            if (Math.random() < 0.5 && !wxDone) {
                sb.append(generateWX());
                wxDone = true;
                info++;
            }
            if (Math.random() < 0.5 && !rigDone) {
                sb.append(generateRIG());
                rigDone = true;
                info++;
            }
            if (Math.random() < 0.5 && qthDone) {
                sb.append(generateQTH());
                qthDone = true;
                info++;
            }

        }

        // Now send text that emulates handing back to other station or send 73's for a final




        return sb.toString().trim();


    }

    public String generateWX() {
        StringBuilder sb = new StringBuilder();
        sb.append("WX ");
        sb.append("IS ");
        String weather = chooseRandom("WINDY","CLOUDY","SUNNY","OVERCAST","RAINING","SNOWING","WARM","COLD","FREEZING","HOT","DRY","HUMID","WET","STORMY","THUNDERSTORMS","FOGGY","HAZE","SMOG","SMOKE","DUST","SANDSTORM","ICE","SLEET","HAIL","FROST","MIST","DRIZZLE","SHOWERS","DOWNPOUR","DOWNFALL");
        sb.append(weather+" ");
        // Sometimes weather is repeated twice
        if (Math.random() < 0.5) {
            sb.append(weather+" ");
        }
        return sb.toString();
    }

    public String generateRIG() {
        StringBuilder builder = new StringBuilder();
        builder.append("RIG ");
        builder.append("IS ");
        builder.append("A ");
        String rig = chooseRandom("ICOM","KENWOOD","YAESU","BAOFENG","ALINCO","ELECRAFT","TEN-TEC","ELAD","FLEXRADIO","ANAN","SUNSDR","SPE EXPERT","RM ITALY","RM KL");
        builder.append(rig+" ");
        if (Math.random() < 0.5) {
            builder.append(rig + " ");
        }
        builder.append(" ");
        return builder.toString();
    }

    public String generateQTH() {
        StringBuilder builder = new StringBuilder();
        builder.append("QTH ");
        builder.append("IS ");
        builder.append("A ");
        String qth = chooseRandom("CITY","TOWN","VILLAGE","HAMLET","FARM","HOUSE","COTTAGE","FLAT","APARTMENT","BUNGALOW","CABIN","CAMP","CARAVAN","CHALET","CONDO","DUPLEX","ESTATE","LODGE","MANSION","MANSIONETTE","MANOR","MILL","PENTHOUSE","RANCH","RESORT","RETREAT","SEMI","SHACK","STUDIO","TERRACE","VILLA","YURT");
        builder.append(qth+" ");
        if (Math.random() < 0.5) {
            builder.append(qth + " ");
        }
        builder.append(" ");
        return builder.toString();
    }

    /**
     * This generates an ITU compliant ham radio callsign
     *
     * @return
     */
    private String generateITUCallsign() {
        StringBuilder sb = new StringBuilder();
        String CALLSIGN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String CALLSIGN_NUMERS = "1234567890";

        // Build the callsign up - could be a 5 or 6 character callsign in the format of 2 letters, 1 number, 2 or 3 letters
        sb.append(CALLSIGN_CHARS.charAt((int) (Math.random() * CALLSIGN_CHARS.length())));
        if (Math.random() < 0.5) {
            sb.append(CALLSIGN_CHARS.charAt((int) (Math.random() * CALLSIGN_CHARS.length())));
        }
        sb.append(CALLSIGN_NUMERS.charAt((int) (Math.random() * CALLSIGN_NUMERS.length())));
        sb.append(CALLSIGN_CHARS.charAt((int) (Math.random() * CALLSIGN_CHARS.length())));
        sb.append(CALLSIGN_CHARS.charAt((int) (Math.random() * CALLSIGN_CHARS.length())));
        if (Math.random() < 0.5) {
            sb.append(CALLSIGN_CHARS.charAt((int) (Math.random() * CALLSIGN_CHARS.length())));
        }

        return sb.toString();
    }

    public String chooseRandom(String... words) {
        return words[(int) (Math.random() * words.length)];
    }

}