package com.grocery.ui.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.grocery.ui.MainApp;
import com.grocery.ui.services.ApiService;
import com.grocery.ui.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Map;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        new Thread(() -> {
            try {
                JsonNode response = ApiService.postWithStatus("/auth/register",
                        Map.of("fullName", name, "email", email, "password", password));

                long id = response.get("id").asLong();
                String role = response.get("role").asText();
                String userEmail = response.get("email").asText();
                double balance = response.get("balance").asDouble();
                SessionManager.setUser(id, name, userEmail, role, balance);

                Platform.runLater(() -> {
                    try {
                        MainApp.showCustomerDashboard();
                    } catch (Exception e) {
                        errorLabel.setText("Navigation error.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText(e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            MainApp.primaryStage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Error.");
        }
    }
}
