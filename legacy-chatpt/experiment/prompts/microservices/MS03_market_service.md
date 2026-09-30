Implemente o Market Service.

Responsabilidade exclusiva:

GET /api/quotes/{symbol}

Utilize cotações fixas:

AAPL = 200.00
MSFT = 400.00
GOOGL = 170.00
AMZN = 190.00
NVDA = 120.00

Para símbolos inexistentes, retornar HTTP 400.

O serviço não necessita banco de dados.

Utilize Spring Boot e registre posteriormente o serviço no Eureka.

Antes de executar, verifique explicitamente que a estrutura contém:

pom.xml
src/main/java
src/main/resources

Não implemente funcionalidades financeiras adicionais.