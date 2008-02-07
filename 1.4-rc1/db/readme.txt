Setting up a database
---------------------
To set up a database, copy the file "datasource.properties.example" to "datasource.properties".
in one of the search paths.  (See next section; ${user.home}/.psc is recommended for development.)
Then edit datasource.properties to match your database.  In particular, you'll need to:

  * Set `datasource.url` to the JDBC URL for your database
  * Set the username and password to use to access this URL
  * Uncomment the database driver line that corresponds to your database type

Configuration locations
-----------------------
PSC will search for the datasource configuration file in ${catalina.home}/conf/psc,
${user.home}/.psc, and /etc/psc, in that order.

Switching between databases
---------------------------
If you want to maintain configurations for several different databases, create copies of
datasource.properties (appropriately configured) with different names.  You can then build for
a different database by providing the `config.database` property to ant.  E.g.:

  $ ant -Dconfig.database=oracle compile

This will configure the application such that it will search for a file called oracle.properties.
There are ant tasks to simplify specifying which file to use for the supported databases:

  $ ant oracle compile
  $ ant postgresql compile
  $ ant hsqldb compile

Running unit tests with HSQLDB
------------------------------
Note that you do not need to manually create a properties file in order to run the unit tests with
HSQLDB.  The `create-hsqldb` task will build ~/.psc/hsqldb.properties matching the in-memory
database it generates.

Running with Oracle
-------------------
If you wish to use Oracle, please add the Oracle JDBC driver and its dependencies to db/lib before
running. (Oracle's license prevents us from redistributing the JDBC driver, but you can download it
from them for free.)  As of this writing, the JARs you need are ojdbc14.jar and
jakarta-oro-2.0.8.jar, though another version will probably work fine.
