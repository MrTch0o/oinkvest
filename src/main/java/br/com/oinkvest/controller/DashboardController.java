package br.com.oinkvest.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.BinanceService;
import br.com.oinkvest.service.OperationService;
import br.com.oinkvest.service.WalletService;

@Controller
public class DashboardController {

    private final BinanceService binanceService;
    private final WalletService walletService;
    private final OperationService operationService;

    public DashboardController(BinanceService binanceService,
            WalletService walletService,
            OperationService operationService) {
        this.binanceService = binanceService;
        this.walletService = walletService;
        this.operationService = operationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "BTCUSDT") String symbol,
            Model model) {
        List<String> paresUsdt = binanceService.listarParesUsdt();
        String preco = binanceService.obterPrecoAtual(symbol);

        model.addAttribute("pares", paresUsdt);
        model.addAttribute("precoAtual", preco);
        model.addAttribute("moedaSelecionada", symbol);
        model.addAttribute("content", "dashboard");
        model.addAttribute("title", "Dashboard - OinkVest");

        return "fragments/layout";
    }

    @PostMapping("/dashboard/operar")
    public String operar(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam String tipo,
            @RequestParam String moeda,
            @RequestParam double preco,
            @RequestParam double quantidade,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioDetails.getUsuario();
        double total = preco * quantidade;

        try {
            if ("COMPRA".equalsIgnoreCase(tipo)) {
                walletService.realizarCompra(usuario, moeda, quantidade, preco);
                operationService.registrarOperacao(usuario, moeda, quantidade, total, Operacao.TipoOperacao.COMPRA);
                System.out.println("Compra realizada com sucesso!");
            } else if ("VENDA".equalsIgnoreCase(tipo)) {
                walletService.realizarVenda(usuario, moeda, quantidade, preco);
                operationService.registrarOperacao(usuario, moeda, quantidade, total, Operacao.TipoOperacao.VENDA);
            }
        } catch (RuntimeException ex) {
            System.out.println("Erro ao realizar operação: " + ex.getMessage());
            ex.printStackTrace(); // mostra a stack completa
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/dashboard";
        }

        return "redirect:/dashboard";
    }
}
