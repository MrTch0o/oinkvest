package br.com.oinkvest.service;

import br.com.oinkvest.dto.DetalhesMoedaDTO;

import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Operacao.TipoOperacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.CarteiraRepository;
import br.com.oinkvest.repository.OperacaoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private CarteiraRepository carteiraRepository;

    @Autowired
    private OperacaoRepository operacaoRepository;

    public Carteira depositar(Carteira carteira, double valor) {
        carteira.setSaldoFiat(carteira.getSaldoFiat() + valor);
        return carteiraRepository.save(carteira);
    }

    public Carteira sacar(Carteira carteira, double valor) {
        if (carteira.getSaldoFiat() >= valor) {
            carteira.setSaldoFiat(carteira.getSaldoFiat() - valor);
            return carteiraRepository.save(carteira);
        }
        throw new RuntimeException("Saldo insuficiente para saque.");
    }

    public Optional<Carteira> buscarPorId(Long id) {
        return carteiraRepository.findById(id);
    }

    public void realizarCompra(Usuario usuario, String moeda, double qtd, double preco) {
        Carteira carteira = carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira nao encontrada."));
        double total = qtd * preco;

        if (carteira.getSaldoFiat() < total)
            throw new RuntimeException("Saldo USDT insuficiente para compra.");

        carteira.setSaldoFiat(carteira.getSaldoFiat() - total);
        carteira.setSaldoTrade(carteira.getSaldoTrade() + total);
        carteiraRepository.save(carteira);
    }

    public void realizarVenda(Usuario usuario, String moeda, double qtd, double preco) {
        Carteira carteira = carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira nao encontrada."));
        double total = qtd * preco;

        if (carteira.getSaldoTrade() < total)
            throw new RuntimeException("Ativos insuficientes para venda.");

        carteira.setSaldoTrade(carteira.getSaldoTrade() - total);
        carteira.setSaldoFiat(carteira.getSaldoFiat() + total);
        carteiraRepository.save(carteira);
    }

    public DetalhesMoedaDTO calcularDetalhesMoeda(Usuario usuario, String moeda) {
        // 1. Buscar todas as operações do usuário para a moeda selecionada
        List<Operacao> operacoes = operacaoRepository.findByUsuarioAndMoeda(usuario, moeda);

        // 2. Inicializar acumuladores
        BigDecimal totalComprado = BigDecimal.ZERO;
        BigDecimal totalVendido = BigDecimal.ZERO;
        BigDecimal quantidadeComprada = BigDecimal.ZERO;
        BigDecimal quantidadeVendida = BigDecimal.ZERO;

         BigDecimal saldoParcial = BigDecimal.ZERO;

        // 3. Acumular dados de compra e venda
        for (Operacao op : operacoes) {
            BigDecimal valor = BigDecimal.valueOf(op.getValor());
            BigDecimal qtd = BigDecimal.valueOf(op.getQuantidade());

            switch (op.getTipo()) {
                case COMPRA:
                    totalComprado = totalComprado.add(valor);
                    quantidadeComprada = quantidadeComprada.add(qtd);
                    saldoParcial = saldoParcial.add(qtd);
                    break;
                case VENDA:
                    totalVendido = totalVendido.add(valor);
                    quantidadeVendida = quantidadeVendida.add(qtd);
                    saldoParcial = saldoParcial.subtract(qtd);
                    break;
                default:
                    break;
            }

            if (saldoParcial.compareTo(BigDecimal.ZERO) <= 0) {
                totalComprado = BigDecimal.ZERO;
                totalVendido = BigDecimal.ZERO;
                quantidadeComprada = BigDecimal.ZERO;
                quantidadeVendida = BigDecimal.ZERO;
                saldoParcial = BigDecimal.ZERO;
            }
        }

        // 4. Calcular o saldo da moeda
        BigDecimal saldoMoeda = quantidadeComprada.subtract(quantidadeVendida);

        // 5. Calcular preço médio real baseado no capital líquido investido
        BigDecimal precoMedio;
        if (saldoMoeda.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal custoLiquido = totalComprado.subtract(totalVendido);
            precoMedio = custoLiquido.divide(saldoMoeda, 2, RoundingMode.HALF_UP);
        } else {
            precoMedio = BigDecimal.ZERO;
        }

        // 6. Retornar DTO
        DetalhesMoedaDTO dto = new DetalhesMoedaDTO();
        dto.setSaldoMoeda(saldoMoeda);
        dto.setPrecoMedio(precoMedio);
        return dto;
    }
}
