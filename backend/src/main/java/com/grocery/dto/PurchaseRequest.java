package com.grocery.dto;

import java.util.List;

public class PurchaseRequest {

    private Long userId;
    private List<PurchaseItemRequest> items;
    private String checkoutId;
    private String purchaseTraceId;

    public Long getUserId() {
        return userId;
    }

    public List<PurchaseItemRequest> getItems() {
        return items;
    }

    public String getCheckoutId() {
        return checkoutId;
    }

    public String getPurchaseTraceId() {
        return purchaseTraceId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setItems(List<PurchaseItemRequest> items) {
        this.items = items;
    }

    public void setCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
    }

    public void setPurchaseTraceId(String purchaseTraceId) {
        this.purchaseTraceId = purchaseTraceId;
    }
}
