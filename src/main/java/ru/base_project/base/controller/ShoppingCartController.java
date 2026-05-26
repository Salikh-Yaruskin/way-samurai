package ru.base_project.base.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.base_project.base.domain.api.CartLine;
import ru.base_project.base.domain.api.CheckoutForm;
import ru.base_project.base.domain.api.ShoppingCart;
import ru.base_project.base.repository.ShopProductRepository;
import ru.base_project.base.service.ShopService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShopProductRepository productRepository;
    private final ShopService shopService;

    @ModelAttribute("cart")
    public ShoppingCart cart() {
        return new ShoppingCart();
    }

    @GetMapping
    public String cartPage(@ModelAttribute("cart") ShoppingCart cart,
                           Model model,
                           Authentication authentication) {
        model.addAttribute("lines", buildLines(cart));
        model.addAttribute("total", calculateTotal(cart));
        model.addAttribute("checkoutForm", createCheckoutForm(authentication));
        return "shop/cart/index";
    }

    @PostMapping("/items")
    public String updateItem(@RequestParam UUID productId,
                             @RequestParam int quantity,
                             @ModelAttribute("cart") ShoppingCart cart,
                             RedirectAttributes redirectAttributes) {
        cart.update(productId, quantity);
        redirectAttributes.addFlashAttribute("message", "Корзина обновлена");
        return "redirect:/cart";
    }

    @PostMapping("/items/delete")
    public String removeItem(@RequestParam UUID productId,
                             @ModelAttribute("cart") ShoppingCart cart,
                             RedirectAttributes redirectAttributes) {
        cart.remove(productId);
        redirectAttributes.addFlashAttribute("message", "Товар удален из корзины");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
                           BindingResult bindingResult,
                           @ModelAttribute("cart") ShoppingCart cart,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (cart.isEmpty()) {
            bindingResult.reject("cart", "Корзина пуста. Добавьте товары перед покупкой.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("lines", buildLines(cart));
            model.addAttribute("total", calculateTotal(cart));
            return "shop/cart/index";
        }

        var order = shopService.checkout(checkoutForm, cart.getItems().keySet());
        cart.clear();
        redirectAttributes.addFlashAttribute("message", "Заказ оформлен");
        return "redirect:/cart/success?orderId=" + order.getId();
    }

    @GetMapping("/success")
    public String success(@RequestParam UUID orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "shop/cart/success";
    }

    private List<CartLine> buildLines(ShoppingCart cart) {
        var products = productRepository.findAllByIdIn(cart.getItems().keySet());
        return products.stream()
                .sorted(Comparator.comparing(product -> product.getName().toLowerCase()))
                .map(product -> {
                    int quantity = cart.getItems().getOrDefault(product.getId(), 0);
                    return new CartLine(product, quantity, product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                })
                .toList();
    }

    private BigDecimal calculateTotal(ShoppingCart cart) {
        return buildLines(cart).stream()
                .map(CartLine::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CheckoutForm createCheckoutForm(Authentication authentication) {
        var form = new CheckoutForm();
        if (authentication != null) {
            form.setCustomerName(authentication.getName());
        }
        return form;
    }
}
