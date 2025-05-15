package br.com.oinkvest.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String history(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false) String filtro, Model model) {
        Usuario usuario = usuarioDetails.getUsuario();

        List<Operacao> operacoes = operationService.listarPorUsuario(usuario);

        // aplicar filtro se houver
        if (filtro != null && !filtro.isBlank()) {
            String termo = filtro.trim().toLowerCase();
            operacoes = new ArrayList<>(operacoes.stream()
                    .filter(op -> op.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss"))
                            .toString().toLowerCase().contains(termo)
                            || op.getDataHora().toString().toLowerCase().contains(termo)
                            || op.getTipo().name().toLowerCase().contains(termo)
                            || op.getMoeda().toLowerCase().contains(termo)
                            || String.valueOf(op.getQuantidade()).toLowerCase().contains(termo)
                            || String.valueOf(op.getValor()).toLowerCase().contains(termo)
                            || String.valueOf(op.getQuantidade() * op.getValor()).toLowerCase().contains(termo))
                    .toList());
            model.addAttribute("filtro", filtro);
        }

        operacoes.sort(Comparator.comparing(Operacao::getDataHora).reversed()); // ordem decrescente

        model.addAttribute("operacoes", operacoes);
        model.addAttribute("title", "Histórico - OinkVest");
        model.addAttribute("content", "history");

        return "fragments/layout";
    }
}
