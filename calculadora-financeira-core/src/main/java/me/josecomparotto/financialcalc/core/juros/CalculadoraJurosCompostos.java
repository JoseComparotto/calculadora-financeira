package me.josecomparotto.financialcalc.core.juros;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CalculadoraJurosCompostos implements ICalculadoraJuros<BigDecimal, BigDecimal, Integer> {

    private final MathContext mc;

    public CalculadoraJurosCompostos() {
        this.mc = MathContext.DECIMAL128;
    }

    public CalculadoraJurosCompostos(MathContext mc) {
        this.mc = mc;
    }

    @Override
    public BigDecimal calcularMontante(BigDecimal principal, BigDecimal taxaJuros, Integer tempo, Integer precisao) {
        if (principal == null || taxaJuros == null || tempo == null || precisao == null) {
            throw new IllegalArgumentException("Todos os argumentos devem ser fornecidos");
        }
        if (tempo < 0) {
            throw new IllegalArgumentException("tempo não pode ser negativo em juros compostos");
        }
        if (taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("taxa de juros não pode ser negativa em juros compostos");
        }
        return principal
            .setScale(precisao, RoundingMode.HALF_UP)
            .multiply(BigDecimal.ONE.add(taxaJuros, mc).pow(tempo, mc), mc)
            .setScale(precisao, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularJuros(BigDecimal principal, BigDecimal taxaJuros, Integer tempo, Integer precisao) {
        BigDecimal montante = calcularMontante(principal, taxaJuros, tempo, precisao);
        return montante
            .subtract(principal, mc)
            .setScale(precisao, RoundingMode.HALF_UP);
    }

}
