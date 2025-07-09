package br.com.oinkvest.controller;

import br.com.oinkvest.model.Carteira;
import br.com.oinkvest.model.Usuario;
import br.com.oinkvest.service.UsuarioService;

import java.math.BigDecimal;
import java.util.List;

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
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Usuario usuario, @RequestParam("perfilEscolhido") String perfilEscolhido) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        
        List<String> rolesPermitidas = List.of("ROLE_ANALISTA", "ROLE_CARTEIRA");
        
        if(!rolesPermitidas.contains(perfilEscolhido)) {
            return "register";
        }

        Carteira carteira = new Carteira();
        carteira.setSaldoFiat(BigDecimal.ZERO);
        carteira.setSaldoTrade(BigDecimal.ZERO);
        carteira.setUsuario(usuario);
        usuario.setCarteira(carteira);

        usuarioService.salvar(usuario, perfilEscolhido);
        return "redirect:/login?registered";
    }
}
