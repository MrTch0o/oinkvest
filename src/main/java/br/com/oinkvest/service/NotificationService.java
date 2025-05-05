package br.com.oinkvest.service;

import br.com.oinkvest.model.Notificacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.NotificacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    public Notificacao criarAlerta(Usuario usuario, String moeda, double precoAlvo, Notificacao.Condicao condicao) {
        Notificacao notificacao = Notificacao.builder()
                .usuario(usuario)
                .moeda(moeda)
                .precoAlvo(precoAlvo)
                .condicao(condicao)
                .ativa(true)
                .criadaEm(LocalDateTime.now())
                .build();

        return notificacaoRepository.save(notificacao);
    }

    public List<Notificacao> listarPorUsuario(Usuario usuario) {
        return notificacaoRepository.findByUsuario(usuario);
    }

    @Transactional
    public void removerAlerta(Long id) {
        notificacaoRepository.deleteById(id);
    }
}
