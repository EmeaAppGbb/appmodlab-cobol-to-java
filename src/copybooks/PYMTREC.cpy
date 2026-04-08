      ******************************************************************
      * PAYMENT RECORD LAYOUT                                          *
      * FIXED-WIDTH RECORD FORMAT FOR PAYMENT AUTHORIZATIONS           *
      ******************************************************************
           05  PYMT-CLAIM-NUMBER    PIC X(10).
           05  PYMT-PAYMENT-AMOUNT  PIC 9(7)V99.
           05  PYMT-PAYMENT-DATE    PIC 9(8).
           05  PYMT-AUTH-CODE       PIC X(6).
           05  PYMT-STATUS          PIC X(1).
