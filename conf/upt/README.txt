Setting up UPT for Tomcat

1.	Copy the ApplicationSecurityConfig.xml file from the /conf/csm directory to any directory.

	Note : Replace the existing file with the copy in this directory, if configured for csm (will work for both csm and upt)

2.	Edit the ApplicationSecurityConfig.xml file to replace the <<config_directory_base>> with the fully qualified path of the directory created in step 1.

3.	Copy the studycal.hibernate.cfg.xml file to the same directory created in step 1. 

	Note: Replace the existing hibernate.cfg.xml (if configured for csm), with the copy in this directory. (will work for both csm and upt)

4.	Edit the hibernate.cfg.xml to change the connection username, password and url according to the local server setup.

5.	copy the csm_jaas.conf file to the Tomcat installation /conf directory (will work for both csm and upt)


6.	Edit the csm_jaas.conf file to specify the right database driver, connection url, username and password

7.	Edit the catalina.properties file according to the sample provided. Add the following two entries
	•	gov.nih.nci.security.configFile : is the name of the property which points to the fully qualified path for ApplicationSecurityConfig.xml
	•	java.security.auth.login.config : points to the JAAS config file

	in case the entry are similar to :
	java.security.auth.login.config=${catalina.home}/conf/csm_jaas.config

	create an Environment Variable CATALINA_HOME, with value pointing to the Tomcat installation directory, if its not already present.

8.	create the following JAAS Realm in the server.xml file of Tomcat
	  <Realm className="org.apache.catalina.realm.JAASRealm"                 
	             appName="csm_upt"       
		userClassNames="gov.nih.nci.security.authorization.domainobjects.User"       
		roleClassNames="gov.nih.nci.security.authorization.domainobjects.Role" 
                      debug="99"/>
	as in the sample server.xml.

9.	Run the ant migrate to add the changes to the csm tables for upt
10.	run the sample data script to create base system_admin and study calendar specific users 
11.	copy the upt.war war file to Tomcat/webapps directory

    	


 
