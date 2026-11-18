# Sistema de Gerenciamento de Estoque

Aplicação web para controlar produtos, categorias e movimentações de estoque (entradas, saídas e
ajustes de inventário), com painel de indicadores e alertas de reposição.

**Tecnologias:** Java 25 · Spring Boot 4.1 · Spring MVC · Thymeleaf · Spring Data JPA · Bean Validation · H2

---

## Pré-requisitos

- **JDK 25** (por exemplo, Temurin em https://adoptium.net). Confira com `java -version`.
- **Internet** na primeira execução, para o Maven baixar as dependências (ficam em cache depois).
- Não precisa instalar o Maven: o projeto usa o Maven Wrapper (`mvnw`).

## Como executar

Na pasta do projeto:

```powershell
# Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
./mvnw spring-boot:run
```

Quando aparecer `Started AtividadeProdutosApplication`, acesse **http://localhost:8080**.

Também é possível abrir o projeto no IntelliJ e executar a classe `AtividadeProdutosApplication`.

| Comando | Para que serve |
|---------|----------------|
| `.\mvnw.cmd spring-boot:run` | Inicia a aplicação |
| `.\mvnw.cmd test` | Roda os testes automatizados |
| `.\mvnw.cmd -U clean compile` | Recompila e força nova tentativa de download das dependências |

### Dados de exemplo

Na primeira execução com o banco vazio, são criadas 4 categorias e 10 produtos de exemplo.
Para desativar, defina `estoque.dados-demo=false` em `src/main/resources/application.properties`.

### Banco de dados

- H2 em arquivo: os dados ficam em `./data/estoque.mv.db` e persistem entre execuções.
- Para recomeçar do zero, pare a aplicação e apague a pasta `data/`.
- Console do banco: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/estoque` · usuário: `sa` · senha: (vazia)

---

## Funcionalidades

| Área | O que faz |
|------|-----------|
| **Painel** (`/`) | Produtos ativos, unidades em estoque, valor em estoque (custo e venda potencial), itens abaixo do mínimo, lista de reposição e últimas movimentações |
| **Produtos** (`/produtos`) | Cadastro com SKU único, categoria, unidade, preços de custo e venda e estoque mínimo. Busca por nome/SKU, filtro por categoria, "só abaixo do mínimo" e "incluir inativos". Ativar/inativar e excluir. Tela de detalhe com histórico |
| **Movimentações** (`/movimentacoes`) | Entrada, saída e ajuste de inventário, com motivo, saldo anterior e saldo final. Histórico geral ou por produto |
| **Categorias** (`/categorias`) | Cadastro, edição e exclusão |
| **Relatório** (`/relatorios/estoque.csv`) | Posição de estoque em CSV (separador `;`, UTF-8 com BOM), pronta para abrir no Excel |

## Regras de negócio

- O saldo **só muda por movimentações**; o formulário de produto não edita o saldo.
  No cadastro, a "quantidade inicial" gera uma entrada com o motivo "Estoque inicial".
- **Entrada** soma ao saldo. **Saída** subtrai e é recusada se for maior que o saldo (o estoque
  nunca fica negativo). **Ajuste** define o saldo como a contagem física informada.
- Entrada e saída exigem quantidade maior que zero.
- Produto inativo não pode ser movimentado.
- Produto com movimentações **não pode ser excluído**, apenas inativado.
- Categoria que ainda possui produtos não pode ser excluída.
- SKU é único (sem diferenciar maiúsculas de minúsculas) e é gravado em maiúsculas.
- Um produto está em alerta quando `saldo <= estoque mínimo`.
- Movimentações usam lock pessimista no produto para evitar saldo incorreto em acessos simultâneos.

---

## Estrutura do projeto

```
src/main/java/br/com/sebrae/atividade_produtos
├── model/        Categoria, Produto, Movimentacao, TipoMovimentacao
├── repository/   Interfaces Spring Data JPA
├── service/      EstoqueService (regras do saldo), ProdutoService,
│                 CategoriaService, DashboardService
├── form/         ProdutoForm, MovimentacaoForm (validação)
├── controller/   Dashboard, Produto, Categoria, Movimentacao, Relatorio,
│                 GlobalControllerAdvice (selo de alertas e tratamento de erros)
├── config/       DadosDemoLoader (dados de exemplo)
└── exception/    RegraNegocioException, EstoqueInsuficienteException

src/main/resources
├── application.properties
├── templates/    Páginas Thymeleaf (layout compartilhado em fragments/layout.html)
└── static/       css/estoque.css, js/movimentacao.js

src/test/java/.../service/EstoqueServiceTest.java   Testes das regras de estoque
```

---

## Solução de problemas

**`Could not transfer artifact ... Connect timed out`**
O Maven não alcançou o Maven Central (`repo.maven.apache.org`): é problema de rede, não do projeto.

- Teste a conexão: `curl.exe -I --max-time 15 https://repo.maven.apache.org/maven2/`
  (no PowerShell use `curl.exe`; `curl` sozinho é um apelido de outro comando).
- Tente outra rede (por exemplo, o hotspot do celular), desligue VPN/antivírus e verifique proxy:
  `netsh winhttp show proxy`.
- Em rede com proxy, configure-o em `C:\Users\SEU_USUARIO\.m2\settings.xml`.
- Depois do primeiro download bem-sucedido, as dependências ficam em cache (`.m2`).

**`package org.springframework.web... does not exist` / `package jakarta.validation does not exist`**
As dependências ainda não foram carregadas no IntelliJ. Confirme que o `pom.xml` contém
`spring-boot-starter-webmvc` e `spring-boot-starter-validation`, e recarregue o Maven
(aba Maven → "Reload All Maven Projects").

**`invalid target release: 25` / `release version 25 not supported`**
O JDK instalado é anterior ao 25. Instale o JDK 25 e abra um novo terminal. No IntelliJ, ajuste o
JDK em *File → Project Structure → Project SDK*.

**`Port 8080 was already in use`**
Outra aplicação usa a porta 8080. Feche-a ou defina `server.port=8081` em `application.properties`.

---

## Limitações e próximos passos

- Não há autenticação: antes de expor fora de uma rede confiável, adicione Spring Security
  (login, perfis, CSRF) e troque o H2 por um banco de produção (PostgreSQL, MySQL...).
- Um único depósito; sem fornecedores, pedidos de compra, lotes ou validade.
- Ideias de evolução: relatórios por período, importação de produtos por CSV, múltiplos depósitos.
