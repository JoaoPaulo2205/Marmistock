# MarmiStock

Aplicacao web para gestao pessoal de marmitas congeladas. O backend usa Java + Spring Boot + PostgreSQL, e o frontend e servido pelo proprio Spring Boot com Tailwind em tema escuro.

## Estrutura

- `src/main/java/br/com/marmistock`: aplicacao Spring Boot e API REST.
- `src/main/resources/schema.sql`: schema PostgreSQL baseado em `db_data`.
- `src/main/resources/static`: dashboard e telas CRUD com Tailwind.
- `Banco_de_Dados.pdf`: especificacao do projeto.
- `db_data`: DDL original usado como referencia.
- `Cerrado Marmitas/`: projeto Swing antigo usado como referencia funcional.

## Banco de Dados

Por padrao a aplicacao tenta conectar em:

```text
jdbc:postgresql://localhost:5433/trabalho_banco_dados
usuario: postgres
senha: postgres
```

Para subir um PostgreSQL compativel com esses defaults:

```bash
docker compose up -d
```

Ou use variaveis de ambiente:

```bash
DB_URL=jdbc:postgresql://localhost:5433/trabalho_banco_dados \
DB_USER=seu_usuario \
DB_PASSWORD=sua_senha \
mvn spring-boot:run
```

## Executar

```bash
mvn test
mvn spring-boot:run
```

Depois acesse:

```text
http://localhost:8080
```

## Funcionalidades

- Dashboard inicial com totais, proximas do vencimento, vencidas, consumidas e descartadas.
- Sidebar com telas para marmitas, receitas, lotes, categorias, ingredientes, locais e consumidores.
- Cadastro, edicao e remocao dos principais registros.
- Registro de consumo e descarte de marmitas fisicas.
- Inicializacao automatica das tabelas via `schema.sql`.
