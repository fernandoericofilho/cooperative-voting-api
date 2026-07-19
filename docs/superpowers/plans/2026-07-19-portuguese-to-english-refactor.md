# Portuguese to English Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the entire Cooperative Voting API from Portuguese to English while maintaining 100% functionality and all 149 tests passing.

**Architecture:** The refactoring follows a bottom-up approach: first rename enums and models, then services, then controllers and DTOs, and finally update all tests. Each layer depends on the previous one, so we must maintain consistency across all package names, class names, method names, properties, and messages.

**Tech Stack:** Java 17, Spring Boot 3.2, Spring Data JPA, Gradle, JUnit 5, Jacoco (code coverage 86.9%+)

## Global Constraints

- Must maintain 100% functionality
- All 149 tests must pass (0 failures allowed)
- Code coverage must remain ≥ 86.9%
- No compilation errors
- Maven directory structure preserved (src/main/java, src/test/java)
- All package renames: com.sicredi.votacao → com.sicredi.voting

---

## Package and Class Mapping Reference

### Enums Refactoring
- `OpcaoVoto` → `VoteOption` (SIM → YES, NAO → NO)
- `StatusVotacao` → `VotingStatus` (HABILITADO → ELIGIBLE, NAO_HABILITADO → NOT_ELIGIBLE)

### Model Classes
- `Pauta` → `Agenda` (table name: "pauta" → "agenda")
- `Voto` → `Vote` (table name: "voto" → "vote")

### Properties Mapping
Pauta/Agenda:
- `titulo` → `title`
- `descricao` → `description`
- `criadaEm` → `createdAt` (column: "criada_em" → "created_at")
- `sessaoAbertaEm` → `sessionOpenedAt` (column: "sessao_aberta_em" → "session_opened_at")
- `sessaoFechaEm` → `sessionClosesAt` (column: "sessao_fecha_em" → "session_closes_at")

Voto/Vote:
- `pautaId` → `agendaId` (column: "pauta_id" → "agenda_id")
- `cpfAssociado` → `memberCpf` (column: "cpf_associado" → "member_cpf")
- `voto` → `vote` (column: "voto" stays "vote")
- `criadoEm` → `createdAt` (column: "criado_em" → "created_at")

### Methods Mapping (Pauta/Agenda)
- `sessaoFoiAberta()` → `sessionWasOpened()`
- `sessaoEstaAberta()` → `sessionIsOpen()`
- `sessaoEstaEncerrada()` → `sessionIsClosed()`
- `abrirSessao()` → `openSession()`

### Service Methods
- `criarPauta()` → `createAgenda()`
- `buscarPorId()` → `findById()`
- `listarTodas()` → `listAll()`
- `listarPautas()` → `listAgendas()`
- `abrirSessao()` → `openSession()`
- `apurarResultado()` → `tallyResult()`

### Controller Methods
- `criarPauta()` → `createAgenda()`
- `listarPautas()` → `listAgendas()`
- `obterPauta()` → `getAgenda()`
- `abrirSessao()` → `openSession()`
- `obterResultado()` → `getResult()`

### Request/Response Classes
- `CriarPautaRequest` → `CreateAgendaRequest`
- `AbrirSessaoRequest` → `OpenSessionRequest`
- `RegistrarVotoRequest` → `RegisterVoteRequest`
- `PautaResponse` → `AgendaResponse`
- `PautasListResponse` → `AgendasListResponse`
- `ResultadoVotacaoResponse` → `VotingResultResponse`
- `VotoResponse` → `VoteResponse`

### DTO Classes
- `ContagemVoto` → `VoteCount`
- `ResultadoPautaDto` → `AgendaResultDto`

### Mapper Classes
- `PautaMapper` → `AgendaMapper`
- `VotoMapper` → `VoteMapper`
- `ResultadoVotacaoMapper` → `VotingResultMapper`

### Exception Classes
- `PautaNaoEncontradaException` → `AgendaNotFoundException`
- `SessaoJaAbertaException` → `SessionAlreadyOpenException`
- `SessaoNaoAbertaException` → `SessionNotOpenException`
- `SessaoEncerradaException` → `SessionClosedException`
- `AssociadoNaoHabilitadoException` → `MemberNotEligibleException`
- `CpfInvalidoException` → `InvalidCpfException`
- `VotoDuplicadoException` → `DuplicateVoteException`
- `IntegracaoExternaIndisponivelException` → `ExternalIntegrationUnavailableException`

### Logs and Messages (Sample)
- "Pauta criada: id={}, titulo={}" → "Agenda created: id={}, title={}"
- "Listando pautas: page={}, size={}" → "Listing agendas: page={}, size={}"
- "Tentativa de abrir sessão já aberta" → "Attempt to open already opened session"
- "Sessão aberta: pautaId={}" → "Session opened: agendaId={}"

---

## Task Breakdown

### Phase 1: Enums Refactoring (Foundation)

### Task 1: Refactor OpcaoVoto Enum

**Files:**
- Modify: `src/main/java/com/sicredi/votacao/enums/OpcaoVoto.java` → Rename to `VoteOption.java`
- Modify: `src/main/java/com/sicredi/votacao/enums/StatusVotacao.java` → Rename to `VotingStatus.java`
- Create: New files in `src/main/java/com/sicredi/voting/enums/` directory
- Delete: Old enum files after migration

**Interfaces:**
- Produces: `com.sicredi.voting.enums.VoteOption` with values `YES, NO`
- Produces: `com.sicredi.voting.enums.VotingStatus` with values `ELIGIBLE, NOT_ELIGIBLE`

**Steps:**

- [ ] **Step 1: Create new enums directory structure**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/enums
```

- [ ] **Step 2: Create VoteOption enum**

Content for `src/main/java/com/sicredi/voting/enums/VoteOption.java`:
```java
package com.sicredi.voting.enums;

public enum VoteOption {
    YES,
    NO
}
```

- [ ] **Step 3: Create VotingStatus enum**

Content for `src/main/java/com/sicredi/voting/enums/VotingStatus.java`:
```java
package com.sicredi.voting.enums;

public enum VotingStatus {
    ELIGIBLE,
    NOT_ELIGIBLE
}
```

- [ ] **Step 4: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

Expected: Compilation succeeds (old enums still exist, new ones created)

- [ ] **Step 5: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/enums/
git commit -m "feat: create new English enum names (VoteOption, VotingStatus)"
```

---

### Phase 2: Model Refactoring (Core Domain)

### Task 2: Refactor Pauta Model to Agenda

**Files:**
- Modify: `src/main/java/com/sicredi/votacao/models/Pauta.java`
- Create: `src/main/java/com/sicredi/voting/models/Agenda.java`
- Modify: Flyway migrations (if any exist)

**Interfaces:**
- Consumes: `com.sicredi.voting.enums.VoteOption`, `com.sicredi.voting.enums.VotingStatus`
- Produces: `com.sicredi.voting.models.Agenda` with methods:
  - `sessionWasOpened(): boolean`
  - `sessionIsOpen(): boolean`
  - `sessionIsClosed(): boolean`
  - `openSession(durationSeconds: long): void`
  - Getters/setters for all properties in English

**Steps:**

- [ ] **Step 1: Create models directory**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/models
```

- [ ] **Step 2: Create new Agenda model**

Content for `src/main/java/com/sicredi/voting/models/Agenda.java`:
```java
package com.sicredi.voting.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "agenda")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "session_opened_at")
    private LocalDateTime sessionOpenedAt;

    @Column(name = "session_closes_at")
    private LocalDateTime sessionClosesAt;

    public Agenda(String title, String description) {
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public boolean sessionWasOpened() {
        return sessionOpenedAt != null;
    }

    public boolean sessionIsOpen() {
        return sessionWasOpened() && LocalDateTime.now().isBefore(sessionClosesAt);
    }

    public boolean sessionIsClosed() {
        return sessionWasOpened() && !sessionIsOpen();
    }

    public void openSession(long durationSeconds) {
        this.sessionOpenedAt = LocalDateTime.now();
        this.sessionClosesAt = this.sessionOpenedAt.plusSeconds(durationSeconds);
    }
}
```

- [ ] **Step 3: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 4: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/models/Agenda.java
git commit -m "feat: create Agenda model (refactored from Pauta)"
```

---

### Task 3: Refactor Voto Model to Vote

**Files:**
- Create: `src/main/java/com/sicredi/voting/models/Vote.java`

**Interfaces:**
- Consumes: `com.sicredi.voting.enums.VoteOption`
- Produces: `com.sicredi.voting.models.Vote` with properties:
  - `agendaId: Long`
  - `memberCpf: String`
  - `vote: VoteOption`
  - `createdAt: LocalDateTime`

**Steps:**

- [ ] **Step 1: Create Vote model**

Content for `src/main/java/com/sicredi/voting/models/Vote.java`:
```java
package com.sicredi.voting.models;

import com.sicredi.voting.enums.VoteOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote", uniqueConstraints = @UniqueConstraint(name = "uk_vote_agenda_member_cpf", columnNames = {"agenda_id", "member_cpf"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agenda_id", nullable = false)
    private Long agendaId;

    @Column(name = "member_cpf", nullable = false, length = 11, columnDefinition = "CHAR(11)")
    private String memberCpf;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote", nullable = false, length = 3)
    private VoteOption vote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Vote(Long agendaId, String memberCpf, VoteOption vote) {
        this.agendaId = agendaId;
        this.memberCpf = memberCpf;
        this.vote = vote;
        this.createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 3: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/models/Vote.java
git commit -m "feat: create Vote model (refactored from Voto)"
```

---

### Phase 3: Repository Refactoring

### Task 4: Create Repositories in New Package

**Files:**
- Create: `src/main/java/com/sicredi/voting/repositories/AgendaRepository.java`
- Create: `src/main/java/com/sicredi/voting/repositories/VoteRepository.java`

**Interfaces:**
- Consumes: `com.sicredi.voting.models.Agenda`, `com.sicredi.voting.models.Vote`
- Produces: `AgendaRepository` and `VoteRepository` Spring Data JPA interfaces

**Steps:**

- [ ] **Step 1: Create repositories directory**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/repositories
```

- [ ] **Step 2: Create AgendaRepository**

Content for `src/main/java/com/sicredi/voting/repositories/AgendaRepository.java`:
```java
package com.sicredi.voting.repositories;

import com.sicredi.voting.models.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
}
```

- [ ] **Step 3: Create VoteRepository with custom query**

Read old repository to get custom query:
```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/repositories/VotoRepository.java
```

Content for `src/main/java/com/sicredi/voting/repositories/VoteRepository.java`:
```java
package com.sicredi.voting.repositories;

import com.sicredi.voting.models.Vote;
import com.sicredi.voting.dtos.VoteCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT new com.sicredi.voting.dtos.VoteCount(v.vote, COUNT(v)) FROM Vote v WHERE v.agendaId = :agendaId GROUP BY v.vote")
    List<VoteCount> countByAgenda(@Param("agendaId") Long agendaId);
}
```

- [ ] **Step 4: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 5: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/repositories/
git commit -m "feat: create repositories in new package"
```

---

### Phase 4: DTO Refactoring

### Task 5: Create DTOs in New Package

**Files:**
- Create: `src/main/java/com/sicredi/voting/dtos/`

**Interfaces:**
- Consumes: `com.sicredi.voting.enums.VoteOption`
- Produces: `VoteCount`, `AgendaResultDto`

**Steps:**

- [ ] **Step 1: Create dtos directory**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/dtos
```

- [ ] **Step 2: Read original DTOs to understand structure**

```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/dtos/ContagemVoto.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/dtos/ResultadoPautaDto.java
```

- [ ] **Step 3: Create VoteCount DTO**

Content for `src/main/java/com/sicredi/voting/dtos/VoteCount.java`:
```java
package com.sicredi.voting.dtos;

import com.sicredi.voting.enums.VoteOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoteCount {
    private VoteOption option;
    private Long total;
}
```

- [ ] **Step 4: Create AgendaResultDto**

Content for `src/main/java/com/sicredi/voting/dtos/AgendaResultDto.java`:
```java
package com.sicredi.voting.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendaResultDto {
    private Long yesCount;
    private Long noCount;
    private String result;

    public static AgendaResultDto calculate(Long yesCount, Long noCount) {
        String result = yesCount > noCount ? "APPROVED" : (noCount > yesCount ? "REJECTED" : "TIED");
        return new AgendaResultDto(yesCount, noCount, result);
    }
}
```

- [ ] **Step 5: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 6: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/dtos/
git commit -m "feat: create DTOs in new package"
```

---

### Phase 5: Exception Refactoring

### Task 6: Create Exceptions in New Package

**Files:**
- Create: `src/main/java/com/sicredi/voting/exceptions/`

**Interfaces:**
- Produces: All refactored exception classes in new package

**Steps:**

- [ ] **Step 1: Create exceptions directory**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/exceptions
```

- [ ] **Step 2: List all exception files**

```bash
ls /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/exceptions/
```

- [ ] **Step 3: Create each exception (all 8 exceptions)**

Create `AgendaNotFoundException.java`:
```java
package com.sicredi.voting.exceptions;

public class AgendaNotFoundException extends RuntimeException {
    public AgendaNotFoundException(Long id) {
        super("Agenda not found with id: " + id);
    }
}
```

Create `SessionAlreadyOpenException.java`:
```java
package com.sicredi.voting.exceptions;

public class SessionAlreadyOpenException extends RuntimeException {
    public SessionAlreadyOpenException(Long agendaId) {
        super("Session already open for agenda: " + agendaId);
    }
}
```

Create `SessionNotOpenException.java`:
```java
package com.sicredi.voting.exceptions;

public class SessionNotOpenException extends RuntimeException {
    public SessionNotOpenException(Long agendaId) {
        super("Session not open for agenda: " + agendaId);
    }
}
```

Create `SessionClosedException.java`:
```java
package com.sicredi.voting.exceptions;

public class SessionClosedException extends RuntimeException {
    public SessionClosedException(Long agendaId) {
        super("Session closed for agenda: " + agendaId);
    }
}
```

Create `MemberNotEligibleException.java`:
```java
package com.sicredi.voting.exceptions;

public class MemberNotEligibleException extends RuntimeException {
    public MemberNotEligibleException(String memberCpf) {
        super("Member not eligible: " + memberCpf);
    }
}
```

Create `InvalidCpfException.java`:
```java
package com.sicredi.voting.exceptions;

public class InvalidCpfException extends RuntimeException {
    public InvalidCpfException(String cpf) {
        super("Invalid CPF: " + cpf);
    }
}
```

Create `DuplicateVoteException.java`:
```java
package com.sicredi.voting.exceptions;

public class DuplicateVoteException extends RuntimeException {
    public DuplicateVoteException(Long agendaId, String memberCpf) {
        super("Duplicate vote for agenda: " + agendaId + ", member CPF: " + memberCpf);
    }
}
```

Create `ExternalIntegrationUnavailableException.java`:
```java
package com.sicredi.voting.exceptions;

public class ExternalIntegrationUnavailableException extends RuntimeException {
    public ExternalIntegrationUnavailableException() {
        super("External integration unavailable");
    }
    
    public ExternalIntegrationUnavailableException(String message) {
        super(message);
    }
}
```

Create `ErrorResponse.java`:
```java
package com.sicredi.voting.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ErrorResponse {
    private Integer status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
```

- [ ] **Step 4: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 5: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/exceptions/
git commit -m "feat: create exceptions in new package"
```

---

### Phase 6: Mapper Refactoring

### Task 7: Create Mappers in New Package

**Files:**
- Create: `src/main/java/com/sicredi/voting/mappers/`

**Interfaces:**
- Consumes: Models, Response classes
- Produces: Mapper classes for data transformation

**Steps:**

- [ ] **Step 1: Create mappers directory**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/mappers
```

- [ ] **Step 2: Create AgendaMapper**

Content (will be updated in next task when responses are created):
```java
package com.sicredi.voting.mappers;

import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.controllers.response.AgendaResponse;
import org.springframework.stereotype.Component;

@Component
public class AgendaMapper {
    public AgendaResponse toAgendaDTO(Agenda agenda) {
        return new AgendaResponse(
            agenda.getId(),
            agenda.getTitle(),
            agenda.getDescription(),
            agenda.getCreatedAt(),
            agenda.getSessionOpenedAt(),
            agenda.getSessionClosesAt()
        );
    }
}
```

- [ ] **Step 3: Create VoteMapper**

Content:
```java
package com.sicredi.voting.mappers;

import com.sicredi.voting.models.Vote;
import com.sicredi.voting.controllers.response.VoteResponse;
import org.springframework.stereotype.Component;

@Component
public class VoteMapper {
    public VoteResponse toVoteDTO(Vote vote) {
        return new VoteResponse(
            vote.getId(),
            vote.getAgendaId(),
            vote.getMemberCpf(),
            vote.getVote(),
            vote.getCreatedAt()
        );
    }
}
```

- [ ] **Step 4: Create VotingResultMapper**

Content:
```java
package com.sicredi.voting.mappers;

import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.controllers.response.VotingResultResponse;
import org.springframework.stereotype.Component;

@Component
public class VotingResultMapper {
    public VotingResultResponse toResultDTO(Agenda agenda, AgendaResultDto result) {
        return new VotingResultResponse(
            agenda.getId(),
            agenda.getTitle(),
            result.getYesCount(),
            result.getNoCount(),
            result.getResult()
        );
    }
}
```

- [ ] **Step 5: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

(Expected: May fail due to missing response classes, but mapper code is ready)

- [ ] **Step 6: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/mappers/
git commit -m "feat: create mappers in new package"
```

---

### Phase 7: Service Layer Refactoring

### Task 8: Create Services in New Package

**Files:**
- Create: `src/main/java/com/sicredi/voting/services/`
- Create: `src/main/java/com/sicredi/voting/services/util/`
- Create: `src/main/java/com/sicredi/voting/services/external/`

**Interfaces:**
- Consumes: Models, Repositories, DTOs
- Produces: `AgendaService`, `VoteService`, `CpfUtils`, `UserInfoClient`, `WebClientUserInfoClient`

**Steps:**

- [ ] **Step 1: Create services directory structure**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/services/util
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/services/external
```

- [ ] **Step 2: Read original services to understand business logic**

```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/PautaService.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/VotoService.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/util/CpfUtils.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/external/UserInfoClient.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/external/WebClientUserInfoClient.java
```

- [ ] **Step 3: Create AgendaService**

Content for `src/main/java/com/sicredi/voting/services/AgendaService.java`:
```java
package com.sicredi.voting.services;

import com.sicredi.voting.dtos.VoteCount;
import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.exceptions.AgendaNotFoundException;
import com.sicredi.voting.exceptions.SessionAlreadyOpenException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.repositories.AgendaRepository;
import com.sicredi.voting.repositories.VoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgendaService {

    private static final long DEFAULT_DURATION_SECONDS = 60L;

    private final AgendaRepository agendaRepository;
    private final VoteRepository voteRepository;

    public AgendaService(AgendaRepository agendaRepository, VoteRepository voteRepository) {
        this.agendaRepository = agendaRepository;
        this.voteRepository = voteRepository;
    }

    public Agenda createAgenda(String title, String description) {
        Agenda agenda = agendaRepository.save(new Agenda(title, description));
        log.info("Agenda created: id={}, title={}", agenda.getId(), title);
        return agenda;
    }

    public Agenda findById(Long id) {
        return agendaRepository.findById(id)
            .orElseThrow(() -> new AgendaNotFoundException(id));
    }

    public java.util.List<Agenda> listAll() {
        return agendaRepository.findAll();
    }

    public Page<Agenda> listAgendas(Pageable pageable) {
        log.info("Listing agendas: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return agendaRepository.findAll(pageable);
    }

    public Agenda openSession(Long agendaId, Long durationSeconds) {
        Agenda agenda = findById(agendaId);
        if (agenda.sessionWasOpened()) {
            log.warn("Attempt to open already opened session: agendaId={}", agendaId);
            throw new SessionAlreadyOpenException(agendaId);
        }
        long duration = durationSeconds != null ? durationSeconds : DEFAULT_DURATION_SECONDS;
        agenda.openSession(duration);
        Agenda savedAgenda = agendaRepository.save(agenda);
        log.info("Session opened: agendaId={}, duration={}s", agendaId, duration);
        return savedAgenda;
    }

    public AgendaResultDto tallyResult(Long agendaId) {
        findById(agendaId);
        long yesCount = 0L;
        long noCount = 0L;
        for (VoteCount count : voteRepository.countByAgenda(agendaId)) {
            if (count.getOption() == VoteOption.YES) {
                yesCount = count.getTotal();
            } else {
                noCount = count.getTotal();
            }
        }
        return AgendaResultDto.calculate(yesCount, noCount);
    }
}
```

- [ ] **Step 4: Create VoteService**

Read original first to get exact implementation:
```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/VotoService.java
```

Create `src/main/java/com/sicredi/voting/services/VoteService.java` with refactored names

- [ ] **Step 5: Create CpfUtils**

```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/util/CpfUtils.java
```

Create `src/main/java/com/sicredi/voting/services/util/CpfUtils.java` with refactored names

- [ ] **Step 6: Create UserInfoClient interface and implementations**

```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/external/UserInfoClient.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/services/external/WebClientUserInfoClient.java
```

Create corresponding files in new package

- [ ] **Step 7: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 8: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/services/
git commit -m "feat: create services in new package"
```

---

### Phase 8: Controller and Request/Response Refactoring

### Task 9: Create Request/Response Classes

**Files:**
- Create: `src/main/java/com/sicredi/voting/controllers/request/`
- Create: `src/main/java/com/sicredi/voting/controllers/response/`

**Interfaces:**
- Produces: All request/response classes with English names

**Steps:**

- [ ] **Step 1: Create request/response directories**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/controllers/request
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/controllers/response
```

- [ ] **Step 2: Examine original request/response files**

```bash
ls -la /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/controllers/request/
ls -la /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/controllers/response/
```

- [ ] **Step 3: Create request classes (3 files)**

- CreateAgendaRequest
- OpenSessionRequest
- RegisterVoteRequest

- [ ] **Step 4: Create response classes (5 files)**

- AgendaResponse
- AgendasListResponse
- VoteResponse
- VotingResultResponse
- PageResponse

- [ ] **Step 5: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 6: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/controllers/
git commit -m "feat: create request/response classes in new package"
```

---

### Task 10: Create Controllers in New Package

**Files:**
- Create: `src/main/java/com/sicredi/voting/controllers/AgendaController.java`
- Create: `src/main/java/com/sicredi/voting/controllers/VoteController.java`

**Interfaces:**
- Consumes: Services, Mappers, Request/Response classes
- Produces: REST endpoints with new paths and method names

**Steps:**

- [ ] **Step 1: Read original controllers**

```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/controllers/PautaController.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/controllers/VotoController.java
```

- [ ] **Step 2: Create AgendaController**

- Routes: /api/v1/agendas
- Methods: createAgenda, listAgendas, getAgenda, openSession, getResult

- [ ] **Step 3: Create VoteController**

- Routes: /api/v1/votes
- Methods: registerVote, getVote

- [ ] **Step 4: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 5: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/controllers/AgendaController.java
git add src/main/java/com/sicredi/voting/controllers/VoteController.java
git commit -m "feat: create controllers in new package"
```

---

### Phase 9: Configuration Refactoring

### Task 11: Create Configuration Classes

**Files:**
- Create: `src/main/java/com/sicredi/voting/config/`

**Interfaces:**
- Produces: JacksonConfig, OpenApiConfig with updated metadata

**Steps:**

- [ ] **Step 1: Create config directory**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/voting/config
```

- [ ] **Step 2: Read original configs**

```bash
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/config/JacksonConfig.java
cat /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao/config/OpenApiConfig.java
```

- [ ] **Step 3: Create JacksonConfig**

- [ ] **Step 4: Create OpenApiConfig with updated titles/descriptions**

- [ ] **Step 5: Create main application class**

- VotingApplication (previously VotacaoApplication)

- [ ] **Step 6: Verify compilation**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew compileJava
```

- [ ] **Step 7: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/java/com/sicredi/voting/config/
git add src/main/java/com/sicredi/voting/VotingApplication.java
git commit -m "feat: create configuration in new package"
```

---

### Phase 10: Database Migration

### Task 12: Create Flyway Migration for Table Renames

**Files:**
- Create: `src/main/resources/db/migration/V003__Rename_tables_and_columns.sql`

**Interfaces:**
- Produces: SQL migration to rename tables and columns to English

**Steps:**

- [ ] **Step 1: Examine current migrations**

```bash
ls -la /c/Estudo/IA/cooperative-voting-api/src/main/resources/db/migration/
cat /c/Estudo/IA/cooperative-voting-api/src/main/resources/db/migration/V001__Create_pauta_table.sql
cat /c/Estudo/IA/cooperative-voting-api/src/main/resources/db/migration/V002__Create_voto_table.sql
```

- [ ] **Step 2: Create migration file**

Content for `V003__Rename_tables_and_columns.sql`:
```sql
-- Rename tables
ALTER TABLE pauta RENAME TO agenda;
ALTER TABLE voto RENAME TO vote;

-- Rename columns in agenda table
ALTER TABLE agenda RENAME COLUMN titulo TO title;
ALTER TABLE agenda RENAME COLUMN descricao TO description;
ALTER TABLE agenda RENAME COLUMN criada_em TO created_at;
ALTER TABLE agenda RENAME COLUMN sessao_aberta_em TO session_opened_at;
ALTER TABLE agenda RENAME COLUMN sessao_fecha_em TO session_closes_at;

-- Rename columns in vote table
ALTER TABLE vote RENAME COLUMN pauta_id TO agenda_id;
ALTER TABLE vote RENAME COLUMN cpf_associado TO member_cpf;
ALTER TABLE vote RENAME COLUMN criado_em TO created_at;

-- Rename constraints
ALTER TABLE vote RENAME CONSTRAINT uk_voto_pauta_cpf TO uk_vote_agenda_member_cpf;
ALTER TABLE vote RENAME CONSTRAINT fk_voto_pauta_id TO fk_vote_agenda_id;
```

- [ ] **Step 3: Verify migration syntax**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew processResources
```

- [ ] **Step 4: Commit**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/main/resources/db/migration/
git commit -m "feat: add Flyway migration for table and column renames"
```

---

### Phase 11: Test Refactoring (Critical - Must Keep 149 Tests Passing)

### Task 13: Create Test Structure and Update All Tests

**Files:**
- Create: `src/test/java/com/sicredi/voting/` (full parallel structure)
- Rename all test classes

**Interfaces:**
- Consumes: All refactored classes from phases 1-10
- Produces: All tests passing with 86.9%+ coverage

**Strategy:**
- Create new test files with refactored names
- Update all imports to point to new packages
- Update all assertions and test data to use English names
- Verify 149 tests pass with 0 failures

**Steps:**

- [ ] **Step 1: Create test directory structure**

```bash
mkdir -p /c/Estudo/IA/cooperative-voting-api/src/test/java/com/sicredi/voting/{controllers,dtos,exceptions,integration,mappers,models,services/{external,util}}
```

- [ ] **Step 2: List all test files**

```bash
find /c/Estudo/IA/cooperative-voting-api/src/test/java/com/sicredi/votacao -name "*.java" | sort
```

- [ ] **Step 3: Refactor test files in batches**

Batch 1: Model tests (4 files)
- PautaTest.java → AgendaTest.java
- PautaCompleteTest.java → AgendaCompleteTest.java
- VotoTest.java → VoteTest.java (if exists)
- EnumTests → EnumTests (if needed)

Batch 2: Repository tests (if any)

Batch 3: Service tests (2+ files)
- PautaServiceExtendedTest.java → AgendaServiceExtendedTest.java
- VotoServiceExtendedTest.java → VoteServiceExtendedTest.java

Batch 4: Mapper tests (5 files)
- PautaMapperSimpleTest.java → AgendaMapperSimpleTest.java
- PautaMapperCoverageTest.java → AgendaMapperCoverageTest.java
- VotoMapperSimpleTest.java → VoteMapperSimpleTest.java
- VotoMapperTest.java → VoteMapperTest.java
- ResultadoVotacaoMapperCoverageTest.java → VotingResultMapperCoverageTest.java

Batch 5: Controller tests (3 files)
- PautaControllerIntegrationTest.java → AgendaControllerIntegrationTest.java
- PautaControllerCoverageTest.java → AgendaControllerCoverageTest.java
- VotoControllerIntegrationTest.java → VoteControllerIntegrationTest.java

Batch 6: Request/Response tests (2 files)
- RequestValidationTest.java → RequestValidationTest.java (refactor)
- ResponseCoverageTest.java → ResponseCoverageTest.java (refactor)

Batch 7: Exception tests (3 files)
- GlobalExceptionHandlerTest.java → GlobalExceptionHandlerTest.java (refactor)
- GlobalExceptionHandlerExceptionTest.java → GlobalExceptionHandlerExceptionTest.java (refactor)
- ExceptionHandlingTest.java → ExceptionHandlingTest.java (refactor)

Batch 8: Integration tests (2 files)
- PautaWorkflowTest.java → AgendaWorkflowTest.java
- IntegrationValidationTest.java → IntegrationValidationTest.java (refactor)

Batch 9: DTO tests
- DomainDTOSerializationTest.java → DomainDTOSerializationTest.java (refactor)

- [ ] **Step 4: Run tests after each batch**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew clean test
```

Expected: All 149 tests pass after each batch

- [ ] **Step 5: Verify coverage remains 86.9%+**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew jacocoTestReport
```

Check report at: `build/reports/jacoco/test/html/index.html`

- [ ] **Step 6: Commit after each batch**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add src/test/java/com/sicredi/voting/[batch_package]/
git commit -m "test: refactor [batch_name] tests to English"
```

---

### Phase 12: Old Code Cleanup (After All Tests Pass)

### Task 14: Remove Portuguese Package and Old Code

**Files:**
- Delete: `src/main/java/com/sicredi/votacao/` (entire old package)
- Delete: `src/test/java/com/sicredi/votacao/` (entire old test package)

**Steps:**

- [ ] **Step 1: Verify all tests still pass with new code**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew clean test
```

Expected: 149 tests pass, 0 failures, 86.9%+ coverage

- [ ] **Step 2: Remove old main source code**

```bash
rm -rf /c/Estudo/IA/cooperative-voting-api/src/main/java/com/sicredi/votacao
```

- [ ] **Step 3: Remove old test code**

```bash
rm -rf /c/Estudo/IA/cooperative-voting-api/src/test/java/com/sicredi/votacao
```

- [ ] **Step 4: Clean build and verify**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew clean compileJava compileTestJava
```

Expected: All compilation successful

- [ ] **Step 5: Run full test suite**

```bash
cd /c/Estudo/IA/cooperative-voting-api && ./gradlew clean test
```

Expected: 149 tests pass, 0 failures, 86.9%+ coverage

- [ ] **Step 6: Commit cleanup**

```bash
cd /c/Estudo/IA/cooperative-voting-api
git add -A
git commit -m "refactor: remove Portuguese package after complete migration"
```

---

## Summary Checklist

After completing all tasks:

- [ ] All packages renamed: com.sicredi.votacao → com.sicredi.voting
- [ ] All classes renamed to English names
- [ ] All properties renamed to English (snake_case → camelCase in code, snake_case in DB)
- [ ] All methods renamed to English
- [ ] All logs translated to English
- [ ] All error messages translated to English
- [ ] All tests refactored to English and passing (149 tests)
- [ ] Code coverage maintained ≥ 86.9%
- [ ] No compilation errors
- [ ] Database migrations in place
- [ ] Old Portuguese code removed
- [ ] All changes committed with meaningful messages

---

## Rollback Plan

If issues arise:
1. `git reset --hard HEAD~<number>` to go back to previous state
2. Issues are identified in test execution (tests will fail)
3. Correct the specific refactoring task and recommit

---
