package br.com.oinkvest.service;

import br.com.oinkvest.model.Carteira;
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
}
