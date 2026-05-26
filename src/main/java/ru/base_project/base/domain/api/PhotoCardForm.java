package ru.base_project.base.domain.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class PhotoCardForm {

    @NotBlank(message = "Введите название")
    @Size(max = 160, message = "Название должно быть не длиннее 160 символов")
    private String title;

    @Size(max = 500, message = "Описание должно быть не длиннее 500 символов")
    private String description;

    private MultipartFile photo;
}
