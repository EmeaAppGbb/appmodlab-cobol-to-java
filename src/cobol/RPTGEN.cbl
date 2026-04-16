       IDENTIFICATION DIVISION.
       PROGRAM-ID. RPTGEN.
       AUTHOR. CONTINENTAL INSURANCE GROUP.
      ******************************************************************
      * REPORT GENERATION PROGRAM                                      *
      * PRODUCES FORMATTED SUMMARY REPORTS                             *
      ******************************************************************
       
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-REPORT-LINE           PIC X(132).
       01  WS-LINE-COUNT            PIC 9(3) VALUE 0.
       01  WS-PAGE-COUNT            PIC 9(3) VALUE 1.
       
       01  WS-HEADER-LINE-1.
           05  FILLER               PIC X(50) VALUE SPACES.
           05  FILLER               PIC X(32) 
               VALUE "CONTINENTAL INSURANCE GROUP".
           05  FILLER               PIC X(50) VALUE SPACES.
       
       01  WS-HEADER-LINE-2.
           05  FILLER               PIC X(48) VALUE SPACES.
           05  FILLER               PIC X(36) 
               VALUE "CLAIMS PROCESSING SUMMARY REPORT".
           05  FILLER               PIC X(48) VALUE SPACES.
       
       01  WS-HEADER-LINE-3.
           05  FILLER               PIC X(10) VALUE "PAGE: ".
           05  WS-HDR-PAGE          PIC ZZ9.
           05  FILLER               PIC X(40) VALUE SPACES.
           05  FILLER               PIC X(10) VALUE "DATE: ".
           05  WS-HDR-DATE          PIC X(10).
           05  FILLER               PIC X(59) VALUE SPACES.
       
       01  WS-SEPARATOR             PIC X(132) VALUE ALL "=".
       
       01  WS-DETAIL-LINE-1.
           05  FILLER  PIC X(30) VALUE "TOTAL CLAIMS PROCESSED: ".
           05  WS-DTL-TOTAL         PIC ZZZ,ZZ9.
           05  FILLER               PIC X(96) VALUE SPACES.
       
       01  WS-DETAIL-LINE-2.
           05  FILLER  PIC X(30) VALUE "CLAIMS APPROVED:        ".
           05  WS-DTL-APPROVED      PIC ZZZ,ZZ9.
           05  FILLER               PIC X(10) VALUE SPACES.
           05  FILLER               PIC X(5) VALUE "(   ".
           05  WS-DTL-APPR-PCT      PIC ZZ9.
           05  FILLER               PIC X(2) VALUE "%)".
           05  FILLER               PIC X(79) VALUE SPACES.
       
       01  WS-DETAIL-LINE-3.
           05  FILLER  PIC X(30) VALUE "CLAIMS DENIED:          ".
           05  WS-DTL-DENIED        PIC ZZZ,ZZ9.
           05  FILLER               PIC X(10) VALUE SPACES.
           05  FILLER               PIC X(5) VALUE "(   ".
           05  WS-DTL-DENY-PCT      PIC ZZ9.
           05  FILLER               PIC X(2) VALUE "%)".
           05  FILLER               PIC X(79) VALUE SPACES.
       
       01  WS-DETAIL-LINE-4.
           05  FILLER  PIC X(30) VALUE "CLAIMS PENDING:         ".
           05  WS-DTL-PENDING       PIC ZZZ,ZZ9.
           05  FILLER               PIC X(10) VALUE SPACES.
           05  FILLER               PIC X(5) VALUE "(   ".
           05  WS-DTL-PEND-PCT      PIC ZZ9.
           05  FILLER               PIC X(2) VALUE "%)".
           05  FILLER               PIC X(79) VALUE SPACES.
       
       01  WS-DETAIL-LINE-5.
           05  FILLER               PIC X(30) VALUE SPACES.
           05  FILLER               PIC X(102) VALUE SPACES.
       
       01  WS-DETAIL-LINE-6.
           05  FILLER  PIC X(30) VALUE "TOTAL PAYMENTS:         $".
           05  WS-DTL-PAYMENTS      PIC ZZZ,ZZZ,ZZ9.99.
           05  FILLER               PIC X(87) VALUE SPACES.
       
       01  WS-CALC-FIELDS.
           05  WS-APPROVED-PCT      PIC 9(3).
           05  WS-DENIED-PCT        PIC 9(3).
           05  WS-PENDING-PCT       PIC 9(3).
       
       01  WS-CURRENT-DATE.
           05  WS-CURR-YEAR         PIC 9(4).
           05  WS-CURR-MONTH        PIC 9(2).
           05  WS-CURR-DAY          PIC 9(2).
       
       01  WS-FORMATTED-DATE.
           05  WS-FMT-MONTH         PIC 99.
           05  FILLER               PIC X VALUE "/".
           05  WS-FMT-DAY           PIC 99.
           05  FILLER               PIC X VALUE "/".
           05  WS-FMT-YEAR          PIC 9(4).

       LINKAGE SECTION.
       01  LS-TOTAL-CLAIMS          PIC 9(5).
       01  LS-APPROVED-CLAIMS       PIC 9(5).
       01  LS-DENIED-CLAIMS         PIC 9(5).
       01  LS-PENDING-CLAIMS        PIC 9(5).
       01  LS-TOTAL-PAID            PIC 9(9)V99.
       01  LS-REPORT-FILE           PIC X(132).

       PROCEDURE DIVISION USING LS-TOTAL-CLAIMS
                                LS-APPROVED-CLAIMS
                                LS-DENIED-CLAIMS
                                LS-PENDING-CLAIMS
                                LS-TOTAL-PAID
                                LS-REPORT-FILE.
       
       0000-MAIN-REPORT.
           PERFORM 1000-CALCULATE-PERCENTAGES
           PERFORM 2000-FORMAT-REPORT
           PERFORM 3000-WRITE-REPORT
           GOBACK.

       1000-CALCULATE-PERCENTAGES.
           IF LS-TOTAL-CLAIMS > 0
               COMPUTE WS-APPROVED-PCT = 
                   (LS-APPROVED-CLAIMS / LS-TOTAL-CLAIMS) * 100
               COMPUTE WS-DENIED-PCT = 
                   (LS-DENIED-CLAIMS / LS-TOTAL-CLAIMS) * 100
               COMPUTE WS-PENDING-PCT = 
                   (LS-PENDING-CLAIMS / LS-TOTAL-CLAIMS) * 100
           ELSE
               MOVE ZERO TO WS-APPROVED-PCT
               MOVE ZERO TO WS-DENIED-PCT
               MOVE ZERO TO WS-PENDING-PCT
           END-IF.

       2000-FORMAT-REPORT.
           ACCEPT WS-CURRENT-DATE FROM DATE YYYYMMDD
           MOVE WS-CURR-MONTH TO WS-FMT-MONTH
           MOVE WS-CURR-DAY TO WS-FMT-DAY
           MOVE WS-CURR-YEAR TO WS-FMT-YEAR
           MOVE WS-FORMATTED-DATE TO WS-HDR-DATE
           MOVE WS-PAGE-COUNT TO WS-HDR-PAGE
           
           MOVE LS-TOTAL-CLAIMS TO WS-DTL-TOTAL
           MOVE LS-APPROVED-CLAIMS TO WS-DTL-APPROVED
           MOVE LS-DENIED-CLAIMS TO WS-DTL-DENIED
           MOVE LS-PENDING-CLAIMS TO WS-DTL-PENDING
           MOVE LS-TOTAL-PAID TO WS-DTL-PAYMENTS
           
           MOVE WS-APPROVED-PCT TO WS-DTL-APPR-PCT
           MOVE WS-DENIED-PCT TO WS-DTL-DENY-PCT
           MOVE WS-PENDING-PCT TO WS-DTL-PEND-PCT.

       3000-WRITE-REPORT.
           DISPLAY WS-HEADER-LINE-1
           DISPLAY WS-HEADER-LINE-2
           DISPLAY WS-HEADER-LINE-3
           DISPLAY WS-SEPARATOR
           DISPLAY SPACES
           DISPLAY WS-DETAIL-LINE-1
           DISPLAY SPACES
           DISPLAY WS-DETAIL-LINE-2
           DISPLAY WS-DETAIL-LINE-3
           DISPLAY WS-DETAIL-LINE-4
           DISPLAY WS-DETAIL-LINE-5
           DISPLAY WS-DETAIL-LINE-6
           DISPLAY SPACES
           DISPLAY WS-SEPARATOR.
