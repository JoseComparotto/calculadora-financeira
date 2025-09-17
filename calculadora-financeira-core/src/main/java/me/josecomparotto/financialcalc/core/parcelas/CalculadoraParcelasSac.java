package me.josecomparotto.financialcalc.core.parcelas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraParcelasSac implements ICalculadoraParcelas {

    private final MathContext mc;

    public CalculadoraParcelasSac() {
        this.mc = MathContext.DECIMAL128;
    }

    public CalculadoraParcelasSac(MathContext mc) {
        this.mc = mc;
    }

    @Override
    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, BigDecimal taxaJuros, Integer numeroParcelas,
            Integer precisao) {

        if (valorPrincipal == null) {
            throw new IllegalArgumentException("Valor principal deve ser fornecido");
        }
        if (taxaJuros == null || taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("Taxa de juros deve ser fornecida e não pode ser negativa");
        }
        if (numeroParcelas == null || numeroParcelas <= 0) {
            throw new IllegalArgumentException("Número de parcelas deve ser positivo e diferente de zero");
        }
        if (precisao == null) {
            throw new IllegalArgumentException("Precisão deve ser fornecida");
        }

        BigDecimal valorAmortizacao = valorPrincipal.divide(BigDecimal.valueOf(numeroParcelas), mc);
        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorPrincipal;

        for (int i = 1; i <= numeroParcelas; i++) {

            BigDecimal novoSaldo = valorPrincipal.subtract(valorAmortizacao.multiply(BigDecimal.valueOf(i)));

            BigDecimal saldoAnteriorArred = saldoDevedor.setScale(precisao, RoundingMode.HALF_UP);
            BigDecimal novoSaldoArred = novoSaldo.setScale(precisao, RoundingMode.HALF_UP);

            BigDecimal valorAmortizacaoArred = saldoAnteriorArred.subtract(novoSaldoArred);

            BigDecimal valorJurosArred = saldoDevedor.multiply(taxaJuros).setScale(precisao, RoundingMode.HALF_UP);
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
