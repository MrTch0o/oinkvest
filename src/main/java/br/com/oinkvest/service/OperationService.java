package br.com.oinkvest.service;

import br.com.oinkvest.model.Operacao;
import br.com.oinkvest.model.Operacao.TipoOperacao;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.OperacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
