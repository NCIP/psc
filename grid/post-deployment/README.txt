
instructions:

execute "ant postDeployment" command to perform the post deployment process of grid services. Before executing this command

1. make sure $CATALINA_HOME is set correctly.  For ex : "Users/userhome/tomcat-5.2.26"

2. make sure to take backup of
${CATALINA_HOME}/conf/sever.xml,
${CATALINA_HOME}/webapps/wsrf-psc/WEB-INF/etc/globus_wsrf_core/global_security_descriptor.xml
and
${CATALINA_HOME}/webapps/wsrf-psc/WEB-INF/etc/globus_wsrf_core/server-config.wsdd
 