package org.prowl.deskmorse.utils;


import com.google.common.io.Files;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Tools {

    private static final Log LOG = LogFactory.getLog("Tools");


    public static final void runOnThread(Runnable runme) {
        Thread t = new Thread(runme);
        t.start();
    }

    /**
     * Used for things like rate limiting, avoiding spinning exception loops chewing CPU etc.
     *
     * @param millis
     */
    public static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static String byteArrayToHexString(byte[] output) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < output.length; i++) {
            hexString.append(String.format("%02X", output[i]));
            hexString.append(" ");
        }
        return hexString.toString();
    }

    /**
     * Get a list of all the monospaced fonts available on the system.
     */
    public static List<String> getMonospacedFonts() {
        List<String> found = new ArrayList<>();
        final Text text1 = new Text("i i l i l i l i l");
        final Text text2 = new Text("A B C D E F G H I");
        for (String fontFamilyName : Font.getFamilies()) {
            // Get the font
            Font font = Font.font(fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 12d);
            text1.setFont(font);
            text2.setFont(font);
            if (text1.getLayoutBounds().getWidth() == text2.getLayoutBounds().getWidth()) {
                found.add(fontFamilyName);
            }
        }
        return found;
    }

    /**
     * Return a readable string of bytes from a supplied byte array - we strip everything that isn't a printable character.
     * CR and LF are allowed through as well as printable extended ASCII.
     *
     * @param data
     * @return A human readable string.
     */
    public static String readableTextOnlyFromByteArray(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (b >= 0x20 && b <= 0xFA) {
                sb.append((char) b);
            } else if (b == 0x0D || b == 0x0A) {
                sb.append("\r\n");
            }
        }

        // Strip trailing newlines
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    public static String byteArrayToReadableASCIIString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (b < 0x20 || b > 0xFA) {
                sb.append(ANSI.YELLOW + "<");
                sb.append(String.format("%02X", b));
                sb.append(">" + ANSI.NORMAL);
            } else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    /**
     * Convenience method to read X amount of bytes from a stream
     *
     * @param din
     * @param length
     * @return
     */
    public static final String readString(DataInputStream din, int length) throws IOException {
        byte[] data = new byte[length];
        din.read(data, 0, length);
        return new String(data);
    }

    /**
     * Convenience method to read X amount of bytes from a stream
     *
     * @param din
     * @param length
     * @return
     */
    public static final byte[] readBytes(DataInputStream din, int length) throws IOException {
        byte[] data = new byte[length];
        din.read(data, 0, length);
        return data;
    }

    /**
     * Convenience method to return the sha-256 hash of a string
     *
     * @param s
     * @return
     */
    public static String hashString(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(s.getBytes());
            byte[] digest = md.digest();
            return byteArrayToHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Unpack a file from resources to the user's home directory in our app space, because javas useless media tools
     * can't handle working out what a file is when it lives in a jar file.
     */
    public static void unpackFile(Class cl,String filename) {
        try {

            File file = new File(getAppDir(), filename);
            if (file.exists()) {
                return;
            }
            Files.write(cl.getResourceAsStream(filename).readAllBytes(), file);
        } catch(Throwable e) {
            LOG.error(e.getMessage(),e);
        }
    }

    public static File getAppDir() {
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".deskmorse");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        return appDir;
    }

}
