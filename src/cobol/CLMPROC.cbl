       IDENTIFICATION DIVISION.
       PROGRAM-ID. CLMPROC.
       AUTHOR. CONTINENTAL INSURANCE GROUP.
      ******************************************************************
      * MAIN CLAIMS PROCESSING PROGRAM                                 *
      * READS CLAIMS FILE AND POLICY FILE                              *
      * ORCHESTRATES CLAIM ADJUDICATION AND PAYMENT AUTHORIZATION       *
      ******************************************************************
       
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT CLAIM-FILE
               ASSIGN TO "data/claims.dat"
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-CLAIM-STATUS.
           
           SELECT POLICY-FILE
               ASSIGN TO "data/policies.dat"
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-POLICY-STATUS.
           
           SELECT SUMMARY-REPORT
               ASSIGN TO "reports/summary.txt"
               ORGANIZATION IS LINE SEQUENTIAL.

       DATA DIVISION.
       FILE SECTION.
       FD  CLAIM-FILE.
       01  CLAIM-RECORD.
           COPY CLMREC.
       
       FD  POLICY-FILE.
       01  POLICY-RECORD.
           COPY POLREC.
       
       FD  SUMMARY-REPORT.
       01  REPORT-LINE              PIC X(132).

       WORKING-STORAGE SECTION.
       01  WS-CLAIM-STATUS          PIC XX.
       01  WS-POLICY-STATUS         PIC XX.
       01  WS-EOF-CLAIM             PIC X VALUE 'N'.
           88 EOF-CLAIM             VALUE 'Y'.
       01  WS-EOF-POLICY            PIC X VALUE 'N'.
           88 EOF-POLICY            VALUE 'Y'.
       
       01  WS-COUNTERS.
           05  WS-CLAIMS-READ       PIC 9(5) VALUE 0.
           05  WS-CLAIMS-APPROVED   PIC 9(5) VALUE 0.
           05  WS-CLAIMS-DENIED     PIC 9(5) VALUE 0.
           05  WS-CLAIMS-PENDING    PIC 9(5) VALUE 0.
           05  WS-TOTAL-PAID        PIC 9(9)V99 VALUE 0.
       
       01  WS-POLICY-FOUND          PIC X VALUE 'N'.
           88 POLICY-FOUND          VALUE 'Y'.
           88 POLICY-NOT-FOUND      VALUE 'N'.
       
       01  WS-ADJUDICATION-RESULT   PIC X.
           88 CLAIM-APPROVED        VALUE 'A'.
           88 CLAIM-DENIED          VALUE 'D'.
           88 CLAIM-PENDING         VALUE 'P'.
       
       01  WS-PAYMENT-AMOUNT        PIC 9(7)V99.
       01  WS-ERROR-CODE            PIC 99.
       
       01  WS-CURRENT-DATE-FIELDS.
           05  WS-CURRENT-DATE      PIC 9(8).
           05  WS-CURRENT-TIME      PIC 9(6).

       PROCEDURE DIVISION.
       0000-MAIN-PROCESSING.
           PERFORM 1000-INITIALIZATION
           PERFORM 2000-PROCESS-CLAIMS UNTIL EOF-CLAIM
           PERFORM 3000-GENERATE-SUMMARY
           PERFORM 9000-CLEANUP
           STOP RUN.

       1000-INITIALIZATION.
           DISPLAY "CONTINENTAL INSURANCE - CLAIMS PROCESSING"
           DISPLAY "INITIALIZING SYSTEM..."
           
           OPEN INPUT CLAIM-FILE
           IF WS-CLAIM-STATUS NOT = "00"
               DISPLAY "ERROR OPENING CLAIMS FILE: " WS-CLAIM-STATUS
               CALL 'ERRHANDL' USING BY CONTENT 10
                                     BY CONTENT WS-CLAIM-STATUS
               STOP RUN
           END-IF
           
           OPEN INPUT POLICY-FILE
           IF WS-POLICY-STATUS NOT = "00"
               DISPLAY "ERROR OPENING POLICY FILE: " WS-POLICY-STATUS
               CALL 'ERRHANDL' USING BY CONTENT 11
                                     BY CONTENT WS-POLICY-STATUS
               STOP RUN
           END-IF
           
           OPEN OUTPUT SUMMARY-REPORT
           
           ACCEPT WS-CURRENT-DATE FROM DATE YYYYMMDD
           ACCEPT WS-CURRENT-TIME FROM TIME
           
           DISPLAY "PROCESSING DATE: " WS-CURRENT-DATE
           DISPLAY "PROCESSING TIME: " WS-CURRENT-TIME
           DISPLAY " ".

       2000-PROCESS-CLAIMS.
           READ CLAIM-FILE
               AT END
                   SET EOF-CLAIM TO TRUE
               NOT AT END
                   PERFORM 2100-PROCESS-SINGLE-CLAIM
           END-READ.

       2100-PROCESS-SINGLE-CLAIM.
           ADD 1 TO WS-CLAIMS-READ
           
           DISPLAY "PROCESSING CLAIM: " CLM-CLAIM-NUMBER
           
      *    LOOKUP POLICY
           MOVE 'N' TO WS-POLICY-FOUND
           CALL 'POLYLKUP' USING CLM-POLICY-NUMBER
                                 POLICY-RECORD
                                 WS-POLICY-FOUND
           
           IF POLICY-NOT-FOUND
               DISPLAY "  POLICY NOT FOUND: " CLM-POLICY-NUMBER
               CALL 'ERRHANDL' USING BY CONTENT 20
                                     BY CONTENT CLM-CLAIM-NUMBER
               ADD 1 TO WS-CLAIMS-DENIED
               GO TO 2100-EXIT
           END-IF
           
      *    ADJUDICATE CLAIM
           CALL 'ADJUDCTN' USING CLAIM-RECORD
                                 POLICY-RECORD
                                 WS-ADJUDICATION-RESULT
           
           EVALUATE TRUE
               WHEN CLAIM-APPROVED
                   DISPLAY "  CLAIM APPROVED"
                   ADD 1 TO WS-CLAIMS-APPROVED
                   PERFORM 2200-AUTHORIZE-PAYMENT
               WHEN CLAIM-DENIED
                   DISPLAY "  CLAIM DENIED"
                   ADD 1 TO WS-CLAIMS-DENIED
               WHEN CLAIM-PENDING
                   DISPLAY "  CLAIM PENDING REVIEW"
                   ADD 1 TO WS-CLAIMS-PENDING
           END-EVALUATE.
           
       2100-EXIT.
           EXIT.

       2200-AUTHORIZE-PAYMENT.
           MOVE ZERO TO WS-PAYMENT-AMOUNT
           
           CALL 'PYMTAUTH' USING CLAIM-RECORD
                                 POLICY-RECORD
                                 WS-PAYMENT-AMOUNT
           
           IF WS-PAYMENT-AMOUNT > 0
               ADD WS-PAYMENT-AMOUNT TO WS-TOTAL-PAID
               DISPLAY "  PAYMENT AUTHORIZED: $" WS-PAYMENT-AMOUNT
           END-IF.

       3000-GENERATE-SUMMARY.
           DISPLAY " "
           DISPLAY "GENERATING SUMMARY REPORT..."
           
           CALL 'RPTGEN' USING WS-CLAIMS-READ
                               WS-CLAIMS-APPROVED
                               WS-CLAIMS-DENIED
                               WS-CLAIMS-PENDING
                               WS-TOTAL-PAID
                               SUMMARY-REPORT.

       9000-CLEANUP.
           CLOSE CLAIM-FILE
           CLOSE POLICY-FILE
           CLOSE SUMMARY-REPORT
           
           DISPLAY " "
           DISPLAY "PROCESSING COMPLETE"
           DISPLAY "TOTAL CLAIMS PROCESSED: " WS-CLAIMS-READ
           DISPLAY "APPROVED: " WS-CLAIMS-APPROVED
           DISPLAY "DENIED:   " WS-CLAIMS-DENIED
           DISPLAY "PENDING:  " WS-CLAIMS-PENDING
           DISPLAY "TOTAL PAID: $" WS-TOTAL-PAID.
