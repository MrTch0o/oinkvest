package br.com.oinkvest.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.oinkvest.config.UsuarioDetails;
import br.com.oinkvest.dto.AtivoDTO;
import br.com.oinkvest.dto.DetalhesMoedaDTO;
import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.CarteiraMoeda;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.CarteiraMoedaRepository;
import br.com.oinkvest.repository.OperacaoRepository;
import br.com.oinkvest.service.OperationService;
import br.com.oinkvest.service.WalletService;

@Controller
public class WalletController {

    private final WalletService walletService;
    private final OperationService operationService;
    private final CarteiraMoedaRepository carteiraMoedaRepository;
    private final OperacaoRepository operacaoRepository;

    public WalletController(WalletService walletService, OperationService operationService,
            CarteiraMoedaRepository carteiraMoedaRepository, OperacaoRepository operacaoRepository) {
        this.walletService = walletService;
        this.operationService = operationService;
        this.carteiraMoedaRepository = carteiraMoedaRepository;
        this.operacaoRepository = operacaoRepository;
    }

    @GetMapping("/wallet")
    public String mostrarCarteira(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        Usuario usuario = usuarioDetails.getUsuario();
        Carteira carteira = walletService.buscarCarteiraPorUsuario(usuario);

        List<CarteiraMoeda> moedas = carteiraMoedaRepository.findAllByCarteira(carteira);
        List<AtivoDTO> ativos = new ArrayList<>();
        List<Operacao> depositos = operacaoRepository.findByUsuarioAndTipo(usuario, Operacao.TipoOperacao.DEPOSITO);
        List<Operacao> saques = operacaoRepository.findByUsuarioAndTipo(usuario, Operacao.TipoOperacao.SAQUE);

        BigDecimal saldoFiat = BigDecimal.ZERO;
        BigDecimal saldoTotal = BigDecimal.ZERO;
        BigDecimal quantidadeAtivos = BigDecimal.ZERO;

        for (CarteiraMoeda cm : moedas) {
            if ("USDT".equals(cm.getMoeda())) {
                saldoFiat = cm.getQuantidade();
            } else {
                DetalhesMoedaDTO detalhes = walletService.calcularDetalhesMoeda(usuario, cm.getMoeda());
                saldoTotal = saldoTotal.add(
                        detalhes.getPrecoMedio().multiply(cm.getQuantidade()));
                quantidadeAtivos = quantidadeAtivos.add(cm.getQuantidade());
            }
        }

        for (CarteiraMoeda cm : moedas) {
            if (!"USDT".equals(cm.getMoeda())) {
                DetalhesMoedaDTO detalhes = walletService.calcularDetalhesMoeda(usuario, cm.getMoeda());

                AtivoDTO dto = new AtivoDTO();
                dto.setMoeda(cm.getMoeda());
                dto.setQuantidade(cm.getQuantidade());
                dto.setPrecoMedio(detalhes.getPrecoMedio());

                BigDecimal valor = detalhes.getPrecoMedio().multiply(cm.getQuantidade());
                dto.setValorEstimado(valor);

                ativos.add(dto);
            }
        }

        BigDecimal totalDepositado = depositos.stream()
                .map(Operacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSacado = saques.stream()
                .map(Operacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("saldoFiat", saldoFiat);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("quantidadeAtivos", quantidadeAtivos);
        model.addAttribute("content", "wallet");
        
        if (quantidadeAtivos.compareTo(BigDecimal.ZERO) > 0) {
            model.addAttribute("ativos", ativos);
        } else {
            model.addAttribute("msg", "Não há valores ativos no momento");
        }
        model.addAttribute("totalDepositado", totalDepositado);
        model.addAttribute("totalSacado", totalSacado);

        return "fragments/layout";
    }

    @PostMapping("/wallet/operar")
    public String operarCarteira(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam String tipo,
            @RequestParam BigDecimal valor,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioDetails.getUsuario();

        try {
            if (tipo.equalsIgnoreCase("DEPOSITO")) {
                walletService.depositar(usuario, valor);
                operationService.registrarOperacao(usuario, "USDT", valor, valor, Operacao.TipoOperacao.DEPOSITO);
            } else if (tipo.equalsIgnoreCase("SAQUE")) {
                walletService.sacar(usuario, valor);
                operationService.registrarOperacao(usuario, "USDT", valor, valor, Operacao.TipoOperacao.SAQUE);
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/wallet";
        }

        return "redirect:/wallet";
    }

}