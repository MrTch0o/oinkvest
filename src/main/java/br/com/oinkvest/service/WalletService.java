package br.com.oinkvest.service;

import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.CarteiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private CarteiraRepository carteiraRepository;

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
        Carteira carteira = carteiraRepository.findByUsuario(usuario).orElseThrow(() -> new RuntimeException("Carteira nao encontrada."));
        double total = qtd * preco;

        System.out.println("Quantidade: " + qtd);
        System.out.println("Preço: " + preco);

        System.out.println("Saldo USDT: " + carteira.getSaldoFiat());
        System.out.println("Total: " + total);

        if (carteira.getSaldoFiat() < total)
            throw new RuntimeException("Saldo USDT insuficiente para compra.");

        carteira.setSaldoFiat(carteira.getSaldoFiat() - total);
        carteira.setSaldoTrade(carteira.getSaldoTrade() + total);
        carteiraRepository.save(carteira);
    }

    public void realizarVenda(Usuario usuario, String moeda, double qtd, double preco) {
        Carteira carteira = carteiraRepository.findByUsuario(usuario).orElseThrow(() -> new RuntimeException("Carteira nao encontrada."));
        double total = qtd * preco;

        if (carteira.getSaldoTrade() < total)
            throw new RuntimeException("Ativos insuficientes para venda.");

        carteira.setSaldoTrade(carteira.getSaldoTrade() - total);
        carteira.setSaldoFiat(carteira.getSaldoFiat() + total);
        carteiraRepository.save(carteira);
    }

}
