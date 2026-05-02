package com.grocery.ui.controllers;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableRow;

import com.fasterxml.jackson.databind.JsonNode;
import com.grocery.ui.MainApp;
import com.grocery.ui.services.ApiService;
import com.grocery.ui.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AdminController {

    @FXML private Label welcomeLabel;
    @FXML private VBox dashboardPane, productsPane, customersPane, ordersPane;
    @FXML private Button navDashboard, navProducts, navCustomers, navOrders;

    @FXML private Label revenueLabel, ordersCountLabel, productsCountLabel, customersCountLabel;
    @FXML private TableView<JsonNode> recentOrdersTable;
    @FXML private TableColumn<JsonNode, String> colOrderCustomer, colOrderProduct, colOrderQty, colOrderTotal, colOrderStatus, colOrderDate;

    @FXML private TableView<JsonNode> productsTable;
    @FXML private TableColumn<JsonNode, String> colProdName, colProdCategory, colProdPrice, colProdQty, colProdDesc, colProdActions;
    @FXML private TextField productSearchField;

    @FXML private TableView<JsonNode> customersTable;
    @FXML private TableColumn<JsonNode, String> colCustName, colCustEmail, colCustBalance, colCustStatus, colCustActions;

    @FXML private TableView<JsonNode> ordersTable;
    @FXML private TableColumn<JsonNode, String> colOrdCustomer, colOrdProduct, colOrdQty, colOrdTotal, colOrdStatus, colOrdDate;

    private ObservableList<JsonNode> allProducts = FXCollections.observableArrayList();
    private ObservableList<JsonNode> allCustomers = FXCollections.observableArrayList();
    private ObservableList<JsonNode> allOrders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Hello, " + SessionManager.getFullName());
        setupOrderColumns(colOrderCustomer, colOrderProduct, colOrderQty, colOrderTotal, colOrderStatus, colOrderDate, recentOrdersTable);
        setupOrderColumns(colOrdCustomer, colOrdProduct, colOrdQty, colOrdTotal, colOrdStatus, colOrdDate, ordersTable);
        setupProductsTable();
        setupCustomersTable();
        loadAllData();
    }

    private void setupOrderColumns(TableColumn<JsonNode, String> custCol, TableColumn<JsonNode, String> prodCol,
                                    TableColumn<JsonNode, String> qtyCol, TableColumn<JsonNode, String> totalCol,
                                    TableColumn<JsonNode, String> statusCol, TableColumn<JsonNode, String> dateCol,
                                    TableView<JsonNode> table) {
        custCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("userFullName").asText()));

        prodCol.setText("Receipt #");
        qtyCol.setText("Items");

        prodCol.setCellValueFactory(d ->
                new SimpleStringProperty("#" + d.getValue().path("id").asText())
        );

        qtyCol.setCellValueFactory(d -> {
            JsonNode items = d.getValue().path("items");

            if (!items.isArray()) {
                return new SimpleStringProperty("0");
            }

            return new SimpleStringProperty(String.valueOf(items.size()));
        });

        totalCol.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().path("totalPrice").asDouble())));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("status").asText()));
        dateCol.setCellValueFactory(d -> {
            String raw = d.getValue().path("createdAt").asText();
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
            } catch (Exception e) {
                return new SimpleStringProperty(raw);
            }
        });

        table.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showReceiptDetails(row.getItem());
                }
            });

            return row;
        });

    }

    private void showReceiptDetails(JsonNode order) {
        StringBuilder details = new StringBuilder();

        details.append("Customer: ")
                .append(order.path("userFullName").asText())
                .append("\n\n");

        JsonNode items = order.path("items");

        if (items.isArray()) {
            for (JsonNode item : items) {
                String name = item.path("productName").asText();
                int quantity = item.path("quantity").asInt();
                double priceEach = item.path("priceEach").asDouble();
                double totalPrice = item.path("totalPrice").asDouble();

                details.append(name)
                        .append("\n")
                        .append("Qty: ").append(quantity)
                        .append(" | Each: $").append(String.format("%.2f", priceEach))
                        .append(" | Total: $").append(String.format("%.2f", totalPrice))
                        .append("\n\n");
            }
        }

        details.append("Order Total: $")
                .append(String.format("%.2f", order.path("totalPrice").asDouble()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt Details");
        alert.setHeaderText("Receipt #" + order.path("id").asText());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void setupProductsTable() {

        productsTable.setEditable(true);

        colProdName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("name").asText()));
        colProdCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("category").asText()));
        colProdPrice.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().path("price").asDouble())));
        colProdQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().path("quantity").asInt())));
        colProdDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("description").asText()));

        colProdName.setCellFactory(TextFieldTableCell.forTableColumn());
        colProdCategory.setCellFactory(TextFieldTableCell.forTableColumn());
        colProdPrice.setCellFactory(TextFieldTableCell.forTableColumn());
        colProdQty.setCellFactory(TextFieldTableCell.forTableColumn());
        colProdDesc.setCellFactory(TextFieldTableCell.forTableColumn());

        colProdName.setOnEditCommit(e -> updateField(e.getRowValue(), "name", e.getNewValue()));
        colProdCategory.setOnEditCommit(e -> updateField(e.getRowValue(), "category", e.getNewValue()));

        colProdPrice.setOnEditCommit(e -> {
            try {
                String val = e.getNewValue().replace("$", "");
                double price = Double.parseDouble(val);
                updateField(e.getRowValue(), "price", price);
            } catch (Exception ex) {
                showAlert("Error", "Invalid price");
            }
        });

        colProdQty.setOnEditCommit(e -> {
            try {
                int qty = Integer.parseInt(e.getNewValue());
                updateField(e.getRowValue(), "quantity", qty);
            } catch (Exception ex) {
                showAlert("Error", "Invalid quantity");
            }
        });

        colProdDesc.setOnEditCommit(e -> updateField(e.getRowValue(), "description", e.getNewValue()));

        productsTable.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    productsTable.edit(row.getIndex(), colProdName);
                }
            });
            return row;
        });

        colProdActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                delBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size())
                        showProductDialog(getTableView().getItems().get(getIndex()));
                });

                delBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size())
                        deleteProduct(getTableView().getItems().get(getIndex()).get("id").asLong());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupCustomersTable() {
        colCustName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("fullName").asText()));
        colCustEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("email").asText()));
        colCustBalance.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().path("balance").asDouble())));
        colCustStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("enabled").asBoolean() ? "Active" : "Disabled"));
        colCustActions.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button("Disable");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, toggleBtn, delBtn);
            {
                toggleBtn.getStyleClass().add("btn-edit");
                delBtn.getStyleClass().add("btn-delete");
                toggleBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        JsonNode user = getTableView().getItems().get(getIndex());
                        toggleCustomer(user.get("id").asLong());
                    }
                });
                delBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        JsonNode user = getTableView().getItems().get(getIndex());
                        deleteCustomer(user.get("id").asLong());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getIndex() < getTableView().getItems().size()) {
                    boolean enabled = getTableView().getItems().get(getIndex()).path("enabled").asBoolean();
                    toggleBtn.setText(enabled ? "Disable" : "Enable");
                }
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadAllData() {
        new Thread(() -> {
            try {
                JsonNode revenue = ApiService.get("/orders/revenue");
                JsonNode orders = ApiService.get("/orders");
                JsonNode products = ApiService.get("/products");
                JsonNode customers = ApiService.get("/users/customers");

                ObservableList<JsonNode> orderList = FXCollections.observableArrayList();
                orders.forEach(orderList::add);

                ObservableList<JsonNode> recentList = FXCollections.observableArrayList();
                int limit = Math.min(orderList.size(), 10);
                for (int i = 0; i < limit; i++) recentList.add(orderList.get(i));

                ObservableList<JsonNode> productList = FXCollections.observableArrayList();
                products.forEach(productList::add);

                ObservableList<JsonNode> customerList = FXCollections.observableArrayList();
                customers.forEach(customerList::add);

                Platform.runLater(() -> {
                    allProducts = productList;
                    allCustomers = customerList;
                    allOrders = orderList;

                    revenueLabel.setText("$" + String.format("%.2f", revenue.path("totalRevenue").asDouble()));
                    ordersCountLabel.setText(String.valueOf(revenue.path("totalOrders").asInt()));
                    productsCountLabel.setText(String.valueOf(productList.size()));
                    customersCountLabel.setText(String.valueOf(customerList.size()));

                    recentOrdersTable.setItems(recentList);
                    productsTable.setItems(productList);
                    customersTable.setItems(customerList);
                    ordersTable.setItems(orderList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load data: " + e.getMessage()));
            }
        }).start();
    }

    @FXML public void showDashboard() { switchPane(dashboardPane); setActive(navDashboard); loadAllData(); }
    @FXML public void showProducts() { switchPane(productsPane); setActive(navProducts); }
    @FXML public void showCustomers() { switchPane(customersPane); setActive(navCustomers); }
    @FXML public void showOrders() { switchPane(ordersPane); setActive(navOrders); }

    @FXML
    public void searchProducts() {
        String q = productSearchField.getText().toLowerCase();
        if (q.isEmpty()) {
            productsTable.setItems(allProducts);
        } else {
            ObservableList<JsonNode> filtered = FXCollections.observableArrayList();
            allProducts.forEach(p -> {
                if (p.path("name").asText().toLowerCase().contains(q) ||
                    p.path("category").asText().toLowerCase().contains(q)) {
                    filtered.add(p);
                }
            });
            productsTable.setItems(filtered);
        }
    }

    @FXML
    public void showAddProduct() {
        showProductDialog(null);
    }

    private void showProductDialog(JsonNode product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Add Product" : "Edit Product");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setPrefWidth(360);

        TextField nameF = new TextField(product == null ? "" : product.path("name").asText());
        nameF.setPromptText("Product name");
        nameF.getStyleClass().add("login-field");

        ComboBox<String> catF = new ComboBox<>(FXCollections.observableArrayList(
                "Produce", "Dairy", "Meat", "Seafood", "Bakery", "Frozen", "Beverages", "Pantry"));
        catF.getStyleClass().add("combo-field");
        catF.setValue(product == null ? "Produce" : product.path("category").asText());
        catF.setMaxWidth(Double.MAX_VALUE);

        TextField priceF = new TextField(product == null ? "" : String.valueOf(product.path("price").asDouble()));
        priceF.setPromptText("Price");
        priceF.getStyleClass().add("login-field");

        TextField qtyF = new TextField(product == null ? "" : String.valueOf(product.path("quantity").asInt()));
        qtyF.setPromptText("Quantity");
        qtyF.getStyleClass().add("login-field");

        TextField descF = new TextField(product == null ? "" : product.path("description").asText());
        descF.setPromptText("Description");
        descF.getStyleClass().add("login-field");

        /**
         * Implementing and testing ImagePath @jomarLub17
         */

        TextField imageF = new TextField(product == null ? "" : product.path("imagePath").asText());
        imageF.setPromptText("No image selected");
        imageF.getStyleClass().add("login-field");
        imageF.setEditable(false);

        Button browseBtn = new Button("Browse...");
        browseBtn.getStyleClass().add("btn-edit");
        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Product Image");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = chooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    Path dest = Paths.get("src/main/resources/com/grocery/ui/images/" + file.getName());
                    Files.createDirectories(dest.getParent());
                    Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    imageF.setText("images/" + file.getName());
                } catch (Exception ex) {
                    showAlert("Error", "Could not copy image: " + ex.getMessage());
                }
            }
        });

        HBox imageRow = new HBox(8, imageF, browseBtn);

        form.getChildren().addAll(
            fieldLabel("Name"), nameF,
            fieldLabel("Category"), catF,
            fieldLabel("Price ($)"), priceF,
            fieldLabel("Quantity"), qtyF,
            fieldLabel("Description"), descF,
            fieldLabel("Image"), imageRow
        );
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameF.getText().trim();
            String priceStr = priceF.getText().trim();
            String qtyStr = qtyF.getText().trim();
            if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                showAlert("Validation", "Name, price and quantity are required.");
                return;
            }
            try {
                double price = Double.parseDouble(priceStr);
                int qty = Integer.parseInt(qtyStr);
                Map<String, Object> body = Map.of(
                    "name", name,
                    "category", catF.getValue(),
                    "price", price,
                    "quantity", qty,
                    "description", descF.getText().trim(),
                        "imagePath", imageF.getText().trim()
                );
                new Thread(() -> {
                    try {
                        if (product == null) {
                            ApiService.post("/products", body);
                        } else {
                            ApiService.put("/products/" + product.get("id").asLong(), body);
                        }
                        Platform.runLater(this::loadAllData);
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Error", "Failed to save: " + e.getMessage()));
                    }
                }).start();
            } catch (NumberFormatException e) {
                showAlert("Validation", "Price and quantity must be valid numbers.");
            }
        }
    }

    private void deleteProduct(long id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this product?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiService.delete("/products/" + id);
                        Platform.runLater(this::loadAllData);
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Error", e.getMessage()));
                    }
                }).start();
            }
        });
    }

    private void toggleCustomer(long id) {
        new Thread(() -> {
            try {
                ApiService.put("/users/" + id + "/toggle", Map.of());
                Platform.runLater(this::loadAllData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", e.getMessage()));
            }
        }).start();
    }

    private void deleteCustomer(long id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Customer");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this customer?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiService.delete("/users/" + id);
                        Platform.runLater(this::loadAllData);
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Error", e.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    public void handleLogout() {
        SessionManager.clear();
        try { MainApp.showLogin(); } catch (Exception ignored) {}
    }

    private void switchPane(VBox target) {
        dashboardPane.setVisible(false); dashboardPane.setManaged(false);
        productsPane.setVisible(false); productsPane.setManaged(false);
        customersPane.setVisible(false); customersPane.setManaged(false);
        ordersPane.setVisible(false); ordersPane.setManaged(false);
        target.setVisible(true); target.setManaged(true);
    }

    private void setActive(Button active) {
        navDashboard.getStyleClass().remove("nav-active");
        navProducts.getStyleClass().remove("nav-active");
        navCustomers.getStyleClass().remove("nav-active");
        navOrders.getStyleClass().remove("nav-active");
        if (!active.getStyleClass().contains("nav-active")) active.getStyleClass().add("nav-active");
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    private void updateField(JsonNode product, String field, Object value) {

        long id = product.get("id").asLong();

        Map<String, Object> body = Map.of(
                "name", field.equals("name") ? value : product.get("name").asText(),
                "category", field.equals("category") ? value : product.get("category").asText(),
                "price", field.equals("price") ? value : product.get("price").asDouble(),
                "quantity", field.equals("quantity") ? value : product.get("quantity").asInt(),
                "description", field.equals("description") ? value : product.get("description").asText()
        );

        new Thread(() -> {
            try {
                ApiService.put("/products/" + id, body);
                Platform.runLater(this::loadAllData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Update failed"));
            }
        }).start();
    }

}