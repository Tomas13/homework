# Task 1: Project setup

## Goal
Create a Spring Boot project built with Gradle and make sure it compiles and starts.

## What was done

- **Spring Boot 4.1.2** project generated with the `org.springframework.boot` and `io.spring.dependency-management` plugins
  (group `com.epam.course`, artifact `homework`).
- **Gradle 9.8.2** through the Gradle wrapper (`gradlew`, `gradle/wrapper/`). Nobody needs Gradle installed.
- **Java toolchain set to 25** in `build.gradle`:
  ```groovy
  java {
      toolchain {
          languageVersion = JavaLanguageVersion.of(25)
      }
  }
  ```
- **`foojay-resolver-convention` 1.0.0** in `settings.gradle`, which lets Gradle download a matching JDK if one is missing.
- `HomeworkApplication` started as a `CommandLineRunner` printing `Hello World!`. In task 2 it was reduced to `main()`
  and the startup output moved to `SampleDataLoader`.

## Problems solved

| Error | Cause | Fix |
|---|---|---|
| `JvmVendorSpec does not have member field IBM_SEMERU` | `foojay-resolver-convention` 0.8.0 was built for Gradle 8; Gradle 9 removed `JvmVendorSpec.IBM_SEMERU` | Upgrade the plugin to `1.0.0` in `settings.gradle` |
| `Cannot find a Java installation ... languageVersion=21` | No JDK 21 installed and the foojay download failed | Use the installed JDK 25 (`JavaLanguageVersion.of(25)`) |

## What goes into Git

Committed: `gradlew`, `gradlew.bat`, `gradle/wrapper/*` (including `gradle-wrapper.jar`), `build.gradle`,
`settings.gradle`, `src/`, `.gitignore`, `.gitattributes`.

Ignored (see `.gitignore`): `.gradle/`, `build/`, `.idea/`, `*.iml`, `HELP.md`.

## How to run

```bash
./gradlew bootRun
```
