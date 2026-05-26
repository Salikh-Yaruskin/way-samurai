package ru.base_project.base.domain.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record SocialProfileForm(
        @NotBlank(message = "Имя профиля не должно быть пустым")
        @Size(max = 100, message = "Имя профиля должно быть не длиннее 100 символов")
        String displayName,

        @Size(max = 100, message = "Город должен быть не длиннее 100 символов")
        String city,

        @Size(max = 1000, message = "Интересы должны быть не длиннее 1000 символов")
        String interests,

        @Size(max = 2000, message = "Описание должно быть не длиннее 2000 символов")
        String bio,

        MultipartFile avatar
) {
}
