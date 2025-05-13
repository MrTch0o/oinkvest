package br.com.oinkvest.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.oinkvest.model.Carteira;

public interface CarteiraRepository extends JpaRepository<Carteira, Long> {
    Optional<Carteira> findByUsuario(br.com.oinkvest.model.Usuario usuario);

}
