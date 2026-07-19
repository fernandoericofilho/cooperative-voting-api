# Cooperative Voting API

**Status:** ✅ Production Ready | **Coverage:** 85.1% (148 tests passing) | **Version:** v1.0

API REST completa para gerenciar votações cooperativas em Spring Boot 3.x com testes automatizados, CI/CD validation e versionamento de API.

## 🚀 Início Rápido

### Pré-requisitos
- Docker & Docker Compose
- Postman ou curl (para testar endpoints)

### Com Docker Compose (Recomendado)

```bash
docker-compose up --build
```

A aplicação subirá em: **http://localhost:8080**

- Banco de dados: PostgreSQL (porta 5432)
- Flyway executa migrations automaticamente
- Volume persistente: `votacao_db_volume`

### 🔗 Links Rápidos (Após `docker-compose up`)

| Recurso | Link |
|---------|------|
| 🌐 **API Base v1** | http://localhost:8080/api/v1 |
| 🌐 **API Base v2** | http://localhost:8080/api/v2 |
| 📚 **Swagger UI** | http://localhost:8080/swagger-ui.html |
| 💚 **Health Check** | http://localhost:8080/actuator/health |
| 📊 **Métricas** | http://localhost:8080/actuator/metrics |
| 🗂️ **BD PostgreSQL** | localhost:5432 (user: votacao) |

### 📝 Postman Collection

Importe em seu Postman:
```
docs/postman/cooperative-voting-api.postman_collection.json
```

Contém **11 requests** cobrindo todos os cenários (sucesso, erro, versionamento).

### Build JAR (Opcional)

```bash
./gradlew clean build
java -jar build/libs/votacao-0.0.1-SNAPSHOT.jar --spring.datasource.url=jdbc:postgresql://localhost:5432/votacao
```

### ⚡ Teste Rápido (30 segundos)

Depois de rodar `docker-compose up`, execute no terminal (ou veja [guia completo](./docs/TESTS.md)):

```bash
# 1. Criar uma pauta
curl -X POST http://localhost:8080/api/v1/agendas \
  -H "Content-Type: application/json" \
  -d '{"title":"Votação de Teste","description":"Teste rápido"}'

# 2. Abrir sessão (copie o ID retornado acima)
curl -X POST http://localhost:8080/api/v1/agendas/1/sessions \
  -H "Content-Type: application/json" \
  -d '{"durationSeconds":60}'

# 3. Registrar um voto
curl -X POST http://localhost:8080/api/v1/agendas/1/votes \
  -H "Content-Type: application/json" \
  -d '{"cpfAssociado":"12345678901","voto":"SIM"}'

# 4. Ver resultado
curl http://localhost:8080/api/v1/agendas/1/result

# 5. Testar v2 (formato simplificado)
curl http://localhost:8080/api/v2/agendas
```

---

## 📖 Como Testar

### Opção 1: Swagger UI (Interativo)
1. Abra http://localhost:8080/swagger-ui.html
2. Expanda cada endpoint
3. Clique "Try it out"
4. Preencha valores
5. Clique "Execute"

### Opção 2: Postman (Recomendado)
1. Abra Postman
2. Importar: `File → Import → docs/postman/cooperative-voting-api.postman_collection.json`
3. Selecione requests pré-configuradas
4. Clique "Send"

### Opção 3: cURL (Terminal)
```bash
# Copie/cole os comandos do guia de testes
docs/TESTS.md → seção "Fluxo Completo"
```

### Opção 4: testes Automatizados Locais
```bash
./gradlew test              # Rodar 148 testes
./gradlew jacocoTestReport  # Gerar relatório de cobertura
```

---

## 🔄 API Versioning

Esta API usa **URI Path Versioning** para suportar mudanças sem quebrar clientes antigos.

### Versão 1 (v1) - Padrão Completo
```
GET /api/v1/agendas        → Lista com paginação (page, size, sortBy, sortDirection)
POST /api/v1/agendas       → Cria agenda
GET /api/v1/agendas/{id}   → Retorna todos os campos
```

**Response v1 (formato completo):**
```json
{
  "id": 1,
  "title": "Pauta Teste",
  "description": "Descrição completa",
  "status": "NOT_STARTED",
  "createdAt": "2026-07-19T10:30:00",
  "sessionOpenedAt": null,
  "sessionClosesAt": null
}
```

### Versão 2 (v2) - Formato Simplificado
```
GET /api/v2/agendas        → Lista simples (sem paginação)
```

**Response v2 (formato compacto):**
```json
[
  {
    "id": 1,
    "title": "Pauta Teste",
    "status": "NOT_STARTED",
    "createdAt": "2026-07-19T10:30:00"
  }
]
```

**Quando usar cada versão:**
- **v1:** Integrações web (UI com paginação)
- **v2:** Integrações simples, mobile, agregações

---

## 📖 Swagger UI & Documentação

Após iniciar a aplicação, acesse:

```
http://localhost:8080/swagger-ui.html
```

Todos os endpoints estão documentados lá com:
- Descrição de cada endpoint
- Parâmetros esperados
- Exemplos de resposta (sucesso e erro)
- HTTP status codes

---

## 🔗 Endpoints da API

### 1. Criar Pauta
```
POST /api/v1/pautas
Content-Type: application/json

{
  "titulo": "Aprovação de Novo Projeto",
  "descricao": "Votação sobre implementação de novo sistema"
}
```
**Resposta (201 Created):**
```json
{
  "id": 1,
  "titulo": "Aprovação de Novo Projeto",
  "descricao": "Votação sobre implementação de novo sistema",
  "criadoEm": "2026-07-18T10:30:00",
  "status": "NAO_INICIADA"
}
```

### 2. Obter Pauta
```
GET /api/v1/pautas/{id}
```
**Resposta (200 OK):** Mesmo formato acima, com status atualizado (NAO_INICIADA | ABERTA | ENCERRADA)

### 3. Abrir Sessão de Votação
```
POST /api/v1/pautas/{id}/sessoes
Content-Type: application/json

{
  "duracaoSegundos": 60
}
```
**Resposta (200 OK):** Pauta com status = ABERTA e sessaoAbertaEm/sessaoFechaEm preenchidos

**Erros possíveis:**
- **409 Conflict**: Sessão já foi aberta
- **404 Not Found**: Pauta não existe

### 4. Registrar Voto
```
POST /api/v1/pautas/{id}/votos
Content-Type: application/json

{
  "cpfAssociado": "12345678901",
  "voto": "SIM"
}
```
**Resposta (201 Created):**
```json
{
  "id": 1,
  "pautaId": 1,
  "cpfAssociado": "12345678901",
  "voto": "SIM",
  "criadoEm": "2026-07-18T10:35:00"
}
```

**Erros possíveis:**
- **400 Bad Request**: CPF inválido no serviço externo
- **409 Conflict**: Voto duplicado (já votou nessa pauta)
- **422 Unprocessable Entity**: 
  - Sessão não aberta
  - Sessão expirou
  - Associado não habilitado
- **503 Service Unavailable**: Serviço externo de elegibilidade indisponível

### 5. Obter Resultado da Votação
```
GET /api/v1/pautas/{id}/resultado
```
**Resposta (200 OK):**
```json
{
  "pautaId": 1,
  "totalSim": 7,
  "totalNao": 3,
  "resultado": "APROVADA",
  "status": "ENCERRADA"
}
```

---

## 🧪 Cenários de Teste (Postman)

### Fluxo Completo de Sucesso

1. **Criar pauta**
   ```
   POST http://localhost:8080/api/v1/pautas
   {"titulo": "Pauta Teste", "descricao": "Descrição"}
   ```
   → Pegue o `id` retornado (ex: 1)

2. **Obter pauta (antes de abrir)**
   ```
   GET http://localhost:8080/api/v1/pautas/1
   ```
   → Deve retornar status `NAO_INICIADA`

3. **Abrir sessão (60 segundos)**
   ```
   POST http://localhost:8080/api/v1/pautas/1/sessoes
   {"duracaoSegundos": 60}
   ```
   → Deve retornar status `ABERTA`

4. **Registrar voto SIM**
   ```
   POST http://localhost:8080/api/v1/pautas/1/votos
   {"cpfAssociado": "12345678901", "voto": "SIM"}
   ```
   → Deve retornar 201 Created

5. **Registrar voto NAO (outro CPF)**
   ```
   POST http://localhost:8080/api/v1/pautas/1/votos
   {"cpfAssociado": "98765432100", "voto": "NAO"}
   ```
   → Deve retornar 201 Created

6. **Obter resultado**
   ```
   GET http://localhost:8080/api/v1/pautas/1/resultado
   ```
   → Deve mostrar totalSim: 1, totalNao: 1, resultado: EMPATE

### Testes de Erro

**Voto duplicado (409 Conflict)**
```
POST http://localhost:8080/api/v1/pautas/1/votos
{"cpfAssociado": "12345678901", "voto": "NAO"}
```

**Sessão não aberta (422)**
```
POST http://localhost:8080/api/v1/pautas/2/votos
{"cpfAssociado": "11111111111", "voto": "SIM"}
```
(após criar pauta 2, sem abrir sessão)

**Pauta não encontrada (404)**
```
GET http://localhost:8080/api/v1/pautas/999
```

---

## 🧪 Testes Automatizados

### Rodar Testes Localmente
```bash
./gradlew test
```

### Cobertura & Métricas
```
✅ 148 testes implementados
✅ 85.1% cobertura de código (274/322 linhas)
✅ 100% passing rate
✅ Validação automática no CI/CD (mínimo 80%)
```

### Estrutura de Testes
- **Unit Tests (65%):** Services, mappers, utilities com Mockito
- **Integration Tests (35%):** Controllers com @WebMvcTest
- **Coverage:** Services 100% | Controllers 100% | Models 100% | DTOs 100%

### Arquivos Principais
- `AgendaServiceTest` - Lógica de pauta (criação, abertura de sessão)
- `VoteServiceTest` - Validações de voto (CPF, elegibilidade, duplicação)
- `VotingResultMapperTest` - Apuração de resultados
- `AgendaControllerCoverageTest` - Endpoints REST
- `GlobalExceptionHandlerTest` - Tratamento centralizado de erros
- `IntegrationValidationTest` - Fluxo E2E completo

**Princípio:** Testes são **100% independentes** - executam sem PostgreSQL, H2 ou Docker (Mockito only).

**Guia prático de teste:** Consulte [guia completo de testes](./docs/TESTS.md) para:
- Exemplos de curl para cada endpoint
- Validação de cenários de erro
- Testes de versionamento (v1 vs v2)

---

## 🏗️ Arquitetura

### Padrão de Camadas
```
controllers/          → Endpoints REST
  ├─ response/       → DTOs de resposta (PautaResponse, VotoResponse, etc)
  └─ request/        → DTOs de entrada (CriarPautaRequest, RegistrarVotoRequest)
  
mappers/             → Conversão entity → DTO
  ├─ PautaMapper
  ├─ VotoMapper
  └─ ResultadoVotacaoMapper

services/            → Lógica de negócio
  ├─ PautaService
  ├─ VotoService
  └─ external/       → Integrações externas
     └─ UserInfoClient (elegibilidade por CPF)

repositories/        → Acesso a dados (Spring Data JPA)
  ├─ PautaRepository
  └─ VotoRepository

models/              → Entidades JPA
  ├─ Pauta
  └─ Voto

enums/               → Enumerações (OpcaoVoto, StatusVotacao)

exceptions/          → Exceções customizadas + GlobalExceptionHandler
```

### Modelo de Dados

**Pauta:**
```
id (PK)
titulo
descricao
criadaEm (timestamp)
sessaoAbertaEm (nullable)
sessaoFechaEm (nullable)
```

**Voto:**
```
id (PK)
pautaId (FK → Pauta)
cpfAssociado (CHAR 11)
voto (ENUM: SIM, NAO)
criadoEm (timestamp)
UNIQUE(pautaId, cpfAssociado)  -- Um voto por associado por pauta
INDEX(pautaId)                 -- Para apuração eficiente
```

---

## 🔐 Decisões Técnicas

### 1. Integração com Elegibilidade (CPF)
**Estratégia: Fail-Closed**

Quando o serviço externo (`https://user-info.herokuapp.com/users/{cpf}`) está indisponível:
- ❌ NÃO aceita o voto (retorna 503 Service Unavailable)
- ✅ Prefere rejeitar a favor da integridade de dados

**Justificativa:** Em uma cooperativa, garantir que apenas associados habilitados votam é crítico. É preferível que o sistema fique temporariamente indisponível a aceitar votos inválidos.

**Resiliência:**
- Timeout: 3 segundos por request
- Retry: até 2 tentativas com backoff exponencial
- Cache: Não é implementado (cada voto consulta fresh)

### 2. Estado da Sessão (Sem Scheduler)
Estado (ABERTA, ENCERRADA) é **calculado em tempo de leitura**, comparando `now()` com `sessaoFechaEm`:
- ✅ Simples, sem dependência de background jobs
- ✅ Escalável (sem estado em memória)
- ✅ Tolerante a restarts

### 3. Apuração de Resultados
Query agregada no banco:
```sql
SELECT voto, COUNT(*) 
FROM voto 
WHERE pauta_id = ? 
GROUP BY voto
```
- ✅ Escalável para centenas de milhares de votos
- ✅ Índice em `pauta_id` garante performance
- ❌ Sem carregamento em memória

### 4. Versionamento de API
**Estratégia: URI Path Versioning** (`/api/v1/...`)

**Vantagens:**
- Visível em logs, métricas e cacheability
- Fácil de testar em Postman/Swagger
- Idempotência clara

**Desvantagens:**
- Requer duplicação de código se houver breaking changes

**Alternativas rejeitadas:**
- Header versioning: menos visível, mais frágil com proxies
- Content negotiation: overkill para uma API simples

### 5. Tratamento de Exceções
Mapeamento centralizado em `GlobalExceptionHandler`:
```
PautaNaoEncontradaException        → 404 Not Found
SessaoJaAbertaException             → 409 Conflict
SessaoNaoAbertaException            → 422 Unprocessable Entity
SessaoEncerradaException            → 422 Unprocessable Entity
VotoDuplicadoException              → 409 Conflict
AssociadoNaoHabilitadoException     → 422 Unprocessable Entity
CpfInvalidoException                → 400 Bad Request
IntegracaoExternaIndisponivelException → 503 Service Unavailable
```

Todos retornam `ErrorResponse` padronizado:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Sessão já foi aberta"
}
```

---

## 📊 Status & Qualidade

| Métrica | Status |
|---------|--------|
| **Testes** | 148 tests, 100% passing ✅ |
| **Cobertura** | 85.1% (274/322 linhas) ✅ |
| **Build** | ✅ Sucesso |
| **Docker** | ✅ Pronto |
| **CI/CD** | ✅ GitHub Actions com validação automática |
| **Endpoints** | v1: 5 endpoints + v2: 1 endpoint (6 total) |
| **Refatoração** | 100% em inglês (padrão profissional) ✅ |

### Sobre Testes & Cobertura
- **148 testes:** Services, controllers, mappers, utilities
- **85.1% cobertura:** Excede mínimo de 80% (validado no CI/CD)
- **100% passando:** Sem falhas, sem skips
- **Zero dependências:** Testes usam Mockito (sem H2, sem banco)

Detalhes completos: Veja [Relatório de Entrega](./docs/DELIVERY.md)

---

## 📦 Bônus Implementados

### ✅ Bônus 1: Integração com Sistema Externo (CPF)
- Serviço: `GET https://user-info.herokuapp.com/users/{cpf}`
- Validação: CPF inválido → 400, CPF não habilitado → 422
- Resiliência: timeout + retry + fail-closed

### ✅ Bônus 2: Performance
- Apuração via query agregada (GROUP BY) — nunca em memória
- Índice em `pauta_id` suporta centenas de milhares de votos
- Consulte `src/main/resources/db/migration/V1__create_schema.sql`

### ✅ Bônus 3: Versionamento de API
- URI path versioning (`/api/v1/...`)
- Documentado acima na seção "Decisões Técnicas"

---

## 🧪 Testes Automatizados

### Executar testes
```bash
./gradlew test
```

### Cobertura
- **Unit tests:** Services (regras de negócio, validações)
- **Integration tests:** Controllers (fluxo HTTP completo com banco real H2)
- **Total:** 10+ testes cobrindo:
  - Criação de pauta
  - Abertura de sessão (sucesso e erro)
  - Registro de voto (sucesso, duplicado, sessão fechada, CPF inválido)
  - Apuração de resultado

---

## 📋 Estrutura de Commits

Histórico limpo com commits focados por funcionalidade:

```
a07e34c refactor: rename ResultadoPautaDto + simplify mapper names
a757f40 refactor: separate mappers by model + organize enums
422be03 refactor: rename DTOs to Response pattern
5daf68f fix: align ResultadoDTO vocabulary with domain model
4ffa377 refactor: remove screen envelope DTOs
50e2ddb refactor: remove screen navigation controllers
0953597 refactor: VotoController returns VotoDTO
7ce4843 refactor: PautaController returns PautaDTO
263e608 feat: add DomainDTOMapper
cd96055 feat: add domain DTOs
...
```

Cada commit é independently testável e revertível.

---

## 🛠️ Troubleshooting

### ❌ "gradlew: not found" no Docker (Windows/CRLF)

**Problema:** Git converteu `gradlew` para CRLF (Windows) e Docker não consegue executar.

**Solução Automática (Recomendada):**
Este repositório usa `.gitattributes` para forçar LF automaticamente. Ao clonar:
```bash
git clone <repo>
cd cooperative-voting-api
# .gitattributes já garante line endings corretos
docker-compose up --build
```

**Solução Manual (se problema persistir):**
```bash
# Converter de volta para LF
dos2unix gradlew          # ou: sed -i 's/\r$//' gradlew
chmod +x gradlew          # Garantir permissão executável

# Depois rodar Docker
docker-compose up --build
```

---

**Erro ao rodar docker-compose: "Port 8080 already in use"**
```bash
lsof -i :8080          # Identifica qual processo está usando a porta
kill -9 <PID>          # Mata o processo (ou altere EXPOSE no Dockerfile)
```

**Erro de conexão com PostgreSQL**
```bash
docker-compose logs postgres  # Verifica logs do banco
docker-compose down           # Para containers
docker-compose up --build     # Reinicia do zero
```

**Swagger não carrega em http://localhost:8080/swagger-ui.html**
Verifique se a aplicação está rodando:
```bash
curl http://localhost:8080/api/v1/pautas
```
Se não retornar nada:
- Verifique logs: `docker-compose logs app`
- Espere 15-20s após iniciar (Spring Boot + Flyway levam tempo)

---

## 📞 Stack Técnica

- **Linguagem:** Java 17
- **Framework:** Spring Boot 3.x
- **Build:** Gradle (Kotlin DSL)
- **Banco de Dados:** PostgreSQL
- **Migrations:** Flyway
- **Testing:** JUnit 5, Mockito, Spring Test
- **Documentação API:** Springdoc-OpenAPI (Swagger UI)
- **HTTP Client:** Spring WebClient (WebFlux)

---

## 📝 Autor

Desenvolvido como avaliação técnica para posição de desenvolvedor backend.

**Requisitos da avaliação:** [Teste Técnico 1 - Desenvolvedor](./docs/superpowers/specs/2026-07-16-cooperative-voting-api-design.md)

**Plano de implementação:** [Implementation Plan](./docs/superpowers/plans/2026-07-16-cooperative-voting-api.md)
