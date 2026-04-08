       IDENTIFICATION DIVISION.
       PROGRAM-ID. POLYLKUP.
       AUTHOR. CONTINENTAL INSURANCE GROUP.
      ******************************************************************
      * POLICY LOOKUP SUBROUTINE                                       *
      * READS POLICY FILE TO FIND MATCHING POLICY NUMBER               *
      * SIMULATES VSAM INDEXED FILE ACCESS                             *
      ******************************************************************
       
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT POLICY-MASTER
               ASSIGN TO "data/policies.dat"
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-FILE-STATUS.

       DATA DIVISION.
       FILE SECTION.
       FD  POLICY-MASTER.
       01  POL-MASTER-REC.
           COPY POLREC.

       WORKING-STORAGE SECTION.
       01  WS-FILE-STATUS           PIC XX.
       01  WS-EOF                   PIC X VALUE 'N'.
           88 EOF-POLICIES          VALUE 'Y'.
       01  WS-SEARCH-COUNT          PIC 9(4) VALUE 0.
       01  WS-MAX-SEARCH            PIC 9(4) VALUE 1000.

       LINKAGE SECTION.
       01  LS-POLICY-NUMBER         PIC X(10).
       01  LS-POLICY-RECORD.
           COPY POLREC.
       01  LS-FOUND-FLAG            PIC X.

       PROCEDURE DIVISION USING LS-POLICY-NUMBER
                                LS-POLICY-RECORD
                                LS-FOUND-FLAG.
       
       0000-MAIN-LOOKUP.
           PERFORM 1000-OPEN-FILE
           PERFORM 2000-SEARCH-POLICY
           PERFORM 9000-CLOSE-FILE
           GOBACK.

       1000-OPEN-FILE.
           OPEN INPUT POLICY-MASTER
           IF WS-FILE-STATUS NOT = "00"
               DISPLAY "POLYLKUP ERROR: CANNOT OPEN POLICY FILE"
               MOVE 'N' TO LS-FOUND-FLAG
               GOBACK
           END-IF
           MOVE 'N' TO WS-EOF
           MOVE ZERO TO WS-SEARCH-COUNT.

       2000-SEARCH-POLICY.
           PERFORM 2100-READ-POLICY UNTIL EOF-POLICIES
                                        OR LS-FOUND-FLAG = 'Y'
                                        OR WS-SEARCH-COUNT > WS-MAX-SEARCH.

       2100-READ-POLICY.
           READ POLICY-MASTER
               AT END
                   SET EOF-POLICIES TO TRUE
               NOT AT END
                   ADD 1 TO WS-SEARCH-COUNT
                   PERFORM 2200-CHECK-MATCH
           END-READ.

       2200-CHECK-MATCH.
           IF POL-POLICY-NUMBER = LS-POLICY-NUMBER
               MOVE POL-MASTER-REC TO LS-POLICY-RECORD
               MOVE 'Y' TO LS-FOUND-FLAG
           END-IF.

       9000-CLOSE-FILE.
           CLOSE POLICY-MASTER.
