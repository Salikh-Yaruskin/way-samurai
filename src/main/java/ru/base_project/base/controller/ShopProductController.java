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
import ru.base_project.base.domain.api.ShopProductForm;
import ru.base_project.base.repository.ShopCategoryRepository;
import ru.base_project.base.repository.ShopProductRepository;
import ru.base_project.base.service.ShopService;

import java.util.UUID;

@Controller
@RequestMapping("/shop/products")
@RequiredArgsConstructor
public class ShopProductController {

    private final ShopProductRepository productRepository;
    private final ShopCategoryRepository categoryRepository;
    private final ShopService shopService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) UUID categoryId,
                       @RequestParam(required = false) Boolean active,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        var pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("name").ascending());
        var products = productRepository.search(blankToNull(search), categoryId, active, pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("active", active);
        return "shop/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ShopProductForm());
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("mode", "create");
        return "shop/products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ShopProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
            model.addAttribute("mode", "create");
            return "shop/products/form";
        }

        try {
            var product = shopService.createProduct(form);
            redirectAttributes.addFlashAttribute("message", "Товар создан");
            return "redirect:/shop/products/" + product.getId();
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("save", "Товар с таким артикулом уже существует");
            model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
            model.addAttribute("mode", "create");
            return "shop/products/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        model.addAttribute("product", shopService.getProductWithRelations(id));
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        return "shop/products/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var product = shopService.getProductWithRelations(id);
        model.addAttribute("product", product);
        model.addAttribute("form", shopService.toProductForm(product));
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("mode", "edit");
        return "shop/products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("form") ShopProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", shopService.getProductWithRelations(id));
            model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
            model.addAttribute("mode", "edit");
            return "shop/products/form";
        }

        try {
            shopService.updateProduct(id, form);
            redirectAttributes.addFlashAttribute("message", "Товар обновлен");
            return "redirect:/shop/products/" + id;
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("save", "Товар с таким артикулом уже существует");
            model.addAttribute("product", shopService.getProductWithRelations(id));
            model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
            model.addAttribute("mode", "edit");
            return "shop/products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        shopService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "Товар удален");
        return "redirect:/shop/products";
    }

    @PostMapping("/{id}/categories")
    public String attachCategory(@PathVariable UUID id,
                                 @RequestParam UUID categoryId,
                                 RedirectAttributes redirectAttributes) {
        shopService.attachCategoryToProduct(id, categoryId);
        redirectAttributes.addFlashAttribute("message", "Категория прикреплена к товару");
        return "redirect:/shop/products/" + id;
    }

    @PostMapping("/{id}/categories/{categoryId}/delete")
    public String detachCategory(@PathVariable UUID id,
                                 @PathVariable UUID categoryId,
                                 RedirectAttributes redirectAttributes) {
        shopService.detachCategoryFromProduct(id, categoryId);
        redirectAttributes.addFlashAttribute("message", "Категория откреплена от товара");
        return "redirect:/shop/products/" + id;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
