package ru.base_project.base.domain.api;

import ru.base_project.base.domain.entity.ShopProductEntity;

import java.math.BigDecimal;

public record CartLine(
        ShopProductEntity product,
        int quantity,
        BigDecimal total
) {
}
