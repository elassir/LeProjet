module org.example.leprojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens org.example.leprojet to javafx.fxml, javafx.graphics;
    opens org.example.leprojet.joueur to javafx.graphics, javafx.fxml;

    exports org.example.leprojet;
    exports org.example.leprojet.core;
    exports org.example.leprojet.common;
    exports org.example.leprojet.server;
    exports org.example.leprojet.joueur;
}