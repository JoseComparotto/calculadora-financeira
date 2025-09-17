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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

        @Option(names = {"-f", "--format"}, description = "Formato de saida: csv ou humano (default: humano)", converter = FormatConverter.class)
        Format format = Format.HUMAN;

        @Option(names = {"-o", "--output"}, description = "Arquivo de saida (usar com -f csv)")
        String output;
    }

    enum Format { HUMAN, CSV }

    static class FormatConverter implements CommandLine.ITypeConverter<Format> {
        public Format convert(String value) {
            if (value == null) return Format.HUMAN;
            String v = value.trim().toLowerCase();
            if (v.isEmpty()) return Format.HUMAN;
            return switch (v) {
                case "csv" -> Format.CSV;
                case "human", "humano", "formatado", "formatted", "humana", "formatada" -> Format.HUMAN;
                default -> throw new CommandLine.TypeConversionException("Formato invalido: " + value + ". Use csv ou humano.");
            };
        }
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
            if (common.output != null && common.format != Format.CSV) {
                System.err.println("Erro: --output so e suportado com -f csv.");
                return;
            }
            BigDecimal taxa = parseRate(taxaStr);
            var calc = new CalculadoraJurosSimples(MC);
            BigDecimal montante = calc.calcularMontante(principal, taxa, tempo, common.precision);
            BigDecimal juros = calc.calcularJuros(principal, taxa, tempo, common.precision);
            printJuros("simples", common.format, common.output, principal, taxa, tempo, montante, juros);
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
            if (common.output != null && common.format != Format.CSV) {
                System.err.println("Erro: --output so e suportado com -f csv.");
                return;
            }
            BigDecimal taxa = parseRate(taxaStr);
            var calc = new CalculadoraJurosCompostos(MC);
            BigDecimal montante = calc.calcularMontante(principal, taxa, tempo, common.precision);
            BigDecimal juros = calc.calcularJuros(principal, taxa, tempo, common.precision);
            printJuros("compostos", common.format, common.output, principal, taxa, tempo, montante, juros);
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
            if (common.output != null && common.format != Format.CSV) {
                System.err.println("Erro: --output so e suportado com -f csv.");
                return;
            }
            MathContext mcResultado = new MathContext(common.precision, RoundingMode.HALF_UP);
            var calc = new CalculadoraParcelasSemJuros(MC, mcResultado);
            List<Parcela> parcelas = calc.calcularParcelas(principal, n);
            printParcelas(parcelas, common.format, common.output);
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
            if (common.output != null && common.format != Format.CSV) {
                System.err.println("Erro: --output so e suportado com -f csv.");
                return;
            }
            MathContext mcResultado = new MathContext(common.precision, RoundingMode.HALF_UP);
            var calc = new CalculadoraParcelasSac(MC, mcResultado);
            List<Parcela> parcelas = calc.calcularParcelas(principal, parseRate(taxaStr), n);
            printParcelas(parcelas, common.format, common.output);
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
            if (common.output != null && common.format != Format.CSV) {
                System.err.println("Erro: --output so e suportado com -f csv.");
                return;
            }
            MathContext mcResultado = new MathContext(common.precision, RoundingMode.HALF_UP);
            var calc = new CalculadoraParcelasPrice(MC, mcResultado);
            List<Parcela> parcelas = calc.calcularParcelas(principal, parseRate(taxaStr), n);
            printParcelas(parcelas, common.format, common.output);
        }
    }

    private static void printParcelas(List<Parcela> parcelas, Format format, String outputFile) {
        BigDecimal totalParcela = BigDecimal.ZERO;
        BigDecimal totalAmort = BigDecimal.ZERO;
        BigDecimal totalJuros = BigDecimal.ZERO;
        if (format == Format.CSV) {
            if (outputFile != null && !outputFile.isBlank()) {
                try {
                    Path path = Path.of(outputFile);
                    if (path.getParent() != null) Files.createDirectories(path.getParent());
                    try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
                        pw.println("serie,parcela,amortizacao,juros,saldo");
                        for (Parcela p : parcelas) {
                            pw.printf("%d,%s,%s,%s,%s%n",
                                    p.getSerie(),
                                    p.getValorParcela(),
                                    p.getValorAmortizacao(),
                                    p.getValorJuros(),
                                    p.getSaldoDevedor());
                            totalParcela = totalParcela.add(p.getValorParcela());
                            totalAmort = totalAmort.add(p.getValorAmortizacao());
                            totalJuros = totalJuros.add(p.getValorJuros());
                        }
                        pw.printf("TOTAL,%s,%s,%s,%s%n",
                                totalParcela,
                                totalAmort,
                                totalJuros,
                                "");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Falha ao escrever arquivo: " + e.getMessage(), e);
                }
            } else {
                System.out.println("serie,parcela,amortizacao,juros,saldo");
                for (Parcela p : parcelas) {
                    System.out.printf("%d,%s,%s,%s,%s%n",
                            p.getSerie(),
                            p.getValorParcela(),
                            p.getValorAmortizacao(),
                            p.getValorJuros(),
                            p.getSaldoDevedor());
                    totalParcela = totalParcela.add(p.getValorParcela());
                    totalAmort = totalAmort.add(p.getValorAmortizacao());
                    totalJuros = totalJuros.add(p.getValorJuros());
                }
                System.out.printf("TOTAL,%s,%s,%s,%s%n",
                        totalParcela,
                        totalAmort,
                        totalJuros,
                        "");
            }
        } else {
            System.out.println(" # | Parcela  | Amortizacao | Juros   | Saldo");
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
    }

    private static void printJuros(String tipo, Format format, String outputFile, BigDecimal principal, BigDecimal taxa, int tempo, BigDecimal montante, BigDecimal juros) {
        if (format == Format.CSV) {
            if (outputFile != null && !outputFile.isBlank()) {
                try {
                    Path path = Path.of(outputFile);
                    if (path.getParent() != null) Files.createDirectories(path.getParent());
                    try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
                        pw.println("tipo,principal,taxa,tempo,montante,juros");
                        pw.printf("%s,%s,%s,%d,%s,%s%n", tipo, principal, taxa, tempo, montante, juros);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Falha ao escrever arquivo: " + e.getMessage(), e);
                }
            } else {
                System.out.println("tipo,principal,taxa,tempo,montante,juros");
                System.out.printf("%s,%s,%s,%d,%s,%s%n", tipo, principal, taxa, tempo, montante, juros);
            }
        } else {
            System.out.println("Montante: " + montante);
            System.out.println("Juros: " + juros);
        }
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