package me.josecomparotto.financialcalc.core.parcelas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraParcelasSemJuros implements ICalculadoraParcelas {

    private final MathContext mc;

    public CalculadoraParcelasSemJuros() {
        this.mc = MathContext.DECIMAL128;
    }

    public CalculadoraParcelasSemJuros(MathContext mc) {
        this.mc = mc;
    }

    @Override
    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, BigDecimal taxaJuros, Integer numeroParcelas,
            Integer precisao) {

        if (taxaJuros != null && taxaJuros.signum() != 0) {
            throw new IllegalArgumentException("Esta calculadora é apenas para parcelas sem juros");
        }

        return calcularParcelas(valorPrincipal, numeroParcelas, precisao);
    }

    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, Integer numeroParcelas, Integer precisao) {
        
        if (valorPrincipal == null) {
            throw new IllegalArgumentException("Valor principal deve ser fornecido");
        }
        if (numeroParcelas == null || numeroParcelas <= 0) {
            throw new IllegalArgumentException("Número de parcelas deve ser positivo e diferente de zero");
        }
        if (precisao == null) {
            throw new IllegalArgumentException("Precisão deve ser fornecida");
        }
        
        BigDecimal valorParcela = valorPrincipal.divide(BigDecimal.valueOf(numeroParcelas), mc);
        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorPrincipal;

        for (int i = 1; i <= numeroParcelas; i++) {

            BigDecimal novoSaldo = valorPrincipal.subtract(valorParcela.multiply(BigDecimal.valueOf(i)));

            BigDecimal saldoAnteriorArred = saldoDevedor.setScale(precisao, RoundingMode.HALF_UP);
            BigDecimal novoSaldoArred = novoSaldo.setScale(precisao, RoundingMode.HALF_UP);

            BigDecimal valorParcelaArred = saldoAnteriorArred.subtract(novoSaldoArred);

            saldoDevedor = novoSaldo;

            Parcela parcela = new Parcela();
            parcela.setSerie(i);
            parcela.setValorParcela(valorParcelaArred);
            parcela.setValorAmortizacao(valorParcelaArred);
            parcela.setValorJuros(BigDecimal.ZERO);
            parcela.setSaldoDevedor(novoSaldoArred);
            parcelas.add(parcela);
        }

        return parcelas;
    }

}
