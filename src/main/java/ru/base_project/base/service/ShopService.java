package ru.base_project.base.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.base_project.base.domain.api.ShopCategoryForm;
import ru.base_project.base.domain.api.ShopOrderForm;
import ru.base_project.base.domain.api.ShopProductForm;
import ru.base_project.base.domain.entity.ShopCategoryEntity;
import ru.base_project.base.domain.entity.ShopOrderEntity;
import ru.base_project.base.domain.entity.ShopProductEntity;
import ru.base_project.base.repository.ShopCategoryRepository;
import ru.base_project.base.repository.ShopOrderRepository;
import ru.base_project.base.repository.ShopProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopCategoryRepository categoryRepository;
    private final ShopProductRepository productRepository;
    private final ShopOrderRepository orderRepository;

    @Transactional
    public ShopCategoryEntity createCategory(ShopCategoryForm form) {
        var category = new ShopCategoryEntity();
        applyCategoryForm(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public ShopCategoryEntity updateCategory(UUID id, ShopCategoryForm form) {
        var category = getCategory(id);
        applyCategoryForm(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        var category = getCategory(id);
        category.getProducts().forEach(product -> product.getCategories().remove(category));
        category.getPrimaryProducts().forEach(product -> product.setPrimaryCategory(null));
        categoryRepository.delete(category);
    }

    @Transactional
    public ShopProductEntity createProduct(ShopProductForm form) {
        var product = new ShopProductEntity();
        applyProductForm(product, form);
        return productRepository.save(product);
    }

    @Transactional
    public ShopProductEntity updateProduct(UUID id, ShopProductForm form) {
        var product = getProduct(id);
        applyProductForm(product, form);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        var product = productRepository.findWithRelationsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        product.getCategories().forEach(category -> category.getProducts().remove(product));
        product.getOrders().forEach(order -> order.getProducts().remove(product));
        productRepository.delete(product);
    }

    @Transactional
    public void attachCategoryToProduct(UUID productId, UUID categoryId) {
        var product = productRepository.findWithRelationsById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        var category = getCategory(categoryId);
        product.getCategories().add(category);
        productRepository.save(product);
    }

    @Transactional
    public void detachCategoryFromProduct(UUID productId, UUID categoryId) {
        var product = productRepository.findWithRelationsById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        product.getCategories().removeIf(category -> category.getId().equals(categoryId));
        productRepository.save(product);
    }

    @Transactional
    public ShopOrderEntity createOrder(ShopOrderForm form) {
        var order = new ShopOrderEntity();
        applyOrderForm(order, form);
        return orderRepository.save(order);
    }

    @Transactional
    public ShopOrderEntity updateOrder(UUID id, ShopOrderForm form) {
        var order = getOrder(id);
        applyOrderForm(order, form);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(UUID id) {
        orderRepository.delete(getOrder(id));
    }

    @Transactional
    public void attachProductToOrder(UUID orderId, UUID productId) {
        var order = orderRepository.findWithProductsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
        var product = getProduct(productId);
        order.getProducts().add(product);
        orderRepository.save(order);
    }

    @Transactional
    public void detachProductFromOrder(UUID orderId, UUID productId) {
        var order = orderRepository.findWithProductsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
        order.getProducts().removeIf(product -> product.getId().equals(productId));
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public ShopCategoryEntity getCategoryWithProducts(UUID id) {
        return categoryRepository.findWithProductsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
    }

    @Transactional(readOnly = true)
    public ShopProductEntity getProductWithRelations(UUID id) {
        return productRepository.findWithRelationsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
    }

    @Transactional(readOnly = true)
    public ShopOrderEntity getOrderWithProducts(UUID id) {
        return orderRepository.findWithProductsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    }

    public ShopCategoryForm toCategoryForm(ShopCategoryEntity category) {
        var form = new ShopCategoryForm();
        form.setName(category.getName());
        form.setSlug(category.getSlug());
        form.setDescription(category.getDescription());
        form.setActive(category.getActive());
        return form;
    }

    public ShopProductForm toProductForm(ShopProductEntity product) {
        var form = new ShopProductForm();
        form.setName(product.getName());
        form.setSku(product.getSku());
        form.setPrice(product.getPrice());
        form.setStockQuantity(product.getStockQuantity());
        form.setActive(product.getActive());
        if (product.getPrimaryCategory() != null) {
            form.setPrimaryCategoryId(product.getPrimaryCategory().getId());
        }
        return form;
    }

    public ShopOrderForm toOrderForm(ShopOrderEntity order) {
        var form = new ShopOrderForm();
        form.setCustomerName(order.getCustomerName());
        form.setCustomerEmail(order.getCustomerEmail());
        form.setStatus(order.getStatus());
        return form;
    }

    private void applyCategoryForm(ShopCategoryEntity category, ShopCategoryForm form) {
        category.setName(form.getName());
        category.setSlug(form.getSlug());
        category.setDescription(form.getDescription());
        category.setActive(Boolean.TRUE.equals(form.getActive()));
    }

    private void applyProductForm(ShopProductEntity product, ShopProductForm form) {
        product.setName(form.getName());
        product.setSku(form.getSku());
        product.setPrice(form.getPrice());
        product.setStockQuantity(form.getStockQuantity());
        product.setActive(Boolean.TRUE.equals(form.getActive()));
        product.setPrimaryCategory(form.getPrimaryCategoryId() == null ? null : getCategory(form.getPrimaryCategoryId()));
    }

    private void applyOrderForm(ShopOrderEntity order, ShopOrderForm form) {
        order.setCustomerName(form.getCustomerName());
        order.setCustomerEmail(form.getCustomerEmail());
        order.setStatus(form.getStatus());
    }

    private ShopCategoryEntity getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
    }

    private ShopProductEntity getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
    }

    private ShopOrderEntity getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    }
}
