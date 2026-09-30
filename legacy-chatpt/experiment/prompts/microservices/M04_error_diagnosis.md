Analise exclusivamente o erro, log ou falha fornecido.

Não adicione funcionalidades novas e não refatore componentes
que não estejam relacionados ao problema.

Para cada problema:

1. identifique o erro observado;
2. determine a causa provável;
3. classifique-o como:
   - código;
   - configuração;
   - dependência;
   - integração;
   - banco de dados;
   - teste;
   - infraestrutura;
4. identifique os arquivos envolvidos;
5. proponha a menor correção possível;
6. forneça comandos reproduzíveis para validar a correção;
7. indique como verificar que nenhuma funcionalidade existente
   foi alterada.

Ao final, produza um registro contendo:

architecture
component
error_category
error_description
root_cause
correction
correction_count
status

Não invente causas que não possam ser justificadas pelo log.