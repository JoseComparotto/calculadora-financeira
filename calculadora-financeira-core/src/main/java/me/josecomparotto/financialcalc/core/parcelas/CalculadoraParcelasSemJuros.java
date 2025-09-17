package me.josecomparotto.financialcalc.core.parcelas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraParcelasSemJuros implements ICalculadoraParcelas {

    private final MathContext mcCalculo;
    private final MathContext mcResultado;

    public CalculadoraParcelasSemJuros() {
        this.mcCalculo = MathContext.DECIMAL128;
        this.mcResultado = new MathContext(2, RoundingMode.HALF_UP);
    }

    public CalculadoraParcelasSemJuros(MathContext mcCalculo, MathContext mcResultado) {
        this.mcCalculo = mcCalculo == null ? MathContext.DECIMAL128 : mcCalculo;
        this.mcResultado = mcResultado == null ? new MathContext(2, RoundingMode.HALF_UP) : mcResultado;
    }

    @Override
    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, BigDecimal taxaJuros, Integer numeroParcelas) {

        if (taxaJuros != null && taxaJuros.signum() != 0) {
            throw new IllegalArgumentException("Esta calculadora é apenas para parcelas sem juros");
        }

        return calcularParcelas(valorPrincipal, numeroParcelas);
    }

    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, Integer numeroParcelas) {
        
        if (valorPrincipal == null) {
            throw new IllegalArgumentException("Valor principal deve ser fornecido");
        }
        if (numeroParcelas == null || numeroParcelas <= 0) {
            throw new IllegalArgumentException("Número de parcelas deve ser positivo e diferente de zero");
        }
        BigDecimal valorParcela = valorPrincipal.divide(BigDecimal.valueOf(numeroParcelas), mcCalculo);
        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorPrincipal;

        for (int i = 1; i <= numeroParcelas; i++) {

            BigDecimal novoSaldo = valorPrincipal.subtract(valorParcela.multiply(BigDecimal.valueOf(i)));

            int scale = mcResultado.getPrecision() > 0 ? mcResultado.getPrecision() : 2;
            RoundingMode rm = mcResultado.getRoundingMode() == null ? RoundingMode.HALF_UP : mcResultado.getRoundingMode();
            BigDecimal saldoAnteriorArred = saldoDevedor.setScale(scale, rm);
            BigDecimal novoSaldoArred = novoSaldo.setScale(scale, rm);

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
