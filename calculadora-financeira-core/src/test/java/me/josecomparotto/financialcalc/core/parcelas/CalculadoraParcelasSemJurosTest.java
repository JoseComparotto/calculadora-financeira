package me.josecomparotto.financialcalc.core.parcelas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraParcelasSemJurosTest {

    private final MathContext mc = MathContext.DECIMAL128;
    private final MathContext mcResultado = new MathContext(2, RoundingMode.HALF_UP);
    private final CalculadoraParcelasSemJuros calc = new CalculadoraParcelasSemJuros(mc, mcResultado);

    @Test
    void rejeitaTaxaNaoZero() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.calcularParcelas(new BigDecimal("1000"), new BigDecimal("0.01"), 5));
    }

    @Test
    void geraParcelasIguais_semJuros_eSaldoZerado() {
        List<Parcela> parcelas = calc.calcularParcelas(new BigDecimal("1000"), 5);
        assertEquals(5, parcelas.size());
        // cada parcela deve ser 200.00
        parcelas.forEach(p -> {
            assertEquals(0, p.getValorJuros().compareTo(BigDecimal.ZERO));
            assertEquals(new BigDecimal("200.00"), p.getValorParcela());
            assertEquals(new BigDecimal("200.00"), p.getValorAmortizacao());
            assertTrue(p.getSaldoDevedor().compareTo(BigDecimal.ZERO) >= 0);
        });
        assertEquals(new BigDecimal("0.00"), parcelas.get(4).getSaldoDevedor());
    }

    @Test
    void principalNaoDivisivel_residuoArredondadoDistribuido() {
        BigDecimal principal = new BigDecimal("1000.01");
        int n = 3;
        int prec = 2;
        List<Parcela> parcelas = new CalculadoraParcelasSemJuros(mc, new MathContext(prec, RoundingMode.HALF_UP))
                .calcularParcelas(principal, n);
        assertEquals(n, parcelas.size());
        // Esperado: 333.34, 333.33, 333.34 (soma = 1000.01)
        assertEquals(new BigDecimal("333.34"), parcelas.get(0).getValorParcela());
        assertEquals(new BigDecimal("333.33"), parcelas.get(1).getValorParcela());
        assertEquals(new BigDecimal("333.34"), parcelas.get(2).getValorParcela());
        BigDecimal soma = parcelas.stream().map(Parcela::getValorParcela).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("1000.01"), soma.setScale(2));
        assertEquals(new BigDecimal("0.00"), parcelas.get(2).getSaldoDevedor());
    }

    @Test
    void numeroParcelasZero_ouNegativo_deveLancar() {
        assertThrows(IllegalArgumentException.class, () -> calc.calcularParcelas(new BigDecimal("1000"), 0));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularParcelas(new BigDecimal("1000"), -1));
    }
}
