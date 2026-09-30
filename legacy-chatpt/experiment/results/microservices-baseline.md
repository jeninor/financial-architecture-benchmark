## Source code

Production Java files: 47
Production physical Java LOC: 2206

Test Java files: 1
Test physical Java LOC: 980

## Source code by component

Discovery Server:
- Java files: 1
- Physical Java LOC: 18

API Gateway:
- Java files: 1
- Physical Java LOC: 17

User Service:
- Java files: 14
- Physical Java LOC: 635

Market Service:
- Java files: 6
- Physical Java LOC: 196

Trade Service:
- Java files: 25
- Physical Java LOC: 1340

## Functional tests

Tests: 12
Passed: 12
Failed: 0
Errors: 0
Skipped: 0
Pass rate: 100%

E2E test time: 2.082 s
Maven total time: 10.103 s

## Development errors observed

Configuration/build errors: 3

1. Market Service - missing pom.xml
2. API Gateway - missing/unavailable main class
3. User Service - missing Spring AMQP dependency

All observed errors were resolved.