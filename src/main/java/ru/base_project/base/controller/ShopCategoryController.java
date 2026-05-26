package ru.base_project.base.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.base_project.base.domain.api.ShopCategoryForm;
import ru.base_project.base.repository.ShopCategoryRepository;
import ru.base_project.base.service.ShopService;

import java.util.UUID;

@Controller
@RequestMapping("/shop/categories")
@RequiredArgsConstructor
public class ShopCategoryController {

    private final ShopCategoryRepository categoryRepository;
    private final ShopService shopService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) Boolean active,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        var pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("name").ascending());
        var categories = categoryRepository.search(blankToNull(search), active, pageable);

        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("active", active);
        return "shop/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ShopCategoryForm());
        model.addAttribute("mode", "create");
        return "shop/categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ShopCategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "shop/categories/form";
        }

        try {
            var category = shopService.createCategory(form);
            redirectAttributes.addFlashAttribute("message", "Категория создана");
            return "redirect:/shop/categories/" + category.getId();
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("save", "Категория с таким slug уже существует");
            model.addAttribute("mode", "create");
            return "shop/categories/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        model.addAttribute("category", shopService.getCategoryWithProducts(id));
        return "shop/categories/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var category = shopService.getCategoryWithProducts(id);
        model.addAttribute("category", category);
        model.addAttribute("form", shopService.toCategoryForm(category));
        model.addAttribute("mode", "edit");
        return "shop/categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("form") ShopCategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", shopService.getCategoryWithProducts(id));
            model.addAttribute("mode", "edit");
            return "shop/categories/form";
        }

        try {
            shopService.updateCategory(id, form);
            redirectAttributes.addFlashAttribute("message", "Категория обновлена");
            return "redirect:/shop/categories/" + id;
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("save", "Категория с таким slug уже существует");
            model.addAttribute("category", shopService.getCategoryWithProducts(id));
            model.addAttribute("mode", "edit");
            return "shop/categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        shopService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("message", "Категория удалена");
        return "redirect:/shop/categories";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
