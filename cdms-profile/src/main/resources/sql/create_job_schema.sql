DECLARE
  jobno NUMBER;

BEGIN
  dbms_job.submit(
      jobno,
      'DELETE FROM SYSTEM.CDM_PROFILE_MULTICONTEXT WHERE MULTICONTEXT_ID = (SELECT MULTICONTEXT_ID FROM SYSTEM.CDM_PROFILE_TIMEMEASUREMENT WHERE TIMESTAMP < SYSDATE - 15); DELETE FROM SYSTEM.CDM_PROFILE_TIMEMEASUREMENT WHERE TIMESTAMP < SYSDATE - 15;',
      sysdate,
      'trunc(sysdate)+1+4/24');
END;