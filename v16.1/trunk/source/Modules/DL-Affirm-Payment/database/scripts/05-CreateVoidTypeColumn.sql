SET DEFINE OFF
set serveroutput on
set scan off


--DB upgrade script - 14.2.MR - To add VOID_TYPE column in VOID table

----------------------------
-- START : PAYMENT_VOID Table -
----------------------------
set serveroutput on
DECLARE table_exists NUMBER(1);
column_exists number(1);
BEGIN
select count(*) into table_exists from user_tables where table_name = 'PAYMENT_VOID';
if table_exists = 1
then

 select count(*) into column_exists from user_tab_columns where table_name = 'PAYMENT_VOID'
 and column_name  = 'VOID_TYPE';

   if (column_exists = 0) then
   execute immediate 'ALTER TABLE PAYMENT_VOID ADD (VOID_TYPE VARCHAR2(255 CHAR) )';

   dbms_output.put_line( 'Added column VOID_TYPE in Table PAYMENT_VOID  in this schema');

  
  else
   dbms_output.put_line( 'Column VOID_TYPE  in table PAYMENT_VOID already exists in this  schema');

  end if;
  
else
	dbms_output.put_line( 'Table PAYMENT_VOID  does not  exist in this schema');
end if;

end;
/
