package br.com.oinkvest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carteiras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Saldo disponível em USDT
    @Column(nullable = false)
    private Double saldoFiat;

    // Saldo de moedas compradas (valor agregado)
    @Column(nullable = false)
    private Double saldoTrade;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
}
