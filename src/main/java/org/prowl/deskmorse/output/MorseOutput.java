package org.prowl.deskmorse.output;

import org.prowl.deskmorse.config.MorseCodeType;

import java.util.concurrent.Semaphore;

public abstract class MorseOutput {

    Semaphore semaphore = new Semaphore(1);

    /**
     * Send the given morse code string - blocks until sending is complete.
     *
     * @param text The morse code string to send.
     * @param drive The amount of drive on the signal (for audio, this is the volume).
     */
    public abstract void send(String text, float speed, int pitch, double drive, MorseCodeType type, double typeValue);

    /**
     * Claim (and start) the output device
     */
    public void start() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Release the output device.
     */
    public void stop() {
        semaphore.release();
    }

    public abstract void shutdown();

    public abstract void waitForSent();

}
