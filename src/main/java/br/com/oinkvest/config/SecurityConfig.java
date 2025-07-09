package br.com.oinkvest.config;

import br.com.oinkvest.model.Role;
import br.com.oinkvest.repository.RoleRepository;
import br.com.oinkvest.service.CustomUserDetailsService;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Criptografia de senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Autenticação baseada no banco de dados
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }

    // AuthenticationManager para uso em autenticação via API
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Regras de segurança (páginas públicas e protegidas)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // .csrf(csrf->csrf.disable())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/403", "/register", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/api/mobile/login").permitAll()
                        .requestMatchers("/history").hasRole("CARTEIRA")
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/unauthorized"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            List<String> roles = List.of("ROLE_ANALISTA", "ROLE_CARTEIRA", "ROLE_ADMIN");

            for (String nomeRole : roles) {
                if (roleRepository.findByName(nomeRole) == null) {
                    Role role = new Role();
                    role.setName(nomeRole);
                    roleRepository.save(role);
                }
            }
        };
    }
}