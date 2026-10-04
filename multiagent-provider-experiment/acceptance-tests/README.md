# Suite black-box única — 12 cenários

Esta suíte é externa ao workspace editável pelos agentes e deve ser usada
sem alterações para monólito e microsserviços.

## Unidade de comparação

A mesma suíte chama a mesma API pública:

`http://localhost:8080`

- Monólito: aplicação Spring Boot diretamente em `:8080`.
- Microsserviços: API Gateway em `:8080`.

## Cenários

- T01 cotação válida
- T02 símbolo inválido
- T03 criação de usuário com saldo inicial
- T04 username duplicado
- T05 compra válida
- T06 saldo insuficiente
- T07 shares = 0
- T08 portfólio
- T09 venda válida
- T10 oversell sem alteração de estado
- T11 histórico BUY/SELL
- T12 usuário inexistente

## Sem dependências Python

O runner usa apenas a biblioteca padrão do Python 3.

## Execução

```bash
python3 acceptance-tests/run_acceptance.py \
  --base-url http://localhost:8080 \
  --run-id legacy-monolith-validation \
  --output acceptance-results.json
```

O processo retorna:

- exit code `0`: 12/12 aprovados;
- exit code `1`: pelo menos um cenário falhou.

## Importante: calibração contra `legacy-chatpt`

Antes de usar a suíte como gate do experimento automatizado, execute-a contra
as duas implementações completas de `legacy-chatpt`.

O objetivo é confirmar que o contrato black-box reproduz o comportamento das
implementações anteriormente validadas.

O arquivo `acceptance_contract.json` contém poucos aliases de nomes de campos
e alguns códigos HTTP aceitos (por exemplo 200/201 na criação). Depois da
calibração, recomenda-se endurecer o contrato para refletir exatamente o
comportamento público que será congelado para o novo experimento.

Não copie esta suíte para `runs/<run>/workspace`.
