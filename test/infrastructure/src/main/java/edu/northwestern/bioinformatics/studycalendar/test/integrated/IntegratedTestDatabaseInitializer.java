/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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
        return getClass().getSimpleName() + '|' + UUID.randomUUID();
    }

    public void oneTimeSetup() {
        logInvocation("oneTimeSetup", getInitializerSeries());
        for (SchemaInitializer initializer : getInitializerSeries()) {
            traceInvocation("oneTimeSetup", initializer);
            initializer.oneTimeSetup(connectionSource);
        }
    }

    private void logInvocation(String method, List<SchemaInitializer> initializers) {
        if (log.isDebugEnabled()) {
            log.debug("[{}] Invoking {} on {} initializers", new Object[] { getClass().getSimpleName(), method, initializers.size() });
        }
    }

    private void traceInvocation(String method, SchemaInitializer initializer) {
        log.trace(" - Invoking {} on {}", method, initializer);
    }

    public void beforeAll() {
        logInvocation("beforeAll", getInitializerSeries());
        for (SchemaInitializer initializer : getInitializerSeries()) {
            traceInvocation("beforeAll", initializer);
            initializer.beforeAll(connectionSource);
        }
    }

    public void beforeEach() {
        logInvocation("beforeEach", getInitializerSeries());
        for (SchemaInitializer initializer : getInitializerSeries()) {
            traceInvocation("beforeEach", initializer);
            initializer.beforeEach(connectionSource);
        }
    }

    public void afterEach() {
        List<SchemaInitializer> reverse = getInitializerSeriesInReverse();
        logInvocation("afterEach", reverse);
        for (SchemaInitializer initializer : reverse) {
            traceInvocation("afterEach", initializer);
            initializer.afterEach(connectionSource);
        }
    }

    public void afterAll() {
        List<SchemaInitializer> reverse = getInitializerSeriesInReverse();
        logInvocation("afterAll", reverse);
        for (SchemaInitializer initializer : reverse) {
            traceInvocation("afterAll", initializer);
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
