# Step 04 — Architecture Documentation

## Continental Insurance Group — Java Spring Boot Claims Processor

**Generated:** 2026-04-16
**Source:** Step 03 (Modernization Specification)
**Purpose:** Document the scaffolded Spring Boot project architecture

---

## 1. Overview

This document describes the architecture of the modernized Continental Insurance claims processing system. The original COBOL batch system has been restructured as a Java Spring Boot application using a layered architecture pattern.

### Technology Stack

| Component        | Technology              | Version  |
|------------------|-------------------------|----------|
| Language         | Java                    | 8 (LTS)  |
| Framework        | Spring Boot             | 2.7.18   |
| Data Access      | Spring Data JPA         | via Boot |
| Database (Dev)   | H2 (in-memory)          | via Boot |
| Build Tool       | Apache Maven            | 3.8+     |
| Testing          | JUnit 5 + Mockito       | via Boot |

---

## 2. Project Structure

```
appmodlab-cobol-to-java/
├── pom.xml                                          # Maven build (Spring Boot 2.7.18 parent)
├── src/
│   ├── main/
│   │   ├── java/com/continental/insurance/
│   │   │   ├── ClaimsProcessorApplication.java      # @SpringBootApplication entry point
│   │   │   └── model/
│   │   │       ├── Claim.java                       # JPA entity — from CLMREC.cpy
│   │   │       ├── Policy.java                      # JPA entity — from POLREC.cpy
│   │   │       ├── Payment.java                     # JPA entity — from PYMTREC.cpy
│   │   │       ├── ClaimType.java                   # Enum: MEDICAL, DENTAL, VISION, PHARMACY
│   │   │       ├── ClaimStatus.java                 # Enum: APPROVED, DENIED, PENDING
│   │   │       ├── PlanType.java                    # Enum: BASIC, SILVER, PREMIUM, BRONZE
│   │   │       └── PaymentStatus.java               # Enum: AUTHORIZED, DENIED, PENDING
│   │   └── resources/
│   │       └── application.yml                      # H2 config + business rule constants
│   └── test/
│       └── java/com/continental/insurance/          # Test root (to be populated)
├── src/cobol/                                       # Original COBOL source (reference)
├── src/copybooks/                                   # Original COBOL copybooks (reference)
└── assets/outputs/                                  # Analysis & spec documents
```

---

## 3. Layered Architecture

The application follows a standard Spring Boot layered architecture:

```
┌─────────────────────────────────────────────┐
│              REST Controllers               │  ← HTTP endpoints (future)
├─────────────────────────────────────────────┤
│              Service Layer                  │  ← Business logic (future)
│  ClaimsProcessingService  (CLMPROC)         │
│  AdjudicationService      (ADJUDCTN)        │
│  PolicyLookupService      (POLYLKUP)        │
│  PaymentAuthorizationService (PYMTAUTH)     │
│  ReportGenerationService  (RPTGEN)          │
├─────────────────────────────────────────────┤
│              Repository Layer               │  ← Data access (future)
├─────────────────────────────────────────────┤
│              Entity / Model Layer           │  ← JPA entities (scaffolded)
│  Claim, Policy, Payment + Enums             │
├─────────────────────────────────────────────┤
│              H2 Database (Dev)              │  ← In-memory relational store
└─────────────────────────────────────────────┘
```

---

## 4. Data Model

### 4.1 Entity Relationship Diagram

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│    policies       │       │     claims        │       │    payments       │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ policy_number PK │◄──FK──│ policy_number     │       │ id PK (auto)     │
│ holder_name      │       │ claim_number PK   │──FK──►│ claim_number     │
│ plan_type        │       │ claim_date        │       │ payment_amount   │
│ effective_date   │       │ claim_type        │       │ payment_date     │
│ expiry_date      │       │ claim_amount      │       │ auth_code (uniq) │
│ deductible       │       │ status            │       │ status           │
│ max_coverage     │       │ diagnosis_code    │       └──────────────────┘
│ status           │       │ provider_id       │
└──────────────────┘       └──────────────────┘
```

### 4.2 COBOL-to-Java Type Mapping

| COBOL Type          | Java Type              | Notes                                    |
|---------------------|------------------------|------------------------------------------|
| PIC 9(7)V99         | BigDecimal(9,2)        | Exact decimal arithmetic for money       |
| PIC 9(5)V99         | BigDecimal(7,2)        | Deductible amounts                       |
| PIC 9(8) date       | LocalDate              | Java 8 date; no manual YYYYMMDD parsing  |
| PIC X(2) type code  | Enum (ClaimType, etc.) | Type-safe with fromCode() converter      |
| PIC X(1) status     | Enum / String          | Enum where domain is closed              |
| PIC X(n) text       | String                 | With @Column length constraints          |

### 4.3 Monetary Field Handling

All monetary values use `java.math.BigDecimal`:
- **Precision and scale** match original COBOL PIC definitions
- **Rounding** uses `RoundingMode.HALF_EVEN` to preserve COBOL `ROUNDED` behavior
- **Comparisons** use `compareTo()`, never `equals()` (to avoid scale mismatch)

---

## 5. Enum Design

Each enum that maps from COBOL codes includes:
- A `code` field storing the original COBOL value
- A `fromCode(String)` static factory for deserialization
- JPA persistence via `@Enumerated(EnumType.STRING)` (human-readable in DB)

| Enum           | Values                               | COBOL Source       |
|----------------|--------------------------------------|--------------------|
| ClaimType      | MEDICAL, DENTAL, VISION, PHARMACY    | CLM-CLAIM-TYPE     |
| ClaimStatus    | APPROVED, DENIED, PENDING            | CLM-STATUS         |
| PlanType       | BASIC, SILVER, PREMIUM, BRONZE       | POL-PLAN-TYPE      |
| PaymentStatus  | AUTHORIZED, DENIED, PENDING          | PYMT-STATUS        |

---

## 6. Configuration

Business rule constants are externalized in `application.yml` under the `claims` prefix, replacing COBOL hardcoded values:

| Constant              | YAML Key                            | Value      | COBOL Origin  |
|-----------------------|-------------------------------------|------------|---------------|
| Min claim amount      | claims.adjudication.min-claim-amount | 50.00     | ADJ-201       |
| Auto-approve ceiling  | claims.adjudication.max-auto-approve | 5000.00   | ADJ-404       |
| Manual review limit   | claims.adjudication.manual-review-limit | 25000.00 | ADJ-202    |
| Dental cap            | claims.adjudication.dental-max       | 2500.00   | ADJ-311       |
| Vision cap            | claims.adjudication.vision-max       | 500.00    | ADJ-321       |
| Coinsurance rates     | claims.coinsurance.*                 | 60–90%    | PYMTAUTH      |
| Copay amounts         | claims.copay.*                       | 10–25     | PYMTAUTH      |

---

## 7. COBOL-to-Java Mapping Summary

| COBOL Program | Future Java Service              | Layer      |
|---------------|----------------------------------|------------|
| CLMPROC       | ClaimsProcessingService          | Service    |
| ADJUDCTN      | AdjudicationService              | Service    |
| POLYLKUP      | PolicyLookupService + Repository | Service    |
| PYMTAUTH      | PaymentAuthorizationService      | Service    |
| RPTGEN        | ReportGenerationService          | Service    |
| ERRHANDL      | ErrorHandlingService + Exceptions| Service    |

---

## 8. Java 8 Constraints

This project targets **Java 8** for enterprise compatibility. The following modern Java features are **not used**:

- No `var` keyword (Java 10+)
- No records (Java 16+)
- No text blocks (Java 13+)
- No Lombok — all getters/setters are explicit
- No `switch` expressions (Java 14+)

Permitted Java 8 features in active use:
- `BigDecimal` for monetary arithmetic
- `LocalDate` / `LocalDateTime` from `java.time`
- Lambda expressions and streams
- `Optional` for nullable returns
- `Objects.hash()` / `Objects.equals()` for entity identity

---

## 9. Next Steps

The following layers will be implemented in subsequent steps:

1. **Repository interfaces** — Spring Data JPA repositories for Claim, Policy, Payment
2. **Service layer** — Business logic services mirroring COBOL programs
3. **REST controllers** — API endpoints per the modernization specification
4. **Exception handling** — Custom exception hierarchy with `@ControllerAdvice`
5. **Seed data** — `data.sql` populated from original `.dat` files
6. **Tests** — Unit, integration, and behavioral parity tests
