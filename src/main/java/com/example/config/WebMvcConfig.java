package com.example.config;

import com.example.security.CurrentUser;
import com.example.security.SessionInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final SessionInterceptor sessionInterceptor;

    public WebMvcConfig(SessionInterceptor sessionInterceptor) {
        this.sessionInterceptor = sessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor).addPathPatterns("/**");
    }
}

@ControllerAdvice
class GlobalModelAttributes {
    @ModelAttribute("currentUser")
    public CurrentUser exposeCurrentUser(HttpServletRequest request, Model model) {
        return (CurrentUser) request.getAttribute("currentUser");
    }
}
