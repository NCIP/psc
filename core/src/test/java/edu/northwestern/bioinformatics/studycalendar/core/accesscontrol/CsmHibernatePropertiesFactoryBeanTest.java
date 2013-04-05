/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class CsmHibernatePropertiesFactoryBeanTest extends StudyCalendarTestCase {
    private CsmHibernatePropertiesFactoryBean factory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new CsmHibernatePropertiesFactoryBean();
        factory.setProperties(new HashMap<String, String>());
        factory.getProperties().put(
            "hibernate.connection.driver_class", "org.postgresql.Driver");
    }

    public void testThrowsExceptionIfUnableToGuessDialect() throws Exception {
        try {
            factory.getProperties().put(
                "hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            actualProperties().get("hibernate.dialect");
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Wrong message",
                "Unable to guess CSM hibernate dialect for com.mysql.jdbc.Driver.  Please specify datasource.dialect in your PSC configuration.",
                e.getMessage());
        }
    }

    public void testThrowsExceptionIfUnableToGuessDialectBecauseThereIsNoDriver() throws Exception {
        try {
            factory.getProperties().remove("hibernate.connection.driver_class");
            actualProperties().get("hibernate.dialect");
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Wrong message",
                "Unable to guess CSM hibernate dialect without JDBC driver.  Please specify datasource.driver in your PSC configuration.",
                e.getMessage());
        }
    }

    public void testGuessesPostgresqlForPostgresqlDriver() throws Exception {
        factory.getProperties().put(
            "hibernate.connection.driver_class", "org.postgresql.Driver");
        assertEquals("org.hibernate.dialect.PostgreSQLDialect",
            actualProperties().get("hibernate.dialect"));
    }

    public void testGuessesOracleForOracleDriver() throws Exception {
        factory.getProperties().put(
            "hibernate.connection.driver_class", "oracle.jdbc.OracleDriver");
        assertEquals("org.hibernate.dialect.Oracle10gDialect",
            actualProperties().get("hibernate.dialect"));
    }

    public void testGuessesHsqldbForHsqldbDriver() throws Exception {
        factory.getProperties().put(
            "hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        assertEquals("org.hibernate.dialect.HSQLDialect",
            actualProperties().get("hibernate.dialect"));
    }

    public void testGivesDialectIfSet() throws Exception {
        factory.getProperties().put("hibernate.dialect", "org.foo.Baz");
        assertEquals("org.foo.Baz", actualProperties().get("hibernate.dialect"));
    }

    public void testPassesAlongOtherProperties() throws Exception {
        factory.getProperties().put("foo", "quux");
        assertEquals("quux", actualProperties().get("foo"));
    }

    public void testObjectTypeIsHashMap() throws Exception {
        assertEquals(HashMap.class, factory.getObjectType());
    }

    public void testActualObjectIsHashMap() throws Exception {
        assertTrue(actualProperties() instanceof HashMap);
    }

    @SuppressWarnings({"unchecked"})
    private Map<String, String> actualProperties() throws Exception {
        return (Map<String, String>) factory.getObject();
    }
}

