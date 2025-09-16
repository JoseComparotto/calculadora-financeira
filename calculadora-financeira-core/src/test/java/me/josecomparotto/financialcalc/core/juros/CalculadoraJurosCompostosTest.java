package me.josecomparotto.financialcalc.core.juros;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraJurosCompostosTest {

    private final MathContext mc = MathContext.DECIMAL128;
    private final CalculadoraJurosCompostos calc = new CalculadoraJurosCompostos(mc);

    @Test
    void calcularMontante_basico() {
        BigDecimal montante = calc.calcularMontante(new BigDecimal("1000"), new BigDecimal("0.10"), 2);
        assertEquals(new BigDecimal("1210.00"), montante.setScale(2));
    }

    @Test
    void calcularJuros_basico() {
        BigDecimal juros = calc.calcularJuros(new BigDecimal("1000"), new BigDecimal("0.10"), 2);
        assertEquals(new BigDecimal("210.00"), juros.setScale(2));
    }

    @Test
    void calcularTaxaJuros_compostos() {
        BigDecimal taxa = calc.calcularTaxaJuros(new BigDecimal("1000"), new BigDecimal("1210"), 2);
        // taxa composta deveria ser 0.10
        assertEquals(new BigDecimal("0.10"), taxa.setScale(2));
    }

    @Test
    void calcularTempo_compostos() {
        Integer tempo = calc.calcularTempo(new BigDecimal("1000"), new BigDecimal("1331"), new BigDecimal("0.10"));
        assertEquals(3, tempo);
    }

    @Test
    void calcularPrincipal_compostos() {
        BigDecimal principal = calc.calcularPrincipal(new BigDecimal("1210"), new BigDecimal("0.10"), 2);
        assertEquals(new BigDecimal("1000.00"), principal.setScale(2));
    }

    // Edge cases
    @Test
    void zeroTempo_shouldYieldMontanteEqualsPrincipal_andZeroJuros() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("0.10");
        assertEquals(new BigDecimal("1000.00"), calc.calcularMontante(principal, taxa, 0).setScale(2));
        assertEquals(new BigDecimal("0.00"), calc.calcularJuros(principal, taxa, 0).setScale(2));
    }

    @Test
    void zeroTaxa_shouldYieldMontanteEqualsPrincipal_andZeroJuros() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = BigDecimal.ZERO;
        assertEquals(new BigDecimal("1000.00"), calc.calcularMontante(principal, taxa, 5).setScale(2));
        assertEquals(new BigDecimal("0.00"), calc.calcularJuros(principal, taxa, 5).setScale(2));
    }

    @Test
    void taxaNegativa_shouldThrow() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("-0.10");
        assertThrows(IllegalArgumentException.class, () -> calc.calcularMontante(principal, taxa, 2));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularJuros(principal, taxa, 2));
    }

    @Test
    void tempoNegativo_shouldThrow() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("0.10");
        assertThrows(IllegalArgumentException.class, () -> calc.calcularMontante(principal, taxa, -1));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularJuros(principal, taxa, -1));
    }

    @Test
    void calcularTaxaJuros_withZeroTempo_shouldThrowArithmeticException() {
        assertThrows(ArithmeticException.class, () ->
            calc.calcularTaxaJuros(new BigDecimal("1000"), new BigDecimal("1100"), 0)
        );
    }
}
