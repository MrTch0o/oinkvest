package br.com.oinkvest.controller;

import br.com.oinkvest.model.Contato;
import br.com.oinkvest.service.ContatoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final ContatoService service;

    public SupportController(ContatoService service) {
        this.service = service;
    }

    @GetMapping
    public String suporte(Model model) {
        model.addAttribute("contato", new Contato());
        model.addAttribute("title", "Suporte");
        model.addAttribute("content", "support");
        return "fragments/layout";
    }

    @PostMapping
    public String enviarMensagem(@ModelAttribute Contato contato) {
        service.salvar(contato);
        return "redirect:/support?success=true";
    }
}
