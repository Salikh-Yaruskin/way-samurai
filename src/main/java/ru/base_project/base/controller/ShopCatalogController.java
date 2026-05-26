package ru.base_project.base.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.base_project.base.domain.api.ShoppingCart;
import ru.base_project.base.repository.ShopCategoryRepository;
import ru.base_project.base.repository.ShopProductRepository;
import ru.base_project.base.service.ShopService;

import java.util.UUID;

@Controller
@RequestMapping("/catalog")
@SessionAttributes("cart")
@RequiredArgsConstructor
public class ShopCatalogController {

    private final ShopProductRepository productRepository;
    private final ShopCategoryRepository categoryRepository;
    private final ShopService shopService;

    @ModelAttribute("cart")
    public ShoppingCart cart() {
        return new ShoppingCart();
    }

    @GetMapping
    public String catalog(@RequestParam(required = false) String search,
                          @RequestParam(required = false) UUID categoryId,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        var pageable = PageRequest.of(Math.max(page, 0), 12, Sort.by("name").ascending());
        var products = productRepository.search(blankToNull(search), categoryId, true, pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        return "shop/catalog/list";
    }

    @GetMapping("/products/{id}")
    public String product(@PathVariable UUID id, Model model) {
        model.addAttribute("product", shopService.getProductWithRelations(id));
        return "shop/catalog/product";
    }

    @PostMapping("/cart")
    public String addToCart(@RequestParam UUID productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @ModelAttribute("cart") ShoppingCart cart,
                            RedirectAttributes redirectAttributes) {
        cart.add(productId, quantity);
        redirectAttributes.addFlashAttribute("message", "Товар добавлен в корзину");
        return "redirect:/catalog";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
