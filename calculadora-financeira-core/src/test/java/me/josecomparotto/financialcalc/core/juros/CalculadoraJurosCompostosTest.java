package me.josecomparotto.financialcalc.core.juros;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraJurosCompostosTest {

    private final MathContext mc = MathContext.DECIMAL128;
    private final Integer scale = 2;
    private final CalculadoraJurosCompostos calc = new CalculadoraJurosCompostos(mc);

    @Test
    void calcularMontante_basico() {
        BigDecimal montante = calc.calcularMontante(new BigDecimal("1000"), new BigDecimal("0.10"), 2, scale);
        assertEquals(new BigDecimal("1210.00"), montante.setScale(scale));
    }

    @Test
    void calcularJuros_basico() {
        BigDecimal juros = calc.calcularJuros(new BigDecimal("1000"), new BigDecimal("0.10"), 2, scale);
        assertEquals(new BigDecimal("210.00"), juros.setScale(scale));
    }

    // Edge cases
    @Test
    void zeroTempo_shouldYieldMontanteEqualsPrincipal_andZeroJuros() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("0.10");
        assertEquals(new BigDecimal("1000.00"), calc.calcularMontante(principal, taxa, 0, scale));
        assertEquals(new BigDecimal("0.00"), calc.calcularJuros(principal, taxa, 0, scale));
    }

    @Test
    void zeroTaxa_shouldYieldMontanteEqualsPrincipal_andZeroJuros() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = BigDecimal.ZERO;
        assertEquals(new BigDecimal("1000.00"), calc.calcularMontante(principal, taxa, 5, scale));
        assertEquals(new BigDecimal("0.00"), calc.calcularJuros(principal, taxa, 5, scale));
    }

    @Test
    void taxaNegativa_shouldThrow() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("-0.10");
        assertThrows(IllegalArgumentException.class, () -> calc.calcularMontante(principal, taxa, 2, scale));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularJuros(principal, taxa, 2, scale));
    }

    @Test
    void tempoNegativo_shouldThrow() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("0.10");
        assertThrows(IllegalArgumentException.class, () -> calc.calcularMontante(principal, taxa, -1, scale));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularJuros(principal, taxa, -1, scale));
    }

}
