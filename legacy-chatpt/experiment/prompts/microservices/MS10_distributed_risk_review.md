Analise a arquitetura de microsserviços concluída quanto aos
trade-offs específicos de sistemas distribuídos.

Não modificar código.

Avalie:

1. transações locais;
2. consistência entre user_db e trade_db;
3. falha do Trade Service após debit remoto;
4. falha do User Service;
5. falha do Market Service;
6. dependência do Eureka;
7. dependência do API Gateway;
8. publicação RabbitMQ;
9. entrega e consumo de eventos;
10. disponibilidade da infraestrutura.

Diferencie explicitamente:

OBSERVADO
problema que ocorreu efetivamente no experimento;

RISCO POTENCIAL
problema possível pela arquitetura, mas não observado.

Considere que @Transactional no Trade Service protege apenas
trade_db.

Não afirmar que existe atomicidade distribuída.

Saga, Outbox Pattern e 2PC podem ser mencionados como alternativas,
mas estão fora do escopo da implementação experimental.