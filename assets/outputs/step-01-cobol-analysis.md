# Step 01 — COBOL Legacy System Analysis

## Continental Insurance Group — Claims Processing System

**Analysis Date:** 2026-04-16
**Analyst:** Copilot (Automated Legacy Analysis)
**Purpose:** Comprehensive inventory and dependency mapping for COBOL-to-Java modernization

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Program Inventory](#2-program-inventory)
3. [Copybook / Data Record Layouts](#3-copybook--data-record-layouts)
4. [File I/O Patterns](#4-file-io-patterns)
5. [Subprogram Call Chains](#5-subprogram-call-chains)
6. [Batch Job Orchestration Flow](#6-batch-job-orchestration-flow)
7. [Dependency Map](#7-dependency-map)
8. [Data File Analysis](#8-data-file-analysis)
9. [Build & Deployment Infrastructure](#9-build--deployment-infrastructure)
10. [Modernization Observations](#10-modernization-observations)

---

## 1. Executive Summary

The Continental Insurance Group Claims Processing System is a **batch-oriented COBOL-85 application** compiled with GnuCOBOL. It reads fixed-width flat files (claims and policies), adjudicates claims against business rules, authorizes payments, and produces summary reports. The system is containerized via Docker and orchestrated through shell-based JCL job scripts.

**Key Metrics:**

| Metric | Value |
|---|---|
| Total COBOL Programs | 6 |
| Total Copybooks | 4 |
| Total COBOL LOC | ~653 |
| Batch Job Scripts | 3 |
| Data Files | 3 |
| Main Entry Point | CLMPROC |

---

## 2. Program Inventory

### 2.1 Program Summary Table

| Program ID | File | LOC | Type | Purpose |
|---|---|---|---|---|
| **CLMPROC** | `src/cobol/CLMPROC.cbl` | 191 | Main (executable) | Main claims processing orchestrator — reads claims/policies, drives adjudication, payment, and reporting |
| **ADJUDCTN** | `src/cobol/ADJUDCTN.cbl` | 172 | Subprogram (module) | Adjudication rules engine — applies business rules to approve/deny/pend claims |
| **POLYLKUP** | `src/cobol/POLYLKUP.cbl` | 81 | Subprogram (module) | Policy lookup — sequential search of policy file to find matching policy |
| **PYMTAUTH** | `src/cobol/PYMTAUTH.cbl` | 167 | Subprogram (module) | Payment authorization — calculates net payment with deductibles, copays, coinsurance |
| **RPTGEN** | `src/cobol/RPTGEN.cbl` | 163 | Subprogram (module) | Report generation — produces formatted 132-column summary report |
| **ERRHANDL** | `src/cobol/ERRHANDL.cbl` | 90 | Subprogram (module) | Centralized error handler — formats and displays timestamped error messages |

### 2.2 Program Details

#### CLMPROC (Main Orchestrator)

- **Division Structure:** IDENTIFICATION, ENVIRONMENT, DATA (FILE SECTION, WORKING-STORAGE), PROCEDURE
- **File I/O:** Opens `claims.dat` (INPUT), `policies.dat` (INPUT), `summary.txt` (OUTPUT)
- **Control Flow:** Sequential — init → read claims loop → generate summary → cleanup
- **Key Paragraphs:**
  - `0000-MAIN-PROCESSING` — top-level driver
  - `1000-INITIALIZATION` — opens files, accepts system date/time
  - `2000-PROCESS-CLAIMS` — read loop until EOF
  - `2100-PROCESS-SINGLE-CLAIM` — per-claim: lookup → adjudicate → (pay if approved)
  - `2200-AUTHORIZE-PAYMENT` — delegates to PYMTAUTH
  - `3000-GENERATE-SUMMARY` — delegates to RPTGEN
  - `9000-CLEANUP` — closes files, displays totals
- **Counters Maintained:** claims-read, claims-approved, claims-denied, claims-pending, total-paid

#### ADJUDCTN (Adjudication Rules Engine)

- **Division Structure:** IDENTIFICATION, DATA (WORKING-STORAGE, LINKAGE), PROCEDURE
- **Interface:** Receives CLAIM-RECORD, POLICY-RECORD; returns RESULT ('A'=Approved, 'D'=Denied, 'P'=Pending)
- **Anti-patterns:** Heavy use of GO TO for control flow (8 GO TO statements)
- **Hardcoded Business Constants:**
  - Max claim age: 90 days
  - Min claim amount: $50.00
  - Auto-approve threshold: $5,000.00
  - Dental maximum: $2,500.00
  - Vision maximum: $500.00
  - Manual review limit: $25,000.00
- **Claim Type Rules:**
  - `01` Medical — requires diagnosis code and provider ID
  - `02` Dental — capped at $2,500 (except Premium plans)
  - `03` Vision — capped at $500
  - `04` Pharmacy — requires provider ID
- **Coverage Logic:** claim_amount − deductible; auto-approve if ≤ $5,000; manual review if > max_coverage

#### POLYLKUP (Policy Lookup)

- **Division Structure:** IDENTIFICATION, ENVIRONMENT, DATA (FILE, WORKING-STORAGE, LINKAGE), PROCEDURE
- **Interface:** Receives policy number; returns policy record and found flag ('Y'/'N')
- **Search Strategy:** Linear sequential scan of `policies.dat` (max 1,000 reads)
- **Note:** Opens and closes `policies.dat` on each invocation — reopened per claim (performance concern)

#### PYMTAUTH (Payment Authorization)

- **Division Structure:** IDENTIFICATION, DATA (WORKING-STORAGE, LINKAGE), PROCEDURE
- **Interface:** Receives CLAIM-RECORD, POLICY-RECORD; returns PAYMENT-AMOUNT
- **Calculation Pipeline:** gross → deductible → copay → coinsurance → limits → net
- **Coinsurance Rates by Plan Type:**
  - BS (Basic): 80%
  - SV (Silver): 70%
  - PR (Premium): 90%
  - BR (Bronze): 60%
- **Copay Schedule:**
  - Medical: $25.00
  - Dental: $15.00
  - Vision: $10.00
  - Pharmacy: $10.00
- **Auth Code Generation:** Sequential counter prefixed with 'A' + current date

#### RPTGEN (Report Generator)

- **Division Structure:** IDENTIFICATION, DATA (WORKING-STORAGE, LINKAGE), PROCEDURE
- **Interface:** Receives totals (claims read/approved/denied/pending, total paid) and report file
- **Output:** 132-column formatted report with headers, separator lines, and percentages
- **Note:** Uses DISPLAY rather than WRITE to the passed file descriptor

#### ERRHANDL (Error Handler)

- **Division Structure:** IDENTIFICATION, DATA (WORKING-STORAGE, LINKAGE), PROCEDURE
- **Interface:** Receives error code (PIC 99) and error data (PIC X(20))
- **Error Code Mapping:**
  - 10 → FATAL: Cannot open claims file
  - 11 → FATAL: Cannot open policy file
  - 20 → ERROR: Policy not found for claim
  - 30 → WARNING: Invalid claim data
  - Other → ERROR: Unknown error code
- **Output:** Timestamped error message to DISPLAY (console)

---

## 3. Copybook / Data Record Layouts

### 3.1 CLMREC.cpy — Claim Record

**File:** `src/copybooks/CLMREC.cpy`
**Total Record Length:** 51 bytes

| Field | PIC Clause | Offset | Length | Description |
|---|---|---|---|---|
| CLM-CLAIM-NUMBER | X(10) | 1 | 10 | Unique claim identifier |
| CLM-POLICY-NUMBER | X(10) | 11 | 10 | Foreign key to policy |
| CLM-CLAIM-DATE | 9(8) | 21 | 8 | Claim date (YYYYMMDD) |
| CLM-CLAIM-TYPE | X(2) | 29 | 2 | 01=Medical, 02=Dental, 03=Vision, 04=Pharmacy |
| CLM-CLAIM-AMOUNT | 9(7)V99 | 31 | 9 | Claim dollar amount (implied decimal) |
| CLM-STATUS | X(1) | 40 | 1 | Claim status code |
| CLM-DIAGNOSIS-CODE | X(5) | 41 | 5 | Medical diagnosis code |
| CLM-PROVIDER-ID | X(8) | 46 | 8 | Healthcare provider ID |

### 3.2 POLREC.cpy — Policy Record

**File:** `src/copybooks/POLREC.cpy`
**Total Record Length:** 82 bytes

| Field | PIC Clause | Offset | Length | Description |
|---|---|---|---|---|
| POL-POLICY-NUMBER | X(10) | 1 | 10 | Unique policy identifier |
| POL-HOLDER-NAME | X(30) | 11 | 30 | Policyholder full name |
| POL-PLAN-TYPE | X(2) | 41 | 2 | BS=Basic, SV=Silver, PR=Premium, BR=Bronze |
| POL-EFFECTIVE-DATE | 9(8) | 43 | 8 | Policy start date (YYYYMMDD) |
| POL-EXPIRY-DATE | 9(8) | 51 | 8 | Policy end date (YYYYMMDD) |
| POL-DEDUCTIBLE | 9(5)V99 | 59 | 7 | Annual deductible (implied decimal) |
| POL-MAX-COVERAGE | 9(7)V99 | 66 | 9 | Maximum coverage amount (implied decimal) |
| POL-STATUS | X(1) | 75 | 1 | A=Active, I=Inactive |

### 3.3 PYMTREC.cpy — Payment Record

**File:** `src/copybooks/PYMTREC.cpy`
**Total Record Length:** 34 bytes

| Field | PIC Clause | Offset | Length | Description |
|---|---|---|---|---|
| PYMT-CLAIM-NUMBER | X(10) | 1 | 10 | Reference to claim |
| PYMT-PAYMENT-AMOUNT | 9(7)V99 | 11 | 9 | Authorized payment amount |
| PYMT-PAYMENT-DATE | 9(8) | 20 | 8 | Payment authorization date |
| PYMT-AUTH-CODE | X(6) | 28 | 6 | Authorization code |
| PYMT-STATUS | X(1) | 34 | 1 | A=Authorized |

### 3.4 ERRCODES.cpy — Error Code Definitions

**File:** `src/copybooks/ERRCODES.cpy`

| Constant | Value | Description |
|---|---|---|
| ERR-FILE-OPEN | 10 | File open failure |
| ERR-FILE-READ | 11 | File read failure |
| ERR-POLICY-NOT-FOUND | 20 | Policy lookup miss |
| ERR-INVALID-CLAIM | 30 | Invalid claim data |
| ERR-CALC-OVERFLOW | 40 | Calculation overflow |
| ERR-UNKNOWN | 99 | Unclassified error |

---

## 4. File I/O Patterns

### 4.1 File Access Summary

| Logical Name | Physical File | Organization | Mode | Accessed By |
|---|---|---|---|---|
| CLAIM-FILE | `data/claims.dat` | LINE SEQUENTIAL | INPUT | CLMPROC |
| POLICY-FILE | `data/policies.dat` | LINE SEQUENTIAL | INPUT | CLMPROC (open), POLYLKUP (independent open) |
| POLICY-MASTER | `data/policies.dat` | LINE SEQUENTIAL | INPUT | POLYLKUP |
| SUMMARY-REPORT | `reports/summary.txt` | LINE SEQUENTIAL | OUTPUT | CLMPROC |
| (Error log) | `reports/error.log` | *(defined but not opened)* | — | ERRHANDL (DISPLAY only) |

### 4.2 I/O Flow

```
claims.dat ──[READ]──> CLMPROC ──[WRITE]──> reports/summary.txt
                            │
                            │ (per claim)
                            ▼
                       POLYLKUP ──[READ]──> policies.dat
                            │                (opens/closes each call)
                            ▼
policies.dat ──[READ]──> CLMPROC (also opened at init)
```

### 4.3 I/O Characteristics

- **All files are LINE SEQUENTIAL** (text-mode, newline-delimited)
- **No indexed or relative file access** — simulates VSAM via sequential scan
- **Policy file is opened twice:** once by CLMPROC (at init, but record used via POLYLKUP result) and independently by POLYLKUP for each lookup
- **No transaction logging or audit trail** — results go to console DISPLAY and summary report only
- **ERRHANDL writes to DISPLAY** (stdout), not to the defined error log file path

---

## 5. Subprogram Call Chains

### 5.1 Static Call Graph

```
CLMPROC (main)
├── CALL 'ERRHANDL'          ← error: claims file open failure (code 10)
├── CALL 'ERRHANDL'          ← error: policy file open failure (code 11)
│
├── [per claim loop]
│   ├── CALL 'POLYLKUP'     ← policy lookup (policy#, record, found-flag)
│   │   └── (self-contained file I/O to policies.dat)
│   │
│   ├── CALL 'ERRHANDL'     ← error: policy not found (code 20)
│   │
│   ├── CALL 'ADJUDCTN'     ← adjudicate (claim-rec, policy-rec, result)
│   │   └── COPY ERRCODES   ← uses error code definitions (no CALL)
│   │
│   └── CALL 'PYMTAUTH'     ← authorize payment (claim-rec, policy-rec, amount)
│       └── COPY PYMTREC    ← builds payment record internally
│
└── CALL 'RPTGEN'            ← generate summary (counters, total-paid, report-file)
```

### 5.2 Call Interface Summary

| Caller | Callee | Parameters (BY CONTENT / BY REFERENCE) |
|---|---|---|
| CLMPROC | ERRHANDL | error-code (BY CONTENT), error-data (BY CONTENT) |
| CLMPROC | POLYLKUP | policy-number, policy-record, found-flag |
| CLMPROC | ADJUDCTN | claim-record, policy-record, result-code |
| CLMPROC | PYMTAUTH | claim-record, policy-record, payment-amount |
| CLMPROC | RPTGEN | claims-read, claims-approved, claims-denied, claims-pending, total-paid, report-file |

### 5.3 Copybook Usage Matrix

| Copybook | CLMPROC | ADJUDCTN | POLYLKUP | PYMTAUTH | RPTGEN | ERRHANDL |
|---|---|---|---|---|---|---|
| CLMREC | ✅ (FD) | ✅ (LINKAGE) | — | ✅ (LINKAGE) | — | — |
| POLREC | ✅ (FD) | ✅ (LINKAGE) | ✅ (FD + LINKAGE) | ✅ (LINKAGE) | — | — |
| PYMTREC | — | — | — | ✅ (WS) | — | — |
| ERRCODES | — | ✅ (WS) | — | — | — | — |

---

## 6. Batch Job Orchestration Flow

### 6.1 Job Inventory

| Job Script | File | Purpose | Schedule |
|---|---|---|---|
| NIGHTRUN | `src/jcl/NIGHTRUN.sh` | Nightly batch claims processing | Daily |
| MONTHEND | `src/jcl/MONTHEND.sh` | Month-end reconciliation with backup | Monthly |
| RPTJOB | `src/jcl/RPTJOB.sh` | On-demand report generation | Ad-hoc |

### 6.2 NIGHTRUN.sh — Nightly Batch

```
Step 1: COMPILE ─── make all (conditional — only if binaries missing)
         │
Step 2: EXECUTE ─── ./bin/CLMPROC (RC check: 0=success, else abort)
         │
Step 3: ARCHIVE ─── cp summary.txt → summary_YYYYMMDD_HHMMSS.txt
```

- **Error handling:** Exits with RC=8 on compile failure; propagates CLMPROC RC on process failure
- **Conditional compile:** Checks for all 6 binaries before deciding to compile

### 6.3 MONTHEND.sh — Month-End Processing

```
Step 1: BACKUP  ─── mkdir data/backup_YYYYMM; cp claims.dat, policies.dat
         │
Step 2: COMPILE ─── make all (unconditional — always recompiles)
         │
Step 3: EXECUTE ─── ./bin/CLMPROC (RC check)
         │
Step 4: REPORT  ─── cp summary.txt → monthend_YYYYMM.txt; cat to stdout
         │
Step 5: CLEANUP ─── (placeholder — no actual cleanup logic)
```

- **Key difference from NIGHTRUN:** Always recompiles; backs up data files; archives with month-level granularity

### 6.4 RPTJOB.sh — Report Generation

```
Step 1: VERIFY  ─── Check bin/CLMPROC exists; compile if missing
         │
Step 2: EXECUTE ─── ./bin/CLMPROC (full processing to generate report)
         │
Step 3: DISPLAY ─── cat reports/summary.txt
```

- **Note:** Runs full claims processing just to generate a report (no standalone report capability)
- **Error:** Exits RC=12 if no report file generated

### 6.5 Environment Variables (Common)

```
COBPATH=./src/copybooks
COB_LIBRARY_PATH=./bin
```

---

## 7. Dependency Map

### 7.1 Complete Dependency Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        BATCH JOBS (Shell/JCL)                       │
│                                                                     │
│   NIGHTRUN.sh ──┐                                                   │
│   MONTHEND.sh ──┼──> make all ──> cobc (GnuCOBOL Compiler)        │
│   RPTJOB.sh  ──┘         │                                         │
│                           ▼                                         │
│                      ┌─────────┐                                    │
│                      │ CLMPROC │ (main executable)                  │
│                      └────┬────┘                                    │
│                           │                                         │
│              ┌────────────┼────────────┬──────────┐                 │
│              ▼            ▼            ▼          ▼                 │
│         ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│         │POLYLKUP │ │ADJUDCTN │ │PYMTAUTH │ │ RPTGEN  │           │
│         │ (.so)   │ │ (.so)   │ │ (.so)   │ │ (.so)   │           │
│         └─────────┘ └─────────┘ └─────────┘ └─────────┘           │
│              │                       │                              │
│              │            ┌──────────┘                              │
│              │            ▼                                         │
│              │       ┌─────────┐                                    │
│              │       │ERRHANDL │ (.so) ← called by CLMPROC too     │
│              │       └─────────┘                                    │
│              │                                                      │
└──────────────┼──────────────────────────────────────────────────────┘
               │
┌──────────────┼──────────────────────────────────────────────────────┐
│              ▼         DATA LAYER                                    │
│                                                                     │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐         │
│   │ claims.dat   │    │ policies.dat │    │ partners.dat │         │
│   │ (20 records) │    │ (22 records) │    │ (5 records)  │         │
│   └──────────────┘    └──────────────┘    └──────────────┘         │
│         │                    │                    │                  │
│    Read by CLMPROC     Read by CLMPROC      NOT USED by            │
│                        Read by POLYLKUP     any program             │
│                                                                     │
│   ┌──────────────┐                                                  │
│   │ summary.txt  │  ← Written by CLMPROC (via RPTGEN)             │
│   │ error.log    │  ← Defined but not file-written (DISPLAY only)  │
│   └──────────────┘                                                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         COPYBOOKS                                    │
│                                                                     │
│   CLMREC.cpy ──> CLMPROC, ADJUDCTN, PYMTAUTH                     │
│   POLREC.cpy ──> CLMPROC, ADJUDCTN, POLYLKUP, PYMTAUTH           │
│   PYMTREC.cpy ──> PYMTAUTH                                         │
│   ERRCODES.cpy ──> ADJUDCTN                                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.2 Dependency Matrix (Program × Resource)

| Resource | CLMPROC | ADJUDCTN | POLYLKUP | PYMTAUTH | RPTGEN | ERRHANDL |
|---|---|---|---|---|---|---|
| claims.dat | READ | — | — | — | — | — |
| policies.dat | READ | — | READ | — | — | — |
| summary.txt | WRITE | — | — | — | — | — |
| CLMREC.cpy | COPY | COPY | — | COPY | — | — |
| POLREC.cpy | COPY | COPY | COPY | COPY | — | — |
| PYMTREC.cpy | — | — | — | COPY | — | — |
| ERRCODES.cpy | — | COPY | — | — | — | — |
| POLYLKUP | CALL | — | — | — | — | — |
| ADJUDCTN | CALL | — | — | — | — | — |
| PYMTAUTH | CALL | — | — | — | — | — |
| RPTGEN | CALL | — | — | — | — | — |
| ERRHANDL | CALL | — | — | — | — | — |

---

## 8. Data File Analysis

### 8.1 claims.dat

- **Record count:** 20
- **Format:** Fixed-width, matches CLMREC layout (51 bytes + newline)
- **Claim IDs:** CLM0000001 through CLM0000020
- **Policy references:** POL1000001–POL1000021 (POL9999999 for claim 16 — will trigger "policy not found")
- **Claim types present:** 01 (Medical ×11), 02 (Dental ×5), 03 (Vision ×3), 04 (Pharmacy ×1)
- **Notable test cases:**
  - CLM0000016 references POL9999999 (nonexistent policy — tests error path)
  - CLM0000019 references POL1000021 (inactive policy with status 'I' and expired dates — tests adjudication denial)
  - CLM0000006: $4,500.00 claim type 01 with diagnosis 'D' and provider PROV0001
  - CLM0000015: $3,200.00 claim — high amount medical

### 8.2 policies.dat

- **Record count:** 22
- **Format:** Fixed-width, matches POLREC layout (75 bytes + newline)
- **Policy IDs:** POL1000001 through POL1000022
- **Plan types:** BS (Basic ×7), SV (Silver ×6), PR (Premium ×5), BR (Bronze ×2)
- **Notable records:**
  - POL1000021: Status='I' (Inactive), effective 2020-05-01 to 2021-05-01 (expired) — tests inactive policy logic
  - POL1000006: Status='A' but expires 2024-12-31 — may be expired depending on run date
  - All other policies expire in 2025–2026

### 8.3 partners.dat

- **Record count:** 5
- **Format:** Pipe-delimited (`|` separator) — differs from other files
- **Fields:** Partner ID, Name, Status, Date
- **Not referenced by any COBOL program** — appears to be future/unused reference data
- **Notable:** PARTNER004 (CIGNA NETWORK) is INACTIVE

---

## 9. Build & Deployment Infrastructure

### 9.1 Makefile

- **Compiler:** `cobc` (GnuCOBOL)
- **Main program flags:** `-x -std=mf -I./src/copybooks -fixed` (executable, Micro Focus compatibility, fixed-format)
- **Subprogram flags:** `-m -std=mf -I./src/copybooks -fixed` (shared module .so)
- **Build order:** subprograms (.so) first → then main (CLMPROC)
- **Targets:** `all`, `run`, `nightrun`, `monthend`, `report`, `clean`, `distclean`, `help`

### 9.2 Dockerfile

- **Base image:** ubuntu:22.04
- **Packages:** gnucobol, make, bash, coreutils
- **Build stage:** Compiles all programs at image build time (`make all`)
- **Default CMD:** `make run`
- **Working directory:** `/app`

### 9.3 docker-compose.yml

- **Services:**
  - `cobol-app` — interactive claims processing (default: `make run`)
  - `cobol-batch` — batch processing (profile: `batch`, command: `make nightrun`)
- **Volumes:** data/, reports/, src/ mounted for persistence
- **Resource limits:** 1 CPU / 512MB (app), 2 CPU / 1GB (batch)
- **Timezone:** America/New_York

---

## 10. Modernization Observations

### 10.1 Complexity & Risk Assessment

| Area | Complexity | Risk | Notes |
|---|---|---|---|
| Data structures | Low | Low | Simple fixed-width records; direct mapping to Java POJOs/records |
| Business rules (ADJUDCTN) | Medium | **High** | Heavy GO TO usage; hardcoded constants; interleaved validation and branching — needs careful rule extraction |
| Payment calculation (PYMTAUTH) | Medium | Medium | Multi-step arithmetic with rounding; plan-type/claim-type matrices |
| File I/O | Low | Low | Sequential flat files → easily replaced with DB/file readers |
| Policy lookup (POLYLKUP) | Low | Low | Linear scan → database query or indexed lookup |
| Report generation (RPTGEN) | Low | Low | Fixed-format DISPLAY → templating engine or PDF |
| Error handling (ERRHANDL) | Low | Low | Simple code-to-message mapping → Java exceptions/logging |
| Batch orchestration | Low | Medium | Shell scripts → Spring Batch or scheduler |

### 10.2 Key Modernization Concerns

1. **GO TO Anti-Pattern (ADJUDCTN):** 8 GO TO statements create non-linear control flow. The adjudication logic must be carefully refactored into structured conditionals or a rules engine.

2. **Hardcoded Business Rules:** Claim-type limits, coinsurance rates, copay amounts, and thresholds are all embedded as COBOL literals. These should be externalized to configuration or a rules table.

3. **Policy File Reopening:** POLYLKUP opens and closes `policies.dat` for every single claim lookup — O(n×m) sequential reads. Must be replaced with indexed/cached access.

4. **Implicit Decimal Handling:** COBOL PIC 9(7)V99 uses implied decimal points. Java BigDecimal should be used to preserve precision.

5. **Unused Data:** `partners.dat` is not referenced by any program — clarify if it's needed for the modernized system.

6. **Report Output Mismatch:** RPTGEN receives the report file descriptor but uses DISPLAY (console) instead of WRITE — the summary.txt file written by CLMPROC may be empty or unused.

7. **Error Log Not Written:** ERRHANDL defines `reports/error.log` path but never opens or writes to it — errors go to DISPLAY only.

8. **No Database Layer:** All persistence is flat-file based. The modernized system should introduce a relational database.

### 10.3 Suggested Java Module Mapping

| COBOL Program | Suggested Java Component |
|---|---|
| CLMPROC | `ClaimsProcessingService` (orchestrator / Spring Batch job) |
| ADJUDCTN | `AdjudicationService` + `AdjudicationRules` (strategy/rules engine) |
| POLYLKUP | `PolicyRepository` (JPA repository / database access) |
| PYMTAUTH | `PaymentAuthorizationService` + `PaymentCalculator` |
| RPTGEN | `ReportGenerationService` (Jasper/template-based) |
| ERRHANDL | Standard Java logging (SLF4J/Logback) + custom exceptions |
| CLMREC.cpy | `Claim` entity/record class |
| POLREC.cpy | `Policy` entity/record class |
| PYMTREC.cpy | `PaymentAuthorization` entity/record class |
| ERRCODES.cpy | `ErrorCode` enum |

---

*End of Step 01 Analysis — Continental Insurance Group COBOL Legacy System*
