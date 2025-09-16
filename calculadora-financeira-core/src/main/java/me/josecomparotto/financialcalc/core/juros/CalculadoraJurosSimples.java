package me.josecomparotto.financialcalc.core.juros;

import java.math.BigDecimal;
import java.math.MathContext;

public class CalculadoraJurosSimples implements ICalculadoraJuros<BigDecimal, BigDecimal, Integer> {

    private final MathContext mc;

    public CalculadoraJurosSimples() {
        this.mc = MathContext.DECIMAL128;
    }

    public CalculadoraJurosSimples(MathContext mc) {
        this.mc = mc;
    }

    @Override
    public BigDecimal calcularJuros(BigDecimal principal, BigDecimal taxaJuros, Integer tempo) {
        if (tempo != null && tempo < 0) {
            throw new IllegalArgumentException("tempo não pode ser negativo em juros simples");
        }
        if (taxaJuros != null && taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("taxa de juros não pode ser negativa em juros simples");
        }
        return principal.multiply(taxaJuros).multiply(BigDecimal.valueOf(tempo));
    }

    @Override
    public BigDecimal calcularMontante(BigDecimal principal, BigDecimal taxaJuros, Integer tempo) {
        BigDecimal juros = calcularJuros(principal, taxaJuros, tempo);
        return principal.add(juros);
    }

    @Override
    public BigDecimal calcularTaxaJuros(BigDecimal principal, BigDecimal montante, Integer tempo) {
        BigDecimal juros = montante.subtract(principal);
        return juros.divide(principal, mc).divide(BigDecimal.valueOf(tempo), mc);
    }

    @Override
    public Integer calcularTempo(BigDecimal principal, BigDecimal montante, BigDecimal taxaJuros) {
        BigDecimal juros = montante.subtract(principal);
        return juros.divide(principal, mc).divide(taxaJuros, mc).intValue();
    }

    @Override
    public BigDecimal calcularPrincipal(BigDecimal montante, BigDecimal taxaJuros, Integer tempo) {
        BigDecimal divisor = BigDecimal.ONE.add(taxaJuros.multiply(BigDecimal.valueOf(tempo)));
        return montante.divide(divisor, mc);
    }
}
