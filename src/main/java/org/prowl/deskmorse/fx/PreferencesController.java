package org.prowl.deskmorse.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prowl.deskmorse.config.Conf;
import org.prowl.deskmorse.config.Config;
import org.prowl.deskmorse.eventbus.SingleThreadBus;
import org.prowl.deskmorse.eventbus.events.ConfigurationChangedEvent;

import java.util.List;
import java.util.Locale;

public class PreferencesController {

    private static final Log LOG = LogFactory.getLog("PreferencesController");
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @FXML
    private TextField stationCallsign;


    // APRS


    private Config config;

    private int[] FONT_SIZES = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};

    private int[] NETROM_SSID = {-1, -2, -3, -4, -5, -6, -7};

    private int[] MAILBOX_SSID = {-1, -2, -3, -4, -5, -6, -7};


    @FXML
    public void onCancelButtonClicked() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    @FXML
    public void onSaveButtonClicked() {

        // General
        config.setProperty(Conf.callsign, stationCallsign.getText());

        // Interfaces preference pane
        // Save our preferences config
        config.saveConfig();

        // Probably post a configChanged event here.
        ((Stage) saveButton.getScene().getWindow()).close();

        // Notify anything interested
        SingleThreadBus.INSTANCE.post(new ConfigurationChangedEvent());
    }

    /**
     * Return the configuration that we are modifying.
     *
     * @return
     */
    Config getPreferencesConfiguration() {
        return config;
    }


    public void setup() {

        config = new Config(); // Load a new config we can modify without comitting.

        updateList();
        updateControls();
    }

    public void updateControls() {
        // General preference pane
        stationCallsign.setText(config.getConfig(Conf.callsign, Conf.callsign.stringDefault()).toUpperCase(Locale.ENGLISH));
    }


    public void updateList() {

    }


    /**
     * Remove a uuid from the config only - this is used for cleanup of any classes that are removed from app.
     *
     * @param uuid
     */
    public void removeInterface(String uuid) {
        HierarchicalConfiguration interfacesNode = config.getConfig("interfaces");
        // Get a list of all interfaces
        List<HierarchicalConfiguration> interfaceList = interfacesNode.configurationsAt("interface");
        // Get the one with the correct UUID
        for (HierarchicalConfiguration interfaceNode : interfaceList) {
            if (interfaceNode.getString("uuid").equals(uuid)) {
                // Remove the interface node from the interfaces node.
                config.getConfig("interfaces").getRootNode().removeChild(interfaceNode.getRootNode());
                break;
            }
        }
    }
}
