# Teste de estresse

## Usuário utilizado

| Campo | Valor |
|---|---|
| ID | `3` |
| Nome | Teste de estresse |
| Usuário | `teste` |
| E-mail | `teste@teste.com` |
| Senha | `teste` |

Esta conta foi criada exclusivamente para os testes de volume, mantendo os registros separados dos demais usuários.

## Teste 1 - 500 registros

- **Volume:** 500 marmitas para o usuário de teste.
- **Comportamento observado:** A tabela funciona normalmente.
- **Resultado:** Aprovado.

## Teste 2 - 2.500 registros

- **Volume:** 2.000 novos registros, totalizando 2.500 marmitas para o usuário de teste.
- **Dados:** Nomes únicos, categorias, valores, datas, quantidades e observações variadas.
- **Comportamento observado:** A tabela continua rápida e responsiva.
- **Resultado:** Aprovado.

## Teste 3 - 10.000 registros

- **Volume:** 7.500 novos registros, totalizando 10.000 marmitas para o usuário de teste.
- **Dados:** Nomes únicos, categorias, valores, datas, quantidades e observações variadas.
- **Tempo de atualização:** Aproximadamente 1 segundo.
- **Comportamento observado:** Houve um pequeno atraso perceptível na atualização da tabela em comparação aos testes anteriores, que foram instantâneos. Fora isso, a aplicação continuou muito rápida e responsiva.
- **Resultado:** Aprovado. O desempenho permaneceu muito bom mesmo com 10.000 registros.

## Conclusão

A tabela apresentou bom desempenho em todos os volumes avaliados. Com 500 e 2.500 registros, as atualizações foram instantâneas. No teste final, com 10.000 registros, a atualização levou aproximadamente 1 segundo, sem comprometer a responsividade geral da aplicação.

## Evidências visuais

### Teste 1 - 500 registros

![Tabela com 500 registros](src/br/com/images/teste%201.png)

### Teste 2 - 2.500 registros

![Tabela com 2.500 registros](src/br/com/images/teste%202.png)

### Teste 3 - 10.000 registros

![Tabela com 10.000 registros](src/br/com/images/teste%203.png)

> O indicador "Total de Marmitas" exibido pela aplicação representa a soma das quantidades em estoque, não a quantidade de linhas da tabela.
