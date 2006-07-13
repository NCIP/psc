package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.nwu.bioinformatics.commons.testing.DbTestCase;

import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public class DaoTestCase extends DbTestCase {
    private static ApplicationContext applicationContext = null;

    protected DataSource getDataSource() {
        return (DataSource) getApplicationContext().getBean("dataSource");
    }

    public ApplicationContext getApplicationContext() {
        synchronized (DaoTestCase.class) {
            if (applicationContext == null) {
                applicationContext = ContextTools.createDeployedApplicationContext();
            }
            return applicationContext;
        }
    }
}
