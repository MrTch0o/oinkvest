package br.com.oinkvest.repository;

import br.com.oinkvest.model.Notificacao;
import br.com.oinkvest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuario(Usuario usuario);
    List<Notificacao> findByUsuarioAndMoedaContainingIgnoreCase(Usuario usuario, String moeda);
}
