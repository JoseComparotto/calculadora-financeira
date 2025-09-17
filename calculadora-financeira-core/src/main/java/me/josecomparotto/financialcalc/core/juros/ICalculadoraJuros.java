package me.josecomparotto.financialcalc.core.juros;


public interface ICalculadoraJuros<MoneyType extends Number, RateType extends Number, TimeType extends Number> {

    MoneyType calcularJuros(MoneyType principal, RateType taxaJuros, TimeType tempo, Integer precisao);
    MoneyType calcularMontante(MoneyType principal, RateType taxaJuros, TimeType tempo, Integer precisao);

}
