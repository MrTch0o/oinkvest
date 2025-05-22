package br.com.oinkvest.repository;

import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.CarteiraMoeda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CarteiraMoedaRepository extends JpaRepository<CarteiraMoeda, Long> {
    
    Optional<CarteiraMoeda> findByCarteiraAndMoeda(Carteira carteira, String moeda);

    List<CarteiraMoeda> findAllByCarteira(Carteira carteira);
}
