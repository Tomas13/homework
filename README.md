# Homework: Product Catalog (Spring Boot)

A Spring Boot 4 application that manages a product catalog. It has a JPA repository and a REST API,
is secured with OAuth 2 and JWT, and is monitored with Spring Boot Actuator and Prometheus metrics.

## Tasks

| Task | Topic | Docs |
|---|---|---|
| 1 | Project setup: Gradle, Java toolchain, first Spring Boot app | [docs/task-1-project-setup.md](docs/task-1-project-setup.md) |
| 2 | `Product` entity, Spring Data JPA repository, CRUD REST controller | [docs/task-2-crud-rest-api.md](docs/task-2-crud-rest-api.md) |
| 3 | OAuth 2 authorization server, JWT, role-based access, browser UI | [docs/task-3-security.md](docs/task-3-security.md) |
| 6 | Actuator, custom health indicators, custom Prometheus metrics | [docs/task-6-actuator.md](docs/task-6-actuator.md) |

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
