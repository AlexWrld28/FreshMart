package com.grocery.ui.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.grocery.ui.MainApp;
import com.grocery.ui.services.ApiService;
import com.grocery.ui.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CustomerController {

    @FXML private Label welcomeLabel, balanceLabel;
    @FXML private VBox shopPane, ordersPane, walletPane, cartPane, aiPane;
    @FXML private Button navShop, navOrders, navWallet, navCart, navAI;

    @FXML private TableView<JsonNode> shopTable;
    @FXML private TableColumn<JsonNode, String> colShopName, colShopCategory, colShopPrice, colShopStock, colShopDesc, colShopAction;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;

    @FXML private TableView<JsonNode> ordersTable;
    @FXML private TableColumn<JsonNode, String> colOrdProduct, colOrdQty, colOrdTotal, colOrdStatus, colOrdDate;

    @FXML private Label walletBalanceLabel, topUpMessage;
    @FXML private TextField topUpField;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartName, colCartPrice, colCartQty, colCartSubtotal, colCartAction;
    @FXML private Label cartTotalLabel, cartBadge;

    @FXML private TableView<JsonNode> aiResultTable;
    @FXML private TableColumn<JsonNode, String> colAIName, colAIQty, colAIMatch, colAIPrice, colAIAction;
    @FXML private TextField aiPromptField;
    @FXML private Label aiStatusLabel;
    @FXML private ComboBox<String> dietaryFilter;
    @FXML private TextField budgetField;

    private ObservableList<JsonNode> allProducts = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    public static class CartItem {
        private final long productId;
        private final String name;
        private final double price;
        private int quantity;
        private final int maxStock;

        public CartItem(long productId, String name, double price, int quantity, int maxStock) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.maxStock = maxStock;
        }

        public long getProductId() { return productId; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getMaxStock() { return maxStock; }
        public double getSubtotal() { return price * quantity; }
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Hello, " + SessionManager.getFullName());
        updateBalanceDisplay();
        setupShopTable();
        setupOrdersTable();
        setupCartTable();
        setupAITable();
        dietaryFilter.setItems(FXCollections.observableArrayList(
                "None", "Vegan", "Vegetarian", "Gluten-Free", "Dairy-Free", "Halal", "Kosher"
        ));
        dietaryFilter.setValue("None");
        loadProducts();
        setupCategories();
        cartBadge.setVisible(false);
        cartBadge.setManaged(false);
    }

    private void updateBalanceDisplay() {
        balanceLabel.setText("Balance: $" + String.format("%.2f", SessionManager.getBalance()));
        walletBalanceLabel.setText("$" + String.format("%.2f", SessionManager.getBalance()));
    }

    private void updateCartBadge() {
        int total = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        cartBadge.setText(total > 0 ? String.valueOf(total) : "");
        cartBadge.setVisible(total > 0);
        cartBadge.setManaged(total > 0);
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        cartTotalLabel.setText("Cart Total: $" + String.format("%.2f", total));
    }

    private void setupShopTable() {
        colShopName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("name").asText()));
        colShopCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("category").asText()));
        colShopPrice.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().path("price").asDouble())));
        colShopStock.setCellValueFactory(d -> {
            int qty = d.getValue().path("quantity").asInt();
            return new SimpleStringProperty(qty == 0 ? "Out of Stock" : String.valueOf(qty));
        });
        colShopDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("description").asText()));
        colShopAction.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("Add to Cart");
            {
                addBtn.getStyleClass().add("btn-buy");
                addBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        addToCart(getTableView().getItems().get(getIndex()));
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getIndex() < getTableView().getItems().size()) {
                    addBtn.setDisable(getTableView().getItems().get(getIndex()).path("quantity").asInt() == 0);
                }
                setGraphic(empty ? null : addBtn);
            }
        });
    }

    private void addToCart(JsonNode product) {
        long id = product.path("id").asLong();
        String name = product.path("name").asText();
        double price = product.path("price").asDouble();
        int stock = product.path("quantity").asInt();

        for (CartItem item : cartItems) {
            if (item.getProductId() == id) {
                if (item.getQuantity() < item.getMaxStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    cartTable.refresh();
                    updateCartBadge();
                } else {
                    showAlert("Stock Limit", "No more stock available for " + name);
                }
                return;
            }
        }
        cartItems.add(new CartItem(id, name, price, 1, stock));
        cartTable.refresh();
        updateCartBadge();
    }

    private void setupCartTable() {
        colCartName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colCartPrice.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().getPrice())));
        colCartQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colCartSubtotal.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().getSubtotal())));
        colCartAction.setCellFactory(col -> new TableCell<>() {
            private final Button minusBtn = new Button("-");
            private final Button plusBtn = new Button("+");
            private final Button removeBtn = new Button("Remove");
            private final HBox box = new HBox(6, minusBtn, plusBtn, removeBtn);
            {
                minusBtn.getStyleClass().add("btn-edit");
                plusBtn.getStyleClass().add("btn-edit");
                removeBtn.getStyleClass().add("btn-delete");

                minusBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        CartItem item = getTableView().getItems().get(getIndex());
                        if (item.getQuantity() > 1) {
                            item.setQuantity(item.getQuantity() - 1);
                        } else {
                            cartItems.remove(item);
                        }
                        cartTable.refresh();
                        updateCartBadge();
                    }
                });

                plusBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        CartItem item = getTableView().getItems().get(getIndex());
                        if (item.getQuantity() < item.getMaxStock()) {
                            item.setQuantity(item.getQuantity() + 1);
                            cartTable.refresh();
                            updateCartBadge();
                        } else {
                            showAlert("Stock Limit", "No more stock available.");
                        }
                    }
                });

                removeBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        cartItems.remove(getTableView().getItems().get(getIndex()));
                        cartTable.refresh();
                        updateCartBadge();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        cartTable.setItems(cartItems);
    }

    private void setupOrdersTable() {
        colOrdProduct.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("productName").asText()));
        colOrdQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().path("quantity").asInt())));
        colOrdTotal.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().path("totalPrice").asDouble())));
        colOrdStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("status").asText()));
        colOrdDate.setCellValueFactory(d -> {
            String raw = d.getValue().path("createdAt").asText();
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")));
            } catch (Exception e) {
                return new SimpleStringProperty(raw);
            }
        });
    }

    private void setupAITable() {
        colAIName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("suggestedName").asText()));
        colAIQty.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("suggestedQuantity").asText()));
        colAIMatch.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path("matchedProductName").asText()));
        colAIPrice.setCellValueFactory(d -> {
            double price = d.getValue().path("price").asDouble();
            return new SimpleStringProperty(price > 0 ? "$" + String.format("%.2f", price) : "N/A");
        });
        colAIAction.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("Add to Cart");
            {
                addBtn.getStyleClass().add("btn-buy");
                addBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        JsonNode item = getTableView().getItems().get(getIndex());
                        if (!item.path("inStock").asBoolean()) {
                            showAlert("Out of Stock", item.path("matchedProductName").asText() + " is not available.");
                            return;
                        }
                        long productId = item.path("matchedProductId").asLong();
                        allProducts.stream()
                                .filter(p -> p.path("id").asLong() == productId)
                                .findFirst()
                                .ifPresent(p -> addToCart(p));
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getIndex() < getTableView().getItems().size()) {
                    boolean inStock = getTableView().getItems().get(getIndex()).path("inStock").asBoolean();
                    addBtn.setDisable(!inStock);
                }
                setGraphic(empty ? null : addBtn);
            }
        });
    }

    @FXML
    public void handleGenerateList() {
        String prompt = aiPromptField.getText().trim();
        if (prompt.isBlank()) {
            aiStatusLabel.setText("Please enter what you want to make or buy.");
            return;
        }

        String dietary = dietaryFilter.getValue() != null ? dietaryFilter.getValue() : "None";
        String budgetText = budgetField.getText().trim();
        Double budget = null;
        if (!budgetText.isBlank()) {
            try {
                budget = Double.parseDouble(budgetText);
            } catch (NumberFormatException e) {
                aiStatusLabel.setText("Please enter a valid budget amount.");
                return;
            }
        }

        aiStatusLabel.setText("Generating your grocery list...");
        aiResultTable.getItems().clear();

        final Double finalBudget = budget;
        new Thread(() -> {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("prompt", prompt);
                requestBody.put("dietaryFilter", dietary);
                if (finalBudget != null) requestBody.put("budget", finalBudget);

                JsonNode result = ApiService.postAI("/ai/grocery-list",requestBody);
                ObservableList<JsonNode> list = FXCollections.observableArrayList();
                result.forEach(list::add);
                Platform.runLater(() -> {
                    aiResultTable.setItems(list);
                    aiStatusLabel.setText("Found " + list.size() + " items for your list.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> aiStatusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                JsonNode products = ApiService.get("/products");
                ObservableList<JsonNode> list = FXCollections.observableArrayList();
                products.forEach(list::add);
                allProducts = list;
                Platform.runLater(() -> shopTable.setItems(list));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load products."));
            }
        }).start();
    }

    private void setupCategories() {
        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "Produce", "Dairy", "Meat", "Seafood", "Bakery", "Frozen", "Beverages", "Pantry"));
        categoryFilter.setValue("All");
    }

    @FXML
    public void filterByCategory() {
        String cat = categoryFilter.getValue();
        if (cat == null || cat.equals("All")) {
            shopTable.setItems(allProducts);
        } else {
            ObservableList<JsonNode> filtered = FXCollections.observableArrayList();
            allProducts.forEach(p -> {
                if (p.path("category").asText().equals(cat)) filtered.add(p);
            });
            shopTable.setItems(filtered);
        }
    }

    @FXML
    public void searchProducts() {
        String q = searchField.getText().toLowerCase();
        ObservableList<JsonNode> filtered = FXCollections.observableArrayList();
        allProducts.forEach(p -> {
            if (p.path("name").asText().toLowerCase().contains(q) ||
                    p.path("category").asText().toLowerCase().contains(q)) {
                filtered.add(p);
            }
        });
        shopTable.setItems(filtered);
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty. Add items before checking out.");
            return;
        }
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        if (SessionManager.getBalance() < total) {
            showAlert("Insufficient Balance",
                    "Cart total is $" + String.format("%.2f", total) +
                            " but your balance is $" + String.format("%.2f", SessionManager.getBalance()) +
                            ".\nPlease top up your wallet.");
            return;
        }
        List<CartItem> snapshot = new ArrayList<>(cartItems);
        new Thread(() -> {
            try {
                for (CartItem item : snapshot) {
                    ApiService.postWithStatus("/orders/purchase", Map.of(
                            "userId", SessionManager.getUserId(),
                            "productId", item.getProductId(),
                            "quantity", item.getQuantity()));
                }
                JsonNode userNode = ApiService.get("/users/" + SessionManager.getUserId());
                double newBalance = userNode.path("balance").asDouble();
                SessionManager.setBalance(newBalance);
                Platform.runLater(() -> {
                    cartItems.clear();
                    cartTable.refresh();
                    updateCartBadge();
                    updateBalanceDisplay();
                    loadProducts();
                    showInfo("Checkout Successful",
                            snapshot.size() + " item(s) purchased.\nTotal charged: $" +
                                    String.format("%.2f", total) +
                                    "\nNew balance: $" + String.format("%.2f", newBalance));
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Checkout Failed", e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void handleClearCart() {
        cartItems.clear();
        cartTable.refresh();
        updateCartBadge();
    }

    @FXML
    public void handleTopUp() {
        String amtStr = topUpField.getText().trim();
        try {
            double amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                topUpMessage.setText("Amount must be greater than $0.");
                topUpMessage.getStyleClass().removeAll("success-label");
                topUpMessage.getStyleClass().add("error-label");
                return;
            }
            new Thread(() -> {
                try {
                    JsonNode response = ApiService.postWithStatus("/users/topup",
                            Map.of("userId", SessionManager.getUserId(), "amount", amount));
                    double newBalance = response.path("balance").asDouble();
                    SessionManager.setBalance(newBalance);
                    Platform.runLater(() -> {
                        updateBalanceDisplay();
                        topUpField.clear();
                        topUpMessage.setText("$" + String.format("%.2f", amount) + " added successfully!");
                        topUpMessage.getStyleClass().removeAll("error-label");
                        if (!topUpMessage.getStyleClass().contains("success-label"))
                            topUpMessage.getStyleClass().add("success-label");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> topUpMessage.setText(e.getMessage()));
                }
            }).start();
        } catch (NumberFormatException e) {
            topUpMessage.setText("Please enter a valid amount.");
        }
    }

    @FXML public void showShop() { switchPane(shopPane); setActive(navShop); loadProducts(); }
    @FXML public void showCart() { switchPane(cartPane); setActive(navCart); updateCartTotal(); }
    @FXML public void showWallet() { switchPane(walletPane); setActive(navWallet); updateBalanceDisplay(); }
    @FXML public void showAI() { switchPane(aiPane); setActive(navAI); }

    @FXML
    public void showOrders() {
        switchPane(ordersPane); setActive(navOrders);
        new Thread(() -> {
            try {
                JsonNode orders = ApiService.get("/orders/user/" + SessionManager.getUserId());
                ObservableList<JsonNode> list = FXCollections.observableArrayList();
                orders.forEach(list::add);
                Platform.runLater(() -> ordersTable.setItems(list));
            } catch (Exception ignored) {}
        }).start();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clear();
        try { MainApp.showLogin(); } catch (Exception ignored) {}
    }

    private void switchPane(VBox target) {
        shopPane.setVisible(false);   shopPane.setManaged(false);
        ordersPane.setVisible(false); ordersPane.setManaged(false);
        walletPane.setVisible(false); walletPane.setManaged(false);
        cartPane.setVisible(false);   cartPane.setManaged(false);
        aiPane.setVisible(false);     aiPane.setManaged(false);
        target.setVisible(true);      target.setManaged(true);
    }

    private void setActive(Button active) {
        navShop.getStyleClass().remove("nav-active");
        navOrders.getStyleClass().remove("nav-active");
        navWallet.getStyleClass().remove("nav-active");
        navCart.getStyleClass().remove("nav-active");
        navAI.getStyleClass().remove("nav-active");
        if (!active.getStyleClass().contains("nav-active")) active.getStyleClass().add("nav-active");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
