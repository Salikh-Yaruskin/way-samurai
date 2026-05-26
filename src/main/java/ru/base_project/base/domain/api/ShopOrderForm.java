package ru.base_project.base.domain.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.base_project.base.domain.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
public class ShopOrderForm {

    @NotBlank(message = "Введите имя покупателя")
    @Size(max = 120, message = "Имя должно быть не длиннее 120 символов")
    private String customerName;

    @NotBlank(message = "Введите email покупателя")
    @Email(message = "Введите корректный email")
    @Size(max = 160, message = "Email должен быть не длиннее 160 символов")
    private String customerEmail;

    @NotNull(message = "Выберите статус заказа")
    private OrderStatus status = OrderStatus.NEW;
}
