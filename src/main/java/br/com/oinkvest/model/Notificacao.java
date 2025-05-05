package br.com.oinkvest.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacao {

    public enum Condicao {
        ACIMA,
        ABAIXO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String moeda;

    @Column(nullable = false)
    private Double precoAlvo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condicao condicao;

    @Column(nullable = false)
    private Boolean ativa;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
