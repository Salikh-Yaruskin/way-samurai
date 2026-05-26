package ru.base_project.base.domain.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCart {

    private final Map<UUID, Integer> items = new LinkedHashMap<>();

    public Map<UUID, Integer> getItems() {
        return items;
    }

    public void add(UUID productId, int quantity) {
        int safeQuantity = Math.max(quantity, 1);
        items.merge(productId, safeQuantity, Integer::sum);
    }

    public void update(UUID productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
            return;
        }
        items.put(productId, quantity);
    }

    public void remove(UUID productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }
}
