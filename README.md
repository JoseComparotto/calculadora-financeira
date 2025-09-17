# Calculadora Financeira

Multi-módulo Maven com:
- `calculadora-financeira-core`: bibliotecas de juros e parcelamentos (SAC, PRICE, sem juros)
- `calculadora-financeira-cli`: CLI interativa e baseada em argumentos (picocli)

## Requisitos
- Java 17+
- Maven 3.9+

## Build e testes
```bash
mvn clean package
```

Executa todos os testes do core. Artefatos são gerados em `calculadora-financeira-*/target/`.

## Executar a CLI
Existem dois modos:

1) Sem argumentos (menu interativo)
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar
```

2) Com argumentos (CLI “de verdade” via picocli)
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar --help
```

### Subcomandos
- `simples`: juros simples (montante e juros)
- `compostos`: juros compostos (montante e juros)
- `semjuros`: parcelamento em partes iguais (sem juros)
- `sac`: Sistema de Amortização Constante
- `price`: Sistema PRICE (prestação fixa)

### Opções comuns
- `-P, --principal`: valor principal
- `-i, --taxa`: taxa por período (aceita `0.1`, `10`, ou `10%`)
- `-n, --tempo|--parcelas`: número de períodos/parcelas (conforme o subcomando)
- `-p, --precision`: casas decimais nas parcelas (default: 2)
- `-f, --format`: formato de saída: `humano` (padrão) ou `csv`
- `-o, --output`: arquivo de saída (apenas com `-f csv`)

Observações:
- Valores de taxa `>= 1` são interpretados como percentuais (ex.: `10` → `10%` → `0.10`).
- Para CSV, números são impressos com ponto decimal (locale US).

### Exemplos rápidos
Juros simples (humano):
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar simples -P 1000 -i 10% -n 12
```

Juros compostos (CSV em stdout):
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar compostos -P 2000 -i 10% -n 5 -f csv
```

SAC (CSV em arquivo):
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar sac -P 8000 -i 1% -n 12 -f csv -o out/sac.csv
```

Sem juros (3 parcelas, 2 casas decimais):
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar semjuros -P 999 -n 3 -p 2
```

PRICE (prestação fixa):
```bash
java -jar calculadora-financeira-cli/target/calculadora-financeira-cli-1.0-SNAPSHOT-shaded.jar price -P 5000 -i 2% -n 24 -p 2
```

## Desenvolvimento
- CLI construída com [picocli](https://picocli.info/). Sem argumentos, cai no menu interativo.
- Para rodar apenas a CLI com build do reator:
```bash
mvn -am -pl calculadora-financeira-cli clean package
```

## Estrutura
```
calculadora-financeira/
  calculadora-financeira-core/   # bibliotecas de cálculo
  calculadora-financeira-cli/    # CLI (menu + subcomandos)
  README.md
  pom.xml
```

## Licença
Este projeto é de uso educacional/demonstração. Ajuste conforme suas necessidades.
