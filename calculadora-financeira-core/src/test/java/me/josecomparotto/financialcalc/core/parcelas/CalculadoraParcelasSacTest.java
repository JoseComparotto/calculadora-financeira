package me.josecomparotto.financialcalc.core.parcelas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraParcelasSacTest {

    private final MathContext mc = MathContext.DECIMAL128;
    private final MathContext mcResultado = new MathContext(2, RoundingMode.HALF_UP);
    private final CalculadoraParcelasSac calc = new CalculadoraParcelasSac(mc, mcResultado);

    @Test
    void geraCalendarioSac_basico() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("0.10"); // 10% ao período
        int n = 4;

        List<Parcela> ps = calc.calcularParcelas(principal, taxa, n);
        assertEquals(n, ps.size());

        BigDecimal amortConst = principal.divide(BigDecimal.valueOf(n), mc).setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode());
        for (int i = 0; i < n; i++) {
            Parcela p = ps.get(i);
            assertEquals(i + 1, p.getSerie());
            assertEquals(amortConst, p.getValorAmortizacao());
            BigDecimal expectedSaldo = principal.subtract(amortConst.multiply(BigDecimal.valueOf(i + 1))).setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode());
            assertEquals(expectedSaldo, p.getSaldoDevedor());
            // juros = saldo anterior * taxa
            BigDecimal saldoAnterior = i == 0 ? principal
                    : principal.subtract(amortConst.multiply(BigDecimal.valueOf(i))).setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode());
            BigDecimal expectedJuros = saldoAnterior.multiply(taxa).setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode());
            assertEquals(expectedJuros, p.getValorJuros());
            assertEquals(p.getValorAmortizacao().add(p.getValorJuros()), p.getValorParcela());
        }
        assertEquals(new BigDecimal("0.00"), ps.get(n - 1).getSaldoDevedor());
    }

    @Test
    void taxaZero_funcionaSemJuros() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = BigDecimal.ZERO;
        int n = 5;
        int prec = 2;
        List<Parcela> ps = calc.calcularParcelas(principal, taxa, n);
        BigDecimal amort = principal.divide(BigDecimal.valueOf(n), mc).setScale(prec, RoundingMode.HALF_UP);
        ps.forEach(p -> {
            assertEquals(new BigDecimal("0.00"), p.getValorJuros());
            assertEquals(amort, p.getValorAmortizacao());
            assertEquals(amort, p.getValorParcela());
        });
        assertEquals(new BigDecimal("0.00"), ps.get(n - 1).getSaldoDevedor());
    }

    @Test
    void numeroParcelasZero_ouNegativo_deveLancar() {
        BigDecimal principal = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("0.10");
        assertThrows(IllegalArgumentException.class, () -> calc.calcularParcelas(principal, taxa, 0));
        assertThrows(IllegalArgumentException.class, () -> calc.calcularParcelas(principal, taxa, -1));
    }

    @Test
    void principalNaoDivisivel_residuoAmortizacaoDistribuido() {
        BigDecimal principal = new BigDecimal("1000.01");
        BigDecimal taxa = BigDecimal.ZERO; // simplifica verificação
        int n = 3;
        int prec = 2;
        List<Parcela> ps = new CalculadoraParcelasSac(mc, new MathContext(prec, RoundingMode.HALF_UP))
                .calcularParcelas(principal, taxa, n);
        assertEquals(n, ps.size());
        // Amortizações devem distribuir residuo: 333.34, 333.33, 333.34
        assertEquals(new BigDecimal("333.34"), ps.get(0).getValorAmortizacao());
        assertEquals(new BigDecimal("333.33"), ps.get(1).getValorAmortizacao());
        assertEquals(new BigDecimal("333.34"), ps.get(2).getValorAmortizacao());
        // Com taxa 0, parcela == amortização
        assertEquals(ps.get(0).getValorAmortizacao(), ps.get(0).getValorParcela());
        assertEquals(ps.get(1).getValorAmortizacao(), ps.get(1).getValorParcela());
        assertEquals(ps.get(2).getValorAmortizacao(), ps.get(2).getValorParcela());
        assertEquals(new BigDecimal("0.00"), ps.get(2).getSaldoDevedor());
    }
}
