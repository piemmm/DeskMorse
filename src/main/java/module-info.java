module org.prowl.deskmorse {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.jfree.fxgraphics2d;
    requires javafx.swing;
    requires java.logging;
    requires commons.logging;
    requires commons.configuration;
    requires java.prefs;
    requires commons.lang;
    requires atlantafx.base;
    requires java.sql;
    requires nsmenufx;
    requires com.jthemedetector;
    requires versioncompare;
    requires com.google.common;
    requires javafx.web;
    requires jsyn;

    opens org.prowl.deskmorse to javafx.fxml;
    exports org.prowl.deskmorse;
    exports org.prowl.deskmorse.fx;
    opens org.prowl.deskmorse.fx to javafx.fxml;
}