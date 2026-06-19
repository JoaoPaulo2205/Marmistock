# Repository Guidelines

## Project Structure & Module Organization

This repository currently contains the database specification and a previous Java desktop reference project. `Banco_de_Dados.pdf` describes the MarmiStock domain and requirements. `db_data` contains the PostgreSQL DDL for categories, recipes, ingredients, batches, storage locations, physical meal containers, consumers, and movement records. `Cerrado Marmitas/` is a Java Swing/CSV project used only as a functional reference for CRUD flows, categories, users, stock views, and status indicators.

When adding the new application, keep backend code in a dedicated Spring Boot module such as `backend/` and frontend code in `frontend/`. Keep generated build output out of version control.

## Build, Test, and Development Commands

There is no unified build yet at the repository root. For the reference desktop app, run from `Cerrado Marmitas/`:

```bash
mkdir -p out
javac -encoding UTF-8 -cp src/lib/flatlaf-3.7.1 -d out $(find src/br/com/cerradomarmitas -name '*.java')
java -cp out:src/lib/flatlaf-3.7.1 br.com.cerradomarmitas.Main
```

For the planned web app, prefer standard commands once scaffolded:

```bash
./mvnw spring-boot:run
./mvnw test
npm run dev
npm run build
```

## Coding Style & Naming Conventions

Use Java 21+ conventions for Spring Boot: packages in lowercase, classes in `PascalCase`, fields and methods in `camelCase`, and repository/service/controller suffixes where appropriate. Use 4-space indentation in Java. For frontend files, use TypeScript/JavaScript components in `PascalCase` and utility-first Tailwind classes with readable grouping.

Map database names deliberately: keep PostgreSQL table/column names in `snake_case`, while Java entities use idiomatic camelCase fields.

## Testing Guidelines

Add backend tests under `src/test/java` using JUnit and Spring Boot test support. Name tests after the behavior being verified, for example `ReceitaServiceTest` or `MarmitaFisicaControllerTest`. Cover CRUD operations, foreign-key constraints, status transitions, and expiration calculations. Frontend tests should focus on forms, dashboard summaries, navigation, and API error states once a test runner is added.

## Commit & Pull Request Guidelines

No root Git history is available, so use clear imperative commit messages such as `Add Spring Boot entities for stock tracking` or `Create dark dashboard layout`. Pull requests should include a short summary, database/schema impact, test results, and screenshots for UI changes.

## Security & Configuration Tips

Do not commit local database passwords. Store PostgreSQL credentials in environment variables or an ignored local configuration file. Keep `db_data` as the schema reference and document any schema changes alongside migrations.
