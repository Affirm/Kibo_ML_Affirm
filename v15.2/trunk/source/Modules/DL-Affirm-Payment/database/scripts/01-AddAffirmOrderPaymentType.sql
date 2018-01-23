INSERT INTO ORDER_PAYMENT_TYPE(ORDER_PAYMENT_TYPE_ID,VERSION,NAME,CODE,DESCRIPTION,ACTIVE,DATE_CREATED)
VALUES((SELECT MAX_ID + 1 FROM MAX_ID WHERE TABLE_NAME='ORDER_PAYMENT_TYPE'),0,'AFFIRM','AFFIRM','AFFIRM',1,sysdate);

UPDATE MAX_ID
SET MAX_ID = MAX_ID + 1
WHERE TABLE_NAME='ORDER_PAYMENT_TYPE';

COMMIT;