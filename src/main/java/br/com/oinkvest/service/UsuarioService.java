package br.com.oinkvest.service;

import br.com.oinkvest.model.Role;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.repository.RoleRepository;
import br.com.oinkvest.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario salvar(Usuario usuario, String perfilEscolhido) {
        
        Role role = roleRepository.findByName(perfilEscolhido);

        usuario.setRoles(List.of(role));

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
