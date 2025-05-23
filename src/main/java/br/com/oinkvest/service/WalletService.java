package br.com.oinkvest.service;

import br.com.oinkvest.dto.DetalhesMoedaDTO;

import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.CarteiraMoeda;
import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.CarteiraMoedaRepository;
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

    @Autowired
    private CarteiraMoedaRepository carteiraMoedaRepository;

    public void depositar(Usuario usuario, BigDecimal valor) {
        Carteira carteira = buscarCarteiraPorUsuario(usuario);

        CarteiraMoeda usdt = carteiraMoedaRepository
                .findByCarteiraAndMoeda(carteira, "USDT")
                .orElseGet(() -> {
                    CarteiraMoeda nova = new CarteiraMoeda();
                    nova.setCarteira(carteira);
                    nova.setMoeda("USDT");
                    nova.setQuantidade(BigDecimal.ZERO);
                    return nova;
                });

        usdt.setQuantidade(usdt.getQuantidade().add(valor));
        carteiraMoedaRepository.save(usdt);
    }

    public void sacar(Usuario usuario, BigDecimal valor) {
        Carteira carteira = buscarCarteiraPorUsuario(usuario);

        CarteiraMoeda usdt = carteiraMoedaRepository
                .findByCarteiraAndMoeda(carteira, "USDT")
                .orElseThrow(() -> new RuntimeException("Saldo USDT não encontrado."));

        if (usdt.getQuantidade().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo USDT insuficiente para saque.");
        }

        usdt.setQuantidade(usdt.getQuantidade().subtract(valor));
        carteiraMoedaRepository.save(usdt);
    }

    public Optional<Carteira> buscarPorId(Long id) {
        return carteiraRepository.findById(id);
    }

    public Carteira buscarCarteiraPorUsuario(Usuario usuario) {
        return carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada."));
    }

    public void realizarCompra(Usuario usuario, String moeda, BigDecimal qtd, BigDecimal preco) {
        Carteira carteira = carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada."));

        BigDecimal total = qtd.multiply(preco).setScale(8, RoundingMode.HALF_UP);

        // Buscar ou criar moeda na carteira
        CarteiraMoeda moedaCarteira = carteiraMoedaRepository
                .findByCarteiraAndMoeda(carteira, moeda)
                .orElseGet(() -> {
                    CarteiraMoeda nova = new CarteiraMoeda();
                    nova.setCarteira(carteira);
                    nova.setMoeda(moeda);
                    nova.setQuantidade(BigDecimal.ZERO);
                    return nova;
                });

        // Verifica se há saldo USDT suficiente (precisamos buscar o saldo USDT da
        // carteira)
        CarteiraMoeda usdt = carteiraMoedaRepository
                .findByCarteiraAndMoeda(carteira, "USDT")
                .orElseThrow(() -> new RuntimeException("Saldo USDT não encontrado."));

        if (usdt.getQuantidade().compareTo(total) < 0) {
            throw new RuntimeException("Saldo USDT insuficiente para compra.");
        }

        // Atualizar saldo das moedas
        usdt.setQuantidade(usdt.getQuantidade().subtract(total));
        moedaCarteira.setQuantidade(
                moedaCarteira.getQuantidade().add(qtd).setScale(8, RoundingMode.HALF_UP));

        // Salvar alterações
        carteiraMoedaRepository.save(usdt);
        carteiraMoedaRepository.save(moedaCarteira);
    }

    public void realizarVenda(Usuario usuario, String moeda, BigDecimal qtd, BigDecimal preco) {
        Carteira carteira = carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada."));

        BigDecimal total = qtd.multiply(preco).setScale(8, RoundingMode.HALF_UP);

        CarteiraMoeda moedaCarteira = carteiraMoedaRepository
                .findByCarteiraAndMoeda(carteira, moeda)
                .orElseThrow(() -> new RuntimeException("Saldo da moeda não encontrado."));

        if (moedaCarteira.getQuantidade().compareTo(qtd) < 0) {
            throw new RuntimeException("Quantidade insuficiente para venda.");
        }

        // Buscar ou criar USDT na carteira
        CarteiraMoeda usdt = carteiraMoedaRepository
                .findByCarteiraAndMoeda(carteira, "USDT")
                .orElseGet(() -> {
                    CarteiraMoeda nova = new CarteiraMoeda();
                    nova.setCarteira(carteira);
                    nova.setMoeda("USDT");
                    nova.setQuantidade(BigDecimal.ZERO);
                    return nova;
                });

        // Atualizar os saldos
        moedaCarteira.setQuantidade(moedaCarteira.getQuantidade().subtract(qtd).setScale(8, RoundingMode.HALF_UP));
        usdt.setQuantidade(usdt.getQuantidade().add(total));

        carteiraMoedaRepository.save(moedaCarteira);
        carteiraMoedaRepository.save(usdt);
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
            BigDecimal valor = op.getValor();
            BigDecimal qtd = op.getQuantidade();

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
            precoMedio = custoLiquido.divide(saldoMoeda, 8, RoundingMode.HALF_UP);
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
