# Step 03 — Modernization Specification

## Continental Insurance Group — COBOL to Java Spring Boot

**Specification Date:** 2026-04-16
**Source Analysis:** Step 01 (COBOL Analysis), Step 02 (Business Rules Extraction)
**Purpose:** Complete technical specification for converting the COBOL claims processing system to Java Spring Boot

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Target Technology Stack](#2-target-technology-stack)
3. [Project Structure](#3-project-structure)
4. [COBOL-to-Java Class Mapping](#4-cobol-to-java-class-mapping)
5. [Data Model — JPA Entities](#5-data-model--jpa-entities)
6. [Service Layer Design](#6-service-layer-design)
7. [REST API Design](#7-rest-api-design)
8. [Configuration & Externalized Constants](#8-configuration--externalized-constants)
9. [Error Handling Strategy](#9-error-handling-strategy)
10. [Testing Strategy](#10-testing-strategy)
11. [Migration & Data Loading](#11-migration--data-loading)
12. [Appendix: Behavioral Parity Checklist](#appendix-behavioral-parity-checklist)

---

## 1. Executive Summary

This specification defines the target architecture for modernizing the Continental Insurance Group's batch COBOL claims processing system into a **Java Spring Boot RESTful microservice**. The modernized system will:

- Replace flat-file I/O with a relational database (H2 for development, Azure SQL for production)
- Expose claims processing functionality through REST APIs
- Preserve all existing business rules with exact behavioral parity (boundary conditions, rounding, edge cases)
- Externalize hardcoded business constants to Spring configuration
- Introduce structured logging, proper exception handling, and comprehensive test coverage

**Key Transformation:** Batch-sequential processing → API-driven service with optional batch mode.

---

## 2. Target Technology Stack

### 2.1 Core Stack

| Component | Technology | Version |
|---|---|---|
| Language | Java | 8 (LTS) |
| Framework | Spring Boot | 2.7.x |
| Data Access | Spring Data JPA / Hibernate | 5.6.x (via Spring Boot) |
| Database (Dev) | H2 | Embedded (in-memory) |
| Database (Prod) | Azure SQL Database | Latest |
| Build Tool | Apache Maven | 3.8+ |
| Testing | JUnit 5 (Jupiter) | 5.9.x |
| Mocking | Mockito | 4.x (via spring-boot-starter-test) |
| Logging | SLF4J + Logback | Via Spring Boot |
| API Documentation | SpringDoc OpenAPI (Swagger) | 1.7.x |
| Validation | Hibernate Validator (JSR 380) | Via Spring Boot |

### 2.2 Key Maven Dependencies

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<dependencies>
    <!-- Core -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-validation</dependency>
    <dependency>spring-boot-starter-actuator</dependency>

    <!-- Database -->
    <dependency>com.h2database:h2 (runtime, dev)</dependency>
    <dependency>com.microsoft.sqlserver:mssql-jdbc (runtime, prod)</dependency>

    <!-- Documentation -->
    <dependency>org.springdoc:springdoc-openapi-ui</dependency>

    <!-- Test -->
    <dependency>spring-boot-starter-test (test)</dependency>
</dependencies>
```

### 2.3 Java Version Rationale

Java 8 is selected for maximum compatibility with enterprise environments. The codebase uses:
- `BigDecimal` for all monetary calculations (replacing COBOL implied decimals)
- `LocalDate` / `LocalDateTime` from `java.time` for date handling (replacing PIC 9(8) YYYYMMDD)
- Lambda expressions and streams where they improve clarity
- `Optional` for nullable return values (e.g., policy lookups)

---

## 3. Project Structure

```
claims-processing-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/continental/claims/
│   │   │   ├── ClaimsProcessingApplication.java          # @SpringBootApplication
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── ClaimsBusinessConfig.java              # Externalized business constants
│   │   │   │   └── OpenApiConfig.java                     # Swagger/OpenAPI configuration
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── ClaimsController.java                  # Claims REST endpoints
│   │   │   │   ├── PolicyController.java                  # Policy REST endpoints
│   │   │   │   ├── PaymentController.java                 # Payment REST endpoints
│   │   │   │   └── ReportController.java                  # Report REST endpoints
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── Claim.java                             # JPA entity (from CLMREC.cpy)
│   │   │   │   ├── Policy.java                            # JPA entity (from POLREC.cpy)
│   │   │   │   └── Payment.java                           # JPA entity (from PYMTREC.cpy)
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   ├── ClaimType.java                         # MEDICAL, DENTAL, VISION, PHARMACY
│   │   │   │   ├── PlanType.java                          # BASIC, SILVER, PREMIUM, BRONZE
│   │   │   │   ├── ClaimStatus.java                       # APPROVED, DENIED, PENDING
│   │   │   │   ├── PolicyStatus.java                      # ACTIVE, INACTIVE
│   │   │   │   └── ErrorCode.java                         # From ERRCODES.cpy
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── ClaimRepository.java                   # Spring Data JPA
│   │   │   │   ├── PolicyRepository.java                  # Replaces POLYLKUP file I/O
│   │   │   │   └── PaymentRepository.java                 # Spring Data JPA
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── ClaimsProcessingService.java           # Orchestrator (CLMPROC)
│   │   │   │   ├── AdjudicationService.java               # Rules engine (ADJUDCTN)
│   │   │   │   ├── PolicyLookupService.java               # Policy lookup (POLYLKUP)
│   │   │   │   ├── PaymentAuthorizationService.java       # Payment calc (PYMTAUTH)
│   │   │   │   ├── ReportGenerationService.java           # Reporting (RPTGEN)
│   │   │   │   └── ErrorHandlingService.java              # Logging & error mgmt (ERRHANDL)
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── ClaimRequest.java                      # Inbound claim submission
│   │   │   │   ├── ClaimResponse.java                     # Claim processing result
│   │   │   │   ├── AdjudicationResult.java                # Adjudication outcome
│   │   │   │   ├── PaymentResult.java                     # Payment authorization outcome
│   │   │   │   ├── ProcessingSummary.java                 # Batch summary (replaces RPTGEN output)
│   │   │   │   └── ErrorResponse.java                     # Standardized API error
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── ClaimsProcessingException.java         # Base exception
│   │   │       ├── PolicyNotFoundException.java           # Error code 20
│   │   │       ├── InvalidClaimException.java             # Error code 30
│   │   │       ├── FileProcessingException.java           # Error codes 10, 11
│   │   │       ├── CalculationOverflowException.java      # Error code 40
│   │   │       └── GlobalExceptionHandler.java            # @ControllerAdvice
│   │   │
│   │   └── resources/
│   │       ├── application.yml                            # Default configuration
│   │       ├── application-dev.yml                        # H2 dev profile
│   │       ├── application-prod.yml                       # Azure SQL prod profile
│   │       ├── data.sql                                   # Seed data (from .dat files)
│   │       └── schema.sql                                 # DDL (optional, JPA can auto-generate)
│   │
│   └── test/
│       └── java/com/continental/claims/
│           ├── controller/
│           │   └── ClaimsControllerTest.java              # @WebMvcTest
│           ├── service/
│           │   ├── ClaimsProcessingServiceTest.java       # Integration test
│           │   ├── AdjudicationServiceTest.java           # Unit tests — all rules
│           │   ├── PolicyLookupServiceTest.java           # Unit tests
│           │   ├── PaymentAuthorizationServiceTest.java   # Unit tests — all calculations
│           │   └── ReportGenerationServiceTest.java       # Unit tests
│           ├── repository/
│           │   ├── ClaimRepositoryTest.java               # @DataJpaTest
│           │   └── PolicyRepositoryTest.java              # @DataJpaTest
│           └── integration/
│               └── ClaimsProcessingIntegrationTest.java   # End-to-end with H2
```

---

## 4. COBOL-to-Java Class Mapping

### 4.1 Program-to-Class Mapping

| COBOL Program | Java Class(es) | Layer | Responsibility |
|---|---|---|---|
| **CLMPROC** | `ClaimsProcessingService` | Service | Main orchestrator — coordinates policy lookup, adjudication, payment, and reporting. Replaces the batch loop with single-claim and batch processing methods. |
| **ADJUDCTN** | `AdjudicationService` | Service | Pure business rules engine — evaluates claims against all adjudication rules in the exact COBOL sequence. Returns `AdjudicationResult` (APPROVED/DENIED/PENDING with reason). |
| **POLYLKUP** | `PolicyLookupService` + `PolicyRepository` | Service + Repository | `PolicyLookupService` wraps the repository with business logic (found/not-found handling). `PolicyRepository` replaces the sequential file scan with JPA indexed queries. |
| **PYMTAUTH** | `PaymentAuthorizationService` | Service | Payment calculation pipeline — deductible, copay, coinsurance, limits. Uses `BigDecimal` with `RoundingMode.HALF_EVEN` (COBOL ROUNDED). |
| **RPTGEN** | `ReportGenerationService` | Service | Generates processing summary reports. Replaces 132-column DISPLAY output with structured DTOs and optional formatted text. |
| **ERRHANDL** | `ErrorHandlingService` + custom exceptions | Service + Exception | `ErrorHandlingService` provides structured logging (SLF4J). Custom exceptions replace error code dispatching. `GlobalExceptionHandler` translates exceptions to HTTP responses. |

### 4.2 Copybook-to-Entity Mapping

| COBOL Copybook | Java Entity | JPA Table |
|---|---|---|
| **CLMREC.cpy** | `Claim` | `claims` |
| **POLREC.cpy** | `Policy` | `policies` |
| **PYMTREC.cpy** | `Payment` | `payments` |
| **ERRCODES.cpy** | `ErrorCode` (enum) | N/A (code constant) |

### 4.3 Interface Mapping

| COBOL Interface (LINKAGE SECTION) | Java Equivalent |
|---|---|
| CALL 'POLYLKUP' USING policy-number, policy-record, found-flag | `Optional<Policy> policyLookupService.findByPolicyNumber(String policyNumber)` |
| CALL 'ADJUDCTN' USING claim-record, policy-record, result-code | `AdjudicationResult adjudicationService.adjudicate(Claim claim, Policy policy)` |
| CALL 'PYMTAUTH' USING claim-record, policy-record, payment-amount | `PaymentResult paymentAuthorizationService.authorize(Claim claim, Policy policy)` |
| CALL 'RPTGEN' USING counts, total-paid, report-file | `ProcessingSummary reportGenerationService.generateSummary(ProcessingStats stats)` |
| CALL 'ERRHANDL' USING error-code, error-data | `errorHandlingService.logError(ErrorCode code, String context)` + throw specific exception |

---

## 5. Data Model — JPA Entities

### 5.1 Claim Entity

```java
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @Column(name = "claim_number", length = 10)
    private String claimNumber;                          // CLM-CLAIM-NUMBER

    @Column(name = "policy_number", length = 10, nullable = false)
    private String policyNumber;                         // CLM-POLICY-NUMBER

    @Column(name = "claim_date", nullable = false)
    private LocalDate claimDate;                         // CLM-CLAIM-DATE (YYYYMMDD → LocalDate)

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", length = 10, nullable = false)
    private ClaimType claimType;                         // CLM-CLAIM-TYPE (01→MEDICAL, etc.)

    @Column(name = "claim_amount", precision = 9, scale = 2, nullable = false)
    private BigDecimal claimAmount;                      // CLM-CLAIM-AMOUNT PIC 9(7)V99

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private ClaimStatus status;                          // CLM-STATUS → enum

    @Column(name = "diagnosis_code", length = 5)
    private String diagnosisCode;                        // CLM-DIAGNOSIS-CODE

    @Column(name = "provider_id", length = 8)
    private String providerId;                           // CLM-PROVIDER-ID

    // Getters, setters, equals, hashCode, toString
}
```

**Key type mappings:**
- `PIC 9(7)V99` → `BigDecimal(precision=9, scale=2)` — preserves exact decimal arithmetic
- `PIC 9(8)` date → `LocalDate` — native Java 8 date type; no YYYYMMDD string parsing at runtime
- `PIC X(2)` claim type → `ClaimType` enum with `@Enumerated(EnumType.STRING)`
- `PIC X(1)` status → `ClaimStatus` enum

### 5.2 Policy Entity

```java
@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @Column(name = "policy_number", length = 10)
    private String policyNumber;                         // POL-POLICY-NUMBER

    @Column(name = "holder_name", length = 30, nullable = false)
    private String holderName;                           // POL-HOLDER-NAME

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 10, nullable = false)
    private PlanType planType;                           // POL-PLAN-TYPE (BS→BASIC, etc.)

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;                     // POL-EFFECTIVE-DATE

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;                        // POL-EXPIRY-DATE

    @Column(name = "deductible", precision = 7, scale = 2, nullable = false)
    private BigDecimal deductible;                       // POL-DEDUCTIBLE PIC 9(5)V99

    @Column(name = "max_coverage", precision = 9, scale = 2, nullable = false)
    private BigDecimal maxCoverage;                      // POL-MAX-COVERAGE PIC 9(7)V99

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private PolicyStatus status;                         // POL-STATUS (A→ACTIVE, I→INACTIVE)

    // Getters, setters, equals, hashCode, toString
}
```

### 5.3 Payment Entity

```java
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", length = 10, nullable = false)
    private String claimNumber;                          // PYMT-CLAIM-NUMBER

    @Column(name = "payment_amount", precision = 9, scale = 2, nullable = false)
    private BigDecimal paymentAmount;                    // PYMT-PAYMENT-AMOUNT PIC 9(7)V99

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;                       // PYMT-PAYMENT-DATE

    @Column(name = "auth_code", length = 6, nullable = false, unique = true)
    private String authCode;                             // PYMT-AUTH-CODE ("A" + sequence)

    @Column(name = "status", length = 1, nullable = false)
    private String status;                               // PYMT-STATUS ('A' = Authorized)

    // Getters, setters, equals, hashCode, toString
}
```

### 5.4 Enum Definitions

```java
public enum ClaimType {
    MEDICAL("01"),
    DENTAL("02"),
    VISION("03"),
    PHARMACY("04");

    private final String code;
    // constructor, getCode(), fromCode(String)
}

public enum PlanType {
    BASIC("BS"),
    SILVER("SV"),
    PREMIUM("PR"),
    BRONZE("BR");

    private final String code;
    // constructor, getCode(), fromCode(String)
}

public enum ClaimStatus {
    APPROVED, DENIED, PENDING
}

public enum PolicyStatus {
    ACTIVE, INACTIVE
}

public enum ErrorCode {
    FILE_OPEN_CLAIMS(10, "FATAL", "Cannot open claims file"),
    FILE_OPEN_POLICY(11, "FATAL", "Cannot open policy file"),
    POLICY_NOT_FOUND(20, "ERROR", "Policy not found for claim"),
    INVALID_CLAIM(30, "WARNING", "Invalid claim data"),
    CALC_OVERFLOW(40, "ERROR", "Calculation overflow"),
    UNKNOWN(99, "ERROR", "Unknown error");

    private final int code;
    private final String severity;
    private final String messageTemplate;
    // constructor, getters
}
```

### 5.5 Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   policies   │         │    claims    │         │   payments   │
├──────────────┤         ├──────────────┤         ├──────────────┤
│ policy_number│◄────FK──│ policy_number│         │ id (PK)      │
│ holder_name  │   (PK)  │ claim_number │──FK────►│ claim_number │
│ plan_type    │         │ claim_date   │  (PK)   │ payment_amount│
│ effective_date│        │ claim_type   │         │ payment_date │
│ expiry_date  │         │ claim_amount │         │ auth_code    │
│ deductible   │         │ status       │         │ status       │
│ max_coverage │         │ diagnosis_code│        └──────────────┘
│ status       │         │ provider_id  │
└──────────────┘         └──────────────┘
```

---

## 6. Service Layer Design

### 6.1 ClaimsProcessingService (CLMPROC)

The orchestrator service coordinates the full claims processing pipeline, replacing the COBOL main program's sequential batch loop.

```java
@Service
public class ClaimsProcessingService {

    private final PolicyLookupService policyLookupService;
    private final AdjudicationService adjudicationService;
    private final PaymentAuthorizationService paymentAuthorizationService;
    private final ReportGenerationService reportGenerationService;
    private final ErrorHandlingService errorHandlingService;
    private final ClaimRepository claimRepository;

    /**
     * Process a single claim (API-driven mode).
     * Replaces one iteration of CLMPROC's 2000-PROCESS-CLAIMS loop.
     */
    public ClaimResponse processClaim(String claimNumber);

    /**
     * Process all pending claims (batch mode).
     * Replaces the full CLMPROC batch run.
     */
    public ProcessingSummary processAllClaims();
}
```

**Orchestration logic (preserving COBOL sequence):**

1. Look up claim by claim number
2. Call `PolicyLookupService.findByPolicyNumber()` → if not found, deny claim (error 20), skip to next
3. Call `AdjudicationService.adjudicate(claim, policy)` → returns APPROVED / DENIED / PENDING
4. If APPROVED → call `PaymentAuthorizationService.authorize(claim, policy)`
5. If payment > $0.00 → persist payment record, add to total
6. Update claim status
7. Return result

### 6.2 AdjudicationService (ADJUDCTN)

Implements the exact rule sequence from the COBOL adjudication engine. **Rules are evaluated in strict order; first failure short-circuits.**

```java
@Service
public class AdjudicationService {

    private final ClaimsBusinessConfig config;

    /**
     * Adjudicate a claim against a policy.
     * Returns AdjudicationResult with status and denial/pending reason.
     *
     * Rule evaluation order (must be preserved):
     *   1. Policy status validation (ADJ-101, ADJ-102, ADJ-103)
     *   2. Claim amount thresholds (ADJ-201, ADJ-202)
     *   3. Claim type-specific rules (ADJ-301..ADJ-341)
     *   4. Coverage calculation (ADJ-401..ADJ-405)
     */
    public AdjudicationResult adjudicate(Claim claim, Policy policy);
}
```

**Critical behavioral notes:**
- Boundary conditions use strict `<` and `>` (not `<=` / `>=`) — e.g., $50.00 exactly **passes**, $25,000.00 exactly **passes**
- Default result is DENIED — claim must pass all checks to be approved
- Coverage = `claimAmount - deductible`; auto-approve threshold ($5,000) is checked against **original claim amount**, not calculated coverage

### 6.3 PolicyLookupService (POLYLKUP)

Replaces the sequential file scan with database-backed lookup.

```java
@Service
public class PolicyLookupService {

    private final PolicyRepository policyRepository;
    private final ErrorHandlingService errorHandlingService;

    /**
     * Look up a policy by number.
     * Replaces POLYLKUP's sequential scan of policies.dat.
     */
    public Optional<Policy> findByPolicyNumber(String policyNumber);
}
```

```java
@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {

    Optional<Policy> findByPolicyNumber(String policyNumber);
}
```

### 6.4 PaymentAuthorizationService (PYMTAUTH)

Implements the full payment calculation pipeline with `BigDecimal` arithmetic.

```java
@Service
public class PaymentAuthorizationService {

    private final ClaimsBusinessConfig config;
    private final PaymentRepository paymentRepository;

    /**
     * Calculate and authorize payment for an approved claim.
     *
     * Pipeline (preserving COBOL sequence):
     *   1. gross = claim amount
     *   2. deductible_applied = MIN(gross, policy_deductible)
     *   3. copay = lookup by claim type
     *   4. coinsurance = (gross - deductible - copay) × rate, ROUNDED HALF_EVEN
     *   5. net = MIN(max_coverage, claim_amount, MAX(0, coinsurance))
     *   6. Generate auth code
     */
    public PaymentResult authorize(Claim claim, Policy policy);
}
```

**BigDecimal usage requirements:**
- All monetary calculations use `BigDecimal` — never `double` or `float`
- Coinsurance rounding: `RoundingMode.HALF_EVEN` (preserves COBOL `ROUNDED` behavior)
- Scale: 2 decimal places for all monetary results
- Comparisons: use `compareTo()`, never `equals()` (scale-sensitive)

### 6.5 ReportGenerationService (RPTGEN)

```java
@Service
public class ReportGenerationService {

    /**
     * Generate processing summary from accumulated statistics.
     * Replaces RPTGEN's 132-column DISPLAY output.
     */
    public ProcessingSummary generateSummary(int claimsRead, int claimsApproved,
            int claimsDenied, int claimsPending, BigDecimal totalPaid);
}
```

**ProcessingSummary DTO:**
```java
public class ProcessingSummary {
    private int totalClaimsProcessed;
    private int claimsApproved;
    private int claimsDenied;
    private int claimsPending;
    private BigDecimal totalPayments;
    private double approvedPercentage;    // truncated integer division, per COBOL
    private double deniedPercentage;
    private double pendingPercentage;
    private LocalDateTime reportDate;
}
```

### 6.6 ErrorHandlingService (ERRHANDL)

```java
@Service
public class ErrorHandlingService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingService.class);

    /**
     * Log a structured error and throw the appropriate exception.
     * Replaces ERRHANDL's DISPLAY-based error output.
     */
    public void handleError(ErrorCode errorCode, String context);
}
```

---

## 7. REST API Design

### 7.1 API Overview

| Method | Endpoint | Description | COBOL Equivalent |
|---|---|---|---|
| `POST` | `/api/v1/claims/{claimNumber}/process` | Process a single claim | One iteration of CLMPROC loop |
| `POST` | `/api/v1/claims/process-all` | Batch process all pending claims | Full CLMPROC batch run |
| `GET` | `/api/v1/claims` | List all claims | N/A (new capability) |
| `GET` | `/api/v1/claims/{claimNumber}` | Get claim details | N/A (new capability) |
| `POST` | `/api/v1/claims` | Submit a new claim | N/A (replaces flat-file input) |
| `GET` | `/api/v1/policies` | List all policies | N/A (new capability) |
| `GET` | `/api/v1/policies/{policyNumber}` | Get policy details | POLYLKUP equivalent |
| `GET` | `/api/v1/payments` | List all payments | N/A (new capability) |
| `GET` | `/api/v1/payments/claim/{claimNumber}` | Get payment for a claim | N/A (new capability) |
| `GET` | `/api/v1/reports/summary` | Get processing summary | RPTGEN equivalent |

### 7.2 Endpoint Details

#### POST /api/v1/claims/{claimNumber}/process

Process a single claim through the full pipeline.

**Response 200 (OK):**
```json
{
    "claimNumber": "CLM0000001",
    "policyNumber": "POL1000001",
    "claimType": "MEDICAL",
    "claimAmount": 1500.00,
    "status": "APPROVED",
    "adjudicationResult": {
        "status": "APPROVED",
        "reason": "Auto-approved: claim amount within threshold"
    },
    "payment": {
        "paymentAmount": 1032.50,
        "authCode": "A00001",
        "paymentDate": "2026-04-16"
    }
}
```

**Response 404 (Claim Not Found):**
```json
{
    "error": "CLAIM_NOT_FOUND",
    "message": "Claim CLM9999999 not found",
    "timestamp": "2026-04-16T19:04:04"
}
```

#### POST /api/v1/claims/process-all

Batch process all unprocessed claims. Returns a summary.

**Response 200 (OK):**
```json
{
    "totalClaimsProcessed": 20,
    "claimsApproved": 12,
    "claimsDenied": 5,
    "claimsPending": 3,
    "totalPayments": 45250.75,
    "approvedPercentage": 60,
    "deniedPercentage": 25,
    "pendingPercentage": 15,
    "reportDate": "2026-04-16T19:04:04"
}
```

#### POST /api/v1/claims

Submit a new claim.

**Request Body:**
```json
{
    "claimNumber": "CLM0000021",
    "policyNumber": "POL1000001",
    "claimDate": "2026-04-16",
    "claimType": "MEDICAL",
    "claimAmount": 3000.00,
    "diagnosisCode": "D1234",
    "providerId": "PROV0001"
}
```

**Response 201 (Created):** Returns the created claim.

#### GET /api/v1/reports/summary

Generate and return the processing summary report.

**Response 200 (OK):** Same structure as the batch process-all summary.

### 7.3 Common Error Response Format

All error responses follow a consistent structure:

```json
{
    "error": "ERROR_CODE",
    "message": "Human-readable description",
    "details": "Additional context (optional)",
    "timestamp": "2026-04-16T19:04:04"
}
```

| HTTP Status | Error Code | Trigger |
|---|---|---|
| 400 | `INVALID_CLAIM` | Validation failure (missing required fields) |
| 404 | `CLAIM_NOT_FOUND` | Claim number does not exist |
| 404 | `POLICY_NOT_FOUND` | Policy number does not exist (error code 20) |
| 409 | `CLAIM_ALREADY_PROCESSED` | Claim has already been adjudicated |
| 500 | `PROCESSING_ERROR` | Unexpected processing failure |

---

## 8. Configuration & Externalized Constants

All COBOL hardcoded business constants are externalized to `application.yml`.

### 8.1 application.yml

```yaml
claims:
  adjudication:
    min-claim-amount: 50.00          # ADJ-201: Claims below this are denied
    max-auto-approve: 5000.00        # ADJ-404: Claims at/below this auto-approve
    manual-review-limit: 25000.00    # ADJ-202: Claims above this → pending
    dental-max: 2500.00              # ADJ-311: Dental cap (non-Premium)
    vision-max: 500.00               # ADJ-321: Vision hard cap

  coinsurance:
    basic: 80                        # BS plan rate (%)
    silver: 70                       # SV plan rate (%)
    premium: 90                      # PR plan rate (%)
    bronze: 60                       # BR plan rate (%)
    default-rate: 80                 # Fallback rate (%)

  copay:
    medical: 25.00                   # Type 01 copay
    dental: 15.00                    # Type 02 copay
    vision: 10.00                    # Type 03 copay
    pharmacy: 10.00                  # Type 04 copay

spring:
  profiles:
    active: dev

---
# Dev profile
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:claimsdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

---
# Prod profile
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:sqlserver://${AZURE_SQL_HOST}:1433;database=${AZURE_SQL_DATABASE}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    username: ${AZURE_SQL_USERNAME}
    password: ${AZURE_SQL_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2012Dialect
```

### 8.2 ClaimsBusinessConfig

```java
@Configuration
@ConfigurationProperties(prefix = "claims")
public class ClaimsBusinessConfig {

    private AdjudicationConfig adjudication;
    private Map<String, Integer> coinsurance;
    private Map<String, BigDecimal> copay;

    @Data
    public static class AdjudicationConfig {
        private BigDecimal minClaimAmount;
        private BigDecimal maxAutoApprove;
        private BigDecimal manualReviewLimit;
        private BigDecimal dentalMax;
        private BigDecimal visionMax;
    }

    // Getters, setters
}
```

---

## 9. Error Handling Strategy

### 9.1 Exception Hierarchy

```
ClaimsProcessingException (base, RuntimeException)
├── FileProcessingException          # COBOL error codes 10, 11 (FATAL)
├── PolicyNotFoundException          # COBOL error code 20 (ERROR)
├── InvalidClaimException            # COBOL error code 30 (WARNING)
└── CalculationOverflowException     # COBOL error code 40 (ERROR)
```

### 9.2 COBOL Error Code → Exception Mapping

| COBOL Code | Severity | Java Exception | HTTP Status | Behavior |
|---|---|---|---|---|
| 10 | FATAL | `FileProcessingException` | 500 | App startup failure (data load) |
| 11 | FATAL | `FileProcessingException` | 500 | App startup failure (data load) |
| 20 | ERROR | `PolicyNotFoundException` | 404 | Deny claim, continue processing |
| 30 | WARNING | `InvalidClaimException` | 400 | Reject request with validation detail |
| 40 | ERROR | `CalculationOverflowException` | 500 | Log and fail current claim |
| 99 | ERROR | `ClaimsProcessingException` | 500 | Generic fallback |

### 9.3 GlobalExceptionHandler

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePolicyNotFound(PolicyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("POLICY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InvalidClaimException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClaim(InvalidClaimException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_CLAIM", ex.getMessage()));
    }

    // Additional handlers...
}
```

### 9.4 Logging Strategy

- **Framework:** SLF4J with Logback (Spring Boot default)
- **Replaces:** COBOL DISPLAY-based console output and ERRHANDL timestamped messages
- **Log levels:**
  - `ERROR` — FATAL/ERROR codes (10, 11, 20, 40, 99)
  - `WARN` — WARNING codes (30) and business rule denials
  - `INFO` — Claim processing lifecycle events (received, approved, denied, pending)
  - `DEBUG` — Detailed calculation steps (deductible, copay, coinsurance values)

---

## 10. Testing Strategy

### 10.1 Test Pyramid

```
         ╱╲
        ╱  ╲         Integration Tests (5-10%)
       ╱    ╲        - Full pipeline: claim → adjudicate → pay → report
      ╱──────╲       - @SpringBootTest with H2
     ╱        ╲
    ╱          ╲     API Tests (15-20%)
   ╱            ╲    - @WebMvcTest per controller
  ╱──────────────╲   - MockMvc, mock services
 ╱                ╲
╱                  ╲  Unit Tests (70-80%)
╱────────────────────╲ - Service logic with Mockito
                       - Every business rule, edge case, calculation
```

### 10.2 Unit Tests (JUnit 5 + Mockito)

#### AdjudicationServiceTest — Rule-by-Rule Coverage

Each COBOL adjudication rule from Step 02 maps to one or more test methods:

| Test Method | Rule ID | Validates |
|---|---|---|
| `shouldDenyWhenPolicyInactive()` | ADJ-101 | `POL-STATUS ≠ 'A'` → DENY |
| `shouldDenyWhenClaimBeforeEffectiveDate()` | ADJ-102 | `CLM-CLAIM-DATE < POL-EFFECTIVE-DATE` → DENY |
| `shouldDenyWhenClaimAfterExpiryDate()` | ADJ-103 | `CLM-CLAIM-DATE > POL-EXPIRY-DATE` → DENY |
| `shouldDenyWhenAmountBelowMinimum()` | ADJ-201 | `amount < $50.00` → DENY |
| `shouldPassWhenAmountExactlyMinimum()` | ADJ-201 | `amount == $50.00` → passes (strict `<`) |
| `shouldPendWhenAmountAboveManualReviewLimit()` | ADJ-202 | `amount > $25,000` → PENDING |
| `shouldPassWhenAmountExactlyManualReviewLimit()` | ADJ-202 | `amount == $25,000` → passes (strict `>`) |
| `shouldDenyMedicalWithoutDiagnosisCode()` | ADJ-301 | Missing/zero diagnosis → DENY |
| `shouldDenyMedicalWithoutProviderId()` | ADJ-302 | Missing provider ID → DENY |
| `shouldDenyDentalOverCapNonPremium()` | ADJ-311 | Dental > $2,500 + non-PR → DENY |
| `shouldApproveDentalOverCapPremium()` | ADJ-311 | Dental > $2,500 + PR plan → passes |
| `shouldPassDentalExactlyAtCap()` | ADJ-311 | Dental == $2,500 → passes (strict `>`) |
| `shouldDenyVisionOverCap()` | ADJ-321 | Vision > $500 → DENY |
| `shouldPassVisionExactlyAtCap()` | ADJ-321 | Vision == $500 → passes (strict `>`) |
| `shouldDenyPharmacyWithoutProviderId()` | ADJ-331 | Missing provider → DENY |
| `shouldDenyUnknownClaimType()` | ADJ-341 | Invalid type code → DENY |
| `shouldDenyWhenCoverageNegative()` | ADJ-402 | `claim - deductible ≤ 0` → DENY |
| `shouldPendWhenCoverageExceedsMax()` | ADJ-403 | `coverage > max_coverage` → PENDING |
| `shouldApproveWhenUnderAutoApproveThreshold()` | ADJ-404 | `amount ≤ $5,000` → APPROVE |
| `shouldApproveWhenExactlyAutoApproveThreshold()` | ADJ-404 | `amount == $5,000` → APPROVE (`<=`) |
| `shouldPendWhenOverAutoApproveThreshold()` | ADJ-405 | `amount > $5,000` → PENDING |

#### PaymentAuthorizationServiceTest — Calculation Coverage

| Test Method | Validates |
|---|---|
| `shouldCalculateBasicPlanPayment()` | Full pipeline with 80% coinsurance |
| `shouldCalculateSilverPlanPayment()` | Full pipeline with 70% coinsurance |
| `shouldCalculatePremiumPlanPayment()` | Full pipeline with 90% coinsurance |
| `shouldCalculateBronzePlanPayment()` | Full pipeline with 60% coinsurance |
| `shouldDefaultToBasicForUnknownPlan()` | Unknown plan → 80% default |
| `shouldApplyCorrectCopayPerClaimType()` | Medical=$25, Dental=$15, Vision=$10, Pharmacy=$10 |
| `shouldCapDeductibleAtClaimAmount()` | `MIN(gross, deductible)` when deductible > claim |
| `shouldFloorCoinsuranceAtZero()` | Negative coinsurance → $0.00 |
| `shouldCapPaymentAtMaxCoverage()` | `net > max_coverage` → capped |
| `shouldCapPaymentAtClaimAmount()` | `net > claim_amount` → capped |
| `shouldFloorPaymentAtZero()` | `net < 0` → $0.00 |
| `shouldUseHalfEvenRounding()` | Verify `RoundingMode.HALF_EVEN` (banker's rounding) |
| `shouldGenerateSequentialAuthCodes()` | Auth codes: A00001, A00002, ... |
| `shouldMatchWorkedExample()` | Step 02 §6.7: Medical/Silver $3,000 → $1,732.50 |

#### PolicyLookupServiceTest

| Test Method | Validates |
|---|---|
| `shouldReturnPolicyWhenFound()` | Existing policy → Optional.of(policy) |
| `shouldReturnEmptyWhenNotFound()` | Missing policy → Optional.empty() |

#### ClaimsProcessingServiceTest

| Test Method | Validates |
|---|---|
| `shouldDenyWhenPolicyNotFound()` | Missing policy → deny + error logged |
| `shouldProcessApprovedClaimWithPayment()` | Full happy path: approve + pay |
| `shouldProcessDeniedClaimWithoutPayment()` | Denied → no payment call |
| `shouldProcessPendingClaimWithoutPayment()` | Pending → no payment call |
| `shouldSkipZeroPayment()` | Payment = $0 → not added to totals |
| `shouldProcessBatchAndReturnSummary()` | Multiple claims → correct counters |

### 10.3 API Tests (@WebMvcTest)

```java
@WebMvcTest(ClaimsController.class)
class ClaimsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimsProcessingService claimsProcessingService;

    @Test
    void processClaimShouldReturn200WhenApproved();

    @Test
    void processClaimShouldReturn404WhenClaimNotFound();

    @Test
    void processClaimShouldReturn404WhenPolicyNotFound();

    @Test
    void createClaimShouldReturn201WithValidPayload();

    @Test
    void createClaimShouldReturn400WithInvalidPayload();

    @Test
    void processAllShouldReturn200WithSummary();
}
```

### 10.4 Repository Tests (@DataJpaTest)

```java
@DataJpaTest
class PolicyRepositoryTest {

    @Autowired
    private PolicyRepository policyRepository;

    @Test
    void shouldFindPolicyByNumber();

    @Test
    void shouldReturnEmptyForNonexistentPolicy();
}

@DataJpaTest
class ClaimRepositoryTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Test
    void shouldPersistAndRetrieveClaim();
}
```

### 10.5 Integration Tests (@SpringBootTest)

```java
@SpringBootTest
@AutoConfigureMockMvc
class ClaimsProcessingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * End-to-end test replicating the full COBOL batch run.
     * Loads all 20 claims from seed data, processes them,
     * and verifies counters match COBOL output.
     */
    @Test
    void shouldReplicateCobolBatchRunResults();

    /**
     * Verify that CLM0000016 (referencing POL9999999)
     * is correctly denied due to policy not found.
     */
    @Test
    void shouldDenyClaimWithNonexistentPolicy();

    /**
     * Verify that CLM0000019 (referencing POL1000021, inactive)
     * is correctly denied due to inactive policy.
     */
    @Test
    void shouldDenyClaimWithInactivePolicy();
}
```

### 10.6 Test Data Strategy

- **Seed data:** Convert `data/claims.dat` (20 records) and `data/policies.dat` (22 records) to `data.sql` INSERT statements
- **Known test scenarios from COBOL data:**
  - CLM0000016 → POL9999999 (nonexistent) → **DENY** (policy not found)
  - CLM0000019 → POL1000021 (inactive, expired) → **DENY** (inactive policy)
  - Multiple claim types and plan types for coverage matrix testing
- **Parity validation:** Run COBOL system with `make run`, capture output, compare claim-by-claim with Java results

---

## 11. Migration & Data Loading

### 11.1 Data Conversion

| Source File | Target Table | Records | Conversion Notes |
|---|---|---|---|
| `data/claims.dat` | `claims` | 20 | Parse fixed-width (51 bytes); convert PIC 9(8) dates to `LocalDate`; convert implied decimal to `BigDecimal` |
| `data/policies.dat` | `policies` | 22 | Parse fixed-width (75 bytes); same date/decimal conversions |
| `data/partners.dat` | *(not migrated)* | 5 | Not referenced by any COBOL program; exclude from initial migration |

### 11.2 Seed Data Script (data.sql)

The `data.sql` file will be auto-loaded by Spring Boot on startup (dev profile). Each record from the flat files is converted to an INSERT statement with proper type conversions:

- `YYYYMMDD` → `'YYYY-MM-DD'` (SQL DATE format)
- `PIC 9(7)V99` implied decimal → explicit decimal (e.g., `001500000` → `15000.00`)
- Claim type codes → enum values (`01` → `MEDICAL`)
- Plan type codes → enum values (`BS` → `BASIC`)
- Status codes → enum values (`A` → `ACTIVE`)

### 11.3 Validation Approach

1. **Run COBOL system:** `make run` → capture `reports/summary.txt` output
2. **Run Java system:** Load same data via `data.sql` → call `POST /api/v1/claims/process-all`
3. **Compare:** Verify total counts (approved, denied, pending) and total payment amount match exactly
4. **Claim-by-claim:** Process each of the 20 claims individually and compare adjudication result + payment amount

---

## Appendix: Behavioral Parity Checklist

Critical behaviors from the COBOL system that **must** be preserved exactly in Java:

| # | Behavior | COBOL Source | Java Implementation |
|---|---|---|---|
| 1 | Claim amount exactly $50.00 passes minimum check | `<` not `<=` (ADJUDCTN:81) | `claimAmount.compareTo(minAmount) < 0` for deny |
| 2 | Claim amount exactly $5,000.00 is auto-approved | `<=` $5,000 (ADJUDCTN:152) | `claimAmount.compareTo(autoApprove) <= 0` for approve |
| 3 | Claim amount exactly $25,000.00 passes manual review | `>` not `>=` (ADJUDCTN:85) | `claimAmount.compareTo(manualLimit) > 0` for pending |
| 4 | Dental $2,500.00 exactly passes cap | `>` not `>=` (ADJUDCTN:119) | `claimAmount.compareTo(dentalMax) > 0` for deny |
| 5 | Vision $500.00 exactly passes cap | `>` not `>=` (ADJUDCTN:127) | `claimAmount.compareTo(visionMax) > 0` for deny |
| 6 | Deductible capped at claim amount | `FUNCTION MIN` (PYMTAUTH:97) | `grossAmount.min(deductible)` |
| 7 | Negative coinsurance floored at $0 | IF < 0 check (PYMTAUTH:123) | `BigDecimal.ZERO.max(coinsurance)` |
| 8 | Net payment capped at claim amount | IF > check (PYMTAUTH:136) | `netPayment.min(claimAmount)` |
| 9 | Net payment capped at max coverage | IF > check (PYMTAUTH:131) | `netPayment.min(maxCoverage)` |
| 10 | COBOL ROUNDED on coinsurance | ROUNDED keyword (PYMTAUTH:120) | `RoundingMode.HALF_EVEN` |
| 11 | Unknown plan type → 80% default | OTHER clause (PYMTAUTH:80) | Default case in switch/map |
| 12 | Unknown claim type → copay $0 | No match → 0 (PYMTAUTH:115) | Default case returns BigDecimal.ZERO |
| 13 | Policy not found → deny (not error) | CLMPROC:140–145 | Catch `PolicyNotFoundException`, set DENIED status |
| 14 | Only approved claims go to payment | IF APPROVED guard (CLMPROC:155) | Conditional call in orchestrator |
| 15 | Zero payment not counted in totals | IF > 0 guard (CLMPROC:165) | `if (payment.compareTo(BigDecimal.ZERO) > 0)` |
| 16 | Report percentages use integer truncation | PIC 9(3) division (RPTGEN) | `(int)(approved * 100 / total)` — truncate, not round |
| 17 | Auth code = "A" + sequential counter | WS counter (PYMTAUTH:158) | `AtomicLong` or DB sequence with "A" prefix |

---

*End of Step 03 — Modernization Specification for Continental Insurance Group Claims Processing System*
