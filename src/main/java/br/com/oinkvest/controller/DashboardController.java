package br.com.oinkvest.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.dto.Candle;
import br.com.oinkvest.dto.DetalhesMoedaDTO;
import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.CarteiraMoeda;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.CarteiraMoedaRepository;
import br.com.oinkvest.service.BinanceService;
import br.com.oinkvest.service.OperationService;
import br.com.oinkvest.service.WalletService;

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

        // List<String> paresUsdt = binanceService.listarParesUsdt();
        // String preco = binanceService.obterPrecoAtual(symbol);

        DetalhesMoedaDTO detalhesMoeda = walletService.calcularDetalhesMoeda(usuario, symbol);
        Carteira carteira = walletService.buscarCarteiraPorUsuario(usuario);

        String baseAsset = binanceService.obterNomeCompleto(symbol);
        String iconUrl = String.format("https://cdn.jsdelivr.net/gh/vadimmalykhin/binance-icons/crypto/%s.svg",
        baseAsset.toLowerCase());

        detalhesMoeda.setIconUrl(iconUrl);

        // Busca o saldo de USDT
        CarteiraMoeda usdt = carteiraMoedaRepository.findByCarteiraAndMoeda(carteira, "USDT")
                .orElse(null);

        BigDecimal saldoFiat = (usdt != null) ? usdt.getQuantidade() : BigDecimal.ZERO;

        // model.addAttribute("pares", paresUsdt);
        // model.addAttribute("precoAtual", preco);
        model.addAttribute("moedaSelecionada", symbol);
        model.addAttribute("detalhesMoeda", detalhesMoeda);
        model.addAttribute("saldoFiat", saldoFiat);
        model.addAttribute("content", "dashboard");

        return "fragments/layout";
    }

    @GetMapping("/dashboard/fragment-pares")
    public String fragmentPares(@RequestParam String symbol, Model model) {
        List<String> pares = binanceService.listarParesUsdt(); // ou o método que você usa
        model.addAttribute("pares", pares);
        model.addAttribute("moedaSelecionada", symbol);
        return "fragments/fragment-pares :: select-pares";
    }

    @GetMapping("/dashboard/fragment-preco")
    public String fragmentPreco(
            @RequestParam String symbol,
            @RequestParam String fragment, // 'preco-header', 'preco-input' ou 'preco-hidden'
            Model model) {

        String preco = binanceService.obterPrecoAtual(symbol);
        model.addAttribute("precoAtual", preco);

        return String.format("fragments/fragment-preco :: %s", fragment);
    }

    @PostMapping("/dashboard/operar")
    public String operar(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam String tipo,
            @RequestParam String moeda,
            @RequestParam BigDecimal preco,
            @RequestParam BigDecimal quantidade,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioDetails.getUsuario();
        BigDecimal total = preco.multiply(quantidade).setScale(8, RoundingMode.HALF_UP);

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

    @GetMapping("/grafico-fragment")
    public String carregarFragmentoGrafico(Model model) {
        return "fragments/fragment-grafico"; // Thymeleaf procura por fragments/grafico.html
    }

    @GetMapping("/grafico-json")
    @ResponseBody
    public List<Map<String, Object>> obterDadosGraficoJson(@RequestParam String symbol, @RequestParam(defaultValue = "1m")String interval) {
        List<Candle> candles = binanceService.ultimosPrecos(symbol, interval, 50);

        return candles.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time", c.getOpenTime()); // long
                    map.put("close", c.getClose()); // BigDecimal
                    return map;
                })
                .collect(Collectors.toList());
    }

}
