module com.example.javaraytracer {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.javaraytracer to javafx.fxml;
    exports com.example.javaraytracer;
}