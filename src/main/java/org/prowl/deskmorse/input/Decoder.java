package org.prowl.deskmorse.input;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.utils.EWMA;
import org.prowl.deskmorse.utils.Morse;
import org.prowl.deskmorse.utils.Tools;

/**
 * Takes a stream of dots and dashes and converts them into text, filtering out 'glitches' and other non-morse noise
 * provided by the MorseInput device.
 */
public class Decoder {
    private static final Log LOG = LogFactory.getLog("Decoder");

    private MorseStream morseStream;
    private DecodeListener decodeListener;
    private boolean running = false;
    private StringBuilder dotDarList = new StringBuilder();


    public Decoder() {

    }

    public void stop() {
        running = false;
    }

    /**
     * This sets the stream the decoder will read from
     *
     * @param morseStream
     */
    public void setMorseStream(MorseStream morseStream) {
        this.morseStream = morseStream;
    }

    public void setDecodeListener(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }

    public void start() {
        running = true;
        Tools.runOnThread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (morseStream == null) {
                        Tools.delay(1000);
                        continue;
                    }
                    byte b = morseStream.get();
                    morse(b);

                }
            }
        });
    }


    boolean canSpace = false;

    int space = 0;
    int debounceTone = 0;
    int toneCount = 0;
    int spaceTime = 0;
    int spaceCount = 0;
    int morseArrPos = 0;
    int dit = 80;
    int lastDar = 0;
    boolean textChanged = false;
    double p = 1; // Our 'certainty' of seeing a valid tone. 1=certain, -1= uncertain, 1=neutral
    double plock = 1;
    private EWMA ewma = new EWMA(0.5);


    /**
     * This decodes the incoming more code.
     *
     * @param tone
     */
    public void morse(byte tone) {


        p = ewma.getAverage(Math.abs(tone) / 128.0);
        plock = p;

        // Is tone handling
        boolean isTone = tone > 64;
        if (isTone) {
            toneCount++;
            if (debounceTone++ > 4) {
                spaceCount = 0;
                spaceTime = 0;
            }
        }

        // Space handling
        if (canSpace && spaceTime > dit * 6) {
            // Serial.print(" ");
            decodeListener.decoded(" ");
            canSpace = false;
            spaceTime = 0;
        }

        // Gap handling
        if (!isTone) {
            if (spaceCount > dit * 1.2) {
                spaceF();
                spaceCount = 0;
            }
            debounceTone = 0;


            // If gap size > dit then we have something to check (and debounce)
            if (toneCount > 5 && spaceCount >= dit / 2) {
                // int dist = abs(dit-toneCount);
                if (toneCount > dit * 1.7) {
                    darF();
                    lastDar = 0;
                    if (p > 0.4) {
                        if (toneCount > dit * 3) {
                            dit = dit + ((toneCount / 3) / 30);
                        } else if (toneCount < dit * 3) {
                            dit = dit - ((toneCount / 3) / 30);
                        }
                    }
                    spaceCount = 0;
                    spaceTime = 0;
                } else if (toneCount > dit / 2.2) {
                    ditF();
                    if (p > 0.4) {
                        if (toneCount > dit) {
                            dit = dit + (toneCount / 30);
                        } else if (toneCount < dit) {
                            dit = dit - (toneCount / 30);
                        }
                    }
                    spaceCount = 0;
                    spaceTime = 0;
                }
                toneCount = 0;
            }
            spaceTime++;
            spaceCount++;

            if (dit < 2) {
                dit = 2;
            } else if (dit > 900000) {
                dit = 900000;
            }

            if (lastDar < 1000000) {
                lastDar++;
            }


            //
             if (toneCount < dit / 2.2 && toneCount > 80 && plock > 0.2 && lastDar > dit * 20) {
            //if ((toneCount < dit / 2.2 || lastDar > dit * 20) && toneCount > 80 && plock > 0.2) {
                if (p > 0.4) {
                    if (toneCount > dit) {
                        dit = dit + ((toneCount) / 10);
                    } else if (toneCount < dit) {
                        dit = dit - ((toneCount) / 10);
                    }
                    System.out.println("dit:" + dit);
                }
            }
        }
    }


    void darF() {
        canSpace = true;
        dotDarList.append("-");
        if (dotDarList.length() > 15) {
            spaceF();
        }
    }

    void ditF() {
        dotDarList.append(".");
        canSpace = true;
        if (dotDarList.length() > 15) {
            spaceF();
        }
    }

    void spaceF() {


        if (dotDarList.length() > 0) {
            if (lastDar < dit * 500) {
                textChanged = true;
                Morse morse = Morse.getMorseFromCode(dotDarList.toString());
                decodeListener.decoded(morse.getCharacter());
                canSpace = true;
            }
            dotDarList.delete(0, dotDarList.length());

        }
    }


}
