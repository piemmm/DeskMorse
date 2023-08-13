package org.prowl.deskmorse.fx;

import de.jangassen.MenuToolkit;
import de.jangassen.model.AppearanceMode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.StackPane;
import org.jfree.fx.FXGraphics2D;
import org.prowl.deskmorse.DeskMorse;
import org.prowl.deskmorse.gui.terminal.Connection;
import org.prowl.deskmorse.gui.terminal.Term;
import org.prowl.deskmorse.gui.terminal.Terminal;
import org.prowl.deskmorse.utils.Tools;

import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class DeskMorseController {

    @FXML
    MenuBar menuBar;
    @FXML
    javafx.scene.control.MenuItem preferencesMenuItem;
    @FXML
    ScrollPane theScrollPane;
    @FXML
    StackPane terminalStack;

    TerminalCanvas canvas;
    private Terminal term;
    private PipedInputStream inpis;
    private PipedOutputStream outpos;

    @FXML
    protected void onQuitAction() {
        // Ask the user if they really want to quit?

        // Quit the application
        DeskMorse.INSTANCE.quit();
    }


    @FXML
    protected void onPreferencesAction() {
        //DeskMorse.INSTANCE.showPreferences();
    }


    public void setup() {
        // SingleThreadBus.INSTANCE.register(this);

        // A little messing around with menubars for macos
        final String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().startsWith("mac")) {
            MenuToolkit tk = MenuToolkit.toolkit();

            menuBar.useSystemMenuBarProperty().set(true);
            menuBar.getMenus().add(0, tk.createDefaultApplicationMenu("DeskMorse"));
            tk.setGlobalMenuBar(menuBar);

            tk.setAppearanceMode(AppearanceMode.AUTO);
            javafx.scene.control.Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("DeskMorse");
            tk.setApplicationMenu(defaultApplicationMenu);
            Menu fileMenu = preferencesMenuItem.getParentMenu();
            fileMenu.setVisible(false);
            fileMenu.setDisable(true);
            preferencesMenuItem.getParentMenu().getItems().remove(preferencesMenuItem);
            defaultApplicationMenu.getItems().add(1, preferencesMenuItem);
            defaultApplicationMenu.getItems().add(1, new SeparatorMenuItem());
            defaultApplicationMenu.getItems().get(defaultApplicationMenu.getItems().size() - 1).setOnAction(event -> {
                DeskMorse.INSTANCE.quit();
            });

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().setAboutHandler(new AboutHandler() {
                    @Override
                    public void handleAbout(AboutEvent e) {
                        Platform.runLater(() -> {
                            //        DeskMorse.INSTANCE.showAbout();
                        });

                    }
                });
            }
        } else {
            // traditional menu bar.
        }

        configureTerminal();
        startTerminal();
    }

    public void configureTerminal() {
        terminalStack.getChildren().clear();
        term = new Terminal();
        term.setForeGround(Color.WHITE);
        float fontSize = 14;
        term.setFont("Monospaced", fontSize);

        canvas = new TerminalCanvas(term);
        terminalStack.getChildren().add(canvas);

        canvas.setHeight(2280);
        canvas.widthProperty().bind(terminalStack.widthProperty());
        Platform.runLater(() -> {
            canvas.heightProperty().bind(terminalStack.heightProperty());
        });

        Platform.runLater(() -> {
            // Initially the scroll pane to the bottom.
            theScrollPane.setVvalue(Double.MAX_VALUE);
        });

    }

    public void startTerminal() {
        inpis = new PipedInputStream();
        PipedOutputStream inpos = new PipedOutputStream();
        PipedInputStream outpis = new PipedInputStream();
        outpos = new PipedOutputStream();

        try {
            inpis.connect(inpos);
            outpis.connect(outpos);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Tools.runOnThread(() -> {
            term.start(new Connection() {
                @Override
                public InputStream getInputStream() {
                    return inpis;
                }

                @Override
                public OutputStream getOutputStream() {
                    return outpos;
                }

                @Override
                public void requestResize(Term term) {

                }

                @Override
                public void close() {

                }
            });
        });
    }

    class TerminalCanvas extends Canvas {

        private final FXGraphics2D g2;
        Terminal terminal;

        public TerminalCanvas(Terminal terminal) {
            this.terminal = terminal;
            this.g2 = new FXGraphics2D(getGraphicsContext2D());
            // Redraw canvas when size changes.
            widthProperty().addListener(e -> draw());
            heightProperty().addListener(e -> draw());

        }


        private void draw() {
            Platform.runLater(() -> {
                double width = Math.max(100, getWidth());
                double height = Math.max(100, getHeight());
                terminal.setSize((int) width, (int) height, false);

                // memory leak workaround
                GraphicsContext gc = this.getGraphicsContext2D();
                gc.clearRect(0, 0, width, height);

                this.terminal.paintComponent(g2);
            });
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
    }


}