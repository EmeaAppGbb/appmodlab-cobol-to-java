# Step 02 — Business Rules Extraction

## Continental Insurance Group — Claims Processing System

**Extraction Date:** 2026-04-16
**Source Programs:** CLMPROC, ADJUDCTN, PYMTAUTH, POLYLKUP, RPTGEN, ERRHANDL
**Purpose:** Complete business rules specification for Java implementation

---

## Table of Contents

1. [Domain Glossary](#1-domain-glossary)
2. [Data Model](#2-data-model)
3. [Orchestration Flow](#3-orchestration-flow)
4. [Policy Lookup Rules](#4-policy-lookup-rules)
5. [Adjudication Rules](#5-adjudication-rules)
6. [Payment Calculation Rules](#6-payment-calculation-rules)
7. [Error Handling Rules](#7-error-handling-rules)
8. [Report Generation Rules](#8-report-generation-rules)
9. [Complete Decision Tree](#9-complete-decision-tree)
10. [Business Constants Summary](#10-business-constants-summary)

---

## 1. Domain Glossary

| Term | Definition |
|---|---|
| **Claim** | A request for insurance payment submitted by a policyholder |
| **Policy** | An insurance contract defining coverage terms, deductible, and plan type |
| **Adjudication** | The process of evaluating a claim against business rules to approve, deny, or pend it |
| **Coinsurance** | The percentage of an eligible claim amount paid by the insurer |
| **Copay** | A fixed dollar amount the policyholder pays per claim type |
| **Deductible** | The amount the policyholder must pay before insurance coverage applies |
| **Max Coverage** | The maximum dollar amount a policy will pay |
| **Auto-Approve** | Claims below a threshold approved without manual intervention |
| **Manual Review** | Claims flagged for human review (status = Pending) |
| **Authorization Code** | A unique code generated when a payment is authorized |

---

## 2. Data Model

### 2.1 Claim Record (CLMREC)

| Field | Type | Length | Domain / Constraints |
|---|---|---|---|
| CLM-CLAIM-NUMBER | Alphanumeric | 10 | Unique claim identifier |
| CLM-POLICY-NUMBER | Alphanumeric | 10 | FK → Policy |
| CLM-CLAIM-DATE | Numeric | 8 | Format: YYYYMMDD |
| CLM-CLAIM-TYPE | Alphanumeric | 2 | `01`=Medical, `02`=Dental, `03`=Vision, `04`=Pharmacy |
| CLM-CLAIM-AMOUNT | Decimal | 9(7)V99 | Range: 0.00–9999999.99 |
| CLM-STATUS | Alphanumeric | 1 | Claim processing status |
| CLM-DIAGNOSIS-CODE | Alphanumeric | 5 | Medical diagnosis code; `00000` or spaces = absent |
| CLM-PROVIDER-ID | Alphanumeric | 8 | Healthcare provider; spaces = absent |

### 2.2 Policy Record (POLREC)

| Field | Type | Length | Domain / Constraints |
|---|---|---|---|
| POL-POLICY-NUMBER | Alphanumeric | 10 | Unique policy identifier |
| POL-HOLDER-NAME | Alphanumeric | 30 | Policyholder full name |
| POL-PLAN-TYPE | Alphanumeric | 2 | `BS`=Basic, `SV`=Silver, `PR`=Premium, `BR`=Bronze |
| POL-EFFECTIVE-DATE | Numeric | 8 | Policy start date (YYYYMMDD) |
| POL-EXPIRY-DATE | Numeric | 8 | Policy end date (YYYYMMDD) |
| POL-DEDUCTIBLE | Decimal | 9(5)V99 | Annual deductible: 0.00–99999.99 |
| POL-MAX-COVERAGE | Decimal | 9(7)V99 | Max coverage: 0.00–9999999.99 |
| POL-STATUS | Alphanumeric | 1 | `A`=Active, `I`=Inactive |

### 2.3 Payment Record (PYMTREC)

| Field | Type | Length | Domain / Constraints |
|---|---|---|---|
| PYMT-CLAIM-NUMBER | Alphanumeric | 10 | Reference to originating claim |
| PYMT-PAYMENT-AMOUNT | Decimal | 9(7)V99 | Authorized payment amount |
| PYMT-PAYMENT-DATE | Numeric | 8 | Authorization date (YYYYMMDD) |
| PYMT-AUTH-CODE | Alphanumeric | 6 | Format: `A` + sequential counter |
| PYMT-STATUS | Alphanumeric | 1 | `A`=Authorized |

### 2.4 Claim Types

| Code | Name | Special Requirements |
|---|---|---|
| `01` | Medical | Requires valid diagnosis code AND provider ID |
| `02` | Dental | Capped at $2,500 unless Premium plan |
| `03` | Vision | Capped at $500 (hard limit) |
| `04` | Pharmacy | Requires provider ID |
| Other | Invalid | Claim denied |

### 2.5 Plan Types

| Code | Name | Coinsurance Rate |
|---|---|---|
| `BS` | Basic | 80% |
| `SV` | Silver | 70% |
| `PR` | Premium | 90% |
| `BR` | Bronze | 60% |
| Other | Unknown | 80% (default) |

---

## 3. Orchestration Flow

> **Source:** `CLMPROC.cbl` — main batch processing orchestrator

### 3.1 High-Level Processing Pipeline

```
INITIALIZATION
    │
    ├── Open claims file (INPUT)       → fatal error 10 if fails
    ├── Open policy file (INPUT)       → fatal error 11 if fails
    ├── Open summary report (OUTPUT)
    └── Accept system date and time
    │
    ▼
FOR EACH CLAIM IN claims.dat:
    │
    ├── Step 1: POLICY LOOKUP (POLYLKUP)
    │       → If not found: log error 20, DENY claim, skip to next
    │
    ├── Step 2: ADJUDICATION (ADJUDCTN)
    │       → Returns: A (Approved), D (Denied), P (Pending)
    │
    ├── Step 3: RESULT ROUTING
    │       ├── Approved → proceed to PAYMENT AUTHORIZATION
    │       ├── Denied   → increment denied counter
    │       └── Pending  → increment pending counter
    │
    └── Step 4: PAYMENT AUTHORIZATION (PYMTAUTH) [Approved only]
            → Calculate net payment
            → If payment > 0: add to total paid
    │
    ▼
SUMMARY GENERATION (RPTGEN)
    │
    └── Format and output summary report with counts and totals
    │
    ▼
CLEANUP
    │
    ├── Close all files
    └── Display final totals to console
```

### 3.2 Counters Maintained

| Counter | Updated When |
|---|---|
| claims-read | Every claim read (before any processing) |
| claims-approved | Adjudication returns 'A' |
| claims-denied | Adjudication returns 'D' OR policy not found |
| claims-pending | Adjudication returns 'P' |
| total-paid | Payment amount > 0 after authorization |

### 3.3 Critical Orchestration Rules

1. **Policy-not-found is treated as denial** — increments denied counter, does NOT call adjudication
2. **Only approved claims proceed to payment** — denied and pending claims skip PYMTAUTH entirely
3. **Payment of $0 is not added to totals** — guard: `IF WS-PAYMENT-AMOUNT > 0`
4. **Processing is sequential** — one claim at a time, single-threaded batch
5. **Policy file is reopened per lookup** — POLYLKUP opens/closes `policies.dat` for each call

---

## 4. Policy Lookup Rules

> **Source:** `POLYLKUP.cbl`

### 4.1 Lookup Algorithm

| Rule ID | Rule | Source Line |
|---|---|---|
| PLK-01 | Search strategy is **linear sequential scan** of policies.dat | POLYLKUP:57–61 |
| PLK-02 | Match condition: `POL-POLICY-NUMBER = LS-POLICY-NUMBER` (exact match) | POLYLKUP:73–74 |
| PLK-03 | Maximum search depth: **1,000 records** — stops searching after 1,000 reads | POLYLKUP:29, 61 |
| PLK-04 | File is opened and closed on **every invocation** | POLYLKUP:42–44 |
| PLK-05 | If file cannot be opened: return found-flag = 'N' and GOBACK immediately | POLYLKUP:49–52 |
| PLK-06 | Returns complete policy record when found (full record copy) | POLYLKUP:75 |

---

## 5. Adjudication Rules

> **Source:** `ADJUDCTN.cbl` — the core business rules engine

### 5.1 Adjudication Result Codes

| Code | Meaning | Description |
|---|---|---|
| `A` | Approved | Claim passes all rules and is within auto-approve threshold |
| `D` | Denied | Claim fails one or more business rules |
| `P` | Pending | Claim requires manual review (over auto-approve but within limits) |

**Default result:** `D` (Denied) — claim starts denied and must pass all checks to be approved.

### 5.2 Rule Execution Order

The adjudication engine evaluates rules in a strict sequential order. **A claim that fails any step is immediately denied or pended — remaining rules are not evaluated.**

```
Step 1: VALIDATE POLICY STATUS
    │ FAIL → DENY (immediate exit)
    │
Step 2: CHECK CLAIM AMOUNT
    │ Below $50     → DENY (immediate exit)
    │ Above $25,000 → PENDING / manual review (immediate exit)
    │
Step 3: CHECK CLAIM TYPE RULES
    │ Type-specific validation (may DENY)
    │
Step 4: APPLY COVERAGE RULES
    │ coverage ≤ 0               → DENY
    │ coverage > max_coverage    → PENDING
    │ claim ≤ $5,000             → APPROVE
    │ claim > $5,000             → PENDING
```

### 5.3 Detailed Rule Catalog

#### Rule Group 1: Policy Status Validation

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-101 | `POL-STATUS ≠ 'A'` | **DENY** | ADJUDCTN:59–63 |
| ADJ-102 | `CLM-CLAIM-DATE < POL-EFFECTIVE-DATE` | **DENY** | ADJUDCTN:67–70 |
| ADJ-103 | `CLM-CLAIM-DATE > POL-EXPIRY-DATE` | **DENY** | ADJUDCTN:72–74 |

**Logic summary:** Policy must be Active (`A`), and the claim date must fall within the policy's effective date range (inclusive on both boundaries based on `<` and `>` comparisons).

#### Rule Group 2: Claim Amount Thresholds

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-201 | `CLM-CLAIM-AMOUNT < $50.00` | **DENY** | ADJUDCTN:81–83 |
| ADJ-202 | `CLM-CLAIM-AMOUNT > $25,000.00` | **PENDING** (manual review) | ADJUDCTN:85–87 |

**Logic summary:**
- Claims below the **minimum threshold ($50.00)** are rejected outright
- Claims above the **manual review limit ($25,000.00)** are flagged for human review
- Claims between $50.00 and $25,000.00 (inclusive) proceed to type-specific rules

#### Rule Group 3: Claim Type–Specific Rules

##### 3A — Medical Claims (Type `01`)

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-301 | `CLM-DIAGNOSIS-CODE = "00000"` OR `SPACES` | **DENY** | ADJUDCTN:108–110 |
| ADJ-302 | `CLM-PROVIDER-ID = SPACES` | **DENY** | ADJUDCTN:113–115 |

**Requirements:** Medical claims MUST have a valid (non-zero, non-blank) diagnosis code AND a provider ID.

##### 3B — Dental Claims (Type `02`)

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-311 | `CLM-CLAIM-AMOUNT > $2,500.00` AND `POL-PLAN-TYPE ≠ "PR"` | **DENY** | ADJUDCTN:119–123 |

**Requirements:** Dental claims exceeding **$2,500.00** are denied UNLESS the policyholder has a **Premium (`PR`)** plan. Premium plan holders have no dental cap applied at adjudication.

##### 3C — Vision Claims (Type `03`)

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-321 | `CLM-CLAIM-AMOUNT > $500.00` | **DENY** | ADJUDCTN:127–129 |

**Requirements:** Vision claims are hard-capped at **$500.00** regardless of plan type. No exceptions.

##### 3D — Pharmacy Claims (Type `04`)

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-331 | `CLM-PROVIDER-ID = SPACES` | **DENY** | ADJUDCTN:133–135 |

**Requirements:** Pharmacy claims MUST have a provider ID.

##### 3E — Unknown Claim Types

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-341 | `CLM-CLAIM-TYPE` not in (`01`,`02`,`03`,`04`) | **DENY** | ADJUDCTN:102–103 |

#### Rule Group 4: Coverage Calculation Rules

| Rule ID | Condition | Result | COBOL Source |
|---|---|---|---|
| ADJ-401 | `calculated_coverage = claim_amount − deductible` | (computation) | ADJUDCTN:139–141 |
| ADJ-402 | `calculated_coverage ≤ 0` | **DENY** | ADJUDCTN:143–145 |
| ADJ-403 | `calculated_coverage > POL-MAX-COVERAGE` | **PENDING** | ADJUDCTN:147–149 |
| ADJ-404 | `CLM-CLAIM-AMOUNT ≤ $5,000.00` | **APPROVE** | ADJUDCTN:152–154 |
| ADJ-405 | `CLM-CLAIM-AMOUNT > $5,000.00` | **PENDING** | ADJUDCTN:157 |

**Coverage calculation logic:**
```
calculated_coverage = CLM-CLAIM-AMOUNT − POL-DEDUCTIBLE

IF calculated_coverage ≤ 0       → DENY (deductible exceeds claim)
IF calculated_coverage > max_coverage → PENDING (manual review needed)
IF claim_amount ≤ $5,000         → APPROVE (auto-approve)
IF claim_amount > $5,000         → PENDING (manual review)
```

**Key insight:** The auto-approve threshold ($5,000) is checked against the **original claim amount**, not the calculated coverage amount.

---

## 6. Payment Calculation Rules

> **Source:** `PYMTAUTH.cbl` — invoked ONLY for approved claims

### 6.1 Payment Pipeline

```
INITIALIZE
    └── Zero out all working fields
         │
CALCULATE PAYMENT
    ├── Set gross_amount = claim_amount
    ├── Determine coinsurance_rate (by plan type)
    ├── Apply deductible
    ├── Apply copay (by claim type)
    ├── Calculate coinsurance
    └── Compute net payment
         │
APPLY LIMITS
    ├── Cap at policy max coverage
    ├── Cap at claim amount
    └── Floor at $0
         │
GENERATE AUTH CODE
    └── Build payment record
         │
FINALIZE
    └── Return net payment amount
```

### 6.2 Coinsurance Rates by Plan Type

| Plan Code | Plan Name | Coinsurance Rate | Insurer Pays | Policyholder Pays |
|---|---|---|---|---|
| `BS` | Basic | 80% | 80% of eligible amount | 20% |
| `SV` | Silver | 70% | 70% of eligible amount | 30% |
| `PR` | Premium | 90% | 90% of eligible amount | 10% |
| `BR` | Bronze | 60% | 60% of eligible amount | 40% |
| Other | Unknown | 80% (default) | 80% of eligible amount | 20% |

> **Source:** PYMTAUTH:69–80

### 6.3 Copay Schedule by Claim Type

| Claim Type | Code | Copay Amount |
|---|---|---|
| Medical | `01` | $25.00 |
| Dental | `02` | $15.00 |
| Vision | `03` | $10.00 |
| Pharmacy | `04` | $10.00 |
| Unknown | Other | $0.00 |

> **Source:** PYMTAUTH:21–24, 103–115

### 6.4 Step-by-Step Calculation Formula

#### Step 1: Set Gross Amount
```
gross_amount = CLM-CLAIM-AMOUNT
```

#### Step 2: Apply Deductible
```
IF POL-DEDUCTIBLE > 0:
    deductible_applied = MIN(gross_amount, POL-DEDUCTIBLE)
ELSE:
    deductible_applied = 0
```
> **Note:** Deductible cannot exceed the gross amount. Uses `FUNCTION MIN`.
> **Source:** PYMTAUTH:94–101

#### Step 3: Apply Copay
```
copay_amount = lookup copay by CLM-CLAIM-TYPE (see table 6.3)
```

#### Step 4: Calculate Coinsurance
```
coinsurance_amount = (gross_amount − deductible_applied − copay_amount) × (coinsurance_rate / 100)

IF coinsurance_amount < 0:
    coinsurance_amount = 0
```
> **Note:** Result is **ROUNDED** (COBOL `ROUNDED` keyword).
> **Source:** PYMTAUTH:118–125

#### Step 5: Compute Net Payment
```
net_payment = coinsurance_amount
```
> **Source:** PYMTAUTH:128–129

#### Step 6: Apply Limits
```
IF net_payment > POL-MAX-COVERAGE:
    net_payment = POL-MAX-COVERAGE

IF net_payment > CLM-CLAIM-AMOUNT:
    net_payment = CLM-CLAIM-AMOUNT

IF net_payment < 0:
    net_payment = 0
```
> **Source:** PYMTAUTH:131–145

### 6.5 Complete Payment Formula (Pseudocode)

```
net_payment = MIN(
    POL-MAX-COVERAGE,
    CLM-CLAIM-AMOUNT,
    MAX(0,
        ROUND(
            (CLM-CLAIM-AMOUNT − MIN(CLM-CLAIM-AMOUNT, POL-DEDUCTIBLE) − copay)
            × coinsurance_rate / 100
        )
    )
)
```

### 6.6 Authorization Code Generation

| Component | Value | Source |
|---|---|---|
| Claim Number | Copied from claim | PYMTAUTH:150 |
| Payment Amount | Net payment (post-limits) | PYMTAUTH:151 |
| Payment Date | Current system date (YYYYMMDD) | PYMTAUTH:153–156 |
| Auth Code | `"A"` + sequential counter (6 chars total) | PYMTAUTH:158–161 |
| Status | `"A"` (Authorized) | PYMTAUTH:163 |

> **Note:** The auth counter is a WORKING-STORAGE variable initialized to 0 and incremented per call within a single run. It resets between batch runs.

### 6.7 Worked Example

**Input:**
- Claim: Medical (`01`), amount = $3,000.00
- Policy: Silver (`SV`), deductible = $500.00, max coverage = $50,000.00

**Calculation:**
```
gross_amount         = $3,000.00
deductible_applied   = MIN($3,000.00, $500.00) = $500.00
copay_amount         = $25.00 (medical)
coinsurance_rate     = 70% (Silver)
coinsurance_amount   = ($3,000.00 − $500.00 − $25.00) × 0.70 = $2,475.00 × 0.70 = $1,732.50
net_payment          = $1,732.50
limits check         = MIN($50,000.00, $3,000.00, $1,732.50) = $1,732.50
final_payment        = $1,732.50
```

---

## 7. Error Handling Rules

> **Source:** `ERRHANDL.cbl`, `ERRCODES.cpy`

### 7.1 Error Code Catalog

| Code | Constant Name | Severity | Message Template | Triggered By |
|---|---|---|---|---|
| 10 | ERR-FILE-OPEN | **FATAL** | `"FATAL: Cannot open claims file - {file_status}"` | CLMPROC — claims file open failure |
| 11 | ERR-FILE-READ | **FATAL** | `"FATAL: Cannot open policy file - {file_status}"` | CLMPROC — policy file open failure |
| 20 | ERR-POLICY-NOT-FOUND | **ERROR** | `"ERROR: Policy not found for claim {claim_number}"` | CLMPROC — POLYLKUP returns not-found |
| 30 | ERR-INVALID-CLAIM | **WARNING** | `"WARNING: Invalid claim data - {error_data}"` | (Defined but not explicitly invoked in current code) |
| 40 | ERR-CALC-OVERFLOW | **ERROR** | *(Defined in ERRCODES.cpy, falls to OTHER handler)* | (Defined but not explicitly invoked in current code) |
| 99 | ERR-UNKNOWN | **ERROR** | *(Defined in ERRCODES.cpy, falls to OTHER handler)* | (Defined but not explicitly invoked in current code) |
| Other | — | **ERROR** | `"ERROR: Unknown error code {code} - {error_data}"` | Any unrecognized code |

### 7.2 Error Handling Behavior

| Rule ID | Rule |
|---|---|
| ERR-01 | All errors are formatted with a **timestamp** (YYYY-MM-DD HH:MM:SS) |
| ERR-02 | Errors are output via **DISPLAY** (stdout) — no file-based error logging |
| ERR-03 | FATAL errors (codes 10, 11) cause **immediate STOP RUN** in CLMPROC |
| ERR-04 | ERROR code 20 (policy not found) causes claim to be **denied** but processing continues |
| ERR-05 | Error handler is **stateless** — no error accumulation or count tracking |
| ERR-06 | Error data parameter is **20 characters max** (PIC X(20)) |
| ERR-07 | Codes 30, 40, 99 are **defined** in ERRCODES.cpy but **not actively called** in the current codebase |

### 7.3 Error Flow by Trigger Point

```
CLMPROC Initialization:
    Claims file open failure  → ERRHANDL(10, file_status) → STOP RUN
    Policy file open failure  → ERRHANDL(11, file_status) → STOP RUN

CLMPROC Per-Claim Processing:
    Policy not found          → ERRHANDL(20, claim_number) → Deny claim, continue
```

---

## 8. Report Generation Rules

> **Source:** `RPTGEN.cbl`

### 8.1 Report Content

| Element | Description |
|---|---|
| Header | "CONTINENTAL INSURANCE GROUP" + "CLAIMS PROCESSING SUMMARY REPORT" |
| Metadata | Page number, current date (MM/DD/YYYY format) |
| Total Claims Processed | Count of all claims read |
| Claims Approved | Count + percentage |
| Claims Denied | Count + percentage |
| Claims Pending | Count + percentage |
| Total Payments | Dollar amount with two decimal places |

### 8.2 Percentage Calculation

```
IF total_claims > 0:
    approved_pct = (approved_claims / total_claims) × 100
    denied_pct   = (denied_claims   / total_claims) × 100
    pending_pct  = (pending_claims  / total_claims) × 100
ELSE:
    all percentages = 0
```

> **Note:** Integer division (COBOL PIC 9(3)) — percentages are truncated, not rounded. May not sum to 100%.

### 8.3 Report Format

- **Line width:** 132 columns (standard mainframe report width)
- **Output method:** DISPLAY (console), not WRITE to file
- **Separator:** Full line of `=` characters

---

## 9. Complete Decision Tree

```
CLAIM ARRIVES
│
├─ Policy Lookup
│   ├─ NOT FOUND ──────────────────────────────────────────── ▶ DENY (error 20)
│   └─ FOUND
│       │
│       ├─ Policy Status ≠ 'A' ────────────────────────────── ▶ DENY
│       ├─ Claim Date < Policy Effective Date ─────────────── ▶ DENY
│       ├─ Claim Date > Policy Expiry Date ────────────────── ▶ DENY
│       │
│       ├─ Claim Amount < $50.00 ──────────────────────────── ▶ DENY
│       ├─ Claim Amount > $25,000.00 ──────────────────────── ▶ PENDING
│       │
│       ├─ Claim Type = "01" (Medical)
│       │   ├─ Diagnosis Code = "00000" or SPACES ─────────── ▶ DENY
│       │   └─ Provider ID = SPACES ───────────────────────── ▶ DENY
│       │
│       ├─ Claim Type = "02" (Dental)
│       │   └─ Amount > $2,500 AND Plan ≠ Premium ─────────── ▶ DENY
│       │
│       ├─ Claim Type = "03" (Vision)
│       │   └─ Amount > $500 ──────────────────────────────── ▶ DENY
│       │
│       ├─ Claim Type = "04" (Pharmacy)
│       │   └─ Provider ID = SPACES ───────────────────────── ▶ DENY
│       │
│       ├─ Claim Type = OTHER ─────────────────────────────── ▶ DENY
│       │
│       ├─ Coverage Calculation
│       │   ├─ (claim_amount − deductible) ≤ 0 ───────────── ▶ DENY
│       │   ├─ (claim_amount − deductible) > max_coverage ─── ▶ PENDING
│       │   ├─ claim_amount ≤ $5,000 ──────────────────────── ▶ APPROVE ──┐
│       │   └─ claim_amount > $5,000 ──────────────────────── ▶ PENDING   │
│       │                                                                  │
│       └───────────────────────────────────────────────────────────────────┘
│                                                                  │
│   ┌──────────────────────────────────────────────────────────────┘
│   │
│   ▼  APPROVED CLAIMS ONLY
│   │
│   Payment Calculation:
│   │  gross       = claim_amount
│   │  deductible  = MIN(gross, policy_deductible)
│   │  copay       = lookup(claim_type)
│   │  coinsurance = (gross − deductible − copay) × rate / 100
│   │  net_payment = MIN(max_coverage, claim_amount, MAX(0, coinsurance))
│   │
│   └── Payment > $0 → add to total paid; generate auth code
│
▼
SUMMARY REPORT GENERATED
```

---

## 10. Business Constants Summary

All hardcoded constants that must be externalized in the Java implementation:

### 10.1 Adjudication Thresholds

| Constant | Value | Used In | Purpose |
|---|---|---|---|
| MIN_CLAIM_AMOUNT | $50.00 | ADJUDCTN | Claims below this are denied |
| MAX_AUTO_APPROVE | $5,000.00 | ADJUDCTN | Claims at or below this are auto-approved |
| MANUAL_REVIEW_LIMIT | $25,000.00 | ADJUDCTN | Claims above this require manual review |
| MAX_CLAIM_AGE_DAYS | 90 | ADJUDCTN | Defined but not used in current logic |
| DENTAL_MAX | $2,500.00 | ADJUDCTN | Dental claim cap (non-Premium plans) |
| VISION_MAX | $500.00 | ADJUDCTN | Vision claim hard cap |

### 10.2 Payment Constants

| Constant | Value | Used In | Purpose |
|---|---|---|---|
| COINSURANCE_BASIC | 80% | PYMTAUTH | BS plan coinsurance rate |
| COINSURANCE_SILVER | 70% | PYMTAUTH | SV plan coinsurance rate |
| COINSURANCE_PREMIUM | 90% | PYMTAUTH | PR plan coinsurance rate |
| COINSURANCE_BRONZE | 60% | PYMTAUTH | BR plan coinsurance rate |
| COINSURANCE_DEFAULT | 80% | PYMTAUTH | Fallback coinsurance rate |
| COPAY_MEDICAL | $25.00 | PYMTAUTH | Medical claim copay |
| COPAY_DENTAL | $15.00 | PYMTAUTH | Dental claim copay |
| COPAY_VISION | $10.00 | PYMTAUTH | Vision claim copay |
| COPAY_PHARMACY | $10.00 | PYMTAUTH | Pharmacy claim copay |

### 10.3 Operational Constants

| Constant | Value | Used In | Purpose |
|---|---|---|---|
| MAX_SEARCH_DEPTH | 1,000 | POLYLKUP | Maximum records scanned in policy lookup |
| AUTH_CODE_PREFIX | "A" | PYMTAUTH | Prefix for authorization codes |
| REPORT_LINE_WIDTH | 132 | RPTGEN | Standard report column width |

### 10.4 Status Codes

| Domain | Code | Meaning |
|---|---|---|
| Policy Status | `A` | Active |
| Policy Status | `I` | Inactive |
| Adjudication Result | `A` | Approved |
| Adjudication Result | `D` | Denied |
| Adjudication Result | `P` | Pending (manual review) |
| Payment Status | `A` | Authorized |

---

## Appendix A: Java Implementation Notes

### A.1 Recommended Externalization

The following COBOL hardcoded values should be moved to **configuration** (e.g., `application.properties` or a database table) in Java:
- All monetary thresholds (min claim, max auto-approve, manual review limit, dental/vision caps)
- Coinsurance rates per plan type
- Copay amounts per claim type
- Error code-to-message mappings

### A.2 Behavioral Edge Cases to Preserve

| # | Edge Case | COBOL Behavior | Preserve in Java? |
|---|---|---|---|
| 1 | Claim amount exactly $50.00 | Passes minimum check (`<` not `<=`) | ✅ Yes |
| 2 | Claim amount exactly $5,000.00 | Auto-approved (`<=` $5,000) | ✅ Yes |
| 3 | Claim amount exactly $25,000.00 | Passes manual review check (uses `>`, not `>=`) | ✅ Yes |
| 4 | Dental claim exactly $2,500.00 | Passes dental cap (uses `>`, not `>=`) | ✅ Yes |
| 5 | Vision claim exactly $500.00 | Passes vision cap (uses `>`, not `>=`) | ✅ Yes |
| 6 | Deductible exceeds claim amount | Deductible applied = full claim amount (MIN function) | ✅ Yes |
| 7 | Coinsurance calculation negative | Floored to $0 | ✅ Yes |
| 8 | Net payment > claim amount | Capped at claim amount | ✅ Yes |
| 9 | Net payment > max coverage | Capped at max coverage | ✅ Yes |
| 10 | Unknown plan type | Defaults to 80% coinsurance | ✅ Yes |
| 11 | Unknown claim type in adjudication | Denied | ✅ Yes |
| 12 | Unknown claim type in payment | Copay = $0 | ✅ Yes |
| 13 | MAX_CLAIM_AGE_DAYS (90) defined but unused | Not enforced in adjudication logic | ⚠️ Document, decide |
| 14 | Policy lookup max 1,000 records | Sequential scan limit — not needed with indexed DB | ❌ Replace with proper lookup |
| 15 | Auth code counter resets per batch run | Counter is WS variable, not persistent | ⚠️ Replace with persistent sequence |
| 16 | COBOL ROUNDED on coinsurance | Banker's rounding (round half to even) | ✅ Use `RoundingMode.HALF_EVEN` |

### A.3 Anti-Patterns to Eliminate in Java

| COBOL Anti-Pattern | Java Replacement |
|---|---|
| GO TO–based control flow in ADJUDCTN | Structured if/else or early-return methods |
| Hardcoded business constants | External configuration (properties/DB) |
| Sequential file scan for policy lookup | Database indexed query or HashMap |
| File open/close per lookup call | Connection pooling / repository pattern |
| DISPLAY-based error logging | Structured logging framework (SLF4J/Logback) |
| Global mutable counters | Accumulator objects or method return values |
| Implied decimal (PIC 9(7)V99) | `BigDecimal` with explicit scale |
