package edu.northwestern.bioinformatics.studycalendar.database;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

/**
 * Shared code for getting a simple PSC datasource for testing.
 *
 * @author Rhett Sutphin
 */
public class TrivalTestDataSourceFactory {
    private DataSource dataSource;

    public synchronized DataSource getPscDataSource() {
        if (dataSource == null) {
            dataSource = (DataSource) new ClassPathXmlApplicationContext(
                "test-datasource-context.xml", getClass()).getBean("dataSource");
        }
        return dataSource;
    }
}
