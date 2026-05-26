package ru.base_project.base.domain.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShopCategoryForm {

    @NotBlank(message = "Введите название категории")
    @Size(max = 120, message = "Название должно быть не длиннее 120 символов")
    private String name;

    @NotBlank(message = "Введите slug категории")
    @Size(max = 120, message = "Slug должен быть не длиннее 120 символов")
    private String slug;

    @Size(max = 500, message = "Описание должно быть не длиннее 500 символов")
    private String description;

    private Boolean active = true;
}
