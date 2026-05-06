package com.grocery.ui.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.grocery.ui.MainApp;
import com.grocery.ui.services.ApiService;
import com.grocery.ui.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Map;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private TextField passwordVisible;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button togglePassword;
    @FXML private Button toggleConfirmPassword;
    @FXML private Label reqLength, reqUpper, reqNumber, reqSpecial;

    @FXML
    public void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.isVisible()
                ? passwordField.getText().trim()
                : passwordVisible.getText().trim();
        String confirmPassword = confirmPasswordField.isVisible()
                ? confirmPasswordField.getText().trim()
                : confirmPasswordVisible.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }


        if (password.length() < 8 || !password.matches(".*[A-Z].*") ||
                !password.matches(".*[0-9].*") || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            errorLabel.setText("Password does not meet all requirements.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        new Thread(() -> {
            try {
                JsonNode response = ApiService.postWithStatus("/auth/register",
                        Map.of("fullName", name, "email", email, "password", password, "confirmPassword", confirmPassword));

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
    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkRequirements(newVal);
            if (passwordVisible.isVisible()) passwordVisible.setText(newVal);
        });
        passwordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            checkRequirements(newVal);
            if (passwordField.isVisible()) passwordField.setText(newVal);
        });
    }

    private void checkRequirements(String password) {
        setReq(reqLength, password.length() >= 8, "At least 8 characters");
        setReq(reqUpper, password.matches(".*[A-Z].*"), "One uppercase letter");
        setReq(reqNumber, password.matches(".*[0-9].*"), "One number");
        setReq(reqSpecial, password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"), "One special character (!@#$...)");
    }

    private void setReq(Label label, boolean met, String text) {
        label.setText(met ? "✓  " + text : "✗  " + text);
        label.getStyleClass().removeAll("req-met", "req-unmet");
        label.getStyleClass().add(met ? "req-met" : "req-unmet");
    }

    @FXML
    public void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordVisible.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            togglePassword.setText("Hide");
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePassword.setText("Show");
        }
    }

    @FXML
    public void toggleConfirmPasswordVisibility() {
        if (confirmPasswordField.isVisible()) {
            confirmPasswordVisible.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordVisible.setVisible(true);
            confirmPasswordVisible.setManaged(true);
            toggleConfirmPassword.setText("Hide");
        } else {
            confirmPasswordField.setText(confirmPasswordVisible.getText());
            confirmPasswordVisible.setVisible(false);
            confirmPasswordVisible.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            toggleConfirmPassword.setText("Show");
        }
    }
}
