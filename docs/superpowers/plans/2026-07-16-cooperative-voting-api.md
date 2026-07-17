# Cooperative Voting API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Spring Boot REST API (Java 21) that manages cooperative voting pautas/sessions/votes, exposing both real business endpoints and the Anexo-1 server-driven screen protocol for a mobile client, backed by Postgres (docker-compose) with Flyway migrations, plus the CPF-eligibility integration, performance-oriented result counting, and API versioning.

**Architecture:** Layered package structure by technical concern (`controllers`, `controllers/request`, `services`, `services/external`, `repositories`, `models`, `dtos`, `exceptions`, `mappers`, `config`), matching this org's existing convention. Controllers depend only on services; services own all business rules and return plain DTOs/entities; mappers are the only classes that build the Anexo-1 screen envelope responses from entities; repositories are plain Spring Data JPA. The Anexo-1 screen envelope (`Formulario`/`Selecao`) is a standalone, reusable set of DTOs built once and consumed by every screen-returning controller.

**Tech Stack:** Java 21, Spring Boot 3.3.x, Gradle (Kotlin DSL build script only — all app code is Java), Spring Data JPA, Flyway, PostgreSQL (docker-compose) / H2 file (local profile), Spring WebFlux `WebClient` (blocking use) for the external CPF check, springdoc-openapi, JUnit 5 + Mockito + MockMvc, k6.

## Global Constraints

- All application code is Java; only `build.gradle.kts`/`settings.gradle.kts` use Kotlin DSL syntax.
- Associado identifier is the CPF (11 digits, no formatting) — no `Associado` entity/table.
- Every write to `voto` must be protected by the DB-level `UNIQUE(pauta_id, cpf_associado)` constraint — never rely solely on an application-level pre-check for correctness under concurrency.
- All money is not applicable here (no monetary fields), but all timestamps use `LocalDateTime` and are set server-side, never trusted from the client.
- Screen JSON responses must match Anexo 1 exactly: `FORMULARIO` has `tipo`, `titulo`, `itens`, `botaoOk`, optional `botaoCancelar`; `SELECAO` has `tipo`, `titulo`, `itens` (each with `texto`, `url`, optional `body`). Null optional fields must be omitted from JSON (`@JsonInclude(NON_NULL)`).
- All endpoints are under `/api/v1/...` (URI path versioning).
- External CPF check: base URL from `app.external.user-info-url` (config/env, never hardcoded), 3s timeout, up to 2 retries, fail-closed on persistent failure.
- Every Service/Repository behavior change must ship with a test that fails without the change and passes with it (TDD, per task steps below).

---

### Task 1: Project scaffold and health check

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `src/main/java/com/sicredi/votacao/VotacaoApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `.gitignore`
- Test: `src/test/java/com/sicredi/votacao/VotacaoApplicationTests.java`

**Interfaces:**
- Produces: Spring Boot app bootable via `./gradlew bootRun`, base package `com.sicredi.votacao`, `application.yml` with `default` (Postgres) and `local` (H2) profiles (datasource left as placeholders here — filled in Task 2).

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "cooperative-voting-api"
```

- [ ] **Step 2: Create `build.gradle.kts`**

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.sicredi"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [ ] **Step 3: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx1g
```

- [ ] **Step 4: Create `src/main/java/com/sicredi/votacao/VotacaoApplication.java`**

```java
package com.sicredi.votacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VotacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VotacaoApplication.class, args);
    }
}
```

- [ ] **Step 5: Create `src/main/resources/application.yml`**

```yaml
spring:
  application:
    name: cooperative-voting-api
  profiles:
    active: default

app:
  external:
    user-info-url: https://user-info.herokuapp.com
    timeout-seconds: 3

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- [ ] **Step 6: Create `.gitignore`**

```
build/
.gradle/
*.iml
.idea/
out/
data/
```

- [ ] **Step 7: Write the failing health check test**

```java
package com.sicredi.votacao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VotacaoApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

This will fail at this point because there is no datasource configured yet for the default profile (Task 2 fixes this) — run it now only to confirm the project compiles and Gradle wiring works; expect a datasource-related failure, not a compilation error.

- [ ] **Step 8: Run and verify failure reason is datasource, not compilation**

Run: `./gradlew test --tests VotacaoApplicationTests`
Expected: FAILS with a `DataSource`/`Driver` configuration error (not a compile error).

- [ ] **Step 9: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties src .gitignore
git commit -m "chore: scaffold Spring Boot project"
```

---

### Task 2: Flyway migrations and datasource profiles

**Files:**
- Create: `src/main/resources/db/migration/V1__create_schema.sql`
- Modify: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Test: `src/test/java/com/sicredi/votacao/VotacaoApplicationTests.java` (already exists, now must pass)

**Interfaces:**
- Produces: tables `pauta` and `voto` per the design's data model, available to both `default` (Postgres) and `local` (H2) profiles.

- [ ] **Step 1: Create the Flyway migration**

```sql
CREATE TABLE pauta (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao VARCHAR(2000),
    criada_em TIMESTAMP NOT NULL,
    sessao_aberta_em TIMESTAMP,
    sessao_fecha_em TIMESTAMP
);

CREATE TABLE voto (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pauta_id BIGINT NOT NULL REFERENCES pauta(id),
    cpf_associado CHAR(11) NOT NULL,
    voto VARCHAR(3) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_voto_pauta_cpf UNIQUE (pauta_id, cpf_associado)
);

CREATE INDEX idx_voto_pauta_id ON voto (pauta_id);
```

- [ ] **Step 2: Configure the `default` (Postgres) datasource in `application.yml`**

Add under the existing `spring:` key:

```yaml
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:votacao}
    username: ${DB_USER:votacao}
    password: ${DB_PASSWORD:votacao}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

- [ ] **Step 3: Create `src/main/resources/application-local.yml`**

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/votacao;MODE=PostgreSQL
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

- [ ] **Step 4: Run the health check test against the `local` profile**

Run: `./gradlew test --tests VotacaoApplicationTests -Dspring.profiles.active=local`
Expected: PASS — Flyway applies `V1__create_schema.sql` against a file-based H2 database, context loads successfully.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources
git commit -m "feat: add Flyway schema and datasource profiles"
```

---

### Task 3: Pauta entity, repository, and criarPauta

**Files:**
- Create: `src/main/java/com/sicredi/votacao/pauta/Pauta.java`
- Create: `src/main/java/com/sicredi/votacao/pauta/PautaRepository.java`
- Create: `src/main/java/com/sicredi/votacao/pauta/PautaService.java`
- Create: `src/main/java/com/sicredi/votacao/pauta/PautaNaoEncontradaException.java`
- Test: `src/test/java/com/sicredi/votacao/pauta/PautaServiceTest.java`

**Interfaces:**
- Produces: `PautaService.criarPauta(String titulo, String descricao)` returns `Pauta` (persisted, with generated id); `PautaService.buscarPorId(Long id)` returns `Pauta` or throws `PautaNaoEncontradaException`.
- Consumes: nothing from earlier tasks besides the schema from Task 2.

- [ ] **Step 1: Create the `Pauta` entity**

```java
package com.sicredi.votacao.pauta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "pauta")
public class Pauta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "sessao_aberta_em")
    private LocalDateTime sessaoAbertaEm;

    @Column(name = "sessao_fecha_em")
    private LocalDateTime sessaoFechaEm;

    protected Pauta() {
    }

    public Pauta(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getSessaoAbertaEm() {
        return sessaoAbertaEm;
    }

    public LocalDateTime getSessaoFechaEm() {
        return sessaoFechaEm;
    }

    public boolean sessaoFoiAberta() {
        return sessaoAbertaEm != null;
    }

    public boolean sessaoEstaAberta() {
        return sessaoFoiAberta() && LocalDateTime.now().isBefore(sessaoFechaEm);
    }

    public boolean sessaoEstaEncerrada() {
        return sessaoFoiAberta() && !sessaoEstaAberta();
    }
}
```

- [ ] **Step 2: Create `PautaRepository`**

```java
package com.sicredi.votacao.pauta;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, Long> {
}
```

- [ ] **Step 3: Create `PautaNaoEncontradaException`**

```java
package com.sicredi.votacao.pauta;

public class PautaNaoEncontradaException extends RuntimeException {

    public PautaNaoEncontradaException(Long id) {
        super("Pauta não encontrada: " + id);
    }
}
```

- [ ] **Step 4: Write the failing test for `criarPauta` and `buscarPorId`**

```java
package com.sicredi.votacao.pauta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class PautaServiceTest {

    @Autowired
    private PautaService pautaService;

    @Test
    void criarPautaPersisteEDevolveComId() {
        Pauta pauta = pautaService.criarPauta("Reforma do estatuto", "Discussão sobre o artigo 5");

        assertThat(pauta.getId()).isNotNull();
        assertThat(pautaService.buscarPorId(pauta.getId()).getTitulo()).isEqualTo("Reforma do estatuto");
    }

    @Test
    void buscarPorIdInexistenteLancaExcecao() {
        assertThatThrownBy(() -> pautaService.buscarPorId(999_999L))
            .isInstanceOf(PautaNaoEncontradaException.class);
    }
}
```

- [ ] **Step 5: Run test to verify it fails**

Run: `./gradlew test --tests PautaServiceTest -Dspring.profiles.active=local`
Expected: FAIL — `PautaService` does not exist yet (compile error).

- [ ] **Step 6: Create `PautaService`**

```java
package com.sicredi.votacao.pauta;

import org.springframework.stereotype.Service;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    public Pauta criarPauta(String titulo, String descricao) {
        return pautaRepository.save(new Pauta(titulo, descricao));
    }

    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
            .orElseThrow(() -> new PautaNaoEncontradaException(id));
    }
}
```

- [ ] **Step 7: Run test to verify it passes**

Run: `./gradlew test --tests PautaServiceTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/sicredi/votacao/pauta src/test/java/com/sicredi/votacao/pauta
git commit -m "feat: add Pauta entity, repository and creation service"
```

---

### Task 4: Abrir sessão de votação

**Files:**
- Modify: `src/main/java/com/sicredi/votacao/pauta/Pauta.java`
- Modify: `src/main/java/com/sicredi/votacao/pauta/PautaService.java`
- Create: `src/main/java/com/sicredi/votacao/pauta/SessaoJaAbertaException.java`
- Test: `src/test/java/com/sicredi/votacao/pauta/PautaServiceTest.java`

**Interfaces:**
- Consumes: `Pauta`, `PautaRepository`, `PautaService.buscarPorId` from Task 3.
- Produces: `PautaService.abrirSessao(Long pautaId, Long duracaoSegundos)` — `duracaoSegundos` nullable, defaults to 60; returns updated `Pauta`; throws `SessaoJaAbertaException` if already opened.

- [ ] **Step 1: Write the failing tests**

Append to `PautaServiceTest`:

```java
    @Test
    void abrirSessaoComDuracaoInformadaDefineJanela() {
        Pauta pauta = pautaService.criarPauta("Pauta A", "desc");

        Pauta atualizada = pautaService.abrirSessao(pauta.getId(), 120L);

        assertThat(atualizada.sessaoFoiAberta()).isTrue();
        assertThat(atualizada.getSessaoFechaEm())
            .isAfter(atualizada.getSessaoAbertaEm().plusSeconds(119))
            .isBefore(atualizada.getSessaoAbertaEm().plusSeconds(121));
    }

    @Test
    void abrirSessaoSemDuracaoUsaSessentaSegundosPorDefault() {
        Pauta pauta = pautaService.criarPauta("Pauta B", "desc");

        Pauta atualizada = pautaService.abrirSessao(pauta.getId(), null);

        assertThat(atualizada.getSessaoFechaEm())
            .isAfter(atualizada.getSessaoAbertaEm().plusSeconds(59))
            .isBefore(atualizada.getSessaoAbertaEm().plusSeconds(61));
    }

    @Test
    void abrirSessaoDuasVezesLancaExcecao() {
        Pauta pauta = pautaService.criarPauta("Pauta C", "desc");
        pautaService.abrirSessao(pauta.getId(), null);

        assertThatThrownBy(() -> pautaService.abrirSessao(pauta.getId(), null))
            .isInstanceOf(SessaoJaAbertaException.class);
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests PautaServiceTest -Dspring.profiles.active=local`
Expected: FAIL — compile error (`abrirSessao` and `SessaoJaAbertaException` don't exist).

- [ ] **Step 3: Create `SessaoJaAbertaException`**

```java
package com.sicredi.votacao.pauta;

public class SessaoJaAbertaException extends RuntimeException {

    public SessaoJaAbertaException(Long pautaId) {
        super("Sessão já aberta para a pauta: " + pautaId);
    }
}
```

- [ ] **Step 4: Add `abrirSessao` to `Pauta`**

Add to `Pauta.java`, after `sessaoEstaEncerrada()`:

```java
    public void abrirSessao(long duracaoSegundos) {
        this.sessaoAbertaEm = LocalDateTime.now();
        this.sessaoFechaEm = this.sessaoAbertaEm.plusSeconds(duracaoSegundos);
    }
```

- [ ] **Step 5: Add `abrirSessao` to `PautaService`**

```java
    private static final long DURACAO_PADRAO_SEGUNDOS = 60L;

    public Pauta abrirSessao(Long pautaId, Long duracaoSegundos) {
        Pauta pauta = buscarPorId(pautaId);
        if (pauta.sessaoFoiAberta()) {
            throw new SessaoJaAbertaException(pautaId);
        }
        long duracao = duracaoSegundos != null ? duracaoSegundos : DURACAO_PADRAO_SEGUNDOS;
        pauta.abrirSessao(duracao);
        return pautaRepository.save(pauta);
    }
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew test --tests PautaServiceTest -Dspring.profiles.active=local`
Expected: PASS (all 5 tests in the class)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/sicredi/votacao/pauta src/test/java/com/sicredi/votacao/pauta
git commit -m "feat: add abrir sessao de votacao with default duration"
```

---

### Task 5: Voto entity, repository, and DB-level duplicate protection

**Files:**
- Create: `src/main/java/com/sicredi/votacao/models/OpcaoVoto.java`
- Create: `src/main/java/com/sicredi/votacao/models/Voto.java`
- Create: `src/main/java/com/sicredi/votacao/repositories/VotoRepository.java`
- Create: `src/main/java/com/sicredi/votacao/dtos/ContagemVoto.java`
- Test: `src/test/java/com/sicredi/votacao/repositories/VotoRepositoryTest.java`

**Interfaces:**
- Consumes: `Pauta`/`PautaRepository` from Task 3 (to create a parent pauta id in tests).
- Produces: `VotoRepository.existsByPautaIdAndCpfAssociado(Long, String)`, `VotoRepository.contarPorPauta(Long)` returning `List<ContagemVoto>` where `ContagemVoto.getOpcao()` is `OpcaoVoto` and `getTotal()` is `Long`.

- [ ] **Step 1: Create `OpcaoVoto`**

```java
package com.sicredi.votacao.models;

public enum OpcaoVoto {
    SIM,
    NAO
}
```

- [ ] **Step 2: Create `Voto` entity**

```java
package com.sicredi.votacao.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "voto", uniqueConstraints = @UniqueConstraint(name = "uk_voto_pauta_cpf", columnNames = {"pauta_id", "cpf_associado"}))
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pauta_id", nullable = false)
    private Long pautaId;

    @Column(name = "cpf_associado", nullable = false, length = 11)
    private String cpfAssociado;

    @Enumerated(EnumType.STRING)
    @Column(name = "voto", nullable = false, length = 3)
    private OpcaoVoto voto;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Voto() {
    }

    public Voto(Long pautaId, String cpfAssociado, OpcaoVoto voto) {
        this.pautaId = pautaId;
        this.cpfAssociado = cpfAssociado;
        this.voto = voto;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPautaId() {
        return pautaId;
    }

    public String getCpfAssociado() {
        return cpfAssociado;
    }

    public OpcaoVoto getVoto() {
        return voto;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
```

- [ ] **Step 3: Create the `ContagemVoto` projection**

```java
package com.sicredi.votacao.repositories;

public interface ContagemVoto {

    OpcaoVoto getOpcao();

    Long getTotal();
}
```

- [ ] **Step 4: Create `VotoRepository`**

```java
package com.sicredi.votacao.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPautaIdAndCpfAssociado(Long pautaId, String cpfAssociado);

    @Query("select v.voto as opcao, count(v) as total from Voto v where v.pautaId = :pautaId group by v.voto")
    List<ContagemVoto> contarPorPauta(@Param("pautaId") Long pautaId);
}
```

- [ ] **Step 5: Write the failing test for the unique constraint and aggregate query**

```java
package com.sicredi.votacao.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class VotoRepositoryTest {

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void votoDuplicadoParaMesmaPautaECpfViolaConstraintUnica() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta X", "desc"));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "11122233344", OpcaoVoto.SIM));

        assertThatThrownBy(() ->
            votoRepository.saveAndFlush(new Voto(pauta.getId(), "11122233344", OpcaoVoto.NAO)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void contarPorPautaAgregaPorOpcao() {
        Pauta pauta = pautaRepository.save(new Pauta("Pauta Y", "desc"));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "11111111111", OpcaoVoto.SIM));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "22222222222", OpcaoVoto.SIM));
        votoRepository.saveAndFlush(new Voto(pauta.getId(), "33333333333", OpcaoVoto.NAO));

        var contagem = votoRepository.contarPorPauta(pauta.getId());

        assertThat(contagem).hasSize(2);
        assertThat(contagem.stream().filter(c -> c.getOpcao() == OpcaoVoto.SIM).findFirst().orElseThrow().getTotal())
            .isEqualTo(2L);
        assertThat(contagem.stream().filter(c -> c.getOpcao() == OpcaoVoto.NAO).findFirst().orElseThrow().getTotal())
            .isEqualTo(1L);
    }
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `./gradlew test --tests VotoRepositoryTest -Dspring.profiles.active=local`
Expected: FAIL — compile error (classes from Steps 1–4 not created yet if done out of order; if created first, this step instead validates schema/constraint wiring, so treat "run once before Step 1-4 exist" as the natural TDD red — in practice implement Steps 1–4 first as scaffolding, then this test is the true red/green pair for the constraint behavior itself. Confirm red by temporarily commenting the `uniqueConstraints` attribute on `Voto`, running the test (fails, no constraint violation thrown), then restoring it.

- [ ] **Step 7: Run test with the constraint in place to verify it passes**

Run: `./gradlew test --tests VotoRepositoryTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/sicredi/votacao/models src/main/java/com/sicredi/votacao/repositories src/main/java/com/sicredi/votacao/dtos src/test/java/com/sicredi/votacao/repositories
git commit -m "feat: add Voto entity with unique constraint and aggregate count query"
```

---

### Task 6: Tela envelope DTOs (Anexo 1)

**Files:**
- Create: `src/main/java/com/sicredi/votacao/dtos/ItemFormulario.java`
- Create: `src/main/java/com/sicredi/votacao/dtos/Botao.java`
- Create: `src/main/java/com/sicredi/votacao/dtos/TelaFormulario.java`
- Create: `src/main/java/com/sicredi/votacao/dtos/ItemSelecao.java`
- Create: `src/main/java/com/sicredi/votacao/dtos/TelaSelecao.java`
- Test: `src/test/java/com/sicredi/votacao/dtos/TelaSerializationTest.java`

**Interfaces:**
- Produces: `TelaFormulario`, `TelaSelecao` records serializing to the exact JSON shape from Anexo 1, reused by every controller in Tasks 11–12.

- [ ] **Step 1: Write the failing serialization test**

```java
package com.sicredi.votacao.dtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TelaSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void formularioSerializaSemCamposNulos() throws Exception {
        TelaFormulario tela = new TelaFormulario(
            "TITULO TELA",
            List.of(
                ItemFormulario.texto("Lorem ipsum"),
                ItemFormulario.inputTexto("titulo", "Título", "")
            ),
            new Botao("Ação 1", "http://seudominio.com/ACAO1", Map.of()),
            null
        );

        String json = objectMapper.writeValueAsString(tela);

        assertThat(json).contains("\"tipo\":\"FORMULARIO\"");
        assertThat(json).doesNotContain("botaoCancelar");
        assertThat(json).contains("\"tipo\":\"INPUT_TEXTO\"");
    }

    @Test
    void selecaoSerializaListaDeItens() throws Exception {
        TelaSelecao tela = new TelaSelecao(
            "Lista de seleção",
            List.of(new ItemSelecao("Opção 1", "http://seudominio.com/OPT1", Map.of("dadosOpcao", "campo de teste")))
        );

        String json = objectMapper.writeValueAsString(tela);

        assertThat(json).contains("\"tipo\":\"SELECAO\"");
        assertThat(json).contains("\"texto\":\"Opção 1\"");
    }
}
```

Note: this uses plain `assertThat(json).contains(...)` (no `assertj-json` needed) — remove the unused `assertThatJson` import before running.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests TelaSerializationTest`
Expected: FAIL — compile error, none of the tela classes exist yet.

- [ ] **Step 3: Create `ItemFormulario`**

```java
package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemFormulario(String tipo, String id, String titulo, String texto, Object valor) {

    public static ItemFormulario texto(String texto) {
        return new ItemFormulario("TEXTO", null, null, texto, null);
    }

    public static ItemFormulario inputTexto(String id, String titulo, Object valor) {
        return new ItemFormulario("INPUT_TEXTO", id, titulo, null, valor);
    }

    public static ItemFormulario inputNumero(String id, String titulo, Object valor) {
        return new ItemFormulario("INPUT_NUMERO", id, titulo, null, valor);
    }
}
```

- [ ] **Step 4: Create `Botao`**

```java
package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Botao(String texto, String url, Map<String, Object> body) {
}
```

- [ ] **Step 5: Create `TelaFormulario`**

```java
package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelaFormulario(String tipo, String titulo, List<ItemFormulario> itens, Botao botaoOk, Botao botaoCancelar) {

    public TelaFormulario(String titulo, List<ItemFormulario> itens, Botao botaoOk, Botao botaoCancelar) {
        this("FORMULARIO", titulo, itens, botaoOk, botaoCancelar);
    }
}
```

- [ ] **Step 6: Create `ItemSelecao`**

```java
package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemSelecao(String texto, String url, Map<String, Object> body) {

    public ItemSelecao(String texto, String url) {
        this(texto, url, null);
    }
}
```

- [ ] **Step 7: Create `TelaSelecao`**

```java
package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelaSelecao(String tipo, String titulo, List<ItemSelecao> itens) {

    public TelaSelecao(String titulo, List<ItemSelecao> itens) {
        this("SELECAO", titulo, itens);
    }
}
```

- [ ] **Step 8: Fix the test's unused import and run to verify it passes**

Remove `import static org.assertj.core.api.Assertions.assertThatJson;` from the test (not used).

Run: `./gradlew test --tests TelaSerializationTest`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/sicredi/votacao/dtos src/test/java/com/sicredi/votacao/dtos
git commit -m "feat: add Anexo 1 screen envelope DTOs"
```

---

### Task 7: VotoService business rules (with a fake CPF client)

**Files:**
- Create: `src/main/java/com/sicredi/votacao/services/external/UserInfoClient.java`
- Create: `src/main/java/com/sicredi/votacao/services/external/StatusVotacao.java`
- Create: `src/main/java/com/sicredi/votacao/exceptions/SessaoNaoAbertaException.java`
- Create: `src/main/java/com/sicredi/votacao/exceptions/SessaoEncerradaException.java`
- Create: `src/main/java/com/sicredi/votacao/exceptions/VotoDuplicadoException.java`
- Create: `src/main/java/com/sicredi/votacao/exceptions/AssociadoNaoHabilitadoException.java`
- Create: `src/main/java/com/sicredi/votacao/services/VotoService.java`
- Test: `src/test/java/com/sicredi/votacao/services/VotoServiceTest.java`

**Interfaces:**
- Consumes: `Pauta`/`PautaService.buscarPorId` (Task 3/4), `Voto`/`VotoRepository` (Task 5), `UserInfoClient.consultar(String cpf)` returning `StatusVotacao` (interface only here — real implementation is Task 8).
- Produces: `VotoService.registrarVoto(Long pautaId, String cpfAssociado, OpcaoVoto voto)` returns `Voto`; throws `SessaoNaoAbertaException`, `SessaoEncerradaException`, `VotoDuplicadoException`, `AssociadoNaoHabilitadoException`.

- [ ] **Step 1: Create `StatusVotacao`**

```java
package com.sicredi.votacao.dtos;

public enum StatusVotacao {
    ABLE_TO_VOTE,
    UNABLE_TO_VOTE
}
```

- [ ] **Step 2: Create the `UserInfoClient` interface**

```java
package com.sicredi.votacao.services.external;

import com.sicredi.votacao.dtos.StatusVotacao;

public interface UserInfoClient {

    StatusVotacao consultar(String cpf);
}
```

- [ ] **Step 3: Create the four voto exceptions**

```java
package com.sicredi.votacao.exceptions;

public class SessaoNaoAbertaException extends RuntimeException {

    public SessaoNaoAbertaException(Long pautaId) {
        super("Sessão de votação ainda não foi aberta para a pauta: " + pautaId);
    }
}
```

```java
package com.sicredi.votacao.exceptions;

public class SessaoEncerradaException extends RuntimeException {

    public SessaoEncerradaException(Long pautaId) {
        super("Sessão de votação já encerrada para a pauta: " + pautaId);
    }
}
```

```java
package com.sicredi.votacao.exceptions;

public class VotoDuplicadoException extends RuntimeException {

    public VotoDuplicadoException(Long pautaId, String cpf) {
        super("Associado " + cpf + " já votou na pauta " + pautaId);
    }
}
```

```java
package com.sicredi.votacao.exceptions;

public class AssociadoNaoHabilitadoException extends RuntimeException {

    public AssociadoNaoHabilitadoException(String cpf) {
        super("Associado não habilitado a votar: " + cpf);
    }
}
```

- [ ] **Step 4: Write the failing unit tests with a mocked `UserInfoClient`**

```java
package com.sicredi.votacao.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sicredi.votacao.dtos.StatusVotacao;
import com.sicredi.votacao.exceptions.AssociadoNaoHabilitadoException;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import com.sicredi.votacao.models.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.VotoRepository;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private UserInfoClient userInfoClient;

    private VotoService votoService;

    @BeforeEach
    void setUp() {
        votoService = new VotoService(votoRepository, pautaService, userInfoClient);
    }

    @Test
    void registrarVotoSemSessaoAbertaLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(SessaoNaoAbertaException.class);
    }

    @Test
    void registrarVotoComSessaoEncerradaLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(0L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(SessaoEncerradaException.class);
    }

    @Test
    void registrarVotoDuplicadoLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(60L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(votoRepository.existsByPautaIdAndCpfAssociado(1L, "11122233344")).thenReturn(true);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(VotoDuplicadoException.class);
    }

    @Test
    void registrarVotoComAssociadoNaoHabilitadoLancaExcecao() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(60L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(votoRepository.existsByPautaIdAndCpfAssociado(1L, "11122233344")).thenReturn(false);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.UNABLE_TO_VOTE);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM))
            .isInstanceOf(AssociadoNaoHabilitadoException.class);
    }

    @Test
    void registrarVotoValidoPersisteEDevolveVoto() {
        Pauta pauta = new Pauta("Pauta", "desc");
        pauta.abrirSessao(60L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(votoRepository.existsByPautaIdAndCpfAssociado(1L, "11122233344")).thenReturn(false);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.ABLE_TO_VOTE);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Voto voto = votoService.registrarVoto(1L, "11122233344", OpcaoVoto.SIM);

        assertThat(voto.getCpfAssociado()).isEqualTo("11122233344");
        assertThat(voto.getVoto()).isEqualTo(OpcaoVoto.SIM);
    }
}
```

- [ ] **Step 5: Run tests to verify they fail**

Run: `./gradlew test --tests VotoServiceTest`
Expected: FAIL — compile error, `VotoService` does not exist.

- [ ] **Step 6: Create `VotoService`**

```java
package com.sicredi.votacao.services;

import com.sicredi.votacao.dtos.StatusVotacao;
import com.sicredi.votacao.exceptions.AssociadoNaoHabilitadoException;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import com.sicredi.votacao.models.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.repositories.VotoRepository;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.springframework.stereotype.Service;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final PautaService pautaService;
    private final UserInfoClient userInfoClient;

    public VotoService(VotoRepository votoRepository, PautaService pautaService, UserInfoClient userInfoClient) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
        this.userInfoClient = userInfoClient;
    }

    public Voto registrarVoto(Long pautaId, String cpfAssociado, OpcaoVoto voto) {
        Pauta pauta = pautaService.buscarPorId(pautaId);

        if (!pauta.sessaoFoiAberta()) {
            throw new SessaoNaoAbertaException(pautaId);
        }
        if (pauta.sessaoEstaEncerrada()) {
            throw new SessaoEncerradaException(pautaId);
        }
        if (votoRepository.existsByPautaIdAndCpfAssociado(pautaId, cpfAssociado)) {
            throw new VotoDuplicadoException(pautaId, cpfAssociado);
        }
        if (userInfoClient.consultar(cpfAssociado) == StatusVotacao.UNABLE_TO_VOTE) {
            throw new AssociadoNaoHabilitadoException(cpfAssociado);
        }

        return votoRepository.save(new Voto(pautaId, cpfAssociado, voto));
    }
}
```

- [ ] **Step 7: Run tests to verify they pass**

Run: `./gradlew test --tests VotoServiceTest`
Expected: PASS (all 5 tests)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/sicredi/votacao/services src/main/java/com/sicredi/votacao/dtos src/main/java/com/sicredi/votacao/exceptions src/test/java/com/sicredi/votacao/services
git commit -m "feat: add VotoService business rules"
```

---

### Task 8: Real UserInfoClient (WebClient, timeout, retry, 404 mapping)

**Files:**
- Create: `src/main/java/com/sicredi/votacao/exceptions/CpfInvalidoException.java`
- Create: `src/main/java/com/sicredi/votacao/exceptions/IntegracaoExternaIndisponivelException.java`
- Create: `src/main/java/com/sicredi/votacao/services/external/WebClientUserInfoClient.java`
- Test: `src/test/java/com/sicredi/votacao/services/external/WebClientUserInfoClientTest.java`

**Interfaces:**
- Consumes: `UserInfoClient`, `StatusVotacao` from Task 7.
- Produces: `WebClientUserInfoClient implements UserInfoClient`, registered as the `@Service` bean satisfying `VotoService`'s dependency; reads `app.external.user-info-url` and `app.external.timeout-seconds` from config (already present in `application.yml` from Task 1).

- [ ] **Step 1: Create the two exceptions**

```java
package com.sicredi.votacao.services.external;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException(String cpf) {
        super("CPF inválido: " + cpf);
    }
}
```

```java
package com.sicredi.votacao.services.external;

public class IntegracaoExternaIndisponivelException extends RuntimeException {

    public IntegracaoExternaIndisponivelException(String cpf, Throwable cause) {
        super("Não foi possível validar o associado " + cpf + " no momento", cause);
    }
}
```

- [ ] **Step 2: Write the failing test using MockWebServer**

```java
package com.sicredi.votacao.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.Duration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class WebClientUserInfoClientTest {

    private MockWebServer server;
    private WebClientUserInfoClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
        client = new WebClientUserInfoClient(webClient, Duration.ofSeconds(1));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void cpfHabilitadoRetornaAbleToVote() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"ABLE_TO_VOTE\"}")
            .addHeader("Content-Type", "application/json"));

        assertThat(client.consultar("19839091069")).isEqualTo(StatusVotacao.ABLE_TO_VOTE);
    }

    @Test
    void cpfNaoHabilitadoRetornaUnableToVote() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"UNABLE_TO_VOTE\"}")
            .addHeader("Content-Type", "application/json"));

        assertThat(client.consultar("62289608068")).isEqualTo(StatusVotacao.UNABLE_TO_VOTE);
    }

    @Test
    void cpfInexistenteLancaCpfInvalidoException() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> client.consultar("00000000000"))
            .isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void falhaPersistenteLancaIntegracaoExternaIndisponivelException() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        assertThatThrownBy(() -> client.consultar("11111111111"))
            .isInstanceOf(IntegracaoExternaIndisponivelException.class);
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests WebClientUserInfoClientTest`
Expected: FAIL — compile error, `WebClientUserInfoClient` does not exist.

- [ ] **Step 4: Create `WebClientUserInfoClient`**

```java
package com.sicredi.votacao.services.external;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Service
public class WebClientUserInfoClient implements UserInfoClient {

    private final WebClient webClient;
    private final Duration timeout;

    public WebClientUserInfoClient(
        WebClient.Builder webClientBuilder,
        @Value("${app.external.user-info-url}") String baseUrl,
        @Value("${app.external.timeout-seconds}") long timeoutSeconds
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    WebClientUserInfoClient(WebClient webClient, Duration timeout) {
        this.webClient = webClient;
        this.timeout = timeout;
    }

    @Override
    public StatusVotacao consultar(String cpf) {
        try {
            UserInfoResponse response = webClient.get()
                .uri("/users/{cpf}", cpf)
                .retrieve()
                .bodyToMono(UserInfoResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(200))
                    .filter(this::isRetryable))
                .block();
            return StatusVotacao.valueOf(response.status());
        } catch (WebClientResponseException.NotFound ex) {
            throw new CpfInvalidoException(cpf);
        } catch (Exception ex) {
            throw new IntegracaoExternaIndisponivelException(cpf, ex);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        return !(throwable instanceof WebClientResponseException.NotFound);
    }

    private record UserInfoResponse(String status) {
    }
}
```

- [ ] **Step 5: Wire the bean's config in `application.yml`**

Already present from Task 1 (`app.external.user-info-url`, needs `timeout-seconds` — confirm it reads `app.external.timeout-seconds: 3`; if the key was named differently in Task 1, align it now to exactly `app.external.timeout-seconds`).

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew test --tests WebClientUserInfoClientTest`
Expected: PASS (all 4 tests)

- [ ] **Step 7: Run the full `VotoServiceTest` suite again to confirm no regression**

Run: `./gradlew test --tests VotoServiceTest`
Expected: PASS (still uses the mocked `UserInfoClient` interface, unaffected)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/sicredi/votacao/exceptions src/main/java/com/sicredi/votacao/services/external src/main/resources/application.yml src/test/java/com/sicredi/votacao/services/external
git commit -m "feat: add WebClient-based CPF eligibility integration with retry and timeout"
```

---

### Task 9: Apuração de resultado

**Files:**
- Create: `src/main/java/com/sicredi/votacao/dtos/ResultadoPauta.java`
- Modify: `src/main/java/com/sicredi/votacao/services/PautaService.java`
- Test: `src/test/java/com/sicredi/votacao/services/PautaServiceTest.java`

**Interfaces:**
- Consumes: `VotoRepository.contarPorPauta` (Task 5), `PautaService.buscarPorId` (Task 3).
- Produces: `PautaService.apurarResultado(Long pautaId)` returns `ResultadoPauta(long votosSim, long votosNao, String resultado)` where `resultado` is `"APROVADA"`, `"REPROVADA"`, or `"EMPATE"`.

- [ ] **Step 1: Create `ResultadoPauta`**

```java
package com.sicredi.votacao.dtos;

public record ResultadoPauta(long votosSim, long votosNao, String resultado) {

    public static ResultadoPauta calcular(long votosSim, long votosNao) {
        String resultado;
        if (votosSim > votosNao) {
            resultado = "APROVADA";
        } else if (votosNao > votosSim) {
            resultado = "REPROVADA";
        } else {
            resultado = "EMPATE";
        }
        return new ResultadoPauta(votosSim, votosNao, resultado);
    }
}
```

- [ ] **Step 2: Write the failing test**

Append to `PautaServiceTest` (add `VotoRepository` and a helper to insert votes — inject `VotoRepository` via `@Autowired` since this is a `@SpringBootTest`):

```java
    @Autowired
    private com.sicredi.votacao.repositories.VotoRepository votoRepository;

    @Test
    void apurarResultadoContabilizaVotosEDefineAprovada() {
        Pauta pauta = pautaService.criarPauta("Pauta Resultado", "desc");
        votoRepository.save(new com.sicredi.votacao.models.Voto(pauta.getId(), "11111111111", com.sicredi.votacao.models.OpcaoVoto.SIM));
        votoRepository.save(new com.sicredi.votacao.models.Voto(pauta.getId(), "22222222222", com.sicredi.votacao.models.OpcaoVoto.SIM));
        votoRepository.save(new com.sicredi.votacao.models.Voto(pauta.getId(), "33333333333", com.sicredi.votacao.models.OpcaoVoto.NAO));

        ResultadoPauta resultado = pautaService.apurarResultado(pauta.getId());

        assertThat(resultado.votosSim()).isEqualTo(2L);
        assertThat(resultado.votosNao()).isEqualTo(1L);
        assertThat(resultado.resultado()).isEqualTo("APROVADA");
    }

    @Test
    void apurarResultadoSemVotosDaEmpateZeroAZero() {
        Pauta pauta = pautaService.criarPauta("Pauta Vazia", "desc");

        ResultadoPauta resultado = pautaService.apurarResultado(pauta.getId());

        assertThat(resultado.votosSim()).isZero();
        assertThat(resultado.votosNao()).isZero();
        assertThat(resultado.resultado()).isEqualTo("EMPATE");
    }
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests PautaServiceTest -Dspring.profiles.active=local`
Expected: FAIL — compile error, `apurarResultado` does not exist on `PautaService`.

- [ ] **Step 4: Add `apurarResultado` to `PautaService`**

Modify the constructor and add the method:

```java
    private final VotoRepository votoRepository;

    public PautaService(PautaRepository pautaRepository, VotoRepository votoRepository) {
        this.pautaRepository = pautaRepository;
        this.votoRepository = votoRepository;
    }
```

(Add the import `com.sicredi.votacao.repositories.VotoRepository`, `com.sicredi.votacao.models.OpcaoVoto`, `com.sicredi.votacao.dtos.ContagemVoto`.)

```java
    public ResultadoPauta apurarResultado(Long pautaId) {
        buscarPorId(pautaId);
        long votosSim = 0L;
        long votosNao = 0L;
        for (ContagemVoto contagem : votoRepository.contarPorPauta(pautaId)) {
            if (contagem.getOpcao() == OpcaoVoto.SIM) {
                votosSim = contagem.getTotal();
            } else {
                votosNao = contagem.getTotal();
            }
        }
        return ResultadoPauta.calcular(votosSim, votosNao);
    }
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew test --tests PautaServiceTest -Dspring.profiles.active=local`
Expected: PASS (all tests in the class)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/sicredi/votacao/dtos src/main/java/com/sicredi/votacao/services src/test/java/com/sicredi/votacao/services
git commit -m "feat: add resultado apuration via aggregate count query"
```

---

### Task 10: GlobalExceptionHandler

**Files:**
- Create: `src/main/java/com/sicredi/votacao/exceptions/ErrorResponse.java`
- Create: `src/main/java/com/sicredi/votacao/exceptions/GlobalExceptionHandler.java`
- Test: `src/test/java/com/sicredi/votacao/exceptions/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: every domain exception created in Tasks 3, 4, 7, 8.
- Produces: `@RestControllerAdvice` mapping each exception to its HTTP status; used implicitly by all controllers from Task 11 onward.

- [ ] **Step 1: Create `ErrorResponse`**

```java
package com.sicredi.votacao.exceptions;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message);
    }
}
```

- [ ] **Step 2: Write the failing test**

```java
package com.sicredi.votacao.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.exceptions.AssociadoNaoHabilitadoException;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import com.sicredi.votacao.exceptions.CpfInvalidoException;
import com.sicredi.votacao.exceptions.IntegracaoExternaIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapeiaTodasAsExcecoesDeDominio() {
        assertStatus(handler.handlePautaNaoEncontrada(new PautaNaoEncontradaException(1L)), HttpStatus.NOT_FOUND);
        assertStatus(handler.handleSessaoJaAberta(new SessaoJaAbertaException(1L)), HttpStatus.CONFLICT);
        assertStatus(handler.handleSessaoNaoAberta(new SessaoNaoAbertaException(1L)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleSessaoEncerrada(new SessaoEncerradaException(1L)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleVotoDuplicado(new VotoDuplicadoException(1L, "111")), HttpStatus.CONFLICT);
        assertStatus(handler.handleAssociadoNaoHabilitado(new AssociadoNaoHabilitadoException("111")), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleCpfInvalido(new CpfInvalidoException("111")), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleIntegracaoIndisponivel(new IntegracaoExternaIndisponivelException("111", new RuntimeException())), HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void assertStatus(ResponseEntity<ErrorResponse> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        assertThat(response.getBody()).isNotNull();
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests GlobalExceptionHandlerTest`
Expected: FAIL — compile error, `GlobalExceptionHandler` does not exist.

- [ ] **Step 4: Create `GlobalExceptionHandler`**

```java
package com.sicredi.votacao.exceptions;

import com.sicredi.votacao.exceptions.CpfInvalidoException;
import com.sicredi.votacao.exceptions.IntegracaoExternaIndisponivelException;
import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.exceptions.SessaoJaAbertaException;
import com.sicredi.votacao.exceptions.AssociadoNaoHabilitadoException;
import com.sicredi.votacao.exceptions.SessaoEncerradaException;
import com.sicredi.votacao.exceptions.SessaoNaoAbertaException;
import com.sicredi.votacao.exceptions.VotoDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PautaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handlePautaNaoEncontrada(PautaNaoEncontradaException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessaoJaAbertaException.class)
    public ResponseEntity<ErrorResponse> handleSessaoJaAberta(SessaoJaAbertaException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SessaoNaoAbertaException.class)
    public ResponseEntity<ErrorResponse> handleSessaoNaoAberta(SessaoNaoAbertaException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(SessaoEncerradaException.class)
    public ResponseEntity<ErrorResponse> handleSessaoEncerrada(SessaoEncerradaException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(VotoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleVotoDuplicado(VotoDuplicadoException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AssociadoNaoHabilitadoException.class)
    public ResponseEntity<ErrorResponse> handleAssociadoNaoHabilitado(AssociadoNaoHabilitadoException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCpfInvalido(CpfInvalidoException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IntegracaoExternaIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleIntegracaoIndisponivel(IntegracaoExternaIndisponivelException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
            .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew test --tests GlobalExceptionHandlerTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/sicredi/votacao/exceptions src/test/java/com/sicredi/votacao/exceptions
git commit -m "feat: add global exception handler mapping domain errors to HTTP status"
```

---

### Task 11: Real action controllers (Pauta, Sessão, Voto, Resultado)

**Files:**
- Create: `src/main/java/com/sicredi/votacao/controllers/request/CriarPautaRequest.java`
- Create: `src/main/java/com/sicredi/votacao/controllers/request/AbrirSessaoRequest.java`
- Create: `src/main/java/com/sicredi/votacao/mappers/PautaTelaMapper.java`
- Create: `src/main/java/com/sicredi/votacao/controllers/PautaController.java`
- Create: `src/main/java/com/sicredi/votacao/controllers/request/RegistrarVotoRequest.java`
- Create: `src/main/java/com/sicredi/votacao/controllers/VotoController.java`
- Test: `src/test/java/com/sicredi/votacao/controllers/PautaControllerTest.java`
- Test: `src/test/java/com/sicredi/votacao/controllers/VotoControllerTest.java`

**Interfaces:**
- Consumes: `PautaService` (Tasks 3/4/9), `VotoService` (Task 7), `TelaFormulario`/`ItemFormulario`/`Botao` (Task 6).
- Produces: `PautaTelaMapper.detalhe(Pauta)` returning `TelaFormulario` (state-dependent `botaoOk`, reused by Task 12's navigation controllers); real endpoints `POST /api/v1/pautas`, `POST /api/v1/pautas/{id}/sessoes`, `POST /api/v1/pautas/{id}/votos`, `POST /api/v1/pautas/{id}/resultado`.

- [ ] **Step 1: Create the request DTOs**

```java
package com.sicredi.votacao.controllers.request;

import jakarta.validation.constraints.NotBlank;

public record CriarPautaRequest(@NotBlank String titulo, String descricao) {
}
```

```java
package com.sicredi.votacao.controllers.request;

public record AbrirSessaoRequest(Long duracaoSegundos) {
}
```

```java
package com.sicredi.votacao.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrarVotoRequest(
    @NotBlank @Pattern(regexp = "\\d{11}") String cpfAssociado,
    @NotBlank @Pattern(regexp = "SIM|NAO") String voto
) {
}
```

- [ ] **Step 2: Write the failing controller tests**

```java
package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void criarPautaRetornaTelaFormularioDeDetalhe() throws Exception {
        var request = new CriarPautaRequest("Reforma do estatuto", "desc");

        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("FORMULARIO")))
            .andExpect(jsonPath("$.botaoOk.url", equalTo("/api/v1/pautas/{id}/sessoes/tela".replace("{id}", "1"))
                .toString().isEmpty() ? equalTo("ignore") : equalTo("ignore"));
    }
}
```

Replace the last assertion (it is intentionally awkward above) with a simpler, correct one:

```java
    @Test
    void criarPautaRetornaTelaFormularioDeDetalhe() throws Exception {
        var request = new CriarPautaRequest("Reforma do estatuto", "desc");

        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("FORMULARIO")))
            .andExpect(jsonPath("$.botaoOk.texto", equalTo("Abrir Sessão")));
    }

    @Test
    void abrirSessaoRetornaTelaComBotaoDeVotar() throws Exception {
        String body = mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CriarPautaRequest("Pauta 2", "desc"))))
            .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).at("/botaoOk/body/id").isMissingNode()
            ? extractIdFromBotaoUrl(objectMapper.readTree(body).at("/botaoOk/url").asText())
            : objectMapper.readTree(body).at("/botaoOk/body/id").asLong();

        mockMvc.perform(post("/api/v1/pautas/" + id + "/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(60L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.botaoOk.texto", equalTo("Votar")));
    }

    private Long extractIdFromBotaoUrl(String url) {
        String[] parts = url.split("/");
        return Long.valueOf(parts[parts.length - 2]);
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew test --tests PautaControllerTest -Dspring.profiles.active=local`
Expected: FAIL — compile error, `PautaController`/`PautaTelaMapper` do not exist.

- [ ] **Step 4: Create `PautaTelaMapper`**

```java
package com.sicredi.votacao.mappers;

import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.models.Pauta;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PautaTelaMapper {

    public TelaFormulario detalhe(Pauta pauta) {
        List<ItemFormulario> itens = List.of(
            ItemFormulario.texto("Título: " + pauta.getTitulo()),
            ItemFormulario.texto("Descrição: " + pauta.getDescricao()),
            ItemFormulario.texto("Status: " + status(pauta))
        );
        return new TelaFormulario("Detalhe da Pauta", itens, botaoDeAcao(pauta), null);
    }

    public TelaFormulario resultado(Pauta pauta, ResultadoPauta resultado) {
        List<ItemFormulario> itens = List.of(
            ItemFormulario.texto("Título: " + pauta.getTitulo()),
            ItemFormulario.texto("Sim: " + resultado.votosSim()),
            ItemFormulario.texto("Não: " + resultado.votosNao()),
            ItemFormulario.texto("Resultado: " + resultado.resultado())
        );
        return new TelaFormulario("Resultado da Votação", itens, new Botao("Voltar", "/api/v1/telas/home", Map.of()), null);
    }

    private String status(Pauta pauta) {
        if (!pauta.sessaoFoiAberta()) {
            return "Sessão não aberta";
        }
        return pauta.sessaoEstaAberta() ? "Sessão aberta" : "Sessão encerrada";
    }

    private Botao botaoDeAcao(Pauta pauta) {
        if (!pauta.sessaoFoiAberta()) {
            return new Botao("Abrir Sessão", "/api/v1/pautas/" + pauta.getId() + "/sessoes/tela", Map.of());
        }
        if (pauta.sessaoEstaAberta()) {
            return new Botao("Votar", "/api/v1/pautas/" + pauta.getId() + "/votos/tela", Map.of());
        }
        return new Botao("Ver Resultado", "/api/v1/pautas/" + pauta.getId() + "/resultado", Map.of());
    }
}
```

- [ ] **Step 5: Create `PautaController`**

```java
package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.dtos.TelaFormulario;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;
    private final PautaTelaMapper pautaTelaMapper;

    public PautaController(PautaService pautaService, PautaTelaMapper pautaTelaMapper) {
        this.pautaService = pautaService;
        this.pautaTelaMapper = pautaTelaMapper;
    }

    @PostMapping
    public TelaFormulario criar(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criarPauta(request.titulo(), request.descricao());
        return pautaTelaMapper.detalhe(pauta);
    }

    @PostMapping("/{id}")
    public TelaFormulario detalhe(@PathVariable Long id) {
        return pautaTelaMapper.detalhe(pautaService.buscarPorId(id));
    }

    @PostMapping("/{id}/sessoes")
    public TelaFormulario abrirSessao(@PathVariable Long id, @RequestBody(required = false) AbrirSessaoRequest request) {
        Long duracao = request != null ? request.duracaoSegundos() : null;
        Pauta pauta = pautaService.abrirSessao(id, duracao);
        return pautaTelaMapper.detalhe(pauta);
    }

    @PostMapping("/{id}/resultado")
    public TelaFormulario resultado(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        ResultadoPauta resultado = pautaService.apurarResultado(id);
        return pautaTelaMapper.resultado(pauta, resultado);
    }
}
```

- [ ] **Step 6: Create `VotoController`**

```java
package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.TelaFormulario;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    public TelaFormulario registrar(@PathVariable Long pautaId, @Valid @RequestBody RegistrarVotoRequest request) {
        votoService.registrarVoto(pautaId, request.cpfAssociado(), OpcaoVoto.valueOf(request.voto()));
        List<ItemFormulario> itens = List.of(ItemFormulario.texto("Voto registrado com sucesso"));
        return new TelaFormulario("Confirmação", itens, new Botao("Voltar", "/api/v1/telas/home", Map.of()), null);
    }
}
```

- [ ] **Step 7: Fix the test's malformed first assertion**

Replace the entire `PautaControllerTest` file with the corrected version from Step 2 (the version after "Replace the last assertion... with a simpler, correct one") — the file must not contain the awkward first draft.

- [ ] **Step 8: Run tests to verify they pass**

Run: `./gradlew test --tests PautaControllerTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 9: Write and run `VotoControllerTest`**

```java
package com.sicredi.votacao.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.services.PautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.sicredi.votacao.services.external.StatusVotacao;
import com.sicredi.votacao.services.external.UserInfoClient;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaService pautaService;

    @MockBean
    private UserInfoClient userInfoClient;

    @Test
    void registrarVotoValidoRetornaConfirmacao() throws Exception {
        var pauta = pautaService.criarPauta("Pauta Voto", "desc");
        pautaService.abrirSessao(pauta.getId(), 60L);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.ABLE_TO_VOTE);

        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/votos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegistrarVotoRequest("11122233344", "SIM"))))
            .andExpect(status().isOk());
    }
}
```

Run: `./gradlew test --tests VotoControllerTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/sicredi/votacao/controllers src/main/java/com/sicredi/votacao/controllers/request src/main/java/com/sicredi/votacao/mappers src/test/java/com/sicredi/votacao/controllers
git commit -m "feat: add real action controllers returning Anexo 1 screen envelopes"
```

---

### Task 12: Navigation screen controllers (Tela)

**Files:**
- Create: `src/main/java/com/sicredi/votacao/controllers/TelaController.java`
- Create: `src/main/java/com/sicredi/votacao/controllers/request/CpfFormularioRequest.java` (see Step 1 note)
- Create: `src/main/java/com/sicredi/votacao/controllers/VotoTelaController.java`
- Test: `src/test/java/com/sicredi/votacao/controllers/TelaControllerTest.java`
- Test: `src/test/java/com/sicredi/votacao/controllers/VotoTelaControllerTest.java`

**Interfaces:**
- Consumes: `PautaService` (Task 3/4), `PautaTelaMapper` (Task 11), `TelaFormulario`/`TelaSelecao`/`ItemSelecao`/`Botao` (Task 6).
- Produces: `GET /api/v1/telas/home`, `POST /api/v1/telas/pautas/novo`, `POST /api/v1/telas/pautas`, `POST /api/v1/pautas/{id}/sessoes/tela`, `POST /api/v1/pautas/{id}/votos/tela`, `POST /api/v1/pautas/{id}/votos/opcoes`.

- [ ] **Step 1: Discard the misnamed file from the header** — do not create `CpfFormularioRequest` outside `controllers/request`; the correct file is `src/main/java/com/sicredi/votacao/controllers/request/CpfFormularioRequest.java` (corrected below).

- [ ] **Step 2: Write the failing test for `TelaController`**

```java
package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TelaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homeRetornaSelecaoComDuasOpcoes() throws Exception {
        mockMvc.perform(get("/api/v1/telas/home"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("SELECAO")))
            .andExpect(jsonPath("$.itens.length()", equalTo(2)));
    }

    @Test
    void novaPautaRetornaFormularioVazio() throws Exception {
        mockMvc.perform(post("/api/v1/telas/pautas/novo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("FORMULARIO")))
            .andExpect(jsonPath("$.botaoOk.url", equalTo("/api/v1/pautas")));
    }

    @Test
    void listarPautasRetornaSelecaoComItens() throws Exception {
        mockMvc.perform(post("/api/v1/telas/pautas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("SELECAO")));
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew test --tests TelaControllerTest -Dspring.profiles.active=local`
Expected: FAIL — compile error, `TelaController` does not exist.

- [ ] **Step 4: Create `TelaController`**

```java
package com.sicredi.votacao.controllers;

import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.ItemSelecao;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.dtos.TelaSelecao;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telas")
public class TelaController {

    private final PautaService pautaService;

    public TelaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @GetMapping("/home")
    public TelaSelecao home() {
        List<ItemSelecao> itens = List.of(
            new ItemSelecao("Cadastrar Pauta", "/api/v1/telas/pautas/novo"),
            new ItemSelecao("Listar Pautas", "/api/v1/telas/pautas")
        );
        return new TelaSelecao("Menu", itens);
    }

    @PostMapping("/pautas/novo")
    public TelaFormulario novaPauta() {
        List<ItemFormulario> itens = List.of(
            ItemFormulario.inputTexto("titulo", "Título", ""),
            ItemFormulario.inputTexto("descricao", "Descrição", "")
        );
        return new TelaFormulario("Nova Pauta", itens, new Botao("Cadastrar", "/api/v1/pautas", Map.of()), null);
    }

    @PostMapping("/pautas")
    public TelaSelecao listarPautas() {
        List<ItemSelecao> itens = pautaService.listarTodas().stream()
            .map(this::paraItem)
            .collect(Collectors.toList());
        return new TelaSelecao("Lista de Pautas", itens);
    }

    private ItemSelecao paraItem(Pauta pauta) {
        return new ItemSelecao(pauta.getTitulo(), "/api/v1/pautas/" + pauta.getId());
    }
}
```

- [ ] **Step 5: Add `listarTodas` to `PautaService`**

```java
    public java.util.List<Pauta> listarTodas() {
        return pautaRepository.findAll();
    }
```

- [ ] **Step 6: Run `TelaControllerTest` to verify it passes**

Run: `./gradlew test --tests TelaControllerTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 7: Write the failing test for the voto navigation screens**

```java
package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.CpfFormularioRequest;
import com.sicredi.votacao.services.PautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class VotoTelaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaService pautaService;

    @Test
    void telaDeVotoPedeCpf() throws Exception {
        var pauta = pautaService.criarPauta("Pauta Tela Voto", "desc");

        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/votos/tela"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("FORMULARIO")))
            .andExpect(jsonPath("$.botaoOk.url", equalTo("/api/v1/pautas/" + pauta.getId() + "/votos/opcoes")));
    }

    @Test
    void opcoesDeVotoRetornaSelecaoSimNaoComCpfNoBody() throws Exception {
        var pauta = pautaService.criarPauta("Pauta Opcoes", "desc");

        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/votos/opcoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CpfFormularioRequest("11122233344"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("SELECAO")))
            .andExpect(jsonPath("$.itens.length()", equalTo(2)))
            .andExpect(jsonPath("$.itens[0].body.cpfAssociado", equalTo("11122233344")));
    }
}
```

- [ ] **Step 8: Run test to verify it fails**

Run: `./gradlew test --tests VotoTelaControllerTest -Dspring.profiles.active=local`
Expected: FAIL — compile error, `VotoTelaController`/`CpfFormularioRequest` do not exist. Also update `PautaTelaMapper`'s `/sessoes/tela` path was already covered in Task 11; this task adds the analogous `/votos/tela` screen.

- [ ] **Step 9: Create `CpfFormularioRequest`**

```java
package com.sicredi.votacao.controllers.request;

public record CpfFormularioRequest(String cpfAssociado) {
}
```

- [ ] **Step 10: Create `VotoTelaController`**

```java
package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.CpfFormularioRequest;
import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.ItemSelecao;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.dtos.TelaSelecao;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoTelaController {

    @PostMapping("/tela")
    public TelaFormulario telaVoto(@PathVariable Long pautaId) {
        List<ItemFormulario> itens = List.of(ItemFormulario.inputTexto("cpfAssociado", "CPF do associado", ""));
        Botao botaoOk = new Botao("Continuar", "/api/v1/pautas/" + pautaId + "/votos/opcoes", Map.of());
        return new TelaFormulario("Votar", itens, botaoOk, null);
    }

    @PostMapping("/opcoes")
    public TelaSelecao opcoes(@PathVariable Long pautaId, @RequestBody CpfFormularioRequest request) {
        String url = "/api/v1/pautas/" + pautaId + "/votos";
        List<ItemSelecao> itens = List.of(
            new ItemSelecao("Sim", url, Map.of("cpfAssociado", request.cpfAssociado(), "voto", "SIM")),
            new ItemSelecao("Não", url, Map.of("cpfAssociado", request.cpfAssociado(), "voto", "NAO"))
        );
        return new TelaSelecao("Confirme seu voto", itens);
    }
}
```

- [ ] **Step 11: Run tests to verify they pass**

Run: `./gradlew test --tests VotoTelaControllerTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 12: Run the full test suite to confirm no regressions**

Run: `./gradlew test -Dspring.profiles.active=local`
Expected: PASS (all tests across all packages)

- [ ] **Step 13: Commit**

```bash
git add src/main/java/com/sicredi/votacao/controllers src/main/java/com/sicredi/votacao/controllers/request src/main/java/com/sicredi/votacao/services src/test/java/com/sicredi/votacao/controllers
git commit -m "feat: add screen navigation controllers completing the Anexo 1 flow"
```

---

### Task 13: OpenAPI / Swagger UI

**Files:**
- Create: `src/main/java/com/sicredi/votacao/config/OpenApiConfig.java`
- Test: `src/test/java/com/sicredi/votacao/config/OpenApiConfigTest.java`

**Interfaces:**
- Consumes: nothing new (springdoc-openapi dependency already in `build.gradle.kts` from Task 1).
- Produces: `/v3/api-docs` and `/swagger-ui/index.html` reachable, with API title/description/version metadata.

- [ ] **Step 1: Write the failing test**

```java
package com.sicredi.votacao.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OpenApiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsEstaDisponivel() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run test to verify current behavior**

Run: `./gradlew test --tests OpenApiConfigTest -Dspring.profiles.active=local`
Expected: PASS already (springdoc auto-configures `/v3/api-docs` out of the box) — this step confirms the dependency is wired; if it fails, re-check the `springdoc-openapi-starter-webmvc-ui` dependency from Task 1.

- [ ] **Step 3: Create `OpenApiConfig` to customize metadata**

```java
package com.sicredi.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votacaoOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Cooperative Voting API")
            .description("API para cadastro de pautas, sessões de votação e apuração de resultados, incluindo o protocolo de telas server-driven consumido pelo app mobile.")
            .version("v1"));
    }
}
```

- [ ] **Step 4: Run test again to confirm it still passes**

Run: `./gradlew test --tests OpenApiConfigTest -Dspring.profiles.active=local`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/sicredi/votacao/config src/test/java/com/sicredi/votacao/config
git commit -m "feat: customize OpenAPI metadata for Swagger UI"
```

---

### Task 14: Dockerfile and docker-compose

**Files:**
- Create: `Dockerfile`
- Create: `docker-compose.yml`
- Create: `.dockerignore`

**Interfaces:**
- Produces: `docker-compose up --build` starting `postgres` (with a named volume for persistence across restarts) and `app` (built from source), app reachable on `http://localhost:8080`, Swagger UI on `http://localhost:8080/swagger-ui/index.html`.

- [ ] **Step 1: Create `.dockerignore`**

```
build/
.gradle/
.idea/
data/
*.md
```

- [ ] **Step 2: Create the multi-stage `Dockerfile`**

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN ./gradlew --version
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: Create `docker-compose.yml`**

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: votacao
      POSTGRES_USER: votacao
      POSTGRES_PASSWORD: votacao
    ports:
      - "5432:5432"
    volumes:
      - votacao-db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U votacao"]
      interval: 5s
      timeout: 5s
      retries: 10

  app:
    build: .
    environment:
      DB_HOST: postgres
      DB_PORT: "5432"
      DB_NAME: votacao
      DB_USER: votacao
      DB_PASSWORD: votacao
      APP_EXTERNAL_USER_INFO_URL: https://user-info.herokuapp.com
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  votacao-db-data:
```

- [ ] **Step 4: Add the Gradle wrapper (required by the Dockerfile)**

Run: `gradle wrapper --gradle-version 8.10` (or, if no local Gradle is installed, use the project's IDE-generated wrapper — either way, confirm `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` exist and are committed).

- [ ] **Step 5: Build and start the stack**

Run: `docker-compose up --build -d`
Expected: both containers report healthy/running; `docker-compose logs app` shows `Started VotacaoApplication`.

- [ ] **Step 6: Verify persistence across restart**

Run:
```bash
curl -X POST http://localhost:8080/api/v1/pautas -H "Content-Type: application/json" -d '{"titulo":"Persistencia","descricao":"teste"}'
docker-compose restart app
curl -X POST http://localhost:8080/api/v1/telas/pautas
```
Expected: the pauta created before the restart appears in the list returned after the restart.

- [ ] **Step 7: Stop the stack**

Run: `docker-compose down`

- [ ] **Step 8: Commit**

```bash
git add Dockerfile docker-compose.yml .dockerignore gradlew gradlew.bat gradle
git commit -m "feat: add Dockerfile and docker-compose for one-command execution"
```

---

### Task 15: k6 load test

**Files:**
- Create: `load-test/votacao.js`
- Create: `load-test/README.md`

**Interfaces:**
- Consumes: the running stack from Task 14 (`docker-compose up`), endpoints from Tasks 11/12.
- Produces: a k6 script simulating concurrent voting on a single pauta, with a documented run command and how to read results.

- [ ] **Step 1: Create `load-test/votacao.js`**

```javascript
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    votacao_concorrente: {
      executor: 'shared-iterations',
      vus: 50,
      iterations: 5000,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PAUTA_ID = __ENV.PAUTA_ID;

export default function () {
  const cpf = String(Math.floor(10000000000 + Math.random() * 89999999999));
  const payload = JSON.stringify({ cpfAssociado: cpf, voto: Math.random() > 0.5 ? 'SIM' : 'NAO' });
  const res = http.post(`${BASE_URL}/api/v1/pautas/${PAUTA_ID}/votos`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, {
    'status is 200 or a mapped business error': (r) => [200, 400, 409, 422, 503].includes(r.status),
  });
}
```

- [ ] **Step 2: Create `load-test/README.md`**

```markdown
# Teste de carga (k6)

Pré-requisitos: stack rodando via `docker-compose up -d`, k6 instalado (`https://k6.io/docs/get-started/installation/`).

1. Crie uma pauta e abra a sessão com duração suficiente para o teste (ex: 300s):
   curl -X POST http://localhost:8080/api/v1/pautas -H "Content-Type: application/json" -d '{"titulo":"Carga","descricao":"teste"}'
   curl -X POST http://localhost:8080/api/v1/pautas/{id}/sessoes -H "Content-Type: application/json" -d '{"duracaoSegundos":300}'
2. Rode o script apontando para o id da pauta criada:
   BASE_URL=http://localhost:8080 PAUTA_ID={id} k6 run load-test/votacao.js
3. Resultado esperado: p(95) de latência abaixo de 500ms e menos de 1% de falhas não mapeadas, mesmo com 50 VUs disparando ~5000 votos (CPFs distintos, sem duplicidade).
```

- [ ] **Step 3: Run the load test against the local stack to validate the thresholds**

Run (after starting the stack and creating a pauta/session per the README):
`BASE_URL=http://localhost:8080 PAUTA_ID=<id> k6 run load-test/votacao.js`
Expected: k6 summary shows `http_req_duration p(95)` under 500ms and `http_req_failed` rate under 1%.

- [ ] **Step 4: Commit**

```bash
git add load-test
git commit -m "test: add k6 load test for concurrent voting"
```

---

### Task 16: README

**Files:**
- Create: `README.md`

**Interfaces:**
- Consumes: everything built in Tasks 1–15 (this task only documents; no code changes).

- [ ] **Step 1: Write `README.md`**

```markdown
# Cooperative Voting API

API REST (Java 21 + Spring Boot) para cadastro de pautas, abertura de sessões de votação e apuração de resultados em uma cooperativa, seguindo o protocolo de telas server-driven do Anexo 1 (FORMULARIO/SELECAO) para consumo por um app mobile.

## Como rodar

### Opção principal: docker-compose (recomendado para avaliação)

    docker-compose up --build

Sobe Postgres + a aplicação. API disponível em `http://localhost:8080`, Swagger UI em `http://localhost:8080/swagger-ui/index.html`. Dados persistem em um volume Docker nomeado entre restarts.

### Alternativa: local sem Docker (H2 em arquivo)

    ./gradlew bootRun --args='--spring.profiles.active=local'

Dados persistem em `./data/votacao.mv.db`.

## Fluxo de telas (Anexo 1)

    GET  /api/v1/telas/home
    POST /api/v1/telas/pautas/novo
    POST /api/v1/pautas
    POST /api/v1/telas/pautas
    POST /api/v1/pautas/{id}
    POST /api/v1/pautas/{id}/sessoes/tela
    POST /api/v1/pautas/{id}/sessoes
    POST /api/v1/pautas/{id}/votos/tela
    POST /api/v1/pautas/{id}/votos/opcoes
    POST /api/v1/pautas/{id}/votos
    POST /api/v1/pautas/{id}/resultado

## Exemplo de fluxo completo via curl

    curl -X POST localhost:8080/api/v1/pautas -H "Content-Type: application/json" \
      -d '{"titulo":"Reforma do estatuto","descricao":"Art. 5"}'

    curl -X POST localhost:8080/api/v1/pautas/1/sessoes -H "Content-Type: application/json" \
      -d '{"duracaoSegundos":60}'

    curl -X POST localhost:8080/api/v1/pautas/1/votos -H "Content-Type: application/json" \
      -d '{"cpfAssociado":"19839091069","voto":"SIM"}'

    curl -X POST localhost:8080/api/v1/pautas/1/resultado

## Bônus implementados

- **Integração CPF**: `WebClientUserInfoClient` consulta `GET {app.external.user-info-url}/users/{cpf}` (URL configurável via env `APP_EXTERNAL_USER_INFO_URL`), timeout de 3s, até 2 retries. CPF inexistente (404) rejeita o voto com 400; `UNABLE_TO_VOTE` rejeita com 422; falha persistente do serviço externo rejeita com 503 (fail-closed — nunca aceita um voto sem confirmar habilitação). Uma alternativa com fila (Kafka + DLQ) foi cogitada e descartada: introduziria infraestrutura desproporcional a uma chamada HTTP síncrona simples e quebraria a expectativa do protocolo de telas de uma resposta imediata a cada ação do usuário.
- **Performance**: apuração via `SELECT voto, COUNT(*) ... GROUP BY voto`, nunca carregando votos em memória; índice em `pauta_id` e constraint única `(pauta_id, cpf_associado)` sustentam a agregação e a proteção contra voto duplicado sob alta concorrência. Ver `load-test/` para o script k6 e como rodá-lo.
- **Versionamento**: todos os endpoints sob `/api/v1/...` (URI path versioning). Escolhida por ser visível na própria URL, cacheável, e trivial de testar em Swagger/Postman sem headers extras — adequado ao tamanho desta API. Alternativas descartadas: header versioning (menos descobrível para quem está testando manualmente) e content negotiation (complexidade desproporcional para uma API sem necessidade real de múltiplas representações simultâneas).

## Testes

    ./gradlew test -Dspring.profiles.active=local

Cobre regras de negócio (services, Mockito) e o fluxo HTTP completo (controllers, MockMvc + H2 em memória). Teste de carga (k6) é executado separadamente — ver `load-test/README.md`.

## Decisões de design

- Sem tabela de associados: o identificador único do associado é o próprio CPF, reaproveitado diretamente na integração externa.
- Sessão de votação embutida na própria pauta (sem tabela separada) — relação 1:1 sem necessidade de histórico de múltiplas sessões neste escopo.
- Estado aberta/encerrada calculado comparando `now()` com o horário de fechamento — sem job/scheduler.
- Segurança abstraída conforme o enunciado — nenhuma chamada exige autenticação.
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add README with run instructions and design rationale"
```

---

## Self-Review

**Spec coverage:** cadastro de pauta (Task 3/11), abrir sessão com duração/default 60s (Task 4/11), votar Sim/Não único por associado (Task 5/7/11), apurar resultado (Task 9/11), protocolo de telas Anexo 1 completo (Tasks 6/11/12), persistência sobrevivendo a restart (Task 2 Flyway + Task 14 docker volume, verified in Task 14 Step 6), bônus 1/2/3 (Tasks 8/15/16), testes automatizados (every task), documentação de código/API (Task 13 Swagger + Task 16 README), logs — not yet covered, see gap below.

**Gap found:** the spec's "Logs da aplicação" grading criterion has no dedicated task. Fixing inline: add a Task 17.

### Task 17: Structured logging

**Files:**
- Modify: `src/main/java/com/sicredi/votacao/services/PautaService.java`
- Modify: `src/main/java/com/sicredi/votacao/services/VotoService.java`
- Modify: `src/main/java/com/sicredi/votacao/exceptions/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: existing services/handler from Tasks 3, 4, 7, 9, 10.
- Produces: `action=verb_noun status=ok|error` log lines on every state-changing operation and every handled exception.

- [ ] **Step 1: Add a logger and log calls to `PautaService`**

Add the field and import (`org.slf4j.Logger`, `org.slf4j.LoggerFactory`):

```java
    private static final Logger log = LoggerFactory.getLogger(PautaService.class);
```

In `criarPauta`, after `pautaRepository.save(...)`, before `return`:

```java
        log.info("action=create_pauta status=ok id={}", pauta.getId());
```

(assign the save result to a local `Pauta pauta` first if not already). In `abrirSessao`, after `pautaRepository.save(pauta)`, before `return`:

```java
        log.info("action=abrir_sessao status=ok id={} duracaoSegundos={}", pautaId, duracao);
```

- [ ] **Step 2: Add a logger and log call to `VotoService`**

```java
    private static final Logger log = LoggerFactory.getLogger(VotoService.class);
```

Before the final `return votoRepository.save(...)`, assign to a local variable and log:

```java
        Voto salvo = votoRepository.save(new Voto(pautaId, cpfAssociado, voto));
        log.info("action=registrar_voto status=ok pautaId={} cpf={}", pautaId, cpfAssociado);
        return salvo;
```

- [ ] **Step 3: Add error logging to `GlobalExceptionHandler`**

Add the logger field and, inside the shared `build` method, log before returning:

```java
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        log.warn("action=handle_error status=error httpStatus={} message={}", status.value(), message);
        return ResponseEntity.status(status)
            .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
```

- [ ] **Step 4: Run the full suite to confirm no regressions**

Run: `./gradlew test -Dspring.profiles.active=local`
Expected: PASS (logging is additive, no behavior change)

- [ ] **Step 5: Manually verify log output**

Run: `./gradlew bootRun --args='--spring.profiles.active=local'`, then in another terminal:
`curl -X POST localhost:8080/api/v1/pautas -H "Content-Type: application/json" -d '{"titulo":"Log test","descricao":"x"}'`
Expected: console shows `action=create_pauta status=ok id=<n>`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/sicredi/votacao/services src/main/java/com/sicredi/votacao/exceptions
git commit -m "feat: add structured action/status logging to services and error handler"
```

**Placeholder scan:** no TBD/TODO remain; the one intentionally-shown "wrong draft then corrected" step in Task 11 Step 2 is resolved by Step 7 explicitly replacing the file — flagged here as a known plan artifact, not a placeholder, since the final code block given is complete and correct.

**Type consistency:** `PautaService` constructor changes from `(PautaRepository)` (Task 3) to `(PautaRepository, VotoRepository)` (Task 9) — Task 9 Step 4 updates the constructor explicitly. `VotoService` constructor `(VotoRepository, PautaService, UserInfoClient)` introduced in Task 7 is reused unchanged through Tasks 8, 11, 12. `ResultadoPauta`, `TelaFormulario`, `TelaSelecao`, `Botao`, `ItemFormulario`, `ItemSelecao` signatures are defined once (Tasks 6, 9) and reused identically in every later task.

---

**Plan complete and saved to `docs/superpowers/plans/2026-07-16-cooperative-voting-api.md`.** Two execution options:

1. **Subagent-Driven (recommended)** — dispatch a fresh subagent per task, review between tasks, fast iteration.
2. **Inline Execution** — execute tasks in this session using executing-plans, batch execution with checkpoints.
