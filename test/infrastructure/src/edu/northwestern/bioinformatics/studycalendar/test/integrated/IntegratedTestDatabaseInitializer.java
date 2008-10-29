package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.test.TableOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Rhett Sutphin
 */
public abstract class IntegratedTestDatabaseInitializer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private DataSource dataSource;
    private ConnectionSource connectionSource;
    private String testRunIdentifier;
    private List<SchemaInitializer> initializerSeries;

    public IntegratedTestDatabaseInitializer() {
        this.testRunIdentifier = createTestRunIdentifier();
    }

    private String createTestRunIdentifier() {
        return getClass().getSimpleName() + "|" + UUID.randomUUID();
    }

    public void oneTimeSetup() {
        log.info("[{}] One time setup", getClass().getSimpleName());
        for (SchemaInitializer initializer : getInitializerSeries()) {
            initializer.oneTimeSetup(connectionSource);
        }
    }

    public void beforeAll() {
        log.info("[{}] Before all", getClass().getSimpleName());
        for (SchemaInitializer initializer : getInitializerSeries()) {
            initializer.beforeAll(connectionSource);
        }
    }

    public void beforeEach() {
        log.info("[{}] Before each", getClass().getSimpleName());
        for (SchemaInitializer initializer : getInitializerSeries()) {
            initializer.beforeEach(connectionSource);
        }
    }

    public void afterEach() {
        log.info("[{}] After each", getClass().getSimpleName());
        for (SchemaInitializer initializer : getInitializerSeriesInReverse()) {
            initializer.afterEach(connectionSource);
        }
    }

    public void afterAll() {
        log.info("[{}] After all", getClass().getSimpleName());
        for (SchemaInitializer initializer : getInitializerSeriesInReverse()) {
            initializer.afterAll(connectionSource);
        }
    }

    public synchronized List<SchemaInitializer> getInitializerSeries() {
        if (initializerSeries == null) initializerSeries = createInitializerSeries();
        return initializerSeries;
    }

    public List<SchemaInitializer> getInitializerSeriesInReverse() {
        List<SchemaInitializer> series = new ArrayList<SchemaInitializer>(getInitializerSeries());
        Collections.reverse(series);
        return series;
    }

    private synchronized List<SchemaInitializer> createInitializerSeries() {
        String[] tableOrder = createTableOrderer().insertionOrder();

        List<SchemaInitializer> initializers = new ArrayList<SchemaInitializer>(tableOrder.length);
        for (String table : tableOrder) {
            SchemaInitializer initializer = getTableInitializer(table);
            if (initializer != null) initializers.add(initializer);
        }
        return initializers;
    }

    // exposed for testing
    protected TableOrderer createTableOrderer() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            return new TableOrderer(metadata);
        } catch (SQLException e) {
            throw new StudyCalendarSystemException("Error while introspecting the database", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("Closing the connection {} failed", conn, e);
                }
            }
        }
    }

    public abstract SchemaInitializer getTableInitializer(String tableName);

    ////// BEAN PROPERTIES

    public String getTestRunIdentifier() {
        return testRunIdentifier;
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    ////// CONFIGURATION

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.connectionSource = new ConnectionSource(dataSource);
    }
}
