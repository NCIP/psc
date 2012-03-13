package edu.northwestern.bioinformatics.studycalendar.database;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Shared code for getting a simple PSC datasource for testing.
 *
 * @author Rhett Sutphin
 */
public class TrivalTestDataSourceFactory {
    private DataSource dataSource;

    public synchronized DataSource getPscDataSource() {
        if (dataSource == null) {
            dataSource = (DataSource) getApplicationContext().getBean("dataSource");
        }
        return dataSource;
    }

    protected ApplicationContext getApplicationContext() {
        return new ClassPathXmlApplicationContext("test-datasource-context.xml", getClass());
    }

    public Map<String, String> getDataSourceProperties() {
        return (Map<String, String>) getApplicationContext().getBean("dataSourceProperties");
    }
}
