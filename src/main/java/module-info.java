module org.prowl.deskmorse {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens org.prowl.deskmorse to javafx.fxml;
    exports org.prowl.deskmorse;
    exports org.prowl.deskmorse.fx;
    opens org.prowl.deskmorse.fx to javafx.fxml;
}