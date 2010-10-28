-- From http://stackoverflow.com/questions/842530/plsql-drop-all-database-objects-of-a-user

create or replace
FUNCTION DROP_ALL_SCHEMA_OBJECTS RETURN NUMBER AS
PRAGMA AUTONOMOUS_TRANSACTION;
cursor c_get_objects is
  select object_type,'"'||object_name||'"'||decode(object_type,'TABLE' ,' cascade constraints',null) obj_name
  from user_objects
  where object_type in ('TABLE','VIEW','PACKAGE','SEQUENCE','SYNONYM', 'MATERIALIZED VIEW')
  order by object_type;
cursor c_get_objects_type is
  select object_type, '"'||object_name||'"' obj_name
  from user_objects
  where object_type in ('TYPE');
BEGIN
  begin
    for object_rec in c_get_objects loop
      execute immediate ('drop '||object_rec.object_type||' ' ||object_rec.obj_name);
    end loop;
    for object_rec in c_get_objects_type loop
      begin
        execute immediate ('drop '||object_rec.object_type||' ' ||object_rec.obj_name);
      end;
    end loop;
  end;
  RETURN 0;
END DROP_ALL_SCHEMA_OBJECTS;
/

PURGE RECYCLEBIN
/

SELECT DROP_ALL_SCHEMA_OBJECTS FROM DUAL
/

-- Belt and suspenders
PURGE RECYCLEBIN
/

DROP FUNCTION DROP_ALL_SCHEMA_OBJECTS
/