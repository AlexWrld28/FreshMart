module com.grocery.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens com.grocery.ui to javafx.fxml;
    opens com.grocery.ui.controllers to javafx.fxml;
    opens com.grocery.ui.util to javafx.fxml;
    opens com.grocery.ui.services to javafx.fxml;

    exports com.grocery.ui;
    exports com.grocery.ui.controllers;
}
