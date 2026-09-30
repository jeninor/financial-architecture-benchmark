Adicione mensageria assíncrona à arquitetura de microsserviços
utilizando RabbitMQ.

Não modificar nenhuma regra financeira.

Após uma operação BUY ou SELL concluída, o Trade Service deverá
publicar:

exchange:
financial.exchange

routing key:
trade.completed

O User Service deverá possuir:

queue:
trade.audit.queue

e consumir o evento apenas para auditoria/log.

O evento deve conter:

tradeId
userId
symbol
type
shares
price
total
occurredAt

Adicionar spring-boot-starter-amqp explicitamente aos serviços que
utilizam RabbitMQ.

A mensageria não deverá:

- debitar saldo;
- creditar saldo;
- modificar portfólio;
- substituir comunicação REST existente.

Validar a integração verificando:

1. conexão RabbitMQ;
2. existência do exchange;
3. existência da fila;
4. publicação após BUY;
5. log AUDIT trade.completed no consumidor.