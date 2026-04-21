package org.example.grocery.service;

import org.example.grocery.model.StoreEvent;

@FunctionalInterface
public interface StoreListener {
    void onEvent(StoreEvent event);
}
