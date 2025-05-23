package br.com.oinkvest.controller;

import java.math.BigDecimal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.model.Notificacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public String mostrarAlertas(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        Usuario usuario = usuarioDetails.getUsuario();
        model.addAttribute("notificacao", new Notificacao());
        model.addAttribute("notificacoes", service.listarPorUsuario(usuario));
        model.addAttribute("title", "Alertas de Preço");
        model.addAttribute("content", "notifications");
        return "fragments/layout";
    }

    @PostMapping
    public String criarAlerta(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
                              @RequestParam String moeda,
                              @RequestParam BigDecimal precoAlvo,
                              @RequestParam Notificacao.Condicao condicao) {

        Usuario usuario = usuarioDetails.getUsuario();
        service.criarAlerta(usuario, moeda, precoAlvo, condicao);
        return "redirect:/notifications";
    }

    @PostMapping("/remover/{id}")
    public String removerAlerta(@PathVariable Long id) {
        service.removerAlerta(id);
        return "redirect:/notifications";
    }
}
