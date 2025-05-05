package br.com.oinkvest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoryController {

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("content", "history");
        model.addAttribute("title", "Histórico - OinkVest");
        return "fragments/layout";
    }
}
