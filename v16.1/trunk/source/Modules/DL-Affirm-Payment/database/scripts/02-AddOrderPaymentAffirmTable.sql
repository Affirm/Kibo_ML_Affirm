--------------------------------------------------------
--  DDL for Table ORDER_PAYMENT_AMAZON
--------------------------------------------------------

  CREATE TABLE "ORDER_PAYMENT_AFFIRM" 
   (	
    "ORDER_PAYMENT_ID" NUMBER(10,0)
   );
--------------------------------------------------------
--  DDL for Index ORDER_PAYMENT_AFFIRM_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "ORDER_PAYMENT_AFFIRM_PK" ON "ORDER_PAYMENT_AFFIRM" ("ORDER_PAYMENT_ID");
--------------------------------------------------------
--  Constraints for Table ORDER_PAYMENT_AFFIRM
--------------------------------------------------------

  ALTER TABLE "ORDER_PAYMENT_AFFIRM" ADD CONSTRAINT "ORDER_PAYMENT_AFFIRM_PK" PRIMARY KEY ("ORDER_PAYMENT_ID") ENABLE;
 
  ALTER TABLE "ORDER_PAYMENT_AFFIRM" MODIFY ("ORDER_PAYMENT_ID" NOT NULL ENABLE);
 
--------------------------------------------------------
--  Ref Constraints for Table ORDER_PAYMENT_AFFIRM
--------------------------------------------------------

  ALTER TABLE "ORDER_PAYMENT_AFFIRM" ADD CONSTRAINT "ORDER_AFFIRM_PAYMENT_FK01" FOREIGN KEY ("ORDER_PAYMENT_ID")
	  REFERENCES "ORDER_PAYMENT" ("ORDER_PAYMENT_ID") ENABLE;
