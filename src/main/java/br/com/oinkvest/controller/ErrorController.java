package br.com.oinkvest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "unauthorized"; // Retorna a página principal que inclui o layout
    }
}