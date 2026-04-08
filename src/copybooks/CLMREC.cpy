      ******************************************************************
      * CLAIM RECORD LAYOUT                                            *
      * FIXED-WIDTH RECORD FORMAT FOR INSURANCE CLAIMS                 *
      ******************************************************************
           05  CLM-CLAIM-NUMBER     PIC X(10).
           05  CLM-POLICY-NUMBER    PIC X(10).
           05  CLM-CLAIM-DATE       PIC 9(8).
           05  CLM-CLAIM-TYPE       PIC X(2).
           05  CLM-CLAIM-AMOUNT     PIC 9(7)V99.
           05  CLM-STATUS           PIC X(1).
           05  CLM-DIAGNOSIS-CODE   PIC X(5).
           05  CLM-PROVIDER-ID      PIC X(8).
