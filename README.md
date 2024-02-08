# Browser notepad My Books
My Books is a web application that simulates a notepad for working with a list of books. The application only runs on localhost. \
Application has entities: **Book**, **Genre**, **Review**. \
**Genre** has a *one-to-many* relationship with **Books**. \
**Review** has a *one-to-one* relationship with the **Book**. \
CRUD operations are implemented for each entity. \
Application returns JSON response.

Test REST API using Postman:
* [Import](https://learning.postman.com/docs/integrations/available-integrations/working-with-openAPI/) the file *openapi.yaml* from directory */swagger* into Postman.
* Check that the requests complete successfully.


## Tests and linter status
[![Java CI](https://github.com/dariakoval/my-books/actions/workflows/generate.yml/badge.svg)](https://github.com/dariakoval/my-books/actions/workflows/generate.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/e03fdaa078743dca0449/maintainability)](https://codeclimate.com/github/dariakoval/my-books/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/e03fdaa078743dca0449/test_coverage)](https://codeclimate.com/github/dariakoval/my-books/test_coverage)

## Requirements
* JDK 20
* Gradle 8.3
* GNU Make

## Technology stack
Java, Gradle, GNU Make, Apache Tomcat, JDBC, Lombok, PostgreSQL (development), H2 (testing), JUnit/AssertJ, Unirest, Swagger.

## Run
```bash
make start
```

## REST API documentation
```bash
cd swagger
# Open index.html in browser
```

## Build
```bash
make build
```

## Test
```bash
make test
```

## Report
```bash
make report
cd build/reports/jacoco/test/html
# Open index.html in browser
```

## Linter
```bash
make lint
```
