/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Provides a consistent way to build a static {@link ApplicationContext}
 * for use outside the main webapp.
 *
 * @see #getDeployedApplicationContext()
 * @author Rhett Sutphin
 */
public class StudyCalendarApplicationContextBuilder {
    public static final String[] DEPLOYED_CONFIG_LOCATIONS = new String[] {
        "classpath:applicationContext-api.xml"            ,
        "classpath:applicationContext-dao.xml"            ,
        "classpath:applicationContext-db.xml"             ,
        "classpath:applicationContext-mail.xml"           ,
        "classpath:applicationContext-representations.xml",
        "classpath:applicationContext-authorization-socket.xml"  ,
        "classpath:applicationContext-authorization.xml"  ,
        "classpath:applicationContext-service.xml"        ,
        "classpath:applicationContext-setup.xml"          ,
        "classpath:applicationContext-spring.xml"         ,
        "classpath:applicationContext-core-osgi.xml"
    };

    private static final StaticApplicationContextHelper helper = new StaticApplicationContextHelper() {
        @Override
        protected ApplicationContext createApplicationContext() {
            return createDeployedApplicationContext();
        }
    };

    /**
     * Creates a new copy of the application context, as it is created in the deployed application.
     */
    public static ApplicationContext createDeployedApplicationContext() {
        return new ClassPathXmlApplicationContext(DEPLOYED_CONFIG_LOCATIONS);
    }

    /**
     * Returns a statically-cached (i.e., at the JVM level) copy of the application context
     * created by {@link #createDeployedApplicationContext()}.
     * <p> 
     * If loading the application context fails for any reason, it caches the failure and does
     * not retry the load.  This makes running a partially failing suite much faster.
     */
    public static ApplicationContext getDeployedApplicationContext() {
        return helper.getApplicationContext();
    }

    // static class
    private StudyCalendarApplicationContextBuilder() { }
}
