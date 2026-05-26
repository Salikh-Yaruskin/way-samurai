package ru.base_project.base.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.base_project.base.domain.api.MaboyRegisterRequest;
import ru.base_project.base.service.MaboyService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MaboyService maboyService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("request", new MaboyRegisterRequest(null, null, null));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("request") MaboyRegisterRequest request,
                           BindingResult bindingResult) {
        if (!request.password().equals(request.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "password.mismatch",
                    "Пароли не совпадают");
        }

        if (maboyService.existsByUsername(request.username())) {
            bindingResult.rejectValue("username",
                    "username.exists",
                    "Пользователь с таким именем уже существует"
            );
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        maboyService.register(request);

        return "redirect:/login?registered";
    }
}
