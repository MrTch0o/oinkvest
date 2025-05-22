package br.com.oinkvest.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.dto.DetalhesMoedaDTO;
import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.CarteiraMoeda;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.BinanceService;
import br.com.oinkvest.service.OperationService;
import br.com.oinkvest.service.WalletService;
import br.com.oinkvest.repository.CarteiraMoedaRepository;

@Controller
public class DashboardController {

    private final BinanceService binanceService;
    private final WalletService walletService;
    private final OperationService operationService;
    private final CarteiraMoedaRepository carteiraMoedaRepository;

    public DashboardController(BinanceService binanceService,
            WalletService walletService,
            OperationService operationService,
            CarteiraMoedaRepository carteiraMoedaRepository) {
        this.binanceService = binanceService;
        this.walletService = walletService;
        this.operationService = operationService;
        this.carteiraMoedaRepository = carteiraMoedaRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false, defaultValue = "BTCUSDT") String symbol,
            Model model) {

        Usuario usuario = usuarioDetails.getUsuario();

        List<String> paresUsdt = binanceService.listarParesUsdt();
        String preco = binanceService.obterPrecoAtual(symbol);

        DetalhesMoedaDTO detalhesMoeda = walletService.calcularDetalhesMoeda(usuario, symbol);
        Carteira carteira = walletService.buscarCarteiraPorUsuario(usuario);

        // Busca o saldo de USDT
        CarteiraMoeda usdt = carteiraMoedaRepository.findByCarteiraAndMoeda(carteira, "USDT")
                .orElse(null);

        BigDecimal saldoFiat = (usdt != null) ? usdt.getQuantidade() : BigDecimal.ZERO;

        model.addAttribute("pares", paresUsdt);
        model.addAttribute("precoAtual", preco);
        model.addAttribute("moedaSelecionada", symbol);
        model.addAttribute("detalhesMoeda", detalhesMoeda);
        model.addAttribute("saldoFiat", saldoFiat);
        model.addAttribute("content", "dashboard");

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
                redirectAttributes.addFlashAttribute("success", "Compra realizada com sucesso!");
            } else if ("VENDA".equalsIgnoreCase(tipo)) {
                walletService.realizarVenda(usuario, moeda, quantidade, preco);
                operationService.registrarOperacao(usuario, moeda, quantidade, total, Operacao.TipoOperacao.VENDA);
                redirectAttributes.addFlashAttribute("success", "Venda realizada com sucesso!");
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
