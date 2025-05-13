package br.com.oinkvest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.OperationService;
import br.com.oinkvest.service.WalletService;

@Controller
public class WalletController {

    private final WalletService walletService;
    private final OperationService operationService;

    public WalletController(WalletService walletService, OperationService operationService) {
        this.walletService = walletService;
        this.operationService = operationService;
    }

    @GetMapping("/wallet")
    public String wallet(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        Usuario usuario = usuarioDetails.getUsuario();

        Carteira carteira = walletService.buscarPorId(usuario.getCarteira().getId())
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada."));

        double saldoFiat = carteira.getSaldoFiat();
        double saldoTrade = carteira.getSaldoTrade();
        double saldoTotal = saldoFiat + saldoTrade;

        model.addAttribute("saldoFiat", saldoFiat);
        model.addAttribute("saldoTrade", saldoTrade);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("content", "wallet");
        model.addAttribute("title", "Carteira - OinkVest");

        return "fragments/layout";
    }

    @PostMapping("/wallet/operar")
    public String operarCarteira(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam String tipo,
            @RequestParam double valor) {

        Usuario usuario = usuarioDetails.getUsuario();

        // Buscar carteira associada ao usuário
        Carteira carteira = walletService.buscarPorId(usuario.getCarteira().getId())
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada."));

        if (tipo.equalsIgnoreCase("DEPOSITO")) {
            walletService.depositar(carteira, valor);
            operationService.registrarOperacao(usuario, "USDT", valor, valor, Operacao.TipoOperacao.DEPOSITO);
        } else if (tipo.equalsIgnoreCase("SAQUE")) {
            walletService.sacar(carteira, valor);
            operationService.registrarOperacao(usuario, "USDT", valor, valor, Operacao.TipoOperacao.SAQUE);
        }

        // Redireciona de volta para a tela da carteira
        return "redirect:/wallet";
    }
}