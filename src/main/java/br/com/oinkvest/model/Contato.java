package br.com.oinkvest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contatos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String assunto;

    @Column(length = 1000)
    private String mensagem;
}
