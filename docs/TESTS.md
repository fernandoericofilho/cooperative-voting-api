# Cooperative Voting API - Guia de Testes

Tudo que você precisa para testar e validar a API após `docker-compose up`.

## ⚡ Teste Rápido (2 minutos)

### 1️⃣ Verificar que a API está rodando
```bash
curl http://localhost:8080/api/v1/agendas
```
Esperado: `{"content":[],...}` (lista vazia de agendas)

---

## 📋 Fluxo Completo (5 minutos)

### 1️⃣ Criar uma Pauta
```bash
curl -X POST http://localhost:8080/api/v1/agendas \
  -H "Content-Type: application/json" \
  -d '{"title":"Aprovação de Novo Projeto","description":"Votação para implementação de novo sistema"}'
```

**Esperado (201 Created):**
```json
{
  "id": 1,
  "title": "Aprovação de Novo Projeto",
  "description": "Votação para implementação de novo sistema",
  "status": "NOT_STARTED",
  "createdAt": "2026-07-19T10:30:00",
  "sessionOpenedAt": null,
  "sessionClosesAt": null
}
```

**Copie o `id` (ex: 1) para os próximos passos**

### 2️⃣ Listar Pautas (v1 com Paginação)
```bash
curl http://localhost:8080/api/v1/agendas?page=0&size=10
```

**Esperado:**
```json
{
  "content": [{ "id": 1, "title": "...", ... }],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 10,
  "hasNext": false,
  "hasPrevious": false
}
```

### 3️⃣ Listar Pautas (v2 Simplificado - SEM Paginação)
```bash
curl http://localhost:8080/api/v2/agendas
```

**Esperado (formato compacto):**
```json
[
  {
    "id": 1,
    "title": "Aprovação de Novo Projeto",
    "status": "NOT_STARTED",
    "createdAt": "2026-07-19T10:30:00"
  }
]
```

### 4️⃣ Obter Pauta Específica
```bash
curl http://localhost:8080/api/v1/agendas/1
```

**Esperado:** Mesma resposta do passo 1

### 5️⃣ Abrir Sessão de Votação
```bash
curl -X POST http://localhost:8080/api/v1/agendas/1/sessions \
  -H "Content-Type: application/json" \
  -d '{"durationSeconds":60}'
```

**Esperado (200 OK):**
```json
{
  "id": 1,
  "title": "Aprovação de Novo Projeto",
  "status": "OPEN",
  "createdAt": "2026-07-19T10:30:00",
  "sessionOpenedAt": "2026-07-19T10:31:00",
  "sessionClosesAt": "2026-07-19T10:32:00"
}
```

### 6️⃣ Registrar Voto - SIM
```bash
curl -X POST http://localhost:8080/api/v1/agendas/1/votes \
  -H "Content-Type: application/json" \
  -d '{"cpfAssociado":"12345678901","voto":"SIM"}'
```

**Esperado (201 Created):**
```json
{
  "id": 1,
  "agendaId": 1,
  "cpfAssociado": "123.***.**-01",
  "voto": "SIM",
  "createdAt": "2026-07-19T10:31:15"
}
```

### 7️⃣ Registrar Voto - NAO (CPF Diferente)
```bash
curl -X POST http://localhost:8080/api/v1/agendas/1/votes \
  -H "Content-Type: application/json" \
  -d '{"cpfAssociado":"98765432100","voto":"NAO"}'
```

**Esperado (201 Created):** Voto registrado com NAO

### 8️⃣ Obter Resultado da Votação
```bash
curl http://localhost:8080/api/v1/agendas/1/result
```

**Esperado (200 OK):**
```json
{
  "agendaId": 1,
  "totalYes": 1,
  "totalNo": 1,
  "result": "TIE",
  "status": "CLOSED"
}
```

---

## 🧪 Testes de Erro (Validações)

### ❌ Voto Duplicado (409 Conflict)
```bash
curl -X POST http://localhost:8080/api/v1/agendas/1/votes \
  -H "Content-Type: application/json" \
  -d '{"cpfAssociado":"12345678901","voto":"NAO"}'
```

**Esperado (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Member already voted for this agenda"
}
```

### ❌ Sessão Não Aberta (422 Unprocessable Entity)
```bash
# Criar nova pauta (não abrir sessão)
curl -X POST http://localhost:8080/api/v1/agendas \
  -H "Content-Type: application/json" \
  -d '{"title":"Pauta 2","description":"Teste"}'

# Tentar votar sem abrir sessão
curl -X POST http://localhost:8080/api/v1/agendas/2/votes \
  -H "Content-Type: application/json" \
  -d '{"cpfAssociado":"11111111111","voto":"SIM"}'
```

**Esperado (422):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Voting session is not open"
}
```

### ❌ Pauta Não Encontrada (404 Not Found)
```bash
curl http://localhost:8080/api/v1/agendas/999
```

**Esperado (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Agenda not found with id 999"
}
```

---

## 📊 Swagger UI (Documentação Interativa)

Após `docker-compose up`, abra no navegador:
```
http://localhost:8080/swagger-ui.html
```

Lá você pode:
- ✅ Ver todos os endpoints (v1 e v2)
- ✅ Ver schemas de request/response
- ✅ Executar requisições diretamente
- ✅ Ver exemplos de erro

---

## 🧪 Rodar Testes Localmente

### Testes Unitários
```bash
./gradlew test
```

Saída esperada:
```
BUILD SUCCESSFUL
148 tests passed ✅
85.1% coverage ✅
```

### Cobertura Detalhada
```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html  # Mac/Linux
start build/reports/jacoco/test/html/index.html # Windows
```

---

## 📝 Endpoints Disponíveis

### API v1 (Full Format - Com Paginação)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/agendas` | Criar pauta |
| GET | `/api/v1/agendas` | Listar pautas (paginado) |
| GET | `/api/v1/agendas/{id}` | Obter pauta |
| POST | `/api/v1/agendas/{id}/sessions` | Abrir sessão |
| POST | `/api/v1/agendas/{id}/votes` | Registrar voto |
| GET | `/api/v1/agendas/{id}/result` | Obter resultado |

### API v2 (Simple Format - Sem Paginação)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v2/agendas` | Listar pautas (simples) |

---

## 🔗 Links Importantes

- **Swagger/OpenAPI:** http://localhost:8080/swagger-ui.html
- **API Base v1:** http://localhost:8080/api/v1
- **API Base v2:** http://localhost:8080/api/v2
- **Health Check:** http://localhost:8080/actuator/health
- **Banco de Dados:** postgres://localhost:5432 (user: votacao)

---

## 💾 Docker Commands

```bash
# Subir tudo (com build)
docker-compose up --build

# Parar containers
docker-compose down

# Ver logs
docker-compose logs -f app

# Conectar ao PostgreSQL
docker-compose exec postgres psql -U votacao -d votacao
  # Query: SELECT * FROM agenda;
  # Query: SELECT * FROM vote;
```

---

**Pronto! Você agora pode testar todos os cenários. 🚀**
