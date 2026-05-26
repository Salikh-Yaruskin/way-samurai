package ru.base_project.base.domain.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ShopProductForm {

    @NotBlank(message = "Введите название товара")
    @Size(max = 160, message = "Название должно быть не длиннее 160 символов")
    private String name;

    @NotBlank(message = "Введите артикул")
    @Size(max = 80, message = "Артикул должен быть не длиннее 80 символов")
    private String sku;

    @NotNull(message = "Введите цену")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal price;

    @NotNull(message = "Введите остаток на складе")
    @Min(value = 0, message = "Остаток не может быть отрицательным")
    private Integer stockQuantity;

    private Boolean active = true;

    private UUID primaryCategoryId;
}
