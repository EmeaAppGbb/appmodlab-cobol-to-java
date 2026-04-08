       IDENTIFICATION DIVISION.
       PROGRAM-ID. ERRHANDL.
       AUTHOR. CONTINENTAL INSURANCE GROUP.
      ******************************************************************
      * ERROR HANDLING ROUTINES                                        *
      * CENTRALIZES ERROR LOGGING AND REPORTING                        *
      ******************************************************************
       
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-ERROR-LOG-FILE        PIC X(30) 
           VALUE "reports/error.log".
       01  WS-ERROR-MESSAGE         PIC X(100).
       01  WS-TIMESTAMP.
           05  WS-TS-DATE           PIC X(10).
           05  WS-TS-TIME           PIC X(8).
       
       01  WS-FORMATTED-TS.
           05  WS-FMT-YEAR          PIC 9(4).
           05  FILLER               PIC X VALUE "-".
           05  WS-FMT-MONTH         PIC 99.
           05  FILLER               PIC X VALUE "-".
           05  WS-FMT-DAY           PIC 99.
           05  FILLER               PIC X VALUE " ".
           05  WS-FMT-HOUR          PIC 99.
           05  FILLER               PIC X VALUE ":".
           05  WS-FMT-MIN           PIC 99.
           05  FILLER               PIC X VALUE ":".
           05  WS-FMT-SEC           PIC 99.

       LINKAGE SECTION.
       01  LS-ERROR-CODE            PIC 99.
       01  LS-ERROR-DATA            PIC X(20).

       PROCEDURE DIVISION USING LS-ERROR-CODE LS-ERROR-DATA.
       
       0000-HANDLE-ERROR.
           PERFORM 1000-GET-TIMESTAMP
           PERFORM 2000-FORMAT-ERROR-MESSAGE
           PERFORM 3000-DISPLAY-ERROR
           GOBACK.

       1000-GET-TIMESTAMP.
           ACCEPT WS-TS-DATE FROM DATE YYYYMMDD
           ACCEPT WS-TS-TIME FROM TIME
           
           MOVE WS-TS-DATE(1:4) TO WS-FMT-YEAR
           MOVE WS-TS-DATE(5:2) TO WS-FMT-MONTH
           MOVE WS-TS-DATE(7:2) TO WS-FMT-DAY
           MOVE WS-TS-TIME(1:2) TO WS-FMT-HOUR
           MOVE WS-TS-TIME(3:2) TO WS-FMT-MIN
           MOVE WS-TS-TIME(5:2) TO WS-FMT-SEC.

       2000-FORMAT-ERROR-MESSAGE.
           EVALUATE LS-ERROR-CODE
               WHEN 10
                   STRING "FATAL: Cannot open claims file - "
                          LS-ERROR-DATA
                          DELIMITED BY SIZE
                          INTO WS-ERROR-MESSAGE
                   END-STRING
               WHEN 11
                   STRING "FATAL: Cannot open policy file - "
                          LS-ERROR-DATA
                          DELIMITED BY SIZE
                          INTO WS-ERROR-MESSAGE
                   END-STRING
               WHEN 20
                   STRING "ERROR: Policy not found for claim "
                          LS-ERROR-DATA
                          DELIMITED BY SIZE
                          INTO WS-ERROR-MESSAGE
                   END-STRING
               WHEN 30
                   STRING "WARNING: Invalid claim data - "
                          LS-ERROR-DATA
                          DELIMITED BY SIZE
                          INTO WS-ERROR-MESSAGE
                   END-STRING
               WHEN OTHER
                   STRING "ERROR: Unknown error code "
                          LS-ERROR-CODE " - " LS-ERROR-DATA
                          DELIMITED BY SIZE
                          INTO WS-ERROR-MESSAGE
                   END-STRING
           END-EVALUATE.

       3000-DISPLAY-ERROR.
           DISPLAY WS-FORMATTED-TS " " WS-ERROR-MESSAGE.
