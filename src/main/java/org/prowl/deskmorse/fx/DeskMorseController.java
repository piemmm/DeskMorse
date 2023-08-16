package org.prowl.deskmorse.fx;

import de.jangassen.MenuToolkit;
import de.jangassen.model.AppearanceMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.fx.FXGraphics2D;
import org.prowl.deskmorse.DeskMorse;
import org.prowl.deskmorse.config.*;
import org.prowl.deskmorse.generators.Practice;
import org.prowl.deskmorse.generators.PracticeGenerator;
import org.prowl.deskmorse.gui.terminal.Connection;
import org.prowl.deskmorse.gui.terminal.Term;
import org.prowl.deskmorse.gui.terminal.Terminal;
import org.prowl.deskmorse.input.DecodeListener;
import org.prowl.deskmorse.input.mousemorse.MouseInput;
import org.prowl.deskmorse.output.MorseOutput;
import org.prowl.deskmorse.output.sound.QRM;
import org.prowl.deskmorse.utils.PipedIOStream;
import org.prowl.deskmorse.utils.Tools;

import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class DeskMorseController implements DecodeListener {

    private static final Log LOG = LogFactory.getLog("DeskMorseController");


    @FXML
    MenuBar menuBar;
    @FXML
    javafx.scene.control.MenuItem preferencesMenuItem;
    @FXML
    ScrollPane theScrollPane;
    @FXML
    StackPane terminalStack;
    @FXML
    TextField sendTextField;
    @FXML
    TextField statusBox;
    @FXML
    Slider morseVolume;
    @FXML
    Slider handMorseSendingSkill;
    @FXML
    Slider morsePitch;
    @FXML
    Slider qrmGeneratorVolume;
    @FXML
    Spinner speedWPM;
    @FXML
    Spinner noGroups;
    @FXML
    Spinner groupLength;
    @FXML
    Button startButton;
    @FXML
    Button stopButton;
    @FXML
    Button mouseMorse;
    @FXML
    ChoiceBox sending;
    @FXML
    ChoiceBox<MorseCodeType> codeType;
    @FXML
    ChoiceBox<NoiseGeneratorType> noiseGenerator;

    TerminalCanvas canvas;
    private double volume = 1;
    private Terminal term;
    private PipedIOStream inpis;
    private OutputStream inpos;
    private boolean stopMorse = false;
    private MouseInput mouseInput = new MouseInput();
    private QRM qrmGenerator = new QRM();


    @FXML
    protected void onQuitAction() {
        // Save the config
        DeskMorse.INSTANCE.getConfig().saveConfig();

        // Quit the application
        DeskMorse.INSTANCE.quit();
    }


    @FXML
    protected void onPreferencesAction() {
        //DeskMorse.INSTANCE.showPreferences();
    }

    @FXML
    protected void onMouseMorse(MouseEvent mouseEvent) {

        // If the mouse has entered
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_ENTERED) {
            // Start the mouse input
            mouseInput.start();
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED) {
            // Stop the mouse input
            mouseInput.stop();
        }

        // If the mouse has been pressed
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            // Feed the mouse input
            mouseInput.feed(true);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            // Feed the mouse input
            mouseInput.feed(false);
        }
    }

    /**
     * More decodes appear here from the mousemorse button
     *
     * @param s
     */
    @Override
    public void decoded(String s) {
        write(s);
    }

    @FXML
    protected void onStartPressed() {
        PracticeGenerator practiceGenerator = DeskMorse.INSTANCE.getPracticeGenerator();
        SendingType sendingType = SendingType.valueOf(sending.getValue().toString());
        NoiseGeneratorType noiseGeneratorType = NoiseGeneratorType.valueOf(noiseGenerator.getValue().toString());
        MorseCodeType morseCodeType = MorseCodeType.valueOf(codeType.getValue().toString());
        int speed = (int) speedWPM.getValue();
        Practice practice = practiceGenerator.generatePractice(sendingType, noiseGeneratorType, morseCodeType, (int) noGroups.getValue(), (int) groupLength.getValue(), speed);

        // Practice must build itself.
        practice.generate();
        doMorse(practice.getText());
    }

    @FXML
    protected void onStopPressed() {
        stopMorse = true;
        stopButton.setDisable(true);
    }


    @FXML
    protected void sendText() {
        String text = sendTextField.getText().toUpperCase(Locale.ENGLISH);
        sendTextField.clear();


        doMorse(text);

    }


    @FXML
    protected void onVolumeChanged() {
        volume = morseVolume.getValue() / 100d;
    }

    private double handMorseSendingSkillV = 0;
    private double morsePitchV = 0;
    private double qrmGeneratorVolumeV = 0;
    private double speedWPMV = 0;
    private double noGroupsV = 0;
    private double groupLengthV = 0;

    @FXML
    protected void onHandMorseSendingSkillChanged() {
        handMorseSendingSkillV = handMorseSendingSkill.getValue();
    }

    @FXML
    protected void onMorsePitchChanged() {
        morsePitchV = morsePitch.getValue();
    }

    @FXML
    protected void onQRMGeneratorVolumeChanged() {
        double vol = qrmGeneratorVolume.getValue() / 100d;
        qrmGenerator.setVolume(vol);
    }

    @FXML
    protected void onSpeedWPMChanged() {
        speedWPMV = (int) speedWPM.getValue();
    }

    @FXML
    protected void onNoGroupsChanged() {
        noGroupsV = (int) noGroups.getValue();
    }

    @FXML
    protected void onGroupLengthChanged() {
        groupLengthV = (int) groupLength.getValue();
    }

    public void doMorse(String text) {
        stopMorse = false;
        startButton.setDisable(true);
        stopButton.setDisable(false);
        statusBox.setText("Sending...");
        Tools.runOnThread(() -> {

            qrmGenerator.setSource(noiseGenerator.getValue().getSource());
            qrmGenerator.start();

            try {
                MorseOutput morseOutput = DeskMorse.INSTANCE.getMorseOutput();
                morseOutput.start();
                for (char c : text.toCharArray()) {

                    morseOutput.send("" + c, (float) speedWPMV, (int) morsePitchV, volume, codeType.getValue(), handMorseSendingSkillV);
                    write("" + c);
                    flush();
                    if (stopMorse) {
                        break;
                    }
                }
                write("\r\n");
                morseOutput.stop();
                if (qrmGenerator.isRunning()) {
                    Platform.runLater(() -> {
                        statusBox.setText("Finishing...");
                    });
                    Tools.delay(3000);
                }

            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
            }
            qrmGenerator.stop();
            Platform.runLater(() -> {
                statusBox.setText("Waiting...");
                stopButton.setDisable(true);
                startButton.setDisable(false);
            });
        });
    }

    public void setup() {
        morseVolume.valueProperty().addListener((observable, oldValue, newValue) -> onVolumeChanged());
        handMorseSendingSkill.valueProperty().addListener((observable, oldValue, newValue) -> onHandMorseSendingSkillChanged());
        morsePitch.valueProperty().addListener((observable, oldValue, newValue) -> onMorsePitchChanged());
        qrmGeneratorVolume.valueProperty().addListener((observable, oldValue, newValue) -> onQRMGeneratorVolumeChanged());
        speedWPM.valueProperty().addListener((observable, oldValue, newValue) -> onSpeedWPMChanged());
        noGroups.valueProperty().addListener((observable, oldValue, newValue) -> onNoGroupsChanged());
        groupLength.valueProperty().addListener((observable, oldValue, newValue) -> onGroupLengthChanged());

        // load config
        Config conf = DeskMorse.INSTANCE.getConfig();
        morseVolume.setValue(conf.getConfig(Conf.drive, Conf.drive.doubleDefault()) * 100);
        handMorseSendingSkill.setValue(conf.getConfig(Conf.handMorseSendingSkill, Conf.handMorseSendingSkill.doubleDefault()));
        morsePitch.setValue(conf.getConfig(Conf.pitch, Conf.pitch.doubleDefault()));
        qrmGeneratorVolume.setValue(conf.getConfig(Conf.qrmGeneratorVolume, Conf.qrmGeneratorVolume.doubleDefault()) * 100);
        speedWPM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, conf.getConfig(Conf.speed, Conf.speed.intDefault())));
        noGroups.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, conf.getConfig(Conf.noGgroups, Conf.noGgroups.intDefault())));
        groupLength.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, conf.getConfig(Conf.groupLength, Conf.groupLength.intDefault())));


        sending.setItems(FXCollections.observableArrayList(SendingType.values()));
        sending.setConverter(new StringConverter<SendingType>() {
            @Override
            public String toString(SendingType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }

            @Override
            public SendingType fromString(String string) {
                return SendingType.valueOf(string);
            }
        });
        sending.getSelectionModel().select(SendingType.valueOf(conf.getConfig(Conf.sendingType, Conf.sendingType.stringDefault())));

        codeType.setItems(FXCollections.observableArrayList(MorseCodeType.values()));
        codeType.setConverter(new StringConverter<MorseCodeType>() {
            @Override
            public String toString(MorseCodeType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }

            @Override
            public MorseCodeType fromString(String string) {
                return MorseCodeType.valueOf(string);
            }
        });
        codeType.getSelectionModel().select(MorseCodeType.valueOf(conf.getConfig(Conf.morseCodeType, Conf.morseCodeType.stringDefault())));

        noiseGenerator.setItems(FXCollections.observableArrayList(NoiseGeneratorType.values()));
        noiseGenerator.setConverter(new StringConverter<NoiseGeneratorType>() {
            @Override
            public String toString(NoiseGeneratorType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }

            @Override
            public NoiseGeneratorType fromString(String string) {
                return NoiseGeneratorType.valueOf(string);
            }
        });
        noiseGenerator.getSelectionModel().select(NoiseGeneratorType.valueOf(conf.getConfig(Conf.noiseGeneratorType, Conf.noiseGeneratorType.stringDefault())));

        handMorseSendingSkill.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                if (object == 0.5) {
                    return "Bad";
                } else if (object == 1) {
                    return "Good";
                } else {
                    return "";
                }
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        });

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

        DeskMorse.INSTANCE.getDecoder().setDecodeListener(this);
    }

    public void configureTerminal() {
        terminalStack.getChildren().clear();
        term = new Terminal();
        term.setDefaultBackGround(Color.BLUE.darker().darker().darker());
        term.setBackGround(Color.BLUE.darker().darker().darker());
        term.setForeGround(Color.WHITE);
        float fontSize = 14;
        term.setFont("Monospaced", fontSize);

        canvas = new TerminalCanvas(term);
        canvas.setHeight(412);
        terminalStack.getChildren().add(canvas);

        // canvas.setWidth(100);
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


        inpis = new PipedIOStream();
        inpos = inpis.getOutputStream();


        Tools.runOnThread(() -> {
            LOG.debug("Starting term");
            term.start(new Connection() {
                @Override
                public InputStream getInputStream() {
                    return inpis;
                }

                @Override
                public OutputStream getOutputStream() {
                    return null;
                }

                @Override
                public void requestResize(Term term) {

                }

                @Override
                public void close() {

                }
            });
        });

        Tools.runOnThread(() -> {

            LOG.debug("Writing text to reader");
            try {
                Tools.delay(100);
                for (int i = 0; i < 3; i++) {
                    // Ansi code to move down 100 lines
                    inpos.write("\u001b[100B".getBytes());
                    inpos.flush();
                }
                for (int i = 0; i < 30; i++) {
                    write("DeskMorse Terminal\r\n");
                    inpos.flush();
                }
            } catch (Exception e) {
                LOG.debug(e.getMessage(), e);
            }
        });
    }


    // Convenience write class
    private void write(String s) {
        try {
            inpos.write(s.getBytes());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void flush() {
        try {
            inpos.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
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