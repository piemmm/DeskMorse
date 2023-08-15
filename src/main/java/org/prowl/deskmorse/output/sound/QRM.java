package org.prowl.deskmorse.output.sound;

import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.utils.Tools;

import java.io.File;

/**
 * The QRM class is used to generate noise on the output device from stored samples
 */
public class QRM {

    private static final Log LOG = LogFactory.getLog("QRM");

    private Object source;
    private MediaPlayer player;
    private MediaView mediaView;


    public QRM(Object source, MediaView mediaView) {
        this.mediaView = mediaView;
        this.source = source;
    }

    /**
     * Start playing the sample, looping.
     */
    public void start() {


        try {
            if (source == null || !(source instanceof String)) {
                return; // Nothing to do
            }

//            AudioFormat format = new AudioFormat(
//                    AudioFormat.Encoding.PCM_SIGNED, 44100, 16,
//                    2,   8, 44100, false);
            File file = new File(Tools.getAppDir(), (String) source);

            Platform.runLater(() -> {
                player = new MediaPlayer(new javafx.scene.media.Media(file.toURI().toString()));

                player.setOnReady(() -> {
                    LOG.debug("Playing " + source);
                    player.play();
                });
                player.setOnError(() -> {
                    player.getError().printStackTrace();
                    LOG.error("Error playing " + source+ " " + player.getError().getMessage());
                });
                player.setOnStalled(
                        () -> {
                            LOG.error("Stalled playing " + source);
                        }

                );
                mediaView.setMediaPlayer(player);
                player.setCycleCount(MediaPlayer.INDEFINITE);


            });


        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }


    }

    /**
     * Stop playing the sample.
     */
    public void stop() {

        if (player != null) {
            player.stop();
        }

    }


}
