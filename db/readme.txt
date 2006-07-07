Setting up a database
---------------------
To set up a database, copy the file "studycalendar.properties.example" to "studycalendar.properties".
Then edit studycalendar.properties to match your database.  In particular, you'll need to:

  * Set `datasource.url` to the JDBC URL for your database
  * Set the username and password to use to access this URL
  * Uncomment the database configuration block that corresponds to your database type

Switching between databases
---------------------------
If you want to maintain configurations for several different databases, create copies of
studycalendar.properties (appropriately configured) with different names.  You can then build for
a different database by providing the `config.database` property to ant.  E.g.:

  $ ant -Dconfig.database=oracle compile

This will use a file called oracle.properties.  There are ant tasks to simplify specifying which
file to use for the supported databases:

  $ ant oracle compile
  $ ant postgresql compile
  $ ant hsqldb compile

Running unit tests with HSQLDB
------------------------------
Note that you do not need to manually create a properties file in order to run the unit tests with
HSQLDB.  The `create-hsqldb` task will build an hsqldb.properties file matching the in-memory
database it generates.
