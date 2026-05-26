package ru.base_project.base.domain.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaboyRegisterRequest(
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(min = 4, max = 50, message = "Имя должно быть от 3 до 50 символов")
        String username,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, max = 100, message = "Пароль должен быть минимум 3 символа")
        String password,

        @NotBlank(message = "Повторите пароль")
        String confirmPassword) {
}
