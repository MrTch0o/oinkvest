package br.com.oinkvest.service;

import br.com.oinkvest.model.Contato;
import br.com.oinkvest.repository.ContatoRepository;
import org.springframework.stereotype.Service;

@Service
public class ContatoService {

    private final ContatoRepository repository;

    public ContatoService(ContatoRepository repository) {
        this.repository = repository;
    }

    public void salvar(Contato contato) {
        repository.save(contato);
    }
}
