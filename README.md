# Homework: Product Catalog (Spring Boot)

A Spring Boot 4 application that manages a product catalog. It has a JPA repository and a REST API,
is secured with OAuth 2 and JWT, and is monitored with Spring Boot Actuator and Prometheus metrics.

## Tasks

| Task | Topic | Documentation | Pull request |
|---|---|---|---|
| 1 | Project setup: Gradle, Java toolchain, first Spring Boot app | [Task 1 docs](https://github.com/Tomas13/homework/blob/e9b65de/docs/task-1-project-setup.md) | initial commit on `main` |
| 2 | `Product` entity, Spring Data JPA repository, CRUD REST controller | [Task 2 docs](https://github.com/Tomas13/homework/blob/e9b65de/docs/task-2-crud-rest-api.md) | [#1](https://github.com/Tomas13/homework/pull/1) |
| 3 | OAuth 2 authorization server, JWT, role-based access | [Task 3 docs](https://github.com/Tomas13/homework/blob/e9b65de/docs/task-3-security.md) | [#2](https://github.com/Tomas13/homework/pull/2) |
| 3 | Browser UI for the secured API | [Task 3 docs: browser](https://github.com/Tomas13/homework/blob/e9b65de/docs/task-3-security.md#in-the-browser-recommended) | [#3](https://github.com/Tomas13/homework/pull/3) |
| 6 | Actuator, custom health indicators, custom Prometheus metrics | [Task 6 docs](https://github.com/Tomas13/homework/blob/e9b65de/docs/task-6-actuator.md) | [#4](https://github.com/Tomas13/homework/pull/4) |
| | README and documentation | | [#5](https://github.com/Tomas13/homework/pull/5) |

The pull requests are stacked (each one builds on the previous) and are merged in order, #1 → #5.

## Requirements

- Java 25 (configured as the Gradle toolchain)
- Nothing else: the Gradle wrapper, the H2 in-memory database and the embedded Tomcat server are all included

## Quick start

```bash
./gradlew bootRun          # start the app on http://127.0.0.1:8080
./gradlew test             # run all tests
./gradlew bootJar          # build build/libs/homework-0.0.1-SNAPSHOT.jar
java -jar build/libs/homework-0.0.1-SNAPSHOT.jar
```

Then open **http://127.0.0.1:8080/** and log in with one of the demo users:

| User | Password | Roles | Can |
|---|---|---|---|
| `user` | `password` | USER | read products |
| `admin` | `admin` | USER, ADMIN | read, create, update, delete products; see actuator details |

## Project structure

```
src/main/java/com/epam/course/homework/
├── HomeworkApplication.java      # entry point
├── SampleDataLoader.java         # inserts 3 sample products on startup
├── model/Product.java            # JPA entity
├── repository/                   # Spring Data JPA repository
├── controller/                   # REST controller, request DTO, OAuth redirect page
├── security/SecurityConfig.java  # authorization server, resource server, users, clients
└── monitoring/                   # health indicators and Prometheus metrics
src/main/resources/
├── application.properties
└── static/index.html             # browser UI
scripts/get-token.sh              # gets a JWT from the command line
docs/                             # documentation per task
```
