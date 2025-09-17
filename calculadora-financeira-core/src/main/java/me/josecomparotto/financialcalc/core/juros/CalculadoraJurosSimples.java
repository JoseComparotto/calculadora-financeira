package me.josecomparotto.financialcalc.core.juros;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CalculadoraJurosSimples implements ICalculadoraJuros<BigDecimal, BigDecimal, Integer> {

    private final MathContext mc;

    public CalculadoraJurosSimples() {
        this.mc = MathContext.DECIMAL128;
    }

    public CalculadoraJurosSimples(MathContext mc) {
        this.mc = mc;
    }

    @Override
    public BigDecimal calcularJuros(BigDecimal principal, BigDecimal taxaJuros, Integer tempo, Integer precisao) {
        if (principal == null || taxaJuros == null || tempo == null || precisao == null) {
            throw new IllegalArgumentException("Todos os argumentos devem ser fornecidos");
        }
        if (tempo < 0) {
            throw new IllegalArgumentException("tempo não pode ser negativo em juros simples");
        }
        if (taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("taxa de juros não pode ser negativa em juros simples");
        }
        return principal
                .setScale(precisao, RoundingMode.HALF_UP)
                .multiply(taxaJuros, mc)
                .multiply(BigDecimal.valueOf(tempo), mc)
                .setScale(precisao, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularMontante(BigDecimal principal, BigDecimal taxaJuros, Integer tempo, Integer precisao) {
        BigDecimal juros = calcularJuros(principal, taxaJuros, tempo, precisao);
        return principal.add(juros, mc)
                .setScale(precisao, RoundingMode.HALF_UP);
    }

}
