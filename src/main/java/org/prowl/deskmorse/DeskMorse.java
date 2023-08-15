package org.prowl.deskmorse;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.jthemedetecor.OsThemeDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.config.Config;
import org.prowl.deskmorse.fx.DeskMorseController;
import org.prowl.deskmorse.generators.PracticeGenerator;
import org.prowl.deskmorse.input.Decoder;
import org.prowl.deskmorse.output.MorseOutput;
import org.prowl.deskmorse.output.sound.Sound;

import java.awt.*;
import java.awt.desktop.AppReopenedEvent;
import java.awt.desktop.AppReopenedListener;
import java.io.IOException;

public class DeskMorse extends Application {

    private static final Log LOG = LogFactory.getLog("DeskMorse");

    private Config configuration;
    public static DeskMorse INSTANCE;

    // The output media we will be sending morse with.
    private MorseOutput morseOutput;
    private Decoder decoder;
    private PracticeGenerator practiceGenerator;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() throws Exception {
        super.init();
        INSTANCE = DeskMorse.this;

        try {
            morseOutput = new Sound();
        } catch (Throwable e) {
            LOG.error(e.getMessage());
        }

        practiceGenerator = new PracticeGenerator();

        decoder = new Decoder();
        decoder.start();

        // Push debugging to a file if we are debugging a built app with no console
//        try {
//            File outputFile = File.createTempFile("debug", ".log", FileSystemView.getFileSystemView().getDefaultDirectory());
//            PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)), true);
//            System.setOut(output);
//            System.setErr(output);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    @Override
    public void start(Stage stage) throws IOException {

        Platform.setImplicitExit(false);
        try {

            // Find out if the system theme is a 'dark' or a 'light' theme.
            final OsThemeDetector detector = OsThemeDetector.getDetector();
            detector.registerListener(isDark -> {
                Platform.runLater(() -> {
                    if (isDark) {
                        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                    } else {
                        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                    }
                });
            });
            final boolean isDarkThemeUsed = detector.isDark();
            if (isDarkThemeUsed) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            }

            configuration = new Config();

            // Init resource bundles.
            Messages.init();

            initAll();
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            System.exit(1);
        }


        FXMLLoader fxmlLoader = new FXMLLoader(DeskMorse.class.getResource("fx/DeskMorseController.fxml"));
        Parent root = fxmlLoader.load();
        DeskMorseController controller = fxmlLoader.getController();
        Scene scene = new Scene(root, 720, 440);
        stage.setTitle("DeskMorse");
        stage.setScene(scene);
        stage.show();
        controller.setup();


        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                stage.hide();
            }
        });

        // Set the window icon
        stage.getIcons().add(new Image(DeskMorse.class.getResourceAsStream("img/icon.png")));

        // Set the taskbar icon if the OS supports it
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar task = Taskbar.getTaskbar();
                if (task.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    java.awt.Image icon = toolkit.getImage(getClass().getResource("img/icon.png"));
                    task.setIconImage(icon);
                }
            }
        } catch (SecurityException e) {
            LOG.debug(e.getMessage(), e);
        }


        // Show the main window when the dock icon is clicked.
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().addAppEventListener(new AppReopenedListener() {
                @Override
                public void appReopened(AppReopenedEvent e) {
                    Platform.runLater(() -> {
                        stage.show();
                    });
                }
            });
        }
    }


    public void initAll() {

        // Load configuration and initialise everything needed.
        configuration = new Config();

    }

    /**
     * Time to shut down
     */
    public void quit() {
        Platform.exit();
        System.exit(0);
    }

    public MorseOutput getMorseOutput() {
        return morseOutput;
    }

    public Config getConfig() {
        return configuration;
    }

    public PracticeGenerator getPracticeGenerator() {
        return practiceGenerator;
    }

    public Decoder getDecoder() {
        return decoder;
    }
}