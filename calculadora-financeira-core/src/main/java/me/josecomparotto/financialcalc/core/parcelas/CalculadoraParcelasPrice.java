package me.josecomparotto.financialcalc.core.parcelas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraParcelasPrice implements ICalculadoraParcelas {

    private final MathContext mc;

    public CalculadoraParcelasPrice(MathContext mc) {
        this.mc = mc;
    }

    public CalculadoraParcelasPrice() {
        this.mc = MathContext.DECIMAL128;
    }

    @Override
    public List<Parcela> calcularParcelas(BigDecimal valorPrincipal, BigDecimal taxaJuros, Integer numeroParcelas,
            Integer precisao) {
        
        if (valorPrincipal == null) {
            throw new IllegalArgumentException("Valor principal deve ser fornecido");
        }
        if( taxaJuros == null || taxaJuros.signum() <= 0) {
            throw new IllegalArgumentException("Taxa de juros deve ser positiva e diferente de zero");
        }
        if (numeroParcelas == null || numeroParcelas <= 0) {
            throw new IllegalArgumentException("Número de parcelas deve ser positivo e diferente de zero");
        }
        if (precisao == null) {
            throw new IllegalArgumentException("Precisão deve ser fornecida");
        }

        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorPrincipal;
        BigDecimal valorParcela = calcularValorPrestacao(valorPrincipal, taxaJuros, numeroParcelas, mc);

        BigDecimal valorParcelaArred = valorParcela.setScale(precisao, RoundingMode.HALF_UP);
        BigDecimal taxaJurosAjustada = calcularTaxaJurosAjustada(
                valorPrincipal, numeroParcelas, valorParcela, valorParcelaArred, taxaJuros, mc);

        for (int i = 1; i <= numeroParcelas; i++) {
            BigDecimal jurosReais, // J_k
                    jurosArred, // J_k'
                    amortizacaoReal, // A_k
                    amortizacaoArred, // A_k'
                    novoSaldoDevedor, // SD_k
                    novoSaldoDevedorArred; // SD_k'

            // Calcula os juros da parcela
            // J_k = (SD_{k-1}) * i
            jurosReais = saldoDevedor.multiply(taxaJurosAjustada);

            // Calcula a amortização da parcela
            // A_k = P - J_k
            amortizacaoReal = valorParcelaArred.subtract(jurosReais);

            // Calcula o novo saldo devedor
            // SD_k = SD_{k-1} - A_k
            novoSaldoDevedor = saldoDevedor.subtract(amortizacaoReal);

            // Arredonda o novo saldo devedor para a precisão desejada
            // SD_k' = round(SD_k, 2)
            novoSaldoDevedorArred = novoSaldoDevedor.setScale(precisao, RoundingMode.HALF_UP);

            // Calcula a amortização arredondada para garantir que o saldo devedor zere no
            // final.
            // A_k' = round(SD_{k-1}, 2) - SD_k'
            amortizacaoArred = saldoDevedor.setScale(2, RoundingMode.HALF_UP)
                    .subtract(novoSaldoDevedorArred);

            // Calcula os juros arredondados
            // J_k' = P - A_k'
            jurosArred = valorParcelaArred.subtract(amortizacaoArred);

            // Atualiza o saldo devedor para a próxima iteração
            saldoDevedor = novoSaldoDevedor;

            // Monta o objeto Parcela com os valores calculados.
            Parcela parcela = new Parcela();
            parcela.setSerie(i);
            parcela.setValorParcela(valorParcelaArred);
            parcela.setValorAmortizacao(amortizacaoArred);
            parcela.setValorJuros(jurosArred);
            parcela.setSaldoDevedor(novoSaldoDevedorArred);
            parcelas.add(parcela);
        }

        return parcelas;
    }

    // Calcula o valor fixo da prestação usando a fórmula do sistema PRICE.
    // P(V, i, n) = (V * i) / (1 - (1 + i)^-n)
    private BigDecimal calcularValorPrestacao(
            BigDecimal valorPrincipal,
            BigDecimal taxaJuros,
            int quantidadeParcelas,
            MathContext mc) {
        // Fórmula do sistema PRICE:
        return valorPrincipal
                .multiply(taxaJuros)
                .divide(BigDecimal.ONE.subtract(
                        BigDecimal.ONE.divide(
                                BigDecimal.ONE.add(taxaJuros), mc)
                                .pow(quantidadeParcelas, mc),
                        mc), mc);
    }

    // Derivada parcial de P(V, i, n) em relação a i
    // dP/di = (V*(1-(1+i)^(-n)) - V*i*n*(1+i)^(-n-1)) / (1-(1+i)^(-n))^2
    // P'(V, i, n) = (V*(1-(1+i)^(-n)) - V*i*n*(1+i)^(-n-1)) / (1-(1+i)^(-n))^2
    private BigDecimal derivadaValorPrestacaoPorTaxaJuros(
            BigDecimal valorPrincipal, // V
            BigDecimal taxaJuros, // i
            int quantidadeParcelas, // n
            MathContext mc) {

        BigDecimal umMaisI = BigDecimal.ONE.add(taxaJuros, mc); // (1+i)
        BigDecimal umMaisI_Pot_MenosN = BigDecimal.ONE
                .divide(umMaisI, mc)
                .pow(quantidadeParcelas, mc); // (1+i)^(-n)
        BigDecimal umMaisI_Pot_MenosNMenos1 = BigDecimal.ONE
                .divide(umMaisI, mc)
                .pow(quantidadeParcelas + 1, mc); // (1+i)^(-n-1)
        BigDecimal umMenos_umMaisI_Pot_MenosN = BigDecimal.ONE
                .subtract(umMaisI_Pot_MenosN, mc);

        // Numerador: V*(1-(1+i)^(-n)) - V*i*n*(1+i)^(-n-1)
        BigDecimal numerador = valorPrincipal
                .multiply(umMenos_umMaisI_Pot_MenosN, mc)
                .subtract(
                        valorPrincipal
                                .multiply(taxaJuros, mc)
                                .multiply(BigDecimal.valueOf(quantidadeParcelas), mc)
                                .multiply(umMaisI_Pot_MenosNMenos1, mc),
                        mc);

        // Denominador: (1-(1+i)^(-n))^2
        BigDecimal denominador = umMenos_umMaisI_Pot_MenosN.pow(2, mc);

        return numerador.divide(denominador, mc);
    }

    // Calcula o ajuste na taxa de juros necessário para corrigir a diferença
    // na prestação.
    // di = dP / (dP/di)
    // di(V, i, n, dP) = dP / P'(V, i, n)
    private BigDecimal calcularAjusteTaxaJuros(
            BigDecimal valorPrincipal, // V
            BigDecimal taxaJuros, // i
            int quantidadeParcelas, // n
            BigDecimal diferencaPrestacao, // dP
            MathContext mc) {
        // dP/di = P'(V, i, n)
        BigDecimal derivada = derivadaValorPrestacaoPorTaxaJuros(
                valorPrincipal, taxaJuros, quantidadeParcelas, mc);

        // di = dP / (dP/di)
        return diferencaPrestacao.divide(derivada, mc);
    }

    // Calcula a taxa de juros ajustada para que a prestação calculada se
    // aproxime da desejada.
    // i(V, n, P0, P, i0) = i0 + di
    private BigDecimal calcularTaxaJurosAjustada(
            BigDecimal valorPrincipal, // V
            int quantidadeParcelas, // n
            BigDecimal prestacaoCalculada, // P0
            BigDecimal prestacaoDesejada, // P
            BigDecimal taxaJurosInicial, // i0
            MathContext mc) {
        // dP = P - P0
        BigDecimal diferencaPrestacao = prestacaoDesejada.subtract(prestacaoCalculada);

        // di = di(V, i, n, dP)
        BigDecimal ajusteTaxaJuros = calcularAjusteTaxaJuros(
                valorPrincipal, taxaJurosInicial, quantidadeParcelas, diferencaPrestacao, mc);

        // i = i0 + di
        return taxaJurosInicial.add(ajusteTaxaJuros);
    }
}
