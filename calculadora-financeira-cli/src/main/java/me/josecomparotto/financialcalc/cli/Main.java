package me.josecomparotto.financialcalc.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import me.josecomparotto.financialcalc.core.juros.CalculadoraJurosCompostos;
import me.josecomparotto.financialcalc.core.juros.CalculadoraJurosSimples;
import me.josecomparotto.financialcalc.core.parcelas.CalculadoraParcelasPrice;
import me.josecomparotto.financialcalc.core.parcelas.CalculadoraParcelasSac;
import me.josecomparotto.financialcalc.core.parcelas.CalculadoraParcelasSemJuros;
import me.josecomparotto.financialcalc.core.parcelas.Parcela;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Locale;

@Command(
        name = "calcfin",
        description = "Calculadora Financeira - CLI",
        mixinStandardHelpOptions = true,
        subcommands = {
                Main.Simples.class,
                Main.Compostos.class,
                Main.SemJuros.class,
                Main.Sac.class,
                Main.Price.class
        }
)
public class Main implements Runnable {
    private static final MathContext MC = MathContext.DECIMAL128;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length == 0) {
            new Menu().runInteractive();
            return;
        }
        int exit = new CommandLine(new Main()).execute(args);
        System.exit(exit);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    static class CommonOptions {
        @Option(names = {"-p", "--precision"}, description = "Casas decimais (default: 2)")
        int precision = 2;
    }

    @Command(name = "simples", description = "Juros simples: calcula montante e juros")
    static class Simples implements Runnable {
        @Mixin CommonOptions common;

        @Option(names = {"-P", "--principal"}, required = true, description = "Principal (valor inicial)")
        BigDecimal principal;

        @Option(names = {"-i", "--taxa"}, required = true, description = "Taxa por periodo (ex.: 0.1 ou 10%%)")
        String taxaStr;

        @Option(names = {"-n", "--tempo"}, required = true, description = "Numero de periodos")
        int tempo;

        public void run() {
            BigDecimal taxa = parseRate(taxaStr);
            var calc = new CalculadoraJurosSimples(MC);
            System.out.println("Montante: " + calc.calcularMontante(principal, taxa, tempo));
            System.out.println("Juros: " + calc.calcularJuros(principal, taxa, tempo));
        }
    }

    @Command(name = "compostos", description = "Juros compostos: calcula montante e juros")
    static class Compostos implements Runnable {
        @Mixin CommonOptions common;

        @Option(names = {"-P", "--principal"}, required = true)
        BigDecimal principal;

        @Option(names = {"-i", "--taxa"}, required = true, description = "Taxa por periodo (ex.: 0.1 ou 10%%)")
        String taxaStr;

        @Option(names = {"-n", "--tempo"}, required = true)
        int tempo;

        public void run() {
            BigDecimal taxa = parseRate(taxaStr);
            var calc = new CalculadoraJurosCompostos(MC);
            System.out.println("Montante: " + calc.calcularMontante(principal, taxa, tempo));
            System.out.println("Juros: " + calc.calcularJuros(principal, taxa, tempo));
        }
    }

    @Command(name = "semjuros", description = "Parcelas iguais sem juros")
    static class SemJuros implements Runnable {
        @Mixin CommonOptions common;

        @Option(names = {"-P", "--principal"}, required = true)
        BigDecimal principal;

        @Option(names = {"-n", "--parcelas"}, required = true)
        int n;

        public void run() {
            var calc = new CalculadoraParcelasSemJuros(MC);
            List<Parcela> parcelas = calc.calcularParcelas(principal, n, common.precision);
            printParcelas(parcelas);
        }
    }

    @Command(name = "sac", description = "Sistema de Amortizacao Constante")
    static class Sac implements Runnable {
        @Mixin CommonOptions common;

        @Option(names = {"-P", "--principal"}, required = true)
        BigDecimal principal;

        @Option(names = {"-i", "--taxa"}, required = true, description = "Taxa por periodo (ex.: 0.1 ou 10%%)")
        String taxaStr;

        @Option(names = {"-n", "--parcelas"}, required = true)
        int n;

        public void run() {
            var calc = new CalculadoraParcelasSac(MC);
            List<Parcela> parcelas = calc.calcularParcelas(principal, parseRate(taxaStr), n, common.precision);
            printParcelas(parcelas);
        }
    }

    @Command(name = "price", description = "Sistema PRICE (prestacao fixa)")
    static class Price implements Runnable {
        @Mixin CommonOptions common;

        @Option(names = {"-P", "--principal"}, required = true)
        BigDecimal principal;

        @Option(names = {"-i", "--taxa"}, required = true, description = "Taxa por periodo (ex.: 0.1 ou 10%%)")
        String taxaStr;

        @Option(names = {"-n", "--parcelas"}, required = true)
        int n;

        public void run() {
            var calc = new CalculadoraParcelasPrice(MC);
            List<Parcela> parcelas = calc.calcularParcelas(principal, parseRate(taxaStr), n, common.precision);
            printParcelas(parcelas);
        }
    }

    private static void printParcelas(List<Parcela> parcelas) {
        System.out.println(" # | Parcela  | Amortizacao | Juros   | Saldo");
        BigDecimal totalParcela = BigDecimal.ZERO;
        BigDecimal totalAmort = BigDecimal.ZERO;
        BigDecimal totalJuros = BigDecimal.ZERO;
        for (Parcela p : parcelas) {
            System.out.printf("%2d | %8s | %11s | %7s | %s%n",
                    p.getSerie(),
                    p.getValorParcela(),
                    p.getValorAmortizacao(),
                    p.getValorJuros(),
                    p.getSaldoDevedor());
            totalParcela = totalParcela.add(p.getValorParcela());
            totalAmort = totalAmort.add(p.getValorAmortizacao());
            totalJuros = totalJuros.add(p.getValorJuros());
        }
        int scale = parcelas.isEmpty() ? 2 : parcelas.get(0).getValorParcela().scale();
        System.out.println("Totais:");
        System.out.println("  Parcelas: " + totalParcela.setScale(scale, java.math.RoundingMode.HALF_UP));
        System.out.println("  Amortizacao: " + totalAmort.setScale(scale, java.math.RoundingMode.HALF_UP));
        System.out.println("  Juros: " + totalJuros.setScale(scale, java.math.RoundingMode.HALF_UP));
        System.out.println();
    }

    private static BigDecimal parseRate(String s) {
        String original = s == null ? "" : s.trim();
        boolean hasPercent = original.contains("%");
        String raw = original.replace("%", "").replace(',', '.');
        BigDecimal v = new BigDecimal(raw);
        if (hasPercent || v.abs().compareTo(BigDecimal.ONE) >= 0) {
            return v.divide(BigDecimal.valueOf(100), MC);
        }
        return v;
    }
}