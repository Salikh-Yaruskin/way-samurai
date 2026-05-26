package ru.base_project.base.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.base_project.base.domain.OrderStatus;
import ru.base_project.base.domain.api.ShopOrderForm;
import ru.base_project.base.repository.ShopOrderRepository;
import ru.base_project.base.repository.ShopProductRepository;
import ru.base_project.base.service.ShopService;

import java.util.UUID;

@Controller
@RequestMapping("/shop/orders")
@RequiredArgsConstructor
public class ShopOrderController {

    private final ShopOrderRepository orderRepository;
    private final ShopProductRepository productRepository;
    private final ShopService shopService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) OrderStatus status,
                       @RequestParam(required = false) UUID productId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        var pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("createdAt").descending());
        var orders = orderRepository.search(blankToNull(search), status, productId, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("productId", productId);
        return "shop/orders/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ShopOrderForm());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("mode", "create");
        return "shop/orders/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ShopOrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("mode", "create");
            return "shop/orders/form";
        }

        var order = shopService.createOrder(form);
        redirectAttributes.addFlashAttribute("message", "Заказ создан");
        return "redirect:/shop/orders/" + order.getId();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        model.addAttribute("order", shopService.getOrderWithProducts(id));
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        return "shop/orders/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var order = shopService.getOrderWithProducts(id);
        model.addAttribute("order", order);
        model.addAttribute("form", shopService.toOrderForm(order));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("mode", "edit");
        return "shop/orders/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("form") ShopOrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("order", shopService.getOrderWithProducts(id));
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("mode", "edit");
            return "shop/orders/form";
        }

        shopService.updateOrder(id, form);
        redirectAttributes.addFlashAttribute("message", "Заказ обновлен");
        return "redirect:/shop/orders/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        shopService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("message", "Заказ удален");
        return "redirect:/shop/orders";
    }

    @PostMapping("/{id}/products")
    public String attachProduct(@PathVariable UUID id,
                                @RequestParam UUID productId,
                                RedirectAttributes redirectAttributes) {
        shopService.attachProductToOrder(id, productId);
        redirectAttributes.addFlashAttribute("message", "Товар добавлен в заказ");
        return "redirect:/shop/orders/" + id;
    }

    @PostMapping("/{id}/products/{productId}/delete")
    public String detachProduct(@PathVariable UUID id,
                                @PathVariable UUID productId,
                                RedirectAttributes redirectAttributes) {
        shopService.detachProductFromOrder(id, productId);
        redirectAttributes.addFlashAttribute("message", "Товар удален из заказа");
        return "redirect:/shop/orders/" + id;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
