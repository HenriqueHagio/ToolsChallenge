# ToolsChallenge — API de Pagamentos

API REST de pagamentos com cartão de crédito desenvolvida como solução para o **Tools Java Challenge [B]**.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Padrões de Projeto](#padrões-de-projeto)
- [Regras de Negócio](#regras-de-negócio)
- [Como Executar](#como-executar)
- [Endpoints](#endpoints)
- [Exemplos de Requisição](#exemplos-de-requisição)
- [Testes](#testes)
- [Estrutura do Projeto](#estrutura-do-projeto)

---

## Tecnologias

| Tecnologia           | Versão  | Finalidade                          |
|----------------------|---------|-------------------------------------|
| Java                 | 17      | Linguagem principal                 |
| Spring Boot          | 3.2.0   | Framework web + IoC container       |
| Spring MVC           | 6.x     | Camada REST (Controller)            |
| Spring Validation    | 6.x     | Bean Validation (Jakarta)           |
| Lombok               | 1.18.x  | Redução de boilerplate              |
| JUnit 5              | 5.10.x  | Framework de testes                 |
| AssertJ              | 3.x     | Asserções fluentes nos testes       |
| MockMvc              | -       | Testes de integração HTTP           |
| Maven                | 3.9.x   | Build e gerenciamento de dependências |

---

## Arquitetura

O projeto segue a arquitetura **MVC em camadas**:

```
Controller  →  Service  →  Repository  →  (Map em memória)
    ↑               ↑
  DTOs          Model / Enums
```

- **Controller**: recebe e valida a requisição HTTP, delega ao Service, retorna a resposta.
- **Service**: contém toda a lógica de negócio (unicidade, regras de parcelas, geração de NSU/código).
- **Repository**: abstrai o acesso ao armazenamento (ConcurrentHashMap em memória).
- **DTOs**: desacoplam a representação HTTP do modelo de domínio.
- **Handler**: GlobalExceptionHandler centraliza o tratamento de erros.

> Não há persistência em banco de dados conforme requisito do desafio.

---

## Padrões de Projeto

| Padrão             | Onde é aplicado                                                        |
|--------------------|------------------------------------------------------------------------|
| **Repository**     | `TransacaoRepository` isola o acesso aos dados                         |
| **Service Layer**  | `TransacaoService` concentra toda a lógica de negócio                  |
| **DTO**            | `PagamentoRequest` / `TransacaoResponse` desacoplam HTTP do domínio    |
| **Front Controller** | `GlobalExceptionHandler` (@RestControllerAdvice) trata todos os erros  |
| **Builder**        | Lombok `@Builder` em todas as entidades e DTOs                         |

---

## Regras de Negócio

| Regra                                          | Comportamento                              |
|------------------------------------------------|--------------------------------------------|
| `transacao.id` deve ser único                  | Retorna `422 Unprocessable Entity`         |
| `AVISTA` aceita somente `parcelas = 1`         | Retorna `422 Unprocessable Entity`         |
| Status inicial de todo pagamento               | `AUTORIZADO`                               |
| Status após estorno                            | `CANCELADO`                                |
| Estorno de transação já cancelada              | Retorna `422 Unprocessable Entity`         |
| Consulta por ID inexistente                    | Retorna `404 Not Found`                    |
| `nsu` e `codigoAutorizacao`                    | Gerados automaticamente no pagamento       |

---

## Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.9+

### Compilar e rodar

```bash
# Na raiz do projeto
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

### Apenas compilar

```bash
mvn clean package
java -jar target/challenge-1.0.0.jar
```

---

## Endpoints

| Método | URL                              | Descrição                        | Status de sucesso |
|--------|----------------------------------|----------------------------------|-------------------|
| POST   | `/api/pagamento`                 | Realiza um pagamento             | `201 Created`     |
| POST   | `/api/pagamento/{id}/estorno`    | Estorna uma transação            | `200 OK`          |
| GET    | `/api/pagamento`                 | Lista todas as transações        | `200 OK`          |
| GET    | `/api/pagamento/{id}`            | Consulta transação por ID        | `200 OK`          |

---

## Exemplos de Requisição

### POST `/api/pagamento` — Pagamento

**Request:**
```json
{
  "transacao": {
    "cartao": "4444********1234",
    "id": "1000023568900001",
    "descricao": {
      "valor": "500.50",
      "dataHora": "01/05/2021 18:30:00",
      "estabelecimento": "PetShop Mundo Cão"
    },
    "formaPagamento": {
      "tipo": "AVISTA",
      "parcelas": "1"
    }
  }
}
```

**Response `201 Created`:**
```json
{
  "transacao": {
    "cartao": "4444********1234",
    "id": "1000023568900001",
    "descricao": {
      "valor": "500.50",
      "dataHora": "01/05/2021 18:30:00",
      "estabelecimento": "PetShop Mundo Cão",
      "nsu": "1234567890",
      "codigoAutorizacao": "147258369",
      "status": "AUTORIZADO"
    },
    "formaPagamento": {
      "tipo": "AVISTA",
      "parcelas": "1"
    }
  }
}
```

---

### POST `/api/pagamento/{id}/estorno` — Estorno

**Response `200 OK`:**
```json
{
  "transacao": {
    "cartao": "4444********1234",
    "id": "1000023568900001",
    "descricao": {
      "valor": "500.50",
      "dataHora": "01/05/2021 18:30:00",
      "estabelecimento": "PetShop Mundo Cão",
      "nsu": "1234567890",
      "codigoAutorizacao": "147258369",
      "status": "CANCELADO"
    },
    "formaPagamento": {
      "tipo": "AVISTA",
      "parcelas": "1"
    }
  }
}
```

---

### Resposta de erro (exemplo)

```json
{
  "status": 422,
  "mensagem": "Transação com id '1000023568900001' já existe.",
  "campos": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Testes

O projeto segue a abordagem **TDD** com três classes de teste:

| Classe                      | Tipo              | Cobertura                                              |
|-----------------------------|-------------------|--------------------------------------------------------|
| `TransacaoServiceTest`      | Unitário          | Toda a lógica de negócio do Service (13 casos)         |
| `TransacaoRepositoryTest`   | Unitário          | Comportamento do repositório em memória (7 casos)      |
| `PagamentoControllerTest`   | Integração (HTTP) | Todos os endpoints via MockMvc (12 casos)              |

### Executar os testes

```bash
mvn test
```

### Relatório de cobertura (opcional — adicionar JaCoCo ao pom.xml)

```bash
mvn verify
# Relatório em: target/site/jacoco/index.html
```

---

## Estrutura do Projeto

```
src/
├── main/java/com/tools/challenge/
│   ├── ToolsChallengeApplication.java   # Entry point
│   ├── controller/
│   │   └── PagamentoController.java     # Endpoints REST
│   ├── service/
│   │   └── TransacaoService.java        # Lógica de negócio
│   ├── repository/
│   │   └── TransacaoRepository.java     # Armazenamento em memória
│   ├── model/
│   │   ├── Transacao.java               # Entidade de domínio
│   │   ├── Descricao.java
│   │   └── FormaPagamento.java
│   ├── dto/
│   │   ├── PagamentoRequest.java        # DTO de entrada
│   │   └── TransacaoResponse.java       # DTO de saída
│   ├── enums/
│   │   ├── StatusTransacao.java         # AUTORIZADO, NEGADO, CANCELADO
│   │   └── TipoPagamento.java           # AVISTA, PARCELADO_LOJA, PARCELADO_EMISSOR
│   ├── exception/
│   │   ├── TransacaoDuplicadaException.java
│   │   ├── TransacaoNaoEncontradaException.java
│   │   └── TransacaoJaCanceladaException.java
│   └── handler/
│       └── GlobalExceptionHandler.java  # Tratamento centralizado de erros
└── test/java/com/tools/challenge/
    ├── controller/
    │   └── PagamentoControllerTest.java
    └── service/
        ├── TransacaoServiceTest.java
        └── TransacaoRepositoryTest.java
```

---
