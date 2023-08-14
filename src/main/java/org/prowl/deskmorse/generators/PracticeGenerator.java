package org.prowl.deskmorse.generators;

import org.prowl.deskmorse.config.MorseCodeType;
import org.prowl.deskmorse.config.NoiseGeneratorType;
import org.prowl.deskmorse.config.SendingType;

/**
 * A proactice generator for morse code.
 *
 * This class is responsible for generating practice morse code. - It will generate groups of numbers, letters, QSO messages, and random words
 *
 */
public class PracticeGenerator {

    public PracticeGenerator() {
    }

    public Practice generatePractice(SendingType sendingType, NoiseGeneratorType noiseGeneratorType, MorseCodeType morseCodeType, int noGroups, int groupLength, int speed) {
        return new Practice(sendingType, noiseGeneratorType, morseCodeType, noGroups, groupLength, speed);
    }


}
