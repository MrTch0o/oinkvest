package br.com.oinkvest.repository;

import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperacaoRepository extends JpaRepository<Operacao, Long> {
    List<Operacao> findByUsuario(Usuario usuario);
    List<Operacao> findByUsuarioAndMoeda(Usuario usuario, String moeda);
    List<Operacao> findByUsuarioAndTipo(Usuario usuario, Operacao.TipoOperacao tipo);

}
