package br.com.oinkvest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Define o conteúdo da página que será renderizado dentro do layout
        model.addAttribute("content", "dashboard"); // dashboard.html será injetado no layout
        model.addAttribute("title", "Dashboard - OinkVest"); // usado no <title> via fragment
        return "fragments/layout"; // layout.html que inclui o conteúdo via th:replace
    }
}
