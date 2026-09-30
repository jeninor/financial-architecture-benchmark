Crie uma suíte JUnit de testes end-to-end para a implementação
de microsserviços.

Os testes devem acessar exclusivamente:

http://api-gateway:8080

A suíte deverá reproduzir os mesmos 12 comportamentos funcionais
validados no monólito:

T01 cotação válida
T02 símbolo inválido
T03 criação de usuário com saldo 10.000
T04 username duplicado
T05 compra válida
T06 compra sem saldo suficiente
T07 shares igual a zero
T08 cálculo de portfólio
T09 venda válida
T10 venda superior às ações possuídas
T11 histórico BUY/SELL
T12 usuário inexistente

Utilize usernames únicos para permitir repetição dos testes sem
limpeza manual dos bancos.

Não criar cenários funcionais adicionais nesta suíte.

Resultado esperado da validação:

12 testes executados
0 failures
0 errors
0 skipped