package com.grocery.dto;

import java.util.List;

public class PurchaseRequest {

    private Long userId;
    private List<PurchaseItemRequest> items;

    public Long getUserId() {
        return userId;
    }

    public List<PurchaseItemRequest> getItems() {
        return items;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setItems(List<PurchaseItemRequest> items) {
        this.items = items;
    }
}





