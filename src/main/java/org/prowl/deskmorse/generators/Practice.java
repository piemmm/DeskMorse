package org.prowl.deskmorse.generators;

import org.prowl.deskmorse.config.MorseCodeType;
import org.prowl.deskmorse.config.NoiseGeneratorType;
import org.prowl.deskmorse.config.SendingType;

public class Practice {

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
        return sb.toString();
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
        return sb.toString();
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
        return sb.toString();
    }

    /**
     * Generate a random string of words.
     */
    public String generateRandomWords() {



    }

}
