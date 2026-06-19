# Cerrado Marmitas

Sistema desktop em Java Swing para controle de estoque de marmitas, categorias, validade e usuários. Os dados são persistidos em arquivos CSV, sem necessidade de banco de dados.

## Funcionalidades

- Login e cadastro de usuários.
- Opção **Lembre-se de mim** para login automático local.
- Senhas case-sensitive e nomes de usuário case-insensitive.
- Estoque separado por usuário.
- Cadastro, edição, remoção e detalhes de marmitas.
- Cadastro, edição e remoção de categorias.
- Busca por texto e filtros por categoria e status.
- Ordenação numérica das colunas ID e quantidade.
- Indicadores de total de unidades, próximas do vencimento, vencidas e categorias.
- Perfil com atualização de dados, senha e foto.
- Tema claro/escuro e tamanho de fonte configuráveis.
- Interface estilizada com FlatLaf.

## Requisitos

- JDK 8 ou superior.
- Ambiente gráfico para executar a interface Swing.

O projeto foi validado com OpenJDK 25.

## Executar pelo terminal

Na raiz do projeto:

```bash
mkdir -p out

javac -encoding UTF-8 \
  -cp src/lib/flatlaf-3.7.1 \
  -d out \
  $(find src/br/com/cerradomarmitas -name '*.java')

java -cp out:src/lib/flatlaf-3.7.1 br.com.cerradomarmitas.Main
```

No Windows, substitua `:` por `;` no classpath do comando `java`.

## Executar pelo IntelliJ IDEA

1. Abra a pasta do projeto.
2. Confirme que o módulo utiliza um JDK instalado.
3. Execute a classe `br.com.cerradomarmitas.Main`.

As bibliotecas FlatLaf e JCalendar já estão cadastradas no módulo do IntelliJ.

## Usuários de teste

| Usuário | Senha | Estoque inicial |
|---|---|---:|
| `admin` | `admin123` | 106 unidades |
| `musk` | `spacex` | 587 unidades |

As senhas diferenciam letras maiúsculas de minúsculas. O nome de usuário não diferencia.

## Teste de estresse

O estoque foi avaliado com 500, 2.500 e 10.000 registros vinculados a um usuário exclusivo para testes. Mesmo com 10.000 registros, a tabela permaneceu rápida e responsiva, com atualização em aproximadamente 1 segundo.

Consulte o [relatório completo do teste de estresse](teste%20de%20estresse.md), incluindo resultados e evidências visuais.

## Persistência

Os arquivos ficam no diretório `data/`:

- `usuarios.csv`: contas e caminhos das fotos de perfil.
- `marmitas.csv`: estoque vinculado pelo campo `usuarioId`.
- `categorias.csv`: categorias disponíveis.
- `configuracoes.csv`: tema e tamanho da fonte.
- `sessao.csv`: ID da sessão lembrada; é local e ignorado pelo Git.

O programa deve ser executado pela raiz do projeto para encontrar corretamente o diretório `data/`.

## Regras de estoque

- **Vencida:** validade anterior à data atual.
- **Próxima do vencimento:** vence hoje ou nos próximos 7 dias.
- **OK:** validade superior a 7 dias.
- **Total de Marmitas:** soma do campo quantidade de todas as variações do usuário.

Cada usuário visualiza, adiciona, edita e remove apenas as próprias marmitas.

## Estrutura principal

```text
src/br/com/cerradomarmitas/
├── Main.java
├── models/       # Marmita, Categoria, Usuario e Configuracao
├── util/         # CSV, validação, sessão, aparência e imagens
└── view/         # Telas Swing

data/             # Arquivos CSV
src/lib/          # Dependências locais
```
