Setting up UPT for Tomcat

1. Make sure your database structure is up to date:

    ant migrate

2. Run the tomcat.security build target to create the security configuration files:

    ant tomcat.security

3. Perform the "manual steps" indicated in the output of step 2.

4. Deploy the UPT application by copying the upt.war war file to $CATALINA_HOME/webapps directory.
