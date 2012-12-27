/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Provides access to a single connection across multiple {@link SchemaInitializer}s
 * for better performance, while allowing an individual {@link SchemaInitializer} to
 * indicate that the connection should be dropped and replaced with a new one.
 *
 * @author Rhett Sutphin
 */
public class ConnectionSource {
    private DataSource dataSource;
    private Connection connection;
    private JdbcTemplate jdbcTemplate;

    public ConnectionSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public synchronized Connection currentConnection() {
        if (connection == null) acquireConnection();
        return connection;
    }

    public synchronized JdbcTemplate currentJdbcTemplate() {
        if (connection == null) acquireConnection();
        return jdbcTemplate;
    }

    public synchronized Connection newConnection() {
        if (connection != null) clearConnection();
        return currentConnection();
    }

    public synchronized JdbcTemplate newJdbcTemplate() {
        if (connection != null) clearConnection();
        return currentJdbcTemplate();
    }

    private synchronized void clearConnection() {
        DataSourceUtils.releaseConnection(connection, dataSource);
        connection = null;
        jdbcTemplate = null;
    }

    private synchronized void acquireConnection() {
        connection = DataSourceUtils.getConnection(dataSource);
        jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
    }
}
