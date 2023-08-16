package org.prowl.deskmorse.output.sound;

import javafx.scene.media.AudioClip;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The QRM class is used to generate noise on the output device from stored samples
 */
public class QRM {

    private static final Log LOG = LogFactory.getLog("QRM");

    private Object source;
    private double volume = 0.5d;
    private AudioClip audioClip;

    private final Object MONITOR = new Object();

    public QRM() {

    }

    public boolean isRunning() {
        return audioClip != null;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    /**
     * Start playing the sample, looping.
     */
    public void start() {

        if (source == null) {
            return;
        }
        synchronized (MONITOR) {
            try {
                String f = getClass().getResource((String) source).toExternalForm();
                audioClip = new AudioClip(f);
                audioClip.setVolume(volume);
                audioClip.setCycleCount(AudioClip.INDEFINITE);
                audioClip.play();
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
                audioClip = null;
            }
        }

    }

    public void setVolume(double volume) {
        this.volume = volume;
        synchronized (MONITOR) {
            if (audioClip != null) {
                audioClip.stop();
                audioClip.setVolume(volume);
                audioClip.play();
            }
        }

    }

    /**
     * Stop playing the sample.
     */
    public void stop() {
        synchronized (MONITOR) {
            if (audioClip != null) {
                audioClip.stop();
                audioClip = null;
            }
        }
    }


}
