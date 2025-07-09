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
import br.com.oinkvest.service.BinanceService;
import br.com.oinkvest.service.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final BinanceService binanceService;

    private final NotificationService service;

    public NotificationController(NotificationService service, BinanceService binanceService) {
        this.service = service;
        this.binanceService = binanceService;
    }

    @GetMapping
    public String mostrarAlertas(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false) String filtro, Model model) {
        Usuario usuario = usuarioDetails.getUsuario();

        model.addAttribute("alerta", new Notificacao()); // ✅ CORRIGIDO
        model.addAttribute("pares", binanceService.listarParesUsdt()); // necessário para o select
        model.addAttribute("alertas", service.listarPorUsuario(usuario));
        model.addAttribute("filtro", filtro);
        model.addAttribute("title", "Alertas de Preço");
        model.addAttribute("content", "notifications");

        return "fragments/layout";
    }

    @GetMapping("/fragment-alertas")
    public String fragmentAlertas(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false) String filtro,
            Model model) {

        Usuario usuario = usuarioDetails.getUsuario();
        model.addAttribute("alertas", service.listarPorUsuarioEPorMoeda(usuario, filtro));
        return "fragments/fragment-alertas :: lista-alertas";
    }

    @GetMapping("/fragment-pares")
    public String fragmentPares(@RequestParam String symbol, Model model) {
        model.addAttribute("moedaSelecionada", symbol);
        model.addAttribute("pares", binanceService.listarParesUsdt());
        return "fragments/fragment-pares :: select-pares-notificacoes";
    }

    @GetMapping("/editar/{id}")
    public String editarAlerta(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            Model model) {

        //Usuario usuario = usuarioDetails.getUsuario();
        Notificacao alerta = service.buscarPorId(id);

        model.addAttribute("alerta", alerta);
        model.addAttribute("pares", binanceService.listarParesUsdt()); // lista de moedas para o select
        model.addAttribute("moedaSelecionada", alerta.getMoeda());
        model.addAttribute("title", "Editar Alerta");
        model.addAttribute("content", "notifications");

        return "fragments/layout";
    }

    @PostMapping
    public String criarOuEditarAlerta(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false) Long id,
            @RequestParam String moeda,
            @RequestParam BigDecimal precoAlvo,
            @RequestParam Notificacao.Condicao condicao) {

        
        System.out.println("======= DEBUG ALERTA =======");
        System.out.println("ID: " + id);
        System.out.println("Moeda: " + moeda);
        System.out.println("Preço Alvo: " + precoAlvo);
        System.out.println("Condição recebida: " + condicao);
        System.out.println("============================");

        Usuario usuario = usuarioDetails.getUsuario();

        if (id != null) {
            service.atualizarAlerta(id, moeda, precoAlvo, condicao);
        } else {
            service.criarAlerta(usuario, moeda, precoAlvo, condicao);
        }

        return "redirect:/notifications";
    }

    @PostMapping("/remover/{id}")
    public String removerAlerta(@PathVariable Long id) {
        service.removerAlerta(id);
        return "redirect:/notifications";
    }
}
