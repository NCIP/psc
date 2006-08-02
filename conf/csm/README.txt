Setting up CSM for Tomcat

1. Copy the ApplicationSecurityConfig.xml file from the /conf/csm directory to any directory.
 
2. Copy the studycal.hibernate.cfg.xml file to the same directory

3. Edit the ApplicationSecurityConfig.xml file to replace the <<config_directory_base>> with the fully qualified path of the directory created in step 1.

4. copy the csm_jaas.conf file to the Tomcat installation /conf directory

5. Edit the csm_jaas.conf file to specify the right database driver, connection url, username and password

6. Edit the catalina.properties file according to the sample provided. Add the following two entries:

	* gov.nih.nci.security.configFile : is the name of the property which points to the fully qualified path for ApplicationSecurityConfig.xml
	* java.security.auth.login.config : points to the JAAS config file

7. create the following JAAS Realm in the server.xml file of Tomcat
	  <Realm className="org.apache.catalina.realm.JAASRealm"                 
	             appName="study_calendar"       
		userClassNames="gov.nih.nci.security.authorization.domainobjects.User"       
		roleClassNames="gov.nih.nci.security.authorization.domainobjects.Role" 
                      debug="99"/>

8. copy the deployable war file to Tomcat/webapps directory
    	


 
