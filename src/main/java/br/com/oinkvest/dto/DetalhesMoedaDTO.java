package br.com.oinkvest.dto;

import java.math.BigDecimal;

public class DetalhesMoedaDTO {
    private BigDecimal saldoMoeda;
    private BigDecimal precoMedio;

    public BigDecimal getSaldoMoeda() {
        return saldoMoeda;
    }

    public void setSaldoMoeda(BigDecimal saldoMoeda) {
        this.saldoMoeda = saldoMoeda;
    }

    public BigDecimal getPrecoMedio() {
        return precoMedio;
    }

    public void setPrecoMedio(BigDecimal precoMedio) {
        this.precoMedio = precoMedio;
    }
}
