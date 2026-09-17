# Development Errors

## E01 - Market Service

Architecture: Microservices

Component: Market Service

Category: Configuration / Build

Error:
Maven executed without a pom.xml and reported:

No plugin found for prefix 'spring-boot'

Root cause:
The market-service directory mounted at /workspace contained src/
but did not contain pom.xml.

Correction:
Created the missing pom.xml with the Spring Boot Maven plugin.

Corrections required: 1

Status: Resolved


## E02 - API Gateway

Architecture: Microservices

Component: API Gateway

Category: Configuration / Build

Error:
Spring Boot Maven plugin reported:

Unable to find a suitable main class

Root cause:
ApiGatewayApplication.java was not available in the expected
src/main/java package structure.

Correction:
Created/corrected ApiGatewayApplication.java in the expected
package and rebuilt the service.

Corrections required: 1

Status: Resolved


## E03 - User Service RabbitMQ integration

Architecture: Microservices

Component: User Service

Category: Dependency / Configuration

Error:
Compilation failed because Spring AMQP classes such as
TopicExchange, Queue, Binding and RabbitListener could not be found.

Root cause:
spring-boot-starter-amqp was missing from the user-service pom.xml.

Correction:
Added spring-boot-starter-amqp and rebuilt the service.

Corrections required: 1

Status: Resolved