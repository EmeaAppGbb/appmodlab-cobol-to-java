---
title: "COBOL to Java Modernization"
description: "Transform mainframe COBOL batch systems to Java microservices on Azure"
authors: ["marconsilva"]
category: "Code Modernization"
industry: "Financial Services"
services: ["Azure Container Apps", "Azure SQL Database", "Azure Service Bus", "Azure API Management"]
languages: ["COBOL", "Java"]
frameworks: ["Spring Boot", "Drools"]
modernizationTools: []
agenticTools: []
tags: ["mainframe-migration", "vsam", "batch-processing", "event-driven"]
extensions: ["github.copilot"]
thumbnail: ""
video: ""
version: "1.0.0"
---

## Legacy Application Overview

Continental Insurance Group's claims processing system is a batch-mode COBOL application built for mainframe environments. It runs inside a GnuCOBOL Docker container (`ubuntu:22.04` + `gnucobol`) and processes insurance claims through a classic orchestrator pattern — the main program (`CLMPROC`) calls subprograms compiled as shared modules, with data layouts defined in COBOL copybooks.

### Application Architecture

The application follows a classic mainframe architecture with a main orchestrator program, subprogram modules for adjudication, policy lookup, payment authorization, report generation, and error handling.

![COBOL Code Structure — Mainframe architecture with orchestrator, subprogram modules (.so), and copybook data layouts (.cpy)](assets/screenshots/cobol-code-structure.png)

### Claims Processing Pipeline

The main program processes 20 insurance claims from `data/claims.dat`, looking up each against `data/policies.dat`, running adjudication rules, and authorizing payments. All output is terminal-based (no web UI).

![COBOL Claims Processing Output — Batch processing of 20 insurance claims with policy lookup and adjudication](assets/screenshots/cobol-claims-processing-output.png)

### Batch Report Output

After processing, the system generates a formatted summary report showing approval/denial/pending rates and total payment amounts. Current results: 17 denied, 3 pending, 0 approved.

![COBOL Summary Report — Formatted batch report with approval/denial/pending rates and payment totals](assets/screenshots/cobol-summary-report.png)

---

## Initial Application Screenshots

The COBOL claims processing system was successfully built and executed inside a GnuCOBOL Docker container (`ubuntu:22.04` + `gnucobol`). This is a batch-mode CLI application — no web UI — so the screenshots below capture terminal output from the running program.

### Claims Processing Output

The main program (`CLMPROC`) processes 20 insurance claims from `data/claims.dat`, looking up each against `data/policies.dat`, running adjudication rules, and authorizing payments.

![COBOL Claims Processing Output](assets/screenshots/cobol-claims-processing-output.png)

### Summary Report

After processing, the system generates a formatted summary report showing approval/denial/pending rates and total payment amounts.

![COBOL Summary Report](assets/screenshots/cobol-summary-report.png)

### Legacy Code Structure

The application follows a classic mainframe architecture: a main orchestrator program (`CLMPROC`) calls subprograms compiled as shared modules (`.so`), with data layouts defined in COBOL copybooks (`.cpy`).

![COBOL Code Structure](assets/screenshots/cobol-code-structure.png)

### Build & Run Notes

- **Docker image**: `continental-insurance/claims-processor:latest` (Ubuntu 22.04 + GnuCOBOL)
- **Compiler flags**: `cobc -std=mf -I./src/copybooks -fixed` (Micro Focus dialect, fixed-format)
- **Runtime**: Batch process — container starts, processes all claims, prints report, exits
- **No exposed ports**: This is a batch job, not a web service
- **Data format**: Fixed-width flat files simulating VSAM indexed datasets
- **Key finding**: All 20 claims processed; 17 denied, 3 pending review, 0 approved — suggests adjudication rules need tuning or test data alignment
