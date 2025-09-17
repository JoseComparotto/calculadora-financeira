package me.josecomparotto.financialcalc.core.parcelas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraParcelasSac implements ICalculadoraParcelas {

    private final MathContext mcCalculo;
    private final MathContext mcResultado;

    public CalculadoraParcelasSac() {
        this.mcCalculo = MathContext.DECIMAL128;
        this.mcResultado = new MathContext(2, RoundingMode.HALF_UP);
    }

    public CalculadoraParcelasSac(MathContext mcCalculo, MathContext mcResultado) {
        this.mcCalculo = mcCalculo == null ? MathContext.DECIMAL128 : mcCalculo;
        this.mcResultado = mcResultado == null ? new MathContext(2, RoundingMode.HALF_UP) : mcResultado;
    }

    @Override
    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, BigDecimal taxaJuros, Integer numeroParcelas) {

        if (valorPrincipal == null) {
            throw new IllegalArgumentException("Valor principal deve ser fornecido");
        }
        if (taxaJuros == null || taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("Taxa de juros deve ser fornecida e não pode ser negativa");
        }
        if (numeroParcelas == null || numeroParcelas <= 0) {
            throw new IllegalArgumentException("Número de parcelas deve ser positivo e diferente de zero");
        }
        
        BigDecimal valorAmortizacao = valorPrincipal.divide(BigDecimal.valueOf(numeroParcelas), mcCalculo);
        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorPrincipal;

        for (int i = 1; i <= numeroParcelas; i++) {

            BigDecimal novoSaldo = valorPrincipal.subtract(valorAmortizacao.multiply(BigDecimal.valueOf(i)));

            int scale = mcResultado.getPrecision() > 0 ? mcResultado.getPrecision() : 2;
            RoundingMode rm = mcResultado.getRoundingMode() == null ? RoundingMode.HALF_UP : mcResultado.getRoundingMode();
            BigDecimal saldoAnteriorArred = saldoDevedor.setScale(scale, rm);
            BigDecimal novoSaldoArred = novoSaldo.setScale(scale, rm);

            BigDecimal valorAmortizacaoArred = saldoAnteriorArred.subtract(novoSaldoArred);

            BigDecimal valorJurosArred = saldoDevedor.multiply(taxaJuros).setScale(scale, rm);
            BigDecimal valorParcelaArred = valorAmortizacaoArred.add(valorJurosArred);

            saldoDevedor = novoSaldo;

            Parcela parcela = new Parcela();
            parcela.setSerie(i);
            parcela.setValorParcela(valorParcelaArred);
            parcela.setValorAmortizacao(valorAmortizacaoArred);
            parcela.setValorJuros(valorJurosArred);
            parcela.setSaldoDevedor(novoSaldoArred);
            parcelas.add(parcela);
        }

        return parcelas;
    }

}
