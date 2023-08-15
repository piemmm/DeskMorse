package org.prowl.deskmorse.input.mousemorse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.DeskMorse;
import org.prowl.deskmorse.input.MorseInput;
import org.prowl.deskmorse.input.MorseStream;
import org.prowl.deskmorse.utils.Tools;

import java.util.Timer;
import java.util.TimerTask;

public class MouseInput extends MorseInput {

    private static final Log LOG = LogFactory.getLog("MorseInput");

    private MorseStream stream;
    private Timer timer;
    private TimerTask timerTask;
    private volatile boolean mouseDown = false;
    private boolean active = false;

    private Object monitor = new Object();

    public MouseInput() {
        super();
        stream = new MorseStream(900);

        Tools.runOnThread(() -> {

            while (true) {
                synchronized (monitor) {
                    try {
                        if (!active) {
                            monitor.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mouseDown) {
                    stream.put((byte) 127);
                } else {
                    stream.put((byte) -127);
                }
                Tools.delay(1);
            }

        });

    }

    @Override
    public synchronized void start() {
        DeskMorse.INSTANCE.getDecoder().setMorseStream(stream);
        active = true;
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    @Override
    public void stop() {
        active = false;
        mouseDown = false;
    }

    public void feed(boolean mouseDown) {
        this.mouseDown = mouseDown;
    }


}
