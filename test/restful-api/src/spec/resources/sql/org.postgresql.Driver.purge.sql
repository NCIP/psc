CREATE OR REPLACE FUNCTION drop_db()
RETURNS void AS
$BODY$

DECLARE
objname character varying;

BEGIN

  FOR objname IN SELECT pg_catalog.pg_class.relname FROM pg_catalog.pg_class
                 LEFT JOIN pg_catalog.pg_namespace ON pg_catalog.pg_namespace.oid = pg_catalog.pg_class.relnamespace
			     WHERE relkind ='r' AND pg_catalog.pg_namespace.nspname = 'public'  LOOP
    EXECUTE 'DROP TABLE IF EXISTS ' || objname || ' CASCADE ';
  END LOOP;

  FOR objname IN SELECT pg_catalog.pg_class.relname FROM pg_catalog.pg_class
			   LEFT JOIN pg_catalog.pg_namespace ON pg_catalog.pg_namespace.oid = pg_catalog.pg_class.relnamespace
			   WHERE relkind ='S' AND pg_catalog.pg_namespace.nspname = 'public' LOOP
    EXECUTE 'DROP SEQUENCE IF EXISTS ' || objname;
  END LOOP;

END;
$BODY$
LANGUAGE plpgsql VOLATILE
/

SELECT drop_db()
/
