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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        new Thread(() -> {
            try {
                JsonNode response = ApiService.postWithStatus("/auth/login",
                        Map.of("email", email, "password", password));
                String role = response.get("role").asText();
                long id = response.get("id").asLong();
                String name = response.get("fullName").asText();
                String userEmail = response.get("email").asText();
                double balance = response.get("balance").asDouble();

                SessionManager.setUser(id, name, userEmail, role, balance);

                Platform.runLater(() -> {
                    try {
                        if ("ADMIN".equals(role)) {
                            MainApp.showAdminDashboard();
                        } else {
                            MainApp.showCustomerDashboard();
                        }
                    } catch (Exception e) {
                        errorLabel.setText("Navigation error: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText(e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            MainApp.primaryStage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Error loading register page.");
        }
    }
}
