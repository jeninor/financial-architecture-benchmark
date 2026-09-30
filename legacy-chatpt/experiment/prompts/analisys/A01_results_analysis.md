Analise os resultados do experimento comparando monólito e
microsserviços.

Utilize exclusivamente métricas efetivamente coletadas.

Dimensões:

1. produtividade;
2. qualidade;
3. complexidade.

Resultados estruturais disponíveis:

Monólito:
Physical Java LOC = 1274
Java files = 26
Lizard NLOC = 904
methods/functions = 18
CC total = 29
CC médio = 1.61
CC máximo = 5

Microsserviços:
Physical Java LOC = 2206
Java files = 47
Lizard NLOC = 1520
methods/functions = 35
CC total = 50
CC médio = 1.43
CC máximo = 5

Ambos:
12 testes
12 aprovados
0 failures
0 errors
100% de aprovação

Não afirmar que microsserviços possuem métodos individualmente mais
complexos, pois o CC médio é menor.

Distinguir complexidade acumulada de complexidade média por método.

Não utilizar os tempos das suítes de testes como benchmark de
desempenho, pois as condições de execução foram diferentes.

Não concluir causalmente sobre o efeito da IA, pois não existe
grupo experimental sem IA.