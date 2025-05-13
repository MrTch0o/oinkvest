package br.com.oinkvest.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.OperationService;

@Controller
public class HistoryController {

    private final OperationService operationService;

    public HistoryController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        Usuario usuario = usuarioDetails.getUsuario();

        List<Operacao> operacoes = operationService.listarPorUsuario(usuario);
        operacoes.sort(Comparator.comparing(Operacao::getDataHora).reversed()); // ordem decrescente

        model.addAttribute("operacoes", operacoes);
        model.addAttribute("title", "Histórico - OinkVest");
        model.addAttribute("content", "history");

        return "fragments/layout";
    }
}
