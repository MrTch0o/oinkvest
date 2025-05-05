package br.com.oinkvest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WalletController {

    @GetMapping("/wallet")
    public String wallet(Model model) {
        model.addAttribute("content", "wallet"); // wallet.html
        model.addAttribute("title", "Carteira - OinkVest");
        return "fragments/layout";
    }
}
