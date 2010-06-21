package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Guesses the hibernate dialect if it is null.  This is necessary because
 * {@link gov.nih.nci.security.system.ApplicationSessionFactory} requires that the dialect be set.
 * Hibernate in general does not require this, so we do not want to make it a configuration
 * requirement for all PSC instances everywhere.
 *
 * @author Rhett Sutphin
 */
public class CsmHibernatePropertiesFactoryBean implements FactoryBean {
    private static final String DIALECT_PROPERTY_NAME = "hibernate.dialect";

    private Map<String, String> properties;

    public Object getObject() throws Exception {
        Map<String, String> actual = new HashMap<String, String>();
        if (getProperties() != null) {
            actual.putAll(getProperties());
        }
        if (actual.get(DIALECT_PROPERTY_NAME) == null) {
            actual.put(DIALECT_PROPERTY_NAME,
                guessDialect(actual.get("hibernate.connection.driver_class")));
        }
        return actual;
    }

    private String guessDialect(String driverClass) {
        if (driverClass == null) {
            throw new StudyCalendarSystemException(
                "Unable to guess CSM hibernate dialect without JDBC driver.  Please specify datasource.driver in your PSC configuration.");
        } else if (driverClass.contains("postgresql")) {
            return "org.hibernate.dialect.PostgreSQLDialect";
        } else if (driverClass.contains("oracle")) {
            return "org.hibernate.dialect.Oracle10gDialect";
        } else if (driverClass.contains("hsqldb")) {
            return "org.hibernate.dialect.HSQLDialect";
        } else {
            throw new StudyCalendarSystemException(
                "Unable to guess CSM hibernate dialect for %s.  Please specify datasource.dialect in your PSC configuration.",
                driverClass);
        }
    }

    public Class getObjectType() {
        return HashMap.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    // visible for testing
    Map<String, String> getProperties() {
        return properties;
    }
}
