package br.com.oinkvest.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operacao {

    public enum TipoOperacao {
        DEPOSITO, SAQUE, COMPRA, VENDA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOperacao tipo;

    @Column(nullable = false)
    private String moeda; // Ex: USDT, BTC, ETH...

    @Column(nullable = false, precision = 38, scale = 4)
    private BigDecimal quantidade;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal valor; // valor total da operação

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
