package br.com.oinkvest.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String nome;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Carteira carteira;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<Operacao> operacoes;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<Notificacao> notificacoes;
}
