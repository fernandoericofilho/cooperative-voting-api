# Cooperative Voting API — Design

Data: 2026-07-16
Contexto: avaliação técnica (Sicredi) — API REST em Java/Spring Boot para gerenciar pautas e sessões de votação de cooperativa, consumida por um app mobile via protocolo de telas server-driven (Anexo 1 do enunciado).

## Objetivo

Construir os componentes de servidor (a aplicação cliente está fora do escopo avaliado) que permitam:
- Cadastrar pauta
- Abrir sessão de votação (duração configurável na abertura, default 1 minuto)
- Registrar voto de associado (Sim/Não, um voto por associado por pauta)
- Apurar resultado

Toda comunicação que gera interação do usuário segue o protocolo de telas do Anexo 1 (`FORMULARIO` / `SELECAO`).

## Fora de escopo

- Autenticação/autorização (abstraída pelo enunciado).
- Cadastro de associados — o identificador único do associado é o próprio CPF (dígitos), reaproveitado na integração do bônus 1. Não há tabela de associados.
- Aplicação cliente mobile ou web (inclusive frontend Angular — cogitado e descartado; o enunciado exclui explicitamente o client da avaliação).
- Deploy real em nuvem — a aplicação sobe localmente via docker-compose; fica pronta para deploy (Dockerfile) mas isso não será demonstrado ao vivo em um ambiente de nuvem.

## Stack técnica

- Java 21, Spring Boot 3.x
- Gradle com Kotlin DSL (`build.gradle.kts`) — apenas o build script; todo código de aplicação é Java.
- PostgreSQL (via docker-compose) como banco principal; profile `local` com H2 file-based para desenvolvimento sem Docker.
- Flyway para migrations versionadas (`V1__create_schema.sql`, ...), compatível com Postgres e H2.
- springdoc-openapi (Swagger UI em `/swagger-ui.html`).
- WebClient (Spring WebFlux client, uso síncrono/bloqueante) para a integração externa de CPF.

## Arquitetura — pacotes por feature

```
com.sicredi.votacao
├─ pauta/      Pauta (entity com sessão embutida), repository, service, controller, DTOs
├─ voto/       Voto entity, repository, service, controller
├─ tela/       Envelope Anexo 1: Formulario, Selecao, Item, Botao, Campo (reutilizado por todos os controllers de tela)
├─ external/   UserInfoClient — integração CPF (bônus 1)
└─ common/     GlobalExceptionHandler, OpenApiConfig, WebClientConfig
```

Camadas dentro de cada feature: Controller → Service (regras de negócio, únicas donas de validação) → Repository (Spring Data JPA) → Entity.

## Modelo de dados

- `pauta` (id, titulo, descricao, criada_em, sessao_aberta_em nullable, sessao_fecha_em nullable). Sessão embutida na própria pauta — não há necessidade de múltiplas sessões por pauta neste escopo, então uma tabela separada seria complexidade sem uso.
- `voto` (id, pauta_id FK, cpf_associado CHAR(11), voto ENUM('SIM','NAO'), criado_em). `UNIQUE(pauta_id, cpf_associado)` a nível de banco garante "um voto por associado" mesmo sob concorrência (constraint, não checagem em memória). Índice em `pauta_id` para a apuração agregada.

## Fluxo de telas (Anexo 1) e endpoints

Cada ação de negócio real é um endpoint independente e testável isoladamente (Swagger/curl), sem exigir navegação pela cadeia de telas:

```
GET  /api/v1/telas/home                 → SELECAO: "Cadastrar Pauta" / "Listar Pautas"
POST /api/v1/telas/pautas/novo          → FORMULARIO vazio (titulo, descricao) → botaoOk: POST /pautas
POST /api/v1/pautas                     → cria pauta [ação real] → FORMULARIO de detalhe
POST /api/v1/telas/pautas               → SELECAO listando pautas → item: POST /pautas/{id}
POST /api/v1/pautas/{id}                → FORMULARIO de detalhe; botão varia por estado:
                                             sessão não aberta → POST /pautas/{id}/sessoes/tela
                                             sessão aberta     → POST /pautas/{id}/votos/tela
                                             sessão encerrada  → POST /pautas/{id}/resultado
POST /api/v1/pautas/{id}/sessoes/tela   → FORMULARIO pede duração (default 60s) → botaoOk: POST /sessoes
POST /api/v1/pautas/{id}/sessoes        → abre sessão [ação real] → FORMULARIO de detalhe atualizado
POST /api/v1/pautas/{id}/votos/tela     → FORMULARIO pede CPF → botaoOk: POST /votos/opcoes
POST /api/v1/pautas/{id}/votos/opcoes   → SELECAO Sim/Não (body carrega o CPF capturado) → item: POST /votos
POST /api/v1/pautas/{id}/votos          → registra voto [ação real, chama integração CPF] → FORMULARIO de confirmação
POST /api/v1/pautas/{id}/resultado      → FORMULARIO de exibição com totais e resultado
```

Estado "aberta"/"encerrada" é derivado comparando `now()` com `sessao_fecha_em` — sem job/scheduler, calculado na leitura.

## Bônus 1 — Integração CPF

- `UserInfoClient` (WebClient), base URL externalizável via `app.external.user-info-url` (application.yml / env var), timeout 3s, até 2 retries com backoff curto.
- Chamada ocorre no endpoint real `POST /pautas/{id}/votos` (não nas telas intermediárias), para não gastar a checagem externa antes da confirmação do usuário.
- 404 (CPF inválido) → 400 rejeitando o voto. `UNABLE_TO_VOTE` → 422 rejeitando o voto. Falha/timeout persistente do serviço externo → fail-closed (502/503), nunca aceita voto sem confirmação — decisão consciente de integridade sobre disponibilidade, documentada no README. Alternativa de fila (Kafka) foi cogitada e descartada por ser desproporcional ao problema (chamada HTTP síncrona simples) e incompatível com o protocolo de telas, que espera resposta imediata a cada ação.

## Bônus 2 — Performance

- Apuração via `SELECT voto, COUNT(*) FROM voto WHERE pauta_id = ? GROUP BY voto` — nunca carrega votos em memória; índice em `pauta_id` (e a constraint única) sustentam a agregação em centenas de milhares de linhas.
- Script k6 (`load-test/votacao.js`) simulando associados votando concorrentemente numa pauta, documentado no README com instruções de execução e resultados esperados.

## Bônus 3 — Versionamento

- URI path versioning (`/api/v1/...`) em todos os endpoints. README explica o trade-off frente a header versioning e content negotiation: URI é visível, cacheável e trivial de testar em Swagger/Postman — adequado ao tamanho desta API. Não são implementadas múltiplas versões (o enunciado pede a explicação da estratégia, não uma v2 funcional).

## Testes

- Unitários (JUnit 5 + Mockito) nos services: sessão já aberta, voto duplicado, sessão encerrada, CPF inválido/não habilitado, cálculo de resultado.
- Integração (MockMvc/WebTestClient + H2 em memória) cobrindo o fluxo HTTP completo, incluindo os endpoints de tela do Anexo 1. Sem Testcontainers — evita exigir Docker apenas para rodar `./gradlew test`.
- k6 fora do JUnit, documentado à parte no README.

## Infraestrutura / execução

- `docker-compose.yml`: serviço `postgres` + serviço `app` (Dockerfile multi-stage: build Gradle, runtime JRE 21 slim). `docker-compose up --build` sobe tudo — forma principal de execução para quem for avaliar.
- Profile `local` com H2 file-based para desenvolvimento sem Docker.
- README cobre: como rodar (docker-compose e alternativa local), diagrama do fluxo de telas, exemplos de curl do fluxo completo, explicação das escolhas técnicas (bônus 3, resiliência CPF), como rodar testes e k6.

## Erros e exceções

- `GlobalExceptionHandler` central mapeando exceções de domínio para HTTP: pauta/sessão inexistente → 404; sessão já aberta / sessão encerrada / voto duplicado → 409 ou 422 (a definir por caso, documentado no código); CPF inválido → 400; CPF não habilitado → 422; falha na integração externa → 502/503. Corpo de erro padrão simples (timestamp, status, error, message) — sem envelope de tela para erros, já que o app cliente está fora do escopo avaliado.
