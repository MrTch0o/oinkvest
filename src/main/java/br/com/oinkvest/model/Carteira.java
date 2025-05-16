package br.com.oinkvest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private double saldoFiat;

    private double saldoTrade;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    private Usuario usuario;
}
