# Continental Insurance Group - COBOL Claims Processing System

Legacy COBOL-85 batch processing system for insurance claims handling. This application demonstrates a typical mainframe-style claims processing workflow with policy lookup, adjudication rules, and payment authorization.

## Business Domain

Regional insurance claims processing system handling:
- Claim submissions and validation
- Policy lookup and verification
- Claims adjudication based on business rules
- Payment authorization and calculation
- Summary report generation

## System Architecture

```
┌─────────────────┐
│   CLMPROC       │  Main Claims Processing
│   (Main)        │  
└────────┬────────┘
         │
         ├─────────► POLYLKUP   (Policy Lookup)
         ├─────────► ADJUDCTN   (Adjudication Engine)
         ├─────────► PYMTAUTH   (Payment Authorization)
         ├─────────► RPTGEN     (Report Generation)
         └─────────► ERRHANDL   (Error Handling)
```

## Directory Structure

```
.
├── src/
│   ├── cobol/              # COBOL source programs
│   │   ├── CLMPROC.cbl     # Main claims processor
│   │   ├── POLYLKUP.cbl    # Policy lookup subroutine
│   │   ├── ADJUDCTN.cbl    # Adjudication rules engine
│   │   ├── PYMTAUTH.cbl    # Payment authorization
│   │   ├── RPTGEN.cbl      # Report generation
│   │   └── ERRHANDL.cbl    # Error handler
│   ├── copybooks/          # Record layouts
│   │   ├── CLMREC.cpy      # Claim record layout
│   │   ├── POLREC.cpy      # Policy record layout
│   │   ├── PYMTREC.cpy     # Payment record layout
│   │   └── ERRCODES.cpy    # Error code definitions
│   └── jcl/                # Job Control scripts
│       ├── NIGHTRUN.sh     # Nightly batch job
│       ├── MONTHEND.sh     # Month-end processing
│       └── RPTJOB.sh       # Report generation job
├── data/                   # Data files
│   ├── policies.dat        # Policy master file (22 records)
│   ├── claims.dat          # Claims transaction file (20 records)
│   └── partners.dat        # Partner provider data
├── reports/                # Generated reports
├── bin/                    # Compiled binaries
├── Makefile               # Build automation
├── Dockerfile             # Container image definition
└── docker-compose.yml     # Container orchestration
```

## COBOL Features Demonstrated

### Legacy Anti-Patterns
- **GOTO-based control flow** in ADJUDCTN
- **Hardcoded business rules** (claim amounts, thresholds)
- **Fixed-width data files** (no delimiters)
- **PERFORM THRU** paragraph ranges
- **Level 88 condition names**
- **Sequential file processing**
- **COPY statements** for record layouts

### Data Structures
- **PICTURE clauses**: PIC 9(7)V99 for currency, PIC X(n) for text
- **COMPUTE statements** with complex calculations
- **STRING/UNSTRING** for data manipulation
- **WORKING-STORAGE** with extensive field definitions

### Business Rules (Hardcoded)
- Minimum claim amount: $50.00
- Auto-approve threshold: $5,000.00
- Manual review threshold: $25,000.00
- Dental maximum: $2,500.00
- Vision maximum: $500.00
- Plan-specific coinsurance rates (60%-90%)

## Building and Running

### Prerequisites
- GnuCOBOL 3.x or later
- GNU Make
- Bash (for JCL scripts)
- Docker (optional)

### Compile All Programs
```bash
make all
```

### Run Claims Processing
```bash
make run
```

### Run Batch Jobs
```bash
# Nightly batch processing
make nightrun

# Month-end processing
make monthend

# Generate reports only
make report
```

### Clean Build
```bash
make clean      # Remove binaries
make distclean  # Remove binaries and reports
```

## Docker Usage

### Build and Run with Docker
```bash
# Build the container
docker-compose build

# Run claims processing
docker-compose up

# Run batch job
docker-compose --profile batch up cobol-batch

# Interactive shell
docker-compose run --rm cobol-app /bin/bash
```

### Run Specific Commands in Container
```bash
docker-compose run --rm cobol-app make nightrun
docker-compose run --rm cobol-app make monthend
```

## Data File Formats

### Claims File (claims.dat)
Fixed-width format (51 bytes per record):
- Claim Number: 10 chars
- Policy Number: 10 chars
- Claim Date: 8 digits (YYYYMMDD)
- Claim Type: 2 chars (01=Medical, 02=Dental, 03=Vision, 04=Pharmacy)
- Claim Amount: 9 chars (9 digits with 2 decimal places)
- Status: 1 char (A=Approved, D=Denied, P=Pending)
- Diagnosis Code: 5 chars
- Provider ID: 8 chars

### Policy File (policies.dat)
Fixed-width format (76 bytes per record):
- Policy Number: 10 chars
- Holder Name: 30 chars
- Plan Type: 2 chars (BS=Basic, SV=Silver, PR=Premium, BR=Bronze)
- Effective Date: 8 digits (YYYYMMDD)
- Expiry Date: 8 digits (YYYYMMDD)
- Deductible: 7 chars (5 digits + 2 decimals)
- Max Coverage: 9 chars (7 digits + 2 decimals)
- Status: 1 char (A=Active, I=Inactive)

## Output

### Console Output
The application displays processing progress:
```
CONTINENTAL INSURANCE - CLAIMS PROCESSING
PROCESSING CLAIM: CLM0000001
  CLAIM APPROVED
  PAYMENT AUTHORIZED: $1000.00
...
TOTAL CLAIMS PROCESSED: 20
APPROVED: 15
DENIED: 3
PENDING: 2
```

### Summary Report (reports/summary.txt)
Formatted summary report with:
- Total claims processed
- Approval/denial/pending counts and percentages
- Total payment amounts

## Modernization Opportunities

This legacy application is designed for modernization exercises:
1. **Replace GOTO** with structured control flow
2. **Externalize business rules** from code to configuration
3. **Convert fixed-width files** to CSV/JSON/database
4. **Modularize into microservices** (policy service, adjudication service)
5. **Add REST APIs** for real-time processing
6. **Implement proper error handling** instead of GOTO-based flow
7. **Add unit tests** and integration tests
8. **Migrate to Java/Python/Node.js**
9. **Replace batch processing** with event-driven architecture
10. **Containerize** and deploy to cloud

## License

Continental Insurance Group Internal Use Only

## Authors

Continental Insurance Group IT Department
