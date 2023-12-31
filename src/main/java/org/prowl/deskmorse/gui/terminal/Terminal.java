/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
 *
 * Written by: ymnk<ymnk@jcaft.com>
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.prowl.deskmorse.gui.terminal;


import javafx.application.Platform;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;


public class Terminal implements Term {


    private final Object[] colors = {Color.black, Color.red, Color.green,
            Color.yellow, Color.blue, Color.magenta, Color.cyan, Color.white};
    OutputStream out;
    InputStream in;
    Emulator emulator = null;
    Connection connection = null;
    byte[] obuffer = new byte[3];
    private BufferedImage img;
    private BufferedImage background;
    private Graphics2D paintGraphics;
    private Graphics2D cursor_graphics;
    private Graphics2D graphics;
    private Color defaultbground = Color.black;
    private Color defaultfground = Color.white;
    private Color bground = Color.black;
    private Color fground = Color.white;
    private final Component term_area = null;
    private Font font;
    private boolean bold = false;
    private boolean underline = false;
    private boolean reverse = false;
    private int term_width = 90;
    private int term_height = 24;
    private int descent = 0;
    private int x = 0;
    private int y = 0;
    private int char_width;
    private int char_height;
    //private int line_space=0;
    private int line_space = 0;
    private int compression = 0;
    private boolean antialiasing = true;

    public Terminal() {


        setFont("Monospaced",12f);

       // setSize(getTermWidth(), getTermHeight(), true);

        clear();

        //term_area = this;


        setSize(getTermWidth(), getTermHeight(), true);

        //  setOpaque(true);
    }

    static Color toColor(Object o) {
        if (o instanceof String) {
            try {
                return Color.decode(((String) o).trim());
            } catch (NumberFormatException e) {
            }
            return Color.getColor(((String) o).trim());
        }
        if (o instanceof Color) {
            return (Color) o;
        }
        return Color.white;
    }



    public void setFont(String fname, float size) {

        font = Font.decode(fname);
       font = font.deriveFont(Font.PLAIN, size);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) (img.getGraphics());
        graphics.setFont(font);
        {
            FontMetrics fo = graphics.getFontMetrics();
            descent = fo.getDescent();
      /*
      System.out.println(fo.getDescent());
      System.out.println(fo.getAscent());
      System.out.println(fo.getLeading());
      System.out.println(fo.getHeight());
      System.out.println(fo.getMaxAscent());
      System.out.println(fo.getMaxDescent());
      System.out.println(fo.getMaxDecent());
      System.out.println(fo.getMaxAdvance());
      */
            char_width = fo.charWidth('@');
            char_height = fo.getHeight() + (line_space * 2);
            descent += line_space;
        }

        img.flush();
        graphics.dispose();

        background = new BufferedImage(char_width, char_height,
                BufferedImage.TYPE_INT_RGB);
        {
            Graphics2D foog = (Graphics2D) (background.getGraphics());
            foog.setColor(getBackGround());
            foog.fillRect(0, 0, char_width, char_height);
            foog.dispose();
        }
        setSize(getTermWidth(), getTermHeight(), true);
    }

    public void setSize(int w, int h, boolean clear) {

        //setSize(w, h);
        BufferedImage imgOrg = img;
        if (graphics != null)
            graphics.dispose();

        int column = w / getCharWidth();
        int row = h / getCharHeight();
        term_width = column;
        term_height = row;

        if (emulator != null)
            emulator.reset();

        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        graphics = (Graphics2D) (img.getGraphics());
        graphics.setFont(font);

        if (clear) {
            clear_area(0, 0, w, h);
        }

        redraw(0, 0, w, h);

        if (imgOrg != null) {
            Shape clip = graphics.getClip();
            graphics.setClip(0, 0, getTermWidth(), getTermHeight());
            graphics.drawImage(imgOrg, 0, 0, term_area);
            graphics.setClip(clip);
        }

        resetCursorGraphics();

        setAntiAliasing(antialiasing);

        if (connection != null) {
            connection.requestResize(this);
        }

        if (imgOrg != null) {
            imgOrg.flush();
            imgOrg = null;
        }
    }

    public void start(Connection connection) {
        this.connection = connection;
        in = connection.getInputStream();
        out = connection.getOutputStream();
        emulator = new EmulatorVT100(this, in);
        emulator.reset();
        emulator.start();
        clear();
        redraw(0, 0, getTermWidth(), getTermHeight());
    }

    public void stop() {
        emulator.stop();
        in = null;
        out = null;
        connection = null;
    }

    public void paintComponent(Graphics2D g) {
        this.paintGraphics = g;
        //  Platform.runLater(() -> {

        //  super.paintComponent(g);
        if (img != null && g != null) {
            g.drawImage(img, 0, 0, term_area);
        }
        //  });
    }

//    public void paint(Graphics g) {
//        super.paint(g);
//    }

//    public void processKeyEvent(KeyEvent e) {
//        //System.out.println(e);
//        int id = e.getID();
//        if (id == KeyEvent.KEY_PRESSED) {
//            keyPressed(e);
//        } else if (id == KeyEvent.KEY_RELEASED) {
//            /*keyReleased(e);*/
//        } else if (id == KeyEvent.KEY_TYPED) {
//            keyTyped(e);/*keyTyped(e);*/
//        }
//        e.consume(); // ??
//    }

    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();
        byte[] code = null;
        switch (keycode) {
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_CAPS_LOCK:
                return;
            case KeyEvent.VK_ENTER:
                code = emulator.getCodeENTER();
                break;
            case KeyEvent.VK_UP:
                code = emulator.getCodeUP();
                break;
            case KeyEvent.VK_DOWN:
                code = emulator.getCodeDOWN();
                break;
            case KeyEvent.VK_RIGHT:
                code = emulator.getCodeRIGHT();
                break;
            case KeyEvent.VK_LEFT:
                code = emulator.getCodeLEFT();
                break;
            case KeyEvent.VK_F1:
                code = emulator.getCodeF1();
                break;
            case KeyEvent.VK_F2:
                code = emulator.getCodeF2();
                break;
            case KeyEvent.VK_F3:
                code = emulator.getCodeF3();
                break;
            case KeyEvent.VK_F4:
                code = emulator.getCodeF4();
                break;
            case KeyEvent.VK_F5:
                code = emulator.getCodeF5();
                break;
            case KeyEvent.VK_F6:
                code = emulator.getCodeF6();
                break;
            case KeyEvent.VK_F7:
                code = emulator.getCodeF7();
                break;
            case KeyEvent.VK_F8:
                code = emulator.getCodeF8();
                break;
            case KeyEvent.VK_F9:
                code = emulator.getCodeF9();
                break;
            case KeyEvent.VK_F10:
                code = emulator.getCodeF10();
                break;
            case KeyEvent.VK_TAB:
                code = emulator.getCodeTAB();
                break;
        }
        if (code != null) {
            try {
                out.write(code, 0, code.length);
                out.flush();
            } catch (Exception ee) {
            }
            return;
        }

        char keychar = e.getKeyChar();
        if ((keychar & 0xff00) == 0) {
            obuffer[0] = (byte) (e.getKeyChar());
            try {
                out.write(obuffer, 0, 1);
                out.flush();
            } catch (Exception ee) {
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        char keychar = e.getKeyChar();
        if ((keychar & 0xff00) != 0) {
            char[] foo = new char[1];
            foo[0] = keychar;
            try {
                byte[] goo = new String(foo).getBytes("EUC-JP");
                out.write(goo, 0, goo.length);
                out.flush();
            } catch (Exception eee) {
            }
        }
    }

    public int getTermWidth() {
        return char_width * term_width;
    }

    public int getTermHeight() {
        return char_height * term_height;
    }

    public int getCharWidth() {
        return char_width;
    }

    public int getCharHeight() {
        return char_height;
    }

    public int getColumnCount() {
        return term_width;
    }

    public int getRowCount() {
        return term_height;
    }

    public void clear() {
        Platform.runLater(() -> {
            graphics.setColor(getBackGround());
            graphics.fillRect(0, 0, char_width * term_width, char_height * term_height);
            graphics.setColor(getForeGround());
        });
    }

    public void setCursor(int x, int y) {
        //System.out.println("setCursor: "+x+","+y);
        this.x = x;
        this.y = y;
    }

    public void draw_cursor() {
        //Platform.runLater(() -> {
        cursor_graphics.fillRect(x, y - char_height, char_width, char_height);
        //});
        // repaint(x, y - char_height, char_width, char_height);
    }

    public void redraw(int x, int y, int width, int height) {
        if (paintGraphics != null) {
            Platform.runLater(() -> {
                paintComponent(paintGraphics);
            });
        }
    }

    public void clear_area(int x1, int y1, int x2, int y2) {
        //System.out.println("clear_area: "+x1+" "+y1+" "+x2+" "+y2);
        // Platform.runLater(() -> {
        graphics.setColor(getBackGround());
        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
        graphics.setColor(getForeGround());
        // });
    }

    //  public void keyPressed(KeyEvent event){}

    public void scroll_area(int x, int y, int w, int h, int dx, int dy) {
        //System.out.println("scroll_area: "+x+" "+y+" "+w+" "+h+" "+dx+" "+dy);
        //    Platform.runLater(() -> {

        graphics.copyArea(x, y, w, h, dx, dy);
        //  });
        // repaint(x + dx, y + dy, w, h);
    }

    public void drawBytes(byte[] buf, int s, int len, int x, int y) {
        //    clear_area(x, y, x+len*char_width, y+char_height);
        //    graphics.setColor(getForeGround());

        //System.out.println("drawString: "+x+","+y+" "+len+" "+new String(buf, s, len));
        //  Platform.runLater(() -> {

        graphics.drawBytes(buf, s, len, x, y - descent);
        if (bold)
            graphics.drawBytes(buf, s, len, x + 1, y - descent);

        if (underline) {
            graphics.drawLine(x, y - 1, x + len * char_width, y - 1);
        }
        //   });

    }

    public void drawString(String str, int x, int y) {
        //    clear_area(x, y, x+str.length()*char_width, y+char_height);
        //    graphics.setColor(getForeGround());
        // Platform.runLater(() -> {

        graphics.drawString(str, x, y - descent);
        if (bold)
            graphics.drawString(str, x + 1, y - descent);

        if (underline) {
            graphics.drawLine(x, y - 1, x + str.length() * char_width, y - 1);
        }
        //   });

    }

    public void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Ignores key released events.
     */
    public void keyReleased(KeyEvent event) {
    }

    public void setLineSpace(int foo) {
        this.line_space = foo;
    }

    public boolean getAntiAliasing() {
        return antialiasing;
    }

    public void setAntiAliasing(boolean foo) {
        if (graphics == null)
            return;
        antialiasing = foo;
        Object mode = foo ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING, mode);
        hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHints(hints);
    }

    public void setCompression(int compression) {
        if (compression < 0 || 9 < compression)
            return;
        this.compression = compression;
    }

    public void setDefaultForeGround(Object f) {
        defaultfground = toColor(f);
    }

    public void setDefaultBackGround(Object f) {
        defaultbground = toColor(f);
    }

    private Color getForeGround() {
        if (reverse)
            return bground;
        return fground;
    }

    public void setForeGround(Object f) {
        fground = toColor(f);
        graphics.setColor(getForeGround());
    }

    private Color getBackGround() {
        if (reverse)
            return fground;
        return bground;
    }

    public void setBackGround(Object b) {
        // Platform.runLater(() -> {

        bground = toColor(b);
        Graphics2D foog = (Graphics2D) (background.getGraphics());
        foog.setColor(getBackGround());
        foog.fillRect(0, 0, char_width, char_height);
        foog.dispose();
        //  });
    }

    void resetCursorGraphics() {

        if (cursor_graphics != null)
            cursor_graphics.dispose();

        cursor_graphics = (Graphics2D) (img.getGraphics());
        cursor_graphics.setColor(getForeGround());
        cursor_graphics.setXORMode(getBackGround());
    }

    public Object getColor(int index) {
        if (colors == null || index < 0 || colors.length <= index)
            return null;
        return colors[index];
    }

    public void setBold() {
        bold = true;
    }

    public void setUnderline() {
        underline = true;
    }

    public void setReverse() {
        reverse = true;
        if (graphics != null)
            graphics.setColor(getForeGround());
    }

    public void resetAllAttributes() {
        bold = false;
        underline = false;
        reverse = false;
        bground = defaultbground;
        fground = defaultfground;
        if (graphics != null)
            graphics.setColor(getForeGround());
    }
}
