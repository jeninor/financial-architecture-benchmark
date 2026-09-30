# Resultados Consolidados

## 1. Validação funcional

| Métrica | Monolito | Microsserviços |
|---|---:|---:|
| Casos de teste | 12 | 12 |
| Testes aprovados | 12 | 12 |
| Falhas | 0 | 0 |
| Erros | 0 | 0 |
| Taxa de aprovação | 100% | 100% |

As duas implementações atenderam aos mesmos doze cenários
funcionais definidos para o experimento.

## 2. Complexidade estrutural

| Métrica | Monolito | Microsserviços | Diferença |
|---|---:|---:|---:|
| Java LOC físico | 1274 | 2206 | +73,2% |
| Arquivos Java | 26 | 47 | +80,8% |
| Lizard NLOC | 904 | 1520 | +68,1% |
| Métodos/funções | 18 | 35 | +94,4% |
| CC total | 29 | 50 | +72,4% |
| CC médio | 1,61 | 1,43 | -11,2% |
| CC máximo | 5 | 5 | 0% |

## 3. Microsserviços por componente

| Componente | Arquivos Java | LOC físico |
|---|---:|---:|
| Discovery Server | 1 | 18 |
| API Gateway | 1 | 17 |
| User Service | 14 | 635 |
| Market Service | 6 | 196 |
| Trade Service | 25 | 1340 |
| Total | 47 | 2206 |

## 4. Erros observados durante o desenvolvimento

### Microsserviços

1. Market Service
   - Categoria: configuração/build
   - Problema: pom.xml ausente
   - Correções: 1
   - Status: resolvido

2. API Gateway
   - Categoria: configuração/build
   - Problema: classe principal não localizada
   - Correções: 1
   - Status: resolvido

3. User Service / RabbitMQ
   - Categoria: dependência/configuração
   - Problema: dependência Spring AMQP ausente
   - Correções: 1
   - Status: resolvido

## 5. Produtividade

Os tempos de desenvolvimento e correção serão inseridos somente
a partir dos registros efetivamente coletados durante o experimento.