# Sistema de Gerenciamento de Estoque

Aplicação web em **Spring Boot 4 / Java 25** (Spring MVC + Thymeleaf + Spring Data JPA + H2).

## Como executar

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Abra http://localhost:8080. Na primeira execução o banco é populado com categorias e
produtos de exemplo (desative com `estoque.dados-demo=false` em `application.properties`).

- Dados persistem em `./data/estoque.mv.db` (apague a pasta `data/` para recomeçar).
- Console do banco: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:file:./data/estoque`, usuário `sa`, senha vazia).
- Testes: `./mvnw test`

## Funcionalidades

| Área | O que faz |
|------|-----------|
| **Painel** | Produtos ativos, unidades, valor em estoque (custo e venda), itens abaixo do mínimo, lista de reposição e últimas movimentações |
| **Produtos** | CRUD com SKU único, categoria, unidade, preços, estoque mínimo; busca por nome/SKU, filtro por categoria, "só abaixo do mínimo" e inativos; ativar/inativar |
| **Movimentações** | Entrada, saída e ajuste de inventário, com motivo, saldo anterior/posterior e histórico por produto |
| **Categorias** | CRUD; bloqueia exclusão de categoria com produtos |
| **Relatório** | Exportação da posição de estoque em CSV (`/relatorios/estoque.csv`), pronta para o Excel |

## Regras de negócio

- O saldo **só muda por movimentações** (`EstoqueService`); o formulário de produto não edita o saldo.
- **Entrada** soma; **Saída** subtrai e é recusada se exceder o saldo (nunca fica negativo);
  **Ajuste** define o saldo como a contagem física informada.
- Movimentações usam lock pessimista no produto, evitando saldo errado em acessos simultâneos.
- Produto com histórico de movimentações **não pode ser excluído**, apenas inativado.
- Produto inativo não pode ser movimentado.
- Alerta quando `saldo <= estoque mínimo`.

## Estrutura

```
src/main/java/br/com/sebrae/atividade_produtos
├── model/        Categoria, Produto, Movimentacao, TipoMovimentacao
├── repository/   Spring Data JPA
├── service/      EstoqueService (regras), ProdutoService, CategoriaService, DashboardService
├── form/         ProdutoForm, MovimentacaoForm (validação Bean Validation)
├── controller/   Painel, Produtos, Categorias, Movimentações, Relatório
├── config/       DadosDemoLoader (dados de exemplo)
└── exception/    RegraNegocioException, EstoqueInsuficienteException
src/main/resources
├── templates/    Thymeleaf (layout compartilhado em fragments/layout.html)
└── static/       css/estoque.css, js/movimentacao.js
```

> A aplicação não possui login. Antes de expor fora de uma rede confiável, adicione
> Spring Security (autenticação, CSRF) e troque o H2 por um banco de produção.
