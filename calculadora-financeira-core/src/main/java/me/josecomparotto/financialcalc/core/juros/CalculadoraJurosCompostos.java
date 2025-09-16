package me.josecomparotto.financialcalc.core.juros;

import java.math.BigDecimal;
import java.math.MathContext;

public class CalculadoraJurosCompostos implements ICalculadoraJuros<BigDecimal, BigDecimal, Integer> {

    private final MathContext mc;

    public CalculadoraJurosCompostos() {
        this.mc = MathContext.DECIMAL128;
    }

    public CalculadoraJurosCompostos(MathContext mc) {
        this.mc = mc;
    }

    @Override
    public BigDecimal calcularMontante(BigDecimal principal, BigDecimal taxaJuros, Integer tempo) {
        if (tempo != null && tempo < 0) {
            throw new IllegalArgumentException("tempo não pode ser negativo em juros compostos");
        }
        if (taxaJuros != null && taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("taxa de juros não pode ser negativa em juros compostos");
        }
        return principal.multiply(BigDecimal.ONE.add(taxaJuros).pow(tempo));
    }

    @Override
    public BigDecimal calcularJuros(BigDecimal principal, BigDecimal taxaJuros, Integer tempo) {
        return calcularMontante(principal, taxaJuros, tempo).subtract(principal);
    }

    @Override
    public BigDecimal calcularTaxaJuros(BigDecimal principal, BigDecimal montante, Integer tempo) {
        if (tempo == null || tempo <= 0) {
            throw new ArithmeticException("tempo deve ser positivo para taxa composta");
        }
        BigDecimal ratio = montante.divide(principal, mc);
        double root = Math.pow(ratio.doubleValue(), 1.0 / tempo);
        return BigDecimal.valueOf(root).subtract(BigDecimal.ONE);
    }

    @Override
    public Integer calcularTempo(BigDecimal principal, BigDecimal montante, BigDecimal taxaJuros) {
        double ratio = montante.divide(principal, mc).doubleValue();
        double base = BigDecimal.ONE.add(taxaJuros).doubleValue();
        return (int) Math.round(Math.log(ratio) / Math.log(base));
    }

    @Override
    public BigDecimal calcularPrincipal(BigDecimal montante, BigDecimal taxaJuros, Integer tempo) {
        return montante.divide(BigDecimal.ONE.add(taxaJuros).pow(tempo), mc);
    }
}
