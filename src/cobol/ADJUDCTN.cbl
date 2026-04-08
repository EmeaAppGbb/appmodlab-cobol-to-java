       IDENTIFICATION DIVISION.
       PROGRAM-ID. ADJUDCTN.
       AUTHOR. CONTINENTAL INSURANCE GROUP.
      ******************************************************************
      * ADJUDICATION RULES ENGINE                                      *
      * APPLIES BUSINESS RULES TO DETERMINE CLAIM APPROVAL             *
      * CONTAINS HARDCODED RULES AND GOTO-BASED CONTROL FLOW           *
      ******************************************************************
       
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       COPY ERRCODES.
       
       01  WS-CALCULATED-COVERAGE   PIC 9(9)V99.
       01  WS-DEDUCTIBLE-MET        PIC X VALUE 'N'.
           88 DEDUCTIBLE-SATISFIED  VALUE 'Y'.
       01  WS-CLAIM-AGE-DAYS        PIC 9(5).
       01  WS-POLICY-ACTIVE         PIC X VALUE 'N'.
           88 POLICY-IS-ACTIVE      VALUE 'Y'.
       
      * HARDCODED BUSINESS RULE CONSTANTS
       01  WS-MAX-CLAIM-AGE         PIC 9(3) VALUE 90.
       01  WS-MIN-CLAIM-AMOUNT      PIC 9(5)V99 VALUE 50.00.
       01  WS-MAX-AUTO-APPROVE      PIC 9(7)V99 VALUE 5000.00.
       01  WS-DENTAL-MAX            PIC 9(5)V99 VALUE 2500.00.
       01  WS-VISION-MAX            PIC 9(4)V99 VALUE 500.00.
       01  WS-MANUAL-REVIEW-LIMIT   PIC 9(7)V99 VALUE 25000.00.

       LINKAGE SECTION.
       01  LS-CLAIM-RECORD.
           COPY CLMREC.
       01  LS-POLICY-RECORD.
           COPY POLREC.
       01  LS-RESULT                PIC X.
           88 LS-APPROVED           VALUE 'A'.
           88 LS-DENIED             VALUE 'D'.
           88 LS-PENDING            VALUE 'P'.

       PROCEDURE DIVISION USING LS-CLAIM-RECORD
                                LS-POLICY-RECORD
                                LS-RESULT.
       
       0000-ADJUDICATE-CLAIM.
           MOVE 'D' TO LS-RESULT
           
           PERFORM 1000-VALIDATE-POLICY-STATUS
           IF NOT POLICY-IS-ACTIVE
               GO TO 8000-DENY-CLAIM
           END-IF
           
           PERFORM 2000-CHECK-CLAIM-AMOUNT
           PERFORM 3000-CHECK-CLAIM-TYPE
           PERFORM 4000-APPLY-COVERAGE-RULES
           
           GO TO 9000-EXIT.

       1000-VALIDATE-POLICY-STATUS.
      *    CHECK IF POLICY IS ACTIVE
           IF POL-STATUS = 'A'
               MOVE 'Y' TO WS-POLICY-ACTIVE
           ELSE
               MOVE 'N' TO WS-POLICY-ACTIVE
               GO TO 1000-EXIT
           END-IF
           
      *    CHECK POLICY DATES (SIMPLIFIED - ASSUMES YYYYMMDD)
           IF CLM-CLAIM-DATE < POL-EFFECTIVE-DATE
               MOVE 'N' TO WS-POLICY-ACTIVE
               GO TO 1000-EXIT
           END-IF
           
           IF CLM-CLAIM-DATE > POL-EXPIRY-DATE
               MOVE 'N' TO WS-POLICY-ACTIVE
           END-IF.
           
       1000-EXIT.
           EXIT.

       2000-CHECK-CLAIM-AMOUNT.
      *    ANTI-PATTERN: GOTO-BASED VALIDATION
           IF CLM-CLAIM-AMOUNT < WS-MIN-CLAIM-AMOUNT
               GO TO 8000-DENY-CLAIM
           END-IF
           
           IF CLM-CLAIM-AMOUNT > WS-MANUAL-REVIEW-LIMIT
               GO TO 8500-REQUIRE-MANUAL-REVIEW
           END-IF.

       3000-CHECK-CLAIM-TYPE.
      *    HARDCODED CLAIM TYPE RULES
      *    01=MEDICAL 02=DENTAL 03=VISION 04=PHARMACY
           
           EVALUATE CLM-CLAIM-TYPE
               WHEN "01"
                   PERFORM 3100-CHECK-MEDICAL-RULES
               WHEN "02"
                   PERFORM 3200-CHECK-DENTAL-RULES
               WHEN "03"
                   PERFORM 3300-CHECK-VISION-RULES
               WHEN "04"
                   PERFORM 3400-CHECK-PHARMACY-RULES
               WHEN OTHER
                   GO TO 8000-DENY-CLAIM
           END-EVALUATE.

       3100-CHECK-MEDICAL-RULES.
      *    MEDICAL CLAIMS - CHECK DIAGNOSIS CODE
           IF CLM-DIAGNOSIS-CODE = "00000" OR SPACES
               GO TO 8000-DENY-CLAIM
           END-IF
           
      *    REQUIRE PROVIDER ID
           IF CLM-PROVIDER-ID = SPACES
               GO TO 8000-DENY-CLAIM
           END-IF.

       3200-CHECK-DENTAL-RULES.
      *    DENTAL HAS LOWER MAXIMUM
           IF CLM-CLAIM-AMOUNT > WS-DENTAL-MAX
               IF POL-PLAN-TYPE NOT = "PR"
                   GO TO 8000-DENY-CLAIM
               END-IF
           END-IF.

       3300-CHECK-VISION-RULES.
      *    VISION HAS STRICT LIMITS
           IF CLM-CLAIM-AMOUNT > WS-VISION-MAX
               GO TO 8000-DENY-CLAIM
           END-IF.

       3400-CHECK-PHARMACY-RULES.
      *    PHARMACY REQUIRES PROVIDER
           IF CLM-PROVIDER-ID = SPACES
               GO TO 8000-DENY-CLAIM
           END-IF.

       4000-APPLY-COVERAGE-RULES.
      *    CALCULATE COVERAGE AFTER DEDUCTIBLE
           COMPUTE WS-CALCULATED-COVERAGE = 
               CLM-CLAIM-AMOUNT - POL-DEDUCTIBLE
           END-COMPUTE
           
           IF WS-CALCULATED-COVERAGE <= 0
               GO TO 8000-DENY-CLAIM
           END-IF
           
           IF WS-CALCULATED-COVERAGE > POL-MAX-COVERAGE
               GO TO 8500-REQUIRE-MANUAL-REVIEW
           END-IF
           
      *    AUTO-APPROVE IF UNDER THRESHOLD
           IF CLM-CLAIM-AMOUNT <= WS-MAX-AUTO-APPROVE
               GO TO 9900-APPROVE-CLAIM
           END-IF
           
      *    OTHERWISE REQUIRES MANUAL REVIEW
           GO TO 8500-REQUIRE-MANUAL-REVIEW.

       8000-DENY-CLAIM.
           MOVE 'D' TO LS-RESULT
           GO TO 9000-EXIT.

       8500-REQUIRE-MANUAL-REVIEW.
           MOVE 'P' TO LS-RESULT
           GO TO 9000-EXIT.

       9900-APPROVE-CLAIM.
           MOVE 'A' TO LS-RESULT.

       9000-EXIT.
           GOBACK.
