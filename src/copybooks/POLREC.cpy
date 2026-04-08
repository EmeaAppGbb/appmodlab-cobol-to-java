      ******************************************************************
      * POLICY RECORD LAYOUT                                           *
      * FIXED-WIDTH RECORD FORMAT FOR INSURANCE POLICIES               *
      ******************************************************************
           05  POL-POLICY-NUMBER    PIC X(10).
           05  POL-HOLDER-NAME      PIC X(30).
           05  POL-PLAN-TYPE        PIC X(2).
           05  POL-EFFECTIVE-DATE   PIC 9(8).
           05  POL-EXPIRY-DATE      PIC 9(8).
           05  POL-DEDUCTIBLE       PIC 9(5)V99.
           05  POL-MAX-COVERAGE     PIC 9(7)V99.
           05  POL-STATUS           PIC X(1).
