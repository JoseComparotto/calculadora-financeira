package me.josecomparotto.financialcalc.core.parcelas;

import java.math.BigDecimal;
import java.util.List;

public interface ICalculadoraParcelas {

    List<Parcela> calcularParcelas(
            BigDecimal valorPrincipal,
            BigDecimal taxaJuros,
            Integer numeroParcelas);

}
