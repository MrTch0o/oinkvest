package br.com.oinkvest.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Operacao.TipoOperacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.OperacaoRepository;

@Service
public class OperationService {

    @Autowired
    private OperacaoRepository operacaoRepository;

    public Operacao registrarOperacao(Usuario usuario, String moeda, double quantidade, double valor, TipoOperacao tipo) {
        Operacao operacao = Operacao.builder()
                .usuario(usuario)
                .moeda(moeda)
                .quantidade(quantidade)
                .valor(valor)
                .tipo(tipo)
                .dataHora(LocalDateTime.now())
                .build();

        return operacaoRepository.save(operacao);
    }

    public List<Operacao> listarPorUsuario(Usuario usuario) {
    return operacaoRepository.findByUsuario(usuario);
}
}
