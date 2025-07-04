package br.com.oinkvest.controller;

import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.UsuarioService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/login";
        }
    }
    

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // O usuário já está autenticado
            return "redirect:/dashboard"; // ou qualquer página padrão pós-login
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        Carteira carteira = new Carteira();
        carteira.setSaldoFiat(BigDecimal.ZERO);
        carteira.setSaldoTrade(BigDecimal.ZERO);
        carteira.setUsuario(usuario);
        usuario.setCarteira(carteira);

        usuarioService.salvar(usuario);
        return "redirect:/login?registered";
    }
}
