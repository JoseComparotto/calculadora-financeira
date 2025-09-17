package me.josecomparotto.financialcalc.cli;

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
import java.util.Scanner;

public class Menu {
    private static final MathContext MC = MathContext.DECIMAL128;

    public void runInteractive() {
        Locale.setDefault(Locale.US);
        Scanner sc = new Scanner(System.in);
        System.out.println("Calculadora Financeira CLI\n");
        boolean running = true;
        while (running) {
            try {
                System.out.println("Selecione uma opcao:");
                System.out.println("  1) Juros Simples");
                System.out.println("  2) Juros Compostos");
                System.out.println("  3) Parcelas Sem Juros");
                System.out.println("  4) Parcelas SAC");
                System.out.println("  5) Parcelas PRICE");
                System.out.println("  0) Sair");
                System.out.print("> ");
                String opt = sc.nextLine().trim();
                switch (opt) {
                    case "1" -> menuJurosSimples(sc);
                    case "2" -> menuJurosCompostos(sc);
                    case "3" -> menuParcelasSemJuros(sc);
                    case "4" -> menuParcelasSac(sc);
                    case "5" -> menuParcelasPrice(sc);
                    case "0" -> running = false;
                    default -> System.out.println("Opcao invalida.\n");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
        System.out.println("Ate mais!");
    }

    private BigDecimal readBigDecimal(Scanner sc, String label) {
        while (true) {
            try {
                System.out.print(label + ": ");
                String s = sc.nextLine().trim().replace(',', '.');
                return new BigDecimal(s);
            } catch (NumberFormatException ex) {
                System.out.println("Valor invalido, tente novamente.");
            }
        }
    }

    private int readInt(Scanner sc, String label) {
        while (true) {
            try {
                System.out.print(label + ": ");
                String s = sc.nextLine().trim();
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.out.println("Numero invalido, tente novamente.");
            }
        }
    }

    private int readPrecision(Scanner sc) {
        while (true) {
            System.out.print("Precisao (casas decimais, ENTER para 2): ");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return 2;
            try {
                int p = Integer.parseInt(s);
                if (p < 0) {
                    System.out.println("Precisao deve ser >= 0.");
                } else {
                    return p;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Numero invalido, tente novamente.");
            }
        }
    }

    private BigDecimal readPercentAsRate(Scanner sc, String label) {
        while (true) {
            try {
                System.out.print(label + ": ");
                String raw = sc.nextLine().trim().replace('%', ' ').replace(',', '.');
                if (raw.isEmpty()) {
                    System.out.println("Valor invalido, tente novamente.");
                    continue;
                }
                BigDecimal v = new BigDecimal(raw);
                if (v.abs().compareTo(BigDecimal.ONE) >= 0) {
                    v = v.divide(BigDecimal.valueOf(100), MC);
                }
                return v;
            } catch (NumberFormatException ex) {
                System.out.println("Valor invalido, tente novamente.");
            }
        }
    }

    private void menuJurosSimples(Scanner sc) {
        System.out.println("\n== Juros Simples ==");
        BigDecimal principal = readBigDecimal(sc, "Principal");
        BigDecimal taxa = readPercentAsRate(sc, "Taxa em % (ex.: 10 para 10%)");
        int tempo = readInt(sc, "Tempo (periodos)");
        var calc = new CalculadoraJurosSimples(MC);
        System.out.println("Montante: " + calc.calcularMontante(principal, taxa, tempo));
        System.out.println("Juros: " + calc.calcularJuros(principal, taxa, tempo) + "\n");
    }

    private void menuJurosCompostos(Scanner sc) {
        System.out.println("\n== Juros Compostos ==");
        BigDecimal principal = readBigDecimal(sc, "Principal");
        BigDecimal taxa = readPercentAsRate(sc, "Taxa em % (ex.: 10 para 10%)");
        int tempo = readInt(sc, "Tempo (periodos)");
        var calc = new CalculadoraJurosCompostos(MC);
        System.out.println("Montante: " + calc.calcularMontante(principal, taxa, tempo));
        System.out.println("Juros: " + calc.calcularJuros(principal, taxa, tempo) + "\n");
    }

    private void menuParcelasSemJuros(Scanner sc) {
        System.out.println("\n== Parcelas Sem Juros ==");
        BigDecimal principal = readBigDecimal(sc, "Valor Principal");
        int n = readInt(sc, "Numero de parcelas");
        int prec = readPrecision(sc);
        var calc = new CalculadoraParcelasSemJuros(MC);
        List<Parcela> parcelas = calc.calcularParcelas(principal, n, prec);
        imprimirParcelas(parcelas);
    }

    private void menuParcelasSac(Scanner sc) {
        System.out.println("\n== Parcelas SAC ==");
        BigDecimal principal = readBigDecimal(sc, "Valor Principal");
        BigDecimal taxa = readPercentAsRate(sc, "Taxa em % por periodo (ex.: 10)");
        int n = readInt(sc, "Numero de parcelas");
        int prec = readPrecision(sc);
        var calc = new CalculadoraParcelasSac(MC);
        List<Parcela> parcelas = calc.calcularParcelas(principal, taxa, n, prec);
        imprimirParcelas(parcelas);
    }

    private void menuParcelasPrice(Scanner sc) {
        System.out.println("\n== Parcelas PRICE ==");
        BigDecimal principal = readBigDecimal(sc, "Valor Principal");
        BigDecimal taxa = readPercentAsRate(sc, "Taxa em % por periodo (ex.: 10)");
        int n = readInt(sc, "Numero de parcelas");
        int prec = readPrecision(sc);
        var calc = new CalculadoraParcelasPrice(MC);
        List<Parcela> parcelas = calc.calcularParcelas(principal, taxa, n, prec);
        imprimirParcelas(parcelas);
    }

    private void imprimirParcelas(List<Parcela> parcelas) {
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
}
