module com.example.javaraytracer {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens Core to javafx.fxml;
    exports Core;
}