package me.josecomparotto.financialcalc.core.juros;


public interface ICalculadoraJuros<MoneyType extends Number, RateType extends Number, TimeType extends Number> {

    MoneyType calcularJuros(MoneyType principal, RateType taxaJuros, TimeType tempo);
    MoneyType calcularMontante(MoneyType principal, RateType taxaJuros, TimeType tempo);
    RateType calcularTaxaJuros(MoneyType principal, MoneyType montante, TimeType tempo);
    TimeType calcularTempo(MoneyType principal, MoneyType montante, RateType taxaJuros);
    MoneyType calcularPrincipal(MoneyType montante, RateType taxaJuros, TimeType tempo);

}
