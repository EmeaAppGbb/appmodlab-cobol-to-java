```
 ██████╗ ██████╗ ██████╗  ██████╗ ██╗          ██████╗ ███████╗
██╔════╝██╔═══██╗██╔══██╗██╔═══██╗██║          ╚════██╗╚════██║
██║     ██║   ██║██████╔╝██║   ██║██║           █████╔╝    ██╔╝
██║     ██║   ██║██╔══██╗██║   ██║██║          ██╔═══╝    ██╔╝ 
╚██████╗╚██████╔╝██████╔╝╚██████╔╝███████╗     ███████╗   ██║  
 ╚═════╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚══════╝     ╚══════╝   ╚═╝  
                                                                
           ███╗   ███╗██╗ ██████╗██████╗  ██████╗ ███████╗███████╗██████╗ ██╗   ██╗██╗ ██████╗███████╗███████╗
           ████╗ ████║██║██╔════╝██╔══██╗██╔═══██╗██╔════╝██╔════╝██╔══██╗██║   ██║██║██╔════╝██╔════╝██╔════╝
           ██╔████╔██║██║██║     ██████╔╝██║   ██║███████╗█████╗  ██████╔╝██║   ██║██║██║     █████╗  ███████╗
           ██║╚██╔╝██║██║██║     ██╔══██╗██║   ██║╚════██║██╔══╝  ██╔══██╗╚██╗ ██╔╝██║██║     ██╔══╝  ╚════██║
           ██║ ╚═╝ ██║██║╚██████╗██║  ██║╚██████╔╝███████║███████╗██║  ██║ ╚████╔╝ ██║╚██████╗███████╗███████║
           ╚═╝     ╚═╝╚═╝ ╚═════╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═╝  ╚═══╝  ╚═╝ ╚═════╝╚══════╝╚══════╝
```

<div align="center">

# 🟢 MAINFRAME MODERNIZATION LAB 🟢
### *From Green Screens to Green Containers* 💚

[![COBOL](https://img.shields.io/badge/COBOL-85-00FF00?style=for-the-badge&logo=ibm&logoColor=00FF00)](https://www.ibm.com/docs/en/cobol-zos)
[![Java](https://img.shields.io/badge/Java-21-FF6B35?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Azure](https://img.shields.io/badge/Azure-Container_Apps-0078D4?style=for-the-badge&logo=microsoftazure&logoColor=white)](https://azure.microsoft.com/en-us/products/container-apps)

```
> READY.
> IPL COMPLETE ████████████████████████████ 100%
> SYSTEM STATUS: OPERATIONAL
```

</div>

---

## 📺 OVERVIEW

```
╔══════════════════════════════════════════════════════════════╗
║  CONTINENTAL INSURANCE GROUP MAINFRAME SYSTEM v4.2          ║
║  CLAIMS PROCESSING & ADJUDICATION ENGINE                     ║
║  Running since 1989 • 2.4M claims/year • 99.97% uptime      ║
╚══════════════════════════════════════════════════════════════╝
```

**Welcome to the time machine, developer!** 🕰️ This lab takes you on a journey from the glowing green terminals of 1980s mainframes to the cloud-native world of modern microservices. You'll modernize a **battle-tested COBOL batch processing system** — the Continental Insurance Group's claims adjudication engine — and transform it into a sleek, event-driven architecture running on Azure.

This beauty has processed **billions of insurance claims** over three decades. Now it's time to give it a cloud makeover while preserving that rock-solid business logic! 💎

**What we're migrating:**
- 🏢 **Insurance Claims Processing** — Policy validation, claims adjudication, payment authorization
- 📊 **Nightly Batch Jobs** — 50,000+ claims processed per night
- 📁 **VSAM File System** — Hierarchical data stores (remember those?)
- 📝 **COBOL Copybooks** — Data structures older than most of your team
- ⚡ **JCL Scripts** — Job Control Language that's been running like clockwork since Reagan

---

## 🎯 WHAT YOU'LL LEARN

```
> LOADING OBJECTIVES ████████████████████████ 100%
> JOB SUBMITTED
```

By the end of this lab, you'll master the ancient art of mainframe modernization:

- 🏗️ **Decompose Monolithic COBOL** into independently deployable microservices
- 📋 **Translate Copybook Structures** to Java POJOs and JPA entities
- 🧠 **Externalize Business Rules** from COBOL paragraphs to Drools decision tables
- ⚡ **Replace Batch Processing** with event-driven architecture using Azure Service Bus
- 🐳 **Containerize Legacy Logic** and deploy to Azure Container Apps
- 🔄 **Migrate VSAM Files** to Azure SQL Database with referential integrity
- 🧪 **Validate Business Logic** — ensuring the new system matches the old (down to the penny!)
- 📊 **Monitor & Scale** your modernized application in the cloud

**SPOILER ALERT:** 🚨 You'll gain mad respect for those COBOL developers who built systems that just. keep. running. 💪

---

## 🛠️ PREREQUISITES

```
> CHECKING SYSTEM REQUIREMENTS...
> OPERATOR: ENSURE ALL COMPONENTS AVAILABLE
```

### Required Software 🖥️

| Component | Version | Purpose |
|-----------|---------|---------|
| 🟢 **GnuCOBOL** | 3.1+ (or Docker) | Running the legacy system |
| ☕ **JDK** | 21+ | Building Java microservices |
| 📦 **Maven** | 3.8+ | Dependency management |
| 🐳 **Docker Desktop** | Latest | Containerization |
| ☁️ **Azure Subscription** | Active | Cloud deployment |
| 🤖 **GitHub Copilot CLI** | Latest | AI-powered development |
| 🔧 **Azure CLI** | 2.50+ | Azure resource management |

### Azure Resources Needed ☁️

- 💾 Azure SQL Database (S1 tier or higher)
- 🚌 Azure Service Bus (Standard tier)
- 📦 Azure Container Registry
- 🚀 Azure Container Apps Environment
- 📊 Azure Application Insights (optional but recommended)

### Knowledge Prerequisites 🧠

- ✅ Basic understanding of enterprise applications
- ✅ Familiarity with REST APIs
- ✅ SQL fundamentals
- ⚠️ **No COBOL experience required!** (We'll teach you to read it)
- ⚠️ **No mainframe experience required!** (We'll guide you through)

```
> IPL STATUS: READY TO PROCEED
> PRESS ENTER TO CONTINUE ▮
```

---

## 🚀 QUICK START

```
> OPERATOR: INITIATE STARTUP SEQUENCE
> MOUNTING VOLUMES ████████████████████ 100%
```

Get the legacy system running in **under 5 minutes**:

### 1️⃣ Clone & Enter the Lab 📂

```bash
cd appmodlab-cobol-to-java
```

### 2️⃣ Fire Up the Mainframe (Docker Style) 🐳

```bash
docker-compose up -d
```

This spins up:
- 🟢 **COBOL Runtime Environment** (GnuCOBOL container)
- 📊 **PostgreSQL** (simulating VSAM data)
- 🖥️ **Web Terminal** for that authentic green-screen experience

### 3️⃣ Run the Nightly Batch Job 🌙

```bash
docker exec -it cobol-runtime /app/scripts/run-batch.sh
```

Watch those claims process in real-time! ⚡

### 4️⃣ View the SYSOUT Reports 📄

```bash
docker exec -it cobol-runtime cat /app/output/claims-report-$(date +%Y%m%d).txt
```

**CONGRATULATIONS!** 🎉 You just ran a mainframe batch job. Welcome to 1989! 

```
> JOB COMPLETED SUCCESSFULLY
> RETURN CODE: 0000
> READY. ▮
```

---

## 📁 PROJECT STRUCTURE

```
appmodlab-cobol-to-java/
│
├─ 🟢 legacy-cobol/                    # The mainframe source
│  ├─ src/
│  │  ├─ CLMPRC01.cob                 # Claims processing driver
│  │  ├─ POLVAL01.cob                 # Policy validation
│  │  ├─ ADJENG01.cob                 # Adjudication engine
│  │  ├─ PYMAUTH01.cob                # Payment authorization
│  │  └─ REPORT01.cob                 # Report generation
│  ├─ copybooks/
│  │  ├─ CLAIM-REC.cpy                # Claim record structure
│  │  ├─ POLICY-REC.cpy               # Policy record structure
│  │  └─ PAYMENT-REC.cpy              # Payment record structure
│  ├─ jcl/
│  │  └─ NIGHTLY.jcl                  # Nightly batch job
│  └─ data/
│     ├─ claims.vsam                  # Simulated VSAM file
│     └─ policies.vsam                # Policy master file
│
├─ ☕ java-microservices/              # The cloud-native future
│  ├─ claims-service/                 # Claims API
│  ├─ policy-service/                 # Policy API
│  ├─ adjudication-service/           # Business rules engine (Drools)
│  ├─ payment-service/                # Payment processing
│  └─ reporting-service/              # Analytics & reporting
│
├─ 🗄️ database/
│  ├─ migration-scripts/              # VSAM → SQL migration
│  └─ schema/                         # Target database schema
│
├─ ☁️ azure/
│  ├─ bicep/                          # Infrastructure as Code
│  └─ scripts/                        # Deployment automation
│
├─ 🧪 tests/
│  └─ validation/                     # COBOL vs Java output comparison
│
└─ 📖 docs/
   ├─ data-dictionary.md              # Field mappings
   ├─ business-rules.md               # Logic documentation
   └─ architecture.md                 # Target design
```

```
> DIRECTORY LISTING COMPLETE
> 147 FILES CATALOGED
```

---

## 🏛️ THE MAINFRAME LEGACY

```
╔════════════════════════════════════════════════════════════╗
║  "If it ain't broke, don't fix it... until it is."       ║
║                                    - Every COBOL Dev Ever  ║
╚════════════════════════════════════════════════════════════╝
```

### The System That Never Sleeps 💤

This **beauty** has been running since **before Java was invented**, before the web existed, before most of us had email addresses. Continental Insurance's claims processing system is a **masterpiece of reliability**:

- 🕐 **30+ years in production** — Literally older than some of the developers modernizing it
- 📈 **99.97% uptime** — That's only 2.6 hours of downtime per year
- 🔥 **Zero data loss** — Ever. Not a single claim.
- ⚡ **2.4 million claims/year** — Peak processing: 8,000 claims/hour
- 💰 **$14 billion in payments** — Per year. No pressure.

### The Technology Stack (Vintage 1989) 🕹️

#### COBOL Programs 🟢

Five main programs orchestrate the nightly processing:

| Program | Lines of Code | Purpose |
|---------|---------------|---------|
| `CLMPRC01` | 3,200 | Main driver, orchestrates the workflow |
| `POLVAL01` | 1,800 | Validates policy status & coverage |
| `ADJENG01` | 4,500 | Core adjudication logic (the crown jewel 👑) |
| `PYMAUTH01` | 2,100 | Authorizes payments, applies limits |
| `REPORT01` | 1,600 | Generates management reports |

**Fun fact:** `ADJENG01` has **87 nested IF statements**. We'll replace that with Drools. Your eyes will thank us. 👀

#### Copybooks 📋

These are like header files, but... vintage:

```cobol
01  CLAIM-RECORD.
    05  CLAIM-ID            PIC 9(10).
    05  POLICY-NUMBER       PIC X(12).
    05  INSURED-NAME        PIC X(40).
    05  CLAIM-DATE          PIC 9(8).
    05  CLAIM-AMOUNT        PIC 9(9)V99.
    05  CLAIM-STATUS        PIC X(2).
       88  PENDING          VALUE 'PN'.
       88  APPROVED         VALUE 'AP'.
       88  DENIED           VALUE 'DN'.
```

**Translation:** This is a struct. We'll turn it into a nice Java record. 😎

#### VSAM Files 🗄️

**Virtual Storage Access Method** — IBM's hierarchical file system:

- `CLAIMS.MASTER` — 1.2M records, keyed by claim ID
- `POLICY.MASTER` — 850K records, keyed by policy number
- `PAYMENT.PENDING` — Transactional file, cleared nightly

**The Problem:** No relational integrity, no referential constraints, just raw speed and discipline.

#### JCL Scripts 📜

**Job Control Language** — Think shell scripts, but from the mainframe era:

```jcl
//NIGHTLY  JOB  (ACCT123),'CLAIMS PROCESSING',
//         CLASS=A,MSGCLASS=H,NOTIFY=&SYSUID
//STEP01   EXEC PGM=CLMPRC01
//CLAIMS   DD   DSN=CLAIMS.MASTER,DISP=SHR
//POLICIES DD   DSN=POLICY.MASTER,DISP=SHR
//SYSOUT   DD   SYSOUT=*
```

**Translation:** This kicks off the nightly job. We'll replace it with event-driven processing. ⚡

#### Batch Processing Cycle 🔄

```
23:00 EDT ──→ Job starts
23:15 EDT ──→ Policy validation complete
00:30 EDT ──→ Adjudication complete
02:15 EDT ──→ Payment authorization complete
02:45 EDT ──→ Reports generated
03:00 EDT ──→ Job complete, operators notified
```

**8-hour batch window**. Modern goal? **Real-time**. 🚀

```
> LEGACY SYSTEM ANALYSIS COMPLETE
> READY FOR MODERNIZATION ▮
```

---

## 🎯 TARGET ARCHITECTURE

```
> LOADING NEW WORLD ORDER...
> DEPLOYING TO CLOUD ████████████████ 100%
```

### The Modern Stack ☁️

We're transforming that monolithic batch beast into **5 cloud-native microservices**:

```
┌─────────────────────────────────────────────────────────────┐
│                     🌐 API GATEWAY                          │
│                  (Azure API Management)                     │
└────────────┬────────────────────────────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼────┐      ┌────▼─────┐
│ 📋     │      │  🛡️      │
│ Claims │◄────►│ Policy   │
│ Service│      │ Service  │
└───┬────┘      └──────────┘
    │
    │ Event: ClaimSubmitted
    ▼
┌────────────────┐
│  🚌 Azure      │
│  Service Bus   │
└───┬────────────┘
    │
    │ Event: ClaimToAdjudicate
    ▼
┌───────────────┐       ┌──────────────┐
│  🧠           │       │  💰          │
│  Adjudication │──────►│  Payment     │
│  Service      │       │  Service     │
│  (Drools)     │       └──────┬───────┘
└───────────────┘              │
                               │
                        ┌──────▼───────┐
                        │  📊          │
                        │  Reporting   │
                        │  Service     │
                        └──────────────┘
```

### Microservices Breakdown ☕

#### 1️⃣ Claims Service 📋

**Port:** `8081` | **Tech:** Spring Boot 3.2, Spring Data JPA

- REST API for claim submission, retrieval, updates
- Publishes `ClaimSubmitted` events to Service Bus
- Stores claims in Azure SQL
- **Replaces:** 40% of `CLMPRC01.cob`

#### 2️⃣ Policy Service 🛡️

**Port:** `8082` | **Tech:** Spring Boot 3.2, Spring Data JPA, Redis Cache

- REST API for policy lookups and validation
- Caches active policies for performance
- Exposes policy coverage limits
- **Replaces:** `POLVAL01.cob` entirely

#### 3️⃣ Adjudication Service 🧠

**Port:** `8083` | **Tech:** Spring Boot 3.2, Drools 8.x

- Consumes `ClaimSubmitted` events
- Applies business rules via Drools engine
- Publishes `ClaimApproved` or `ClaimDenied` events
- **Replaces:** `ADJENG01.cob` (all 4,500 lines!) 🎉

**The Magic:** Those 87 nested IFs become **declarative Drools rules**:

```drools
rule "Auto-approve under deductible"
when
    $claim : Claim(amount < policy.deductible)
then
    modify($claim) { setStatus(APPROVED) }
    insert(new ClaimApproved($claim));
end
```

#### 4️⃣ Payment Service 💰

**Port:** `8084` | **Tech:** Spring Boot 3.2, Spring Batch

- Consumes `ClaimApproved` events
- Authorizes payments against policy limits
- Integrates with external payment gateway (simulated)
- **Replaces:** `PYMAUTH01.cob`

#### 5️⃣ Reporting Service 📊

**Port:** `8085` | **Tech:** Spring Boot 3.2, Apache POI, JasperReports

- Generates daily, weekly, monthly reports
- Exposes analytics API
- Stores aggregates in reporting database
- **Replaces:** `REPORT01.cob`

### Data Layer 🗄️

- **Azure SQL Database** — Relational storage with full ACID guarantees
- **Azure Redis Cache** — Policy and reference data caching
- **Azure Blob Storage** — Report archives, claim documents

### Messaging & Events 🚌

- **Azure Service Bus** — Event-driven choreography between services
- **Topics:**
  - `claims-submitted` → Triggers adjudication
  - `claims-adjudicated` → Triggers payment
  - `payments-authorized` → Triggers reporting

### Infrastructure ☁️

- **Azure Container Apps** — Serverless microservices hosting
- **Azure API Management** — API gateway, rate limiting, authentication
- **Azure Application Insights** — Distributed tracing, monitoring
- **Azure Key Vault** — Secrets management

```
> TARGET ARCHITECTURE LOADED
> READY TO BUILD ▮
```

---

## 🕹️ LAB WALKTHROUGH

```
╔══════════════════════════════════════════════════════════╗
║  MAINFRAME MODERNIZATION BOOTCAMP                       ║
║  Estimated Duration: 6-8 hours                          ║
║  Difficulty: ████████░░ ADVANCED                        ║
╚══════════════════════════════════════════════════════════╝
```

### 🎮 LEVEL 1: Explore the Legacy System

```
> OPERATOR: ANALYZE EXISTING SYSTEM
> MOUNTING PRODUCTION FILES (READ-ONLY) ✅
```

**Objective:** Understand what you're modernizing before you modernize it! 🔍

#### 1.1 Fire Up the COBOL Environment 🟢

```bash
cd legacy-cobol
docker-compose up -d
```

**What's happening:**
- GnuCOBOL runtime environment starts
- Sample VSAM data files are mounted
- Web terminal becomes available at `http://localhost:3000`

#### 1.2 Examine the Copybooks 📋

```bash
cat copybooks/CLAIM-REC.cpy
cat copybooks/POLICY-REC.cpy
```

**Task:** Map these data structures to planned Java POJOs. Copilot can help! 🤖

```bash
gh copilot explain "What does PIC 9(10) mean in COBOL?"
```

#### 1.3 Run the Nightly Batch 🌙

```bash
./scripts/run-batch.sh
```

**Watch the magic:**
- `CLMPRC01` processes 500 sample claims
- Policies validated against master file
- Claims adjudicated according to business rules
- Payments authorized
- Reports generated in `/output/`

#### 1.4 Analyze the Output 📊

```bash
cat output/claims-report-$(date +%Y%m%d).txt
cat output/payment-summary.txt
```

**Success Criteria:** ✅ You understand the input → processing → output flow

```
> SYSOUT CAPTURED
> LEVEL 1 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 2: Design Target Schema

```
> OPERATOR: BEGIN LOGICAL DATA MODELING
> LOADING DB2 SCHEMA DESIGNER...
```

**Objective:** Transform flat VSAM records into a normalized relational schema 🗄️

#### 2.1 Review the Data Dictionary 📖

```bash
cat docs/data-dictionary.md
```

This maps every COBOL field to a SQL column with rationale.

#### 2.2 Generate the SQL Schema 💾

```bash
cd database/schema
cat claims-schema.sql
cat policies-schema.sql
```

**Key improvements over VSAM:**
- ✅ Foreign key constraints
- ✅ Check constraints (no more invalid statuses!)
- ✅ Indexes on query patterns
- ✅ Audit columns (created_at, updated_at)

#### 2.3 Create the Azure SQL Database ☁️

```bash
# Using Azure CLI
az sql server create \
  --name cobol-modernization-sql \
  --resource-group appmod-rg \
  --location eastus \
  --admin-user sqladmin \
  --admin-password <secure-password>

az sql db create \
  --resource-group appmod-rg \
  --server cobol-modernization-sql \
  --name continental-insurance \
  --service-objective S1
```

#### 2.4 Deploy the Schema 🚀

```bash
sqlcmd -S cobol-modernization-sql.database.windows.net \
  -d continental-insurance \
  -U sqladmin \
  -i claims-schema.sql

sqlcmd -S cobol-modernization-sql.database.windows.net \
  -d continental-insurance \
  -U sqladmin \
  -i policies-schema.sql
```

**Success Criteria:** ✅ Schema deployed, all tables created with constraints

```
> DATABASE INITIALIZED
> LEVEL 2 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 3: Migrate Data

```
> OPERATOR: INITIATE DATA MIGRATION
> WARNING: VALIDATE CHECKSUMS POST-MIGRATION ⚠️
```

**Objective:** Move data from VSAM files to Azure SQL with **zero data loss** 💾

#### 3.1 Export VSAM to CSV 📄

The COBOL export utility reads VSAM and writes delimited files:

```bash
cd legacy-cobol
./scripts/export-vsam-to-csv.sh
```

**Output:**
- `data/export/claims.csv` — 500 claim records
- `data/export/policies.csv` — 250 policy records

#### 3.2 Validate Export Integrity ✅

```bash
# Count records
wc -l data/export/claims.csv
# Should match: SELECT COUNT(*) FROM VSAM claims file

# Checksum verification
./scripts/checksum-vsam.sh > checksums-source.txt
```

#### 3.3 Run the Migration Scripts 🚀

```bash
cd database/migration-scripts
python migrate-to-sql.py \
  --source ../../legacy-cobol/data/export \
  --target $AZURE_SQL_CONNECTION_STRING \
  --validate
```

**What it does:**
- Reads CSV exports
- Transforms data types (COBOL → SQL)
- Inserts into Azure SQL
- Validates foreign key integrity
- Generates migration report

#### 3.4 Verify Migration ✅

```bash
# Run validation queries
sqlcmd -S cobol-modernization-sql.database.windows.net \
  -d continental-insurance \
  -U sqladmin \
  -i validation-queries.sql
```

**Success Criteria:** 
✅ Record counts match  
✅ Checksums match  
✅ No orphaned foreign keys  
✅ All constraints valid

```
> DATA MIGRATION COMPLETE
> RETURN CODE: 0000
> LEVEL 3 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 4: Build Claims Service

```
> OPERATOR: COMPILE JAVA MODULE - CLAIMS
> JOB SUBMITTED ████████████████████ 100%
```

**Objective:** Create the first microservice — Claims REST API 📋

#### 4.1 Generate Spring Boot Project ☕

```bash
cd java-microservices/claims-service
# Use Spring Initializr or Copilot

gh copilot suggest "Create a Spring Boot 3.2 project with Web, JPA, Azure Service Bus dependencies"
```

#### 4.2 Create the Claim Entity 🗂️

Transform `CLAIM-REC.cpy` copybook into JPA entity:

```java
@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;
    
    @Column(nullable = false, length = 12)
    private String policyNumber;
    
    @Column(nullable = false, length = 40)
    private String insuredName;
    
    @Column(nullable = false)
    private LocalDate claimDate;
    
    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal claimAmount;
    
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;
    
    // Getters, setters, constructors
}
```

**Pro Tip:** Use Copilot to generate this from the copybook! 🤖

```bash
gh copilot suggest "Convert COBOL copybook CLAIM-REC to JPA entity"
```

#### 4.3 Build the REST Controller 🎛️

```java
@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    
    @PostMapping
    public ResponseEntity<ClaimDTO> submitClaim(@RequestBody @Valid ClaimDTO claimDTO) {
        // Submit claim logic
        // Publish ClaimSubmitted event to Service Bus
    }
    
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimDTO> getClaim(@PathVariable Long claimId) {
        // Retrieve claim
    }
    
    @GetMapping
    public Page<ClaimDTO> searchClaims(
        @RequestParam(required = false) String policyNumber,
        @RequestParam(required = false) ClaimStatus status,
        Pageable pageable
    ) {
        // Search claims
    }
}
```

#### 4.4 Integrate Azure Service Bus 🚌

```java
@Service
public class ClaimEventPublisher {
    
    private final ServiceBusSenderClient senderClient;
    
    public void publishClaimSubmitted(Claim claim) {
        ServiceBusMessage message = new ServiceBusMessage(
            objectMapper.writeValueAsString(claim)
        );
        message.setContentType("application/json");
        message.setSubject("ClaimSubmitted");
        
        senderClient.sendMessage(message);
    }
}
```

#### 4.5 Test the Service 🧪

```bash
./mvnw clean test
./mvnw spring-boot:run
```

**Manual test:**

```bash
curl -X POST http://localhost:8081/api/claims \
  -H "Content-Type: application/json" \
  -d '{
    "policyNumber": "POL123456789",
    "insuredName": "John Doe",
    "claimDate": "2024-01-15",
    "claimAmount": 5000.00
  }'
```

**Success Criteria:** ✅ Claim created, event published to Service Bus

```
> COMPILATION SUCCESSFUL
> MODULE CLAIMS-SERVICE READY
> LEVEL 4 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 5: Build Policy Service

```
> OPERATOR: COMPILE JAVA MODULE - POLICY
> LOADING BUSINESS OBJECTS...
```

**Objective:** Create the policy validation service with caching 🛡️

#### 5.1 Create Policy Entity & Repository 🗂️

Similar to Claims Service, transform `POLICY-REC.cpy`:

```java
@Entity
@Table(name = "policies")
public class Policy {
    @Id
    private String policyNumber;
    
    private String insuredName;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private BigDecimal coverageLimit;
    private BigDecimal deductible;
    private PolicyStatus status;
    
    // Business logic methods
    public boolean isActive() {
        return status == PolicyStatus.ACTIVE 
            && LocalDate.now().isBefore(expirationDate);
    }
}
```

#### 5.2 Add Redis Caching 💨

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
            )
            .build();
    }
}

@Service
public class PolicyService {
    
    @Cacheable(value = "policies", key = "#policyNumber")
    public Policy findByPolicyNumber(String policyNumber) {
        return policyRepository.findById(policyNumber)
            .orElseThrow(() -> new PolicyNotFoundException(policyNumber));
    }
}
```

**Why caching?** Policies don't change often, but they're queried for every claim. 🚀

#### 5.3 Build REST API 🎛️

```java
@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    
    @GetMapping("/{policyNumber}")
    public ResponseEntity<PolicyDTO> getPolicy(@PathVariable String policyNumber) {
        // Return policy with coverage details
    }
    
    @GetMapping("/{policyNumber}/validate")
    public ResponseEntity<ValidationResult> validatePolicy(
        @PathVariable String policyNumber,
        @RequestParam LocalDate claimDate
    ) {
        // Validate policy is active, claim date is within coverage period
    }
}
```

#### 5.4 Test the Service 🧪

```bash
./mvnw clean test
./mvnw spring-boot:run
```

```bash
curl http://localhost:8082/api/policies/POL123456789/validate?claimDate=2024-01-15
```

**Success Criteria:** ✅ Policy validation works, cache hit on second request

```
> MODULE POLICY-SERVICE READY
> LEVEL 5 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 6: Externalize Business Rules

```
> OPERATOR: MIGRATE BUSINESS LOGIC TO DROOLS
> WARNING: THIS IS WHERE IT GETS INTERESTING 🧠
```

**Objective:** Replace `ADJENG01.cob`'s 87 nested IFs with **declarative Drools rules** 🔥

#### 6.1 Understand the COBOL Logic 🟢

Open `legacy-cobol/src/ADJENG01.cob` and find this beauty:

```cobol
IF CLAIM-AMOUNT < POLICY-DEDUCTIBLE
    MOVE 'DN' TO CLAIM-STATUS
    MOVE 'BELOW DEDUCTIBLE' TO DENIAL-REASON
ELSE
    IF POLICY-STATUS = 'ACTIVE'
        IF CLAIM-DATE >= POLICY-EFF-DATE
            IF CLAIM-DATE <= POLICY-EXP-DATE
                IF CLAIM-AMOUNT <= POLICY-COVERAGE-LIMIT
                    MOVE 'AP' TO CLAIM-STATUS
                ELSE
                    MOVE 'DN' TO CLAIM-STATUS
                    MOVE 'EXCEEDS COVERAGE LIMIT' TO DENIAL-REASON
                END-IF
            ELSE
                MOVE 'DN' TO CLAIM-STATUS
                MOVE 'CLAIM DATE OUT OF COVERAGE PERIOD' TO DENIAL-REASON
            END-IF
        ...
```

**Now imagine 87 levels deep.** 😱 We're fixing this!

#### 6.2 Create Drools Rules 🧠

Create `src/main/resources/rules/adjudication.drl`:

```drools
package com.continental.adjudication.rules;

import com.continental.adjudication.model.*;

rule "Deny - Claim below deductible"
salience 100
when
    $claim : Claim(amount < policy.deductible)
then
    modify($claim) {
        setStatus(ClaimStatus.DENIED),
        setDenialReason("Claim amount is below policy deductible")
    }
end

rule "Deny - Policy inactive"
salience 90
when
    $claim : Claim(policy.status != PolicyStatus.ACTIVE)
then
    modify($claim) {
        setStatus(ClaimStatus.DENIED),
        setDenialReason("Policy is not active")
    }
end

rule "Deny - Claim date before coverage"
salience 80
when
    $claim : Claim(claimDate < policy.effectiveDate)
then
    modify($claim) {
        setStatus(ClaimStatus.DENIED),
        setDenialReason("Claim date before policy effective date")
    }
end

rule "Deny - Claim date after coverage"
salience 80
when
    $claim : Claim(claimDate > policy.expirationDate)
then
    modify($claim) {
        setStatus(ClaimStatus.DENIED),
        setDenialReason("Claim date after policy expiration")
    }
end

rule "Deny - Exceeds coverage limit"
salience 70
when
    $claim : Claim(amount > policy.coverageLimit)
then
    modify($claim) {
        setStatus(ClaimStatus.DENIED),
        setDenialReason("Claim amount exceeds policy coverage limit")
    }
end

rule "Approve - All validations passed"
salience 10
when
    $claim : Claim(
        status == ClaimStatus.PENDING,
        amount >= policy.deductible,
        amount <= policy.coverageLimit,
        policy.status == PolicyStatus.ACTIVE,
        claimDate >= policy.effectiveDate,
        claimDate <= policy.expirationDate
    )
then
    modify($claim) {
        setStatus(ClaimStatus.APPROVED),
        setApprovedAmount($claim.getAmount())
    }
    insert(new ClaimApprovedEvent($claim));
end
```

**BEAUTIFUL!** 😍 No more nesting hell. Rules are:
- ✅ **Declarative** — What, not how
- ✅ **Testable** — Each rule can be unit tested
- ✅ **Maintainable** — Business analysts can read and modify
- ✅ **Traceable** — Know exactly which rule fired

#### 6.3 Build Adjudication Service ⚙️

```java
@Service
public class AdjudicationEngine {
    
    private final KieContainer kieContainer;
    
    public AdjudicationResult adjudicate(Claim claim, Policy policy) {
        KieSession kieSession = kieContainer.newKieSession();
        
        claim.setPolicy(policy);
        kieSession.insert(claim);
        kieSession.fireAllRules();
        kieSession.dispose();
        
        return new AdjudicationResult(claim);
    }
}
```

#### 6.4 Create Event Listener 🚌

```java
@Component
public class ClaimSubmittedListener {
    
    @ServiceBusListener(topic = "claims-submitted", subscription = "adjudication-sub")
    public void processClaimSubmitted(ClaimSubmittedEvent event) {
        // Fetch policy
        Policy policy = policyService.findByPolicyNumber(event.getPolicyNumber());
        
        // Run adjudication
        AdjudicationResult result = adjudicationEngine.adjudicate(event.getClaim(), policy);
        
        // Publish result
        if (result.isApproved()) {
            eventPublisher.publishClaimApproved(result);
        } else {
            eventPublisher.publishClaimDenied(result);
        }
    }
}
```

#### 6.5 Validate Against COBOL Output 🧪

**This is critical!** We need to prove the rules produce identical results:

```bash
cd tests/validation
python compare-adjudication-results.py \
  --cobol-output ../../legacy-cobol/output/adjudication-results.txt \
  --java-output ./java-adjudication-results.json
```

**Success Criteria:**
✅ 100% match on approved/denied decisions  
✅ Identical approved amounts (to the penny!)  
✅ Equivalent denial reasons

```
> BUSINESS RULES EXTERNALIZED
> DROOLS ENGINE ONLINE
> LEVEL 6 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 7: Event-Driven Workflow

```
> OPERATOR: CONFIGURE MESSAGE-ORIENTED MIDDLEWARE
> INITIALIZING AZURE SERVICE BUS...
```

**Objective:** Replace batch processing with real-time event choreography 🚌

#### 7.1 Create Azure Service Bus Namespace ☁️

```bash
az servicebus namespace create \
  --resource-group appmod-rg \
  --name continental-insurance-sb \
  --location eastus \
  --sku Standard

az servicebus topic create \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --name claims-submitted

az servicebus topic create \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --name claims-adjudicated

az servicebus topic create \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --name payments-authorized
```

#### 7.2 Create Subscriptions 📥

```bash
# Adjudication service subscribes to claims-submitted
az servicebus topic subscription create \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --topic-name claims-submitted \
  --name adjudication-subscription

# Payment service subscribes to claims-adjudicated (approved only)
az servicebus topic subscription create \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --topic-name claims-adjudicated \
  --name payment-subscription

az servicebus topic subscription rule create \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --topic-name claims-adjudicated \
  --subscription-name payment-subscription \
  --name ApprovedClaimsOnly \
  --filter-sql-expression "status='APPROVED'"
```

**Filtering FTW!** 🎯 Payment service only sees approved claims.

#### 7.3 Configure Event Publishers 📤

Already implemented in previous levels! Each service publishes:

- **Claims Service** → `ClaimSubmittedEvent`
- **Adjudication Service** → `ClaimApprovedEvent` or `ClaimDeniedEvent`
- **Payment Service** → `PaymentAuthorizedEvent`

#### 7.4 Test End-to-End Flow 🔄

```bash
# Submit a claim
curl -X POST http://localhost:8081/api/claims \
  -H "Content-Type: application/json" \
  -d '{
    "policyNumber": "POL123456789",
    "insuredName": "Jane Doe",
    "claimDate": "2024-01-20",
    "claimAmount": 3500.00
  }'

# Watch the events flow through Application Insights
az monitor app-insights events show \
  --app continental-insurance-insights \
  --type trace \
  --query "[?message contains 'Event']"
```

**The Flow:**
1. 📋 **Claims Service** receives REST call → saves claim → publishes `ClaimSubmitted`
2. 🧠 **Adjudication Service** receives event → runs Drools → publishes `ClaimApproved`
3. 💰 **Payment Service** receives event → authorizes payment → publishes `PaymentAuthorized`
4. 📊 **Reporting Service** receives event → updates dashboards

**All in under 2 seconds!** ⚡ (vs. 8-hour batch window)

**Success Criteria:** ✅ Events flow through all services, claim processed end-to-end

```
> EVENT-DRIVEN ARCHITECTURE OPERATIONAL
> REAL-TIME PROCESSING ENABLED
> LEVEL 7 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 8: Containerize and Deploy

```
> OPERATOR: PACKAGE FOR CLOUD DEPLOYMENT
> DOCKER BUILD INITIATED ████████████ 100%
```

**Objective:** Deploy microservices to Azure Container Apps 🐳☁️

#### 8.1 Create Dockerfiles 🐳

Each service needs a multi-stage Dockerfile:

```dockerfile
# claims-service/Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 8.2 Build and Push Images 🚀

```bash
# Login to Azure Container Registry
az acr create \
  --resource-group appmod-rg \
  --name continentalinsuranceacr \
  --sku Basic

az acr login --name continentalinsuranceacr

# Build and push each service
for service in claims-service policy-service adjudication-service payment-service reporting-service; do
  docker build -t continentalinsuranceacr.azurecr.io/$service:v1.0.0 ./java-microservices/$service
  docker push continentalinsuranceacr.azurecr.io/$service:v1.0.0
done
```

#### 8.3 Create Container Apps Environment 🌐

```bash
az containerapp env create \
  --name continental-insurance-env \
  --resource-group appmod-rg \
  --location eastus
```

#### 8.4 Deploy Services to Container Apps 🚀

```bash
# Claims Service
az containerapp create \
  --name claims-service \
  --resource-group appmod-rg \
  --environment continental-insurance-env \
  --image continentalinsuranceacr.azurecr.io/claims-service:v1.0.0 \
  --target-port 8081 \
  --ingress external \
  --min-replicas 2 \
  --max-replicas 10 \
  --cpu 1.0 \
  --memory 2Gi \
  --env-vars \
    SPRING_DATASOURCE_URL=secretref:sql-connection-string \
    AZURE_SERVICEBUS_NAMESPACE=secretref:servicebus-namespace

# Repeat for other services...
```

**Auto-scaling FTW!** 📈 Container Apps scales from 2 to 10 instances based on load.

#### 8.5 Configure Secrets 🔐

```bash
az containerapp secret set \
  --name claims-service \
  --resource-group appmod-rg \
  --secrets \
    sql-connection-string="jdbc:sqlserver://..." \
    servicebus-namespace="Endpoint=sb://..."
```

#### 8.6 Deploy Infrastructure with Bicep 🏗️

```bash
cd azure/bicep
az deployment group create \
  --resource-group appmod-rg \
  --template-file main.bicep \
  --parameters @parameters.json
```

**Bicep deploys:**
- All 5 Container Apps
- Azure SQL Database
- Azure Service Bus
- Azure Redis Cache
- API Management Gateway
- Application Insights

**Success Criteria:** ✅ All services deployed, health checks passing

```
> CLOUD DEPLOYMENT COMPLETE
> ALL SYSTEMS OPERATIONAL
> LEVEL 8 COMPLETE ████████████ 100%
```

---

### 🎮 LEVEL 9: Validate End-to-End

```
> OPERATOR: INITIATE FINAL SYSTEM TEST
> COMPARING LEGACY VS MODERN OUTPUT...
```

**Objective:** Prove the new system produces identical business results 🎯

#### 9.1 Run Legacy Batch (Baseline) 🟢

```bash
cd legacy-cobol
./scripts/run-batch.sh
cp output/claims-report-$(date +%Y%m%d).txt ../tests/validation/baseline-report.txt
```

#### 9.2 Run Modern Microservices (Test) ☕

```bash
cd tests/validation
python submit-test-claims.py \
  --api-url https://claims-service.azurecontainerapps.io \
  --claims-file ../../legacy-cobol/data/test-claims.csv
```

This submits the same 500 test claims through the REST API.

#### 9.3 Compare Results 🔍

```bash
python compare-results.py \
  --baseline baseline-report.txt \
  --test modern-report.json \
  --tolerance 0.01  # Penny tolerance for floating point
```

**Validation checks:**
- ✅ **Claim statuses match** (approved/denied)
- ✅ **Approved amounts match** (within $0.01)
- ✅ **Denial reasons equivalent** (may differ in wording)
- ✅ **Payment authorizations match**
- ✅ **Report totals match**

#### 9.4 Performance Testing 🚀

```bash
# Load test with 10,000 concurrent claims
artillery run load-test.yml
```

**Expected results:**
- **Throughput:** 5,000+ claims/minute (vs. batch: 100/minute)
- **Latency p50:** < 200ms
- **Latency p95:** < 500ms
- **Latency p99:** < 1s

#### 9.5 Generate Final Report 📊

```bash
python generate-validation-report.py > VALIDATION_REPORT.md
```

**Success Criteria:**
✅ 100% functional equivalence  
✅ 50x performance improvement  
✅ Zero data loss  
✅ All business rules validated

```
> VALIDATION COMPLETE
> LEGACY SYSTEM: MATCHED ✅
> MODERN SYSTEM: OPERATIONAL ✅
> 
> ██████████████████████████████████████ 100%
> 
> GAME OVER - YOU WIN! 🎉🎉🎉
```

---

## ⏱️ ESTIMATED DURATION

```
╔═══════════════════════════════════════════════════╗
║  TOTAL LAB TIME: 6-8 HOURS                       ║
╚═══════════════════════════════════════════════════╝
```

**Breakdown:**

| Level | Task | Duration |
|-------|------|----------|
| 🎮 1 | Explore Legacy System | 45 min |
| 🎮 2 | Design Target Schema | 30 min |
| 🎮 3 | Migrate Data | 45 min |
| 🎮 4 | Build Claims Service | 60 min |
| 🎮 5 | Build Policy Service | 45 min |
| 🎮 6 | Externalize Business Rules | 90 min |
| 🎮 7 | Event-Driven Workflow | 60 min |
| 🎮 8 | Containerize and Deploy | 90 min |
| 🎮 9 | Validate End-to-End | 45 min |
| **TOTAL** | | **6.5 hours** |

**Pro Tips for Speed:**
- 🤖 **Use GitHub Copilot CLI aggressively** — let AI generate boilerplate
- ⚡ **Run independent tasks in parallel** — build services while migration runs
- 📋 **Prepare Azure resources ahead** — provision infrastructure before coding
- 🧪 **Test incrementally** — don't wait until the end

---

## 🏆 ACHIEVEMENT UNLOCKED

```
╔════════════════════════════════════════════════════════════╗
║                                                            ║
║         🏆 MAINFRAME MODERNIZATION MASTER 🏆              ║
║                                                            ║
║  You have successfully migrated a 30-year-old COBOL       ║
║  mainframe application to cloud-native microservices.     ║
║                                                            ║
║  Skills Acquired:                                          ║
║   ✅ COBOL to Java translation                            ║
║   ✅ Event-driven architecture                            ║
║   ✅ Drools business rules engine                         ║
║   ✅ Azure Container Apps deployment                      ║
║   ✅ Legacy system validation                             ║
║                                                            ║
║  You are now certified in the ancient art of              ║
║  keeping the mainframe spirit alive... in the cloud! ☁️   ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

---

## 🆘 TROUBLESHOOTING

```
> ABEND CODE: U0001
> OPERATOR INTERVENTION REQUIRED
```

### Common Issues 🔧

#### COBOL Won't Compile 🟢

```
ABEND: COMPILATION FAILED
```

**Solution:** Check GnuCOBOL version:

```bash
cobc --version  # Should be 3.1+
```

#### Database Connection Fails 💾

```
ABEND: SQL ERROR 08001
```

**Solution:** Check firewall rules:

```bash
az sql server firewall-rule create \
  --resource-group appmod-rg \
  --server cobol-modernization-sql \
  --name AllowMyIP \
  --start-ip-address $(curl -s ifconfig.me) \
  --end-ip-address $(curl -s ifconfig.me)
```

#### Service Bus Messages Not Processing 🚌

```
ABEND: NO MESSAGES RECEIVED
```

**Solution:** Verify connection string and subscription:

```bash
az servicebus topic subscription show \
  --resource-group appmod-rg \
  --namespace-name continental-insurance-sb \
  --topic-name claims-submitted \
  --name adjudication-subscription
```

#### Container App Won't Start 🐳

```
ABEND: CONTAINER EXIT CODE 1
```

**Solution:** Check logs:

```bash
az containerapp logs show \
  --name claims-service \
  --resource-group appmod-rg \
  --tail 100
```

---

## 📚 ADDITIONAL RESOURCES

### Documentation 📖

- 📘 [GnuCOBOL Manual](https://gnucobol.sourceforge.io/doc/gnucobol.html)
- 📗 [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- 📕 [Drools Documentation](https://docs.drools.org/)
- 📙 [Azure Container Apps](https://learn.microsoft.com/en-us/azure/container-apps/)
- 📔 [Azure Service Bus](https://learn.microsoft.com/en-us/azure/service-bus-messaging/)

### Community 👥

- 💬 [r/COBOL](https://reddit.com/r/cobol) — Yes, it exists!
- 💬 [Spring Boot Discord](https://discord.gg/spring)
- 💬 [Azure Community](https://techcommunity.microsoft.com/t5/azure/ct-p/Azure)

---

## 🎬 CREDITS

```
╔════════════════════════════════════════════════════════╗
║                                                        ║
║  Lab Created By: Dana, Technical Writer               ║
║  Requested By: Marco Antonio Silva                    ║
║  Special Thanks: Continental Insurance Group          ║
║                  (for 30 years of COBOL inspiration)  ║
║                                                        ║
║  Powered By: ☕ Coffee, 🍕 Pizza, 🎮 Retro Vibes      ║
║                                                        ║
╚════════════════════════════════════════════════════════╝
```

---

<div align="center">

## 💚 READY. 💚

```
> _
```

**Now go forth and modernize!** 🚀

[![GitHub](https://img.shields.io/badge/GitHub-appmodlabs-181717?style=for-the-badge&logo=github)](https://github.com/your-org/appmodlabs)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

</div>
