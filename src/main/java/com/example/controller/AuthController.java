package com.example.controller;

import com.example.domain.UserEntity;
import com.example.dto.LoginForm;
import com.example.dto.RegistrationForm;
import com.example.service.SessionService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class AuthController {
    private final UserService userService;
    private final SessionService sessionService;

    public AuthController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("form") RegistrationForm form,
                                 BindingResult binding, HttpServletResponse response) {
        if (!Objects.equals(form.getPassword(), form.getPasswordRepeat())) {
            binding.rejectValue("passwordRepeat", "mismatch", "Пароли не совпадают");
        }
        if (binding.hasErrors()) return "auth/register";
        try {
            UserEntity user = userService.register(form.getLogin(), form.getPassword());
            response.addCookie(sessionService.createSessionAndCookie(user.getId()));
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            binding.rejectValue("login", "notUnique", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("form", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(@Valid @ModelAttribute("form") LoginForm form,
                              BindingResult binding, HttpServletResponse response) {
        if (binding.hasErrors()) return "auth/login";
        return userService.authenticate(form.getLogin(), form.getPassword())
                .map(user -> {
                    response.addCookie(sessionService.createSessionAndCookie(user.getId()));
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    binding.reject("badCredentials", "Неверный логин или пароль");
                    return "auth/login";
                });
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        sessionService.logout(request, response);
        return "redirect:/";
    }
}
