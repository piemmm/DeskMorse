package org.prowl.deskmorse.input;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Stream of integers vs time which represent a confidence level and length of a dot/dash
 * <p>
 * Example: for mouse based morse input this would stream numbers from -127 (space) to +127 (dash/tone heard)
 * for audio, this would represent a likelhood of a dot/dash at a given time (-127 to +127)
 * <p>
 * The range of values is -127 to +127 - a higher value represents a higher confidence level of a tone being present
 * The speed of the stream is dependent on the input device
 * <p>
 * A value of 0 represents a neutral value (no tone, and no blank):
 * -127 represents 'this is definitely a space' and +127 represents 'this is definitely a tone'
 */
public final class MorseStream {

    private static final Log LOG = LogFactory.getLog("MorseStream");

    private final int BUFFER_SIZE = 10240;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int head = 0;
    private int tail = 0;
    private final Object wait = new Object();

    /**
     * The sample rate in Hz
     */
    public int sampleRate;

    public MorseStream(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Get the sample rate in Hz
     *
     * @return
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Get the next byte from the stream
     *
     * @return
     */
    public byte get() {
        while (tail == head) {
            try {
                synchronized (wait) {
                    wait.wait();
                }
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
            }
        }
        return buffer[tail++ % BUFFER_SIZE];
    }


    /**
     * Put a byte into the stream
     *
     * @param b
     */
    public void put(byte b) {
        synchronized (wait) {
            wait.notify();
        }
        buffer[head++ % BUFFER_SIZE] = b;
    }


}
