package me.josecomparotto.financialcalc.core.parcelas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraParcelasPriceTest {

    private final MathContext mc = MathContext.DECIMAL128;
    private final MathContext mcResultado = new MathContext(2, RoundingMode.HALF_UP);
    private final CalculadoraParcelasPrice calc = new CalculadoraParcelasPrice(mc, mcResultado);

    private BigDecimal prestacaoEsperada(BigDecimal V, BigDecimal i, int n) {
        BigDecimal umMaisI = BigDecimal.ONE.add(i, mc);
        BigDecimal umMaisI_pow_neg_n = BigDecimal.ONE.divide(umMaisI, mc).pow(n, mc);
        BigDecimal denominador = BigDecimal.ONE.subtract(umMaisI_pow_neg_n, mc);
        BigDecimal P = V.multiply(i, mc).divide(denominador, mc);
        return P.setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode());
    }

    @Test
    void price_basico_parcelaFixa_saldoFinalZero() {
        BigDecimal V = new BigDecimal("1000.00");
        BigDecimal i = new BigDecimal("0.10");
        int n = 3;

        List<Parcela> ps = calc.calcularParcelas(V, i, n);
        assertEquals(n, ps.size());

        BigDecimal parcelaEsperada = prestacaoEsperada(V, i, n);
        ps.forEach(p -> assertEquals(parcelaEsperada, p.getValorParcela()));

        BigDecimal somaAmort = ps.stream().map(Parcela::getValorAmortizacao).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(V, somaAmort.setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode()));
        assertEquals(new BigDecimal("0.00"), ps.get(n - 1).getSaldoDevedor());
    }

    @Test
    void principalNaoDivisivel_residuo_arredondamentoConsistente() {
        BigDecimal V = new BigDecimal("1000.01");
        BigDecimal i = new BigDecimal("0.10");
        int n = 3;

        List<Parcela> ps = calc.calcularParcelas(V, i, n);
        assertEquals(n, ps.size());

        BigDecimal parcelaEsperada = prestacaoEsperada(V, i, n);
        ps.forEach(p -> assertEquals(parcelaEsperada, p.getValorParcela()));

        BigDecimal somaAmort = ps.stream().map(Parcela::getValorAmortizacao).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(V, somaAmort.setScale(mcResultado.getPrecision(), mcResultado.getRoundingMode()));
        assertEquals(new BigDecimal("0.00"), ps.get(n - 1).getSaldoDevedor());
    }

    @Test
    void taxaZero_deveLancar() {
        BigDecimal V = new BigDecimal("1000.00");
        BigDecimal i = BigDecimal.ZERO;
        int n = 3;
        assertThrows(IllegalArgumentException.class, () -> calc.calcularParcelas(V, i, n));
    }
}
