package com.grocery.dto;

public class PurchaseRequest {
    private Long userId;
    private Long productId;
    private int quantity;
    private String checkoutId;
    private String purchaseTraceId;
    private String productName;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCheckoutId() { return checkoutId; }
    public void setCheckoutId(String checkoutId) { this.checkoutId = checkoutId; }
    public String getPurchaseTraceId() { return purchaseTraceId; }
    public void setPurchaseTraceId(String purchaseTraceId) { this.purchaseTraceId = purchaseTraceId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}
