package org.prowl.deskmorse.output.sound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.config.MorseCodeType;
import org.prowl.deskmorse.output.MorseOutput;
import org.prowl.deskmorse.utils.Morse;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class Sound extends MorseOutput {
    private static final Log LOG = LogFactory.getLog("Sound");


    public static float SAMPLE_RATE = 44100f;
    public static final byte[] buf = new byte[2];
    private int waitBufferAvailable;

    private SourceDataLine sdl;

    public Sound() {
        try {
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            sdl = AudioSystem.getSourceDataLine(af);
            waitBufferAvailable = sdl.getBufferSize();
            if (waitBufferAvailable > 1000) {
                waitBufferAvailable = waitBufferAvailable - 250;
            }
            sdl.open(af);
            //    sdl.start();
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void start() {
        super.start();
        tone(800, 900, 0.0f);
        //  sdl.start();
    }

    public void waitForSent() {
        sdl.drain();
    }

    public void stop() {
        //  sdl.drain();
        //  sdl.stop();
        tone(800, 900, 0.0f);
        super.stop();
    }

    /**
     * Release the output device resources.
     */
    public void shutdown() {
        sdl.close();
    }

    public void tone(int hz, double msecs, double vol) {
        int length = (int) msecs * 8;
        int trailoff = length - (int) (length / 20d);
        double decrease = (vol / ((double) length - (double) trailoff));
        for (int i = 0; i < length; i++) {
            // Reduce vol so no clipping
            if (i > trailoff) {
                vol = (vol - decrease);
            }
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            int sample = (int) (Math.sin(angle) * 32767.0 * vol);
            buf[0] = (byte) (sample & 0xff);
            buf[1] = (byte) ((sample >> 8) & 0xff);
            sdl.write(buf, 0, 2);
        }

        //  sdl.getBufferSize()
    }

    @Override
    public void send(String text, float speed, int pitch, double volume, MorseCodeType type, double typeValue) {

        // Default is farnsworth code.
        double dit = 6400d / speed;
        double dah = dit * 3d;
        double symSpace = dit;
        double wordSpace = 7d * dit;
        double letterSpace = 3d * dit;

        if (type.equals(MorseCodeType.Computer)) {
            dah = dit * 3d;
            symSpace = dit;
            wordSpace = 6d * dit;
            letterSpace = 2.5d * dit;


        } else if (type.equals(MorseCodeType.Hand)) {
            dah = dit * 3d;
            dit = dit * 0.9d; // dits slightly shorter
            symSpace = dit;
            wordSpace = 6d * dit;
            letterSpace = 2.5d * dit;
        }


        sdl.start();
        // Go though each character and generate the dits and dahs
        for (char c : text.toCharArray()) {
            Morse morse = Morse.getMorseFromCharacter(c);
            String code = morse.getMorse();
            tone(pitch, letterSpace, 0.0f);
            for (char m : code.toCharArray()) {

                if (type.equals(MorseCodeType.Hand)) {
                    dit = (6400d / speed) * ((Math.random() * typeValue)+(1-(typeValue/2)));
                    dah = dit * 3d;
                    symSpace = dit;
                    wordSpace = 6d * dit;
                    letterSpace = 2.5d * dit;
                }

                if (m == '-') {
                    tone(pitch, dah, volume);
                } else if (m == '.') {
                    tone(pitch, dit, volume);
                } else if (m == ' ') {
                    tone(pitch, wordSpace - symSpace, 0.0f);
                }
                // intra character delay
                tone(pitch, symSpace, 0.0f);
            }
        }

        // Stupid, stupid, hackbug to prevent pops/clicks because
        // calling drain() doesn't work properly on some implementations.
        while (sdl.available() < waitBufferAvailable) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        sdl.stop();

    }
}
