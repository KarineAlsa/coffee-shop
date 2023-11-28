module com.example.cafeteria {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires lombok;

    opens com.example.cafeteria to javafx.fxml;
    exports com.example.cafeteria to javafx.graphics;
    exports com.example.cafeteria.models;
    exports com.example.cafeteria.hilos;
    exports com.example.cafeteria.types;
    exports com.example.cafeteria.controllers;
    exports com.example.cafeteria.monitor;
    opens com.example.cafeteria.controllers to javafx.fxml;
    exports com.example.cafeteria.publishers;
}