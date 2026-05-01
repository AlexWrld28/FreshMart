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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.layout.FlowPane;

public class CustomerController {

    @FXML private StackPane rootStack;

    @FXML private Label welcomeLabel, balanceLabel;
    @FXML private VBox shopPane, ordersPane, walletPane, cartPane;
    @FXML private Button navShop, navOrders, navWallet, navCart;

    @FXML private FlowPane productGrid; // Replaced TableView components with this line
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;

    @FXML private TableView<JsonNode> ordersTable;
    @FXML private TableColumn<JsonNode, String> colOrdProduct, colOrdQty, colOrdTotal, colOrdStatus, colOrdDate;

    @FXML private Label walletBalanceLabel, topUpMessage;
    @FXML private TextField topUpField;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartName, colCartPrice, colCartQty, colCartSubtotal, colCartAction;
    @FXML private Label cartTotalLabel, cartBadge;

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
//        setupShopTable(); no longer necessary
        setupOrdersTable();
        setupCartTable();
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

    private void buildProductGrid(ObservableList<JsonNode> products) {
        productGrid.getChildren().clear();
        for (JsonNode product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(JsonNode product) {
        String name = product.path("name").asText();
        String category = product.path("category").asText();
        double price = product.path("price").asDouble();
        int stock = product.path("quantity").asInt();
        boolean outOfStock = stock == 0;

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.getStyleClass().add("product-image-placeholder");
        imagePlaceholder.setPrefSize(160, 120);
        imagePlaceholder.setAlignment(javafx.geometry.Pos.CENTER);
        Label emoji = new Label("🛒");
        emoji.setStyle("-fx-font-size: 40px;");
        imagePlaceholder.getChildren().add(emoji);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-card-name");
        nameLabel.setWrapText(true);

        Label catLabel = new Label(category);
        catLabel.getStyleClass().add("product-card-category");

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.getStyleClass().add("product-card-price");

        Label stockLabel = new Label(outOfStock ? "Out of Stock" : "In Stock: " + stock);
        stockLabel.getStyleClass().add(outOfStock ? "product-card-out" : "product-card-stock");

        Button addBtn = new Button("Add to Cart");
        addBtn.getStyleClass().add("btn-buy");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setDisable(outOfStock);
        addBtn.setOnAction(e -> addToCart(product));

        VBox card = new VBox(8, imagePlaceholder, nameLabel, catLabel, priceLabel, stockLabel, addBtn);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180);
        return card;
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
        colOrdProduct.setText("Receipt #");
        colOrdQty.setText("Items");

        colOrdProduct.setCellValueFactory(d ->
                new SimpleStringProperty("#" + d.getValue().path("id").asText())
        );

        colOrdQty.setCellValueFactory(d -> {
            JsonNode items = d.getValue().path("items");

            if (!items.isArray()) {
                return new SimpleStringProperty("0");
            }

            return new SimpleStringProperty(String.valueOf(items.size()));
        });

        colOrdTotal.setCellValueFactory(d ->
                new SimpleStringProperty("$" + String.format("%.2f",
                        d.getValue().path("totalPrice").asDouble()))
        );

        colOrdStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("status").asText())
        );

        colOrdDate.setCellValueFactory(d -> {
            String raw = d.getValue().path("createdAt").asText();
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")));
            } catch (Exception e) {
                return new SimpleStringProperty(raw);
            }
        });

        ordersTable.setRowFactory(tv -> {
                    TableRow<JsonNode> row = new TableRow<>();

                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            showReceiptDetails(row.getItem());
                        }
                    });

                    return row;
        });
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                JsonNode products = ApiService.get("/products");
                ObservableList<JsonNode> list = FXCollections.observableArrayList();
                products.forEach(list::add);
                allProducts = list;
                Platform.runLater(() -> buildProductGrid(list)); // update change from table to Grid
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
            buildProductGrid(filtered);// refactoring all shopTables to productGrid
        } else {
            ObservableList<JsonNode> filtered = FXCollections.observableArrayList();
            allProducts.forEach(p -> {
                if (p.path("category").asText().equals(cat)) filtered.add(p);
            });
            buildProductGrid(filtered);// manual refactor
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
        buildProductGrid(filtered);// manula refactor
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
                List<Map<String, Object>> itemsList = new ArrayList<>();

                for (CartItem item : snapshot) {
                    itemsList.add(Map.of(
                            "productId", item.getProductId(),
                            "quantity", item.getQuantity()
                    ));
                }

                Map<String, Object> requestBody = Map.of(
                        "userId", SessionManager.getUserId(),
                        "items", itemsList
                );

                System.out.println("CHECKOUT REQUEST BODY = " + requestBody);

                ApiService.postWithStatus("/orders/purchase", requestBody);

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

    private void showReceiptDetails(JsonNode order) {

        // Dark background overlay
        VBox overlay = new VBox();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlay.setAlignment(Pos.CENTER);

        // Receipt card
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setMaxWidth(400);
        card.getStyleClass().add("card");

        Label title = new Label("Receipt #" + order.path("id").asText());
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Order Details");
        subtitle.getStyleClass().add("stat-label");

        VBox itemsBox = new VBox(10);

        JsonNode items = order.path("items");

        if (items.isArray()) {
            for (JsonNode item : items) {
                VBox itemBox = new VBox(4);
                itemBox.getStyleClass().add("stat-card");

                Label nameLabel = new Label(item.path("productName").asText());
                nameLabel.getStyleClass().add("section-title");

                Label detailsLabel = new Label(
                        "Qty: " + item.path("quantity").asInt()
                        + " | Each: $" + String.format("%.2f", item.path("priceEach").asDouble())
                        + " | Total: $" + String.format("%.2f", item.path("totalPrice").asDouble())
                );
                detailsLabel.getStyleClass().add("stat-label");

                itemBox.getChildren().addAll(nameLabel, detailsLabel);

                itemsBox.getChildren().add(itemBox);
            }
        }

        Label total = new Label("Total: $" +
                String.format("%.2f", order.path("totalPrice").asDouble()));
        total.getStyleClass().add("stat-value-green");

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-primary");
        closeBtn.setOnAction(e -> rootStack.getChildren().remove(overlay));

        card.getChildren().addAll(title, subtitle, new Separator(), itemsBox, total, closeBtn);

        overlay.getChildren().add(card);

        // Click outside to close
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                rootStack.getChildren().remove(overlay);
            }
        });

        rootStack.getChildren().add(overlay);
    }

    @FXML
    public void handleLogout() {
        SessionManager.clear();
        try { MainApp.showLogin(); } catch (Exception ignored) {}
    }

    private void switchPane(VBox target) {
        shopPane.setVisible(false); shopPane.setManaged(false);
        ordersPane.setVisible(false); ordersPane.setManaged(false);
        walletPane.setVisible(false); walletPane.setManaged(false);
        cartPane.setVisible(false); cartPane.setManaged(false);
        target.setVisible(true); target.setManaged(true);
    }

    private void setActive(Button active) {
        navShop.getStyleClass().remove("nav-active");
        navOrders.getStyleClass().remove("nav-active");
        navWallet.getStyleClass().remove("nav-active");
        navCart.getStyleClass().remove("nav-active");
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
