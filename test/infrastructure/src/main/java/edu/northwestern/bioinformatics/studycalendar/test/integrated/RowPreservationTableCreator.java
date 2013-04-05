/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import static edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer.PK_RECORD_TABLE_NAME;
import static java.lang.String.*;

/**
 * Ensures the temporary table used by {@link RowPreservingInitializer} exists
 * and has sufficient keys.
 *
 * @author Rhett Sutphin
 */
public class RowPreservationTableCreator implements ConnectionCallback {
    private int minKeyCount;
    private final JdbcTemplate template;

    public RowPreservationTableCreator(int minKeyCount, JdbcTemplate template) {
        this.minKeyCount = minKeyCount;
        this.template = template;
    }

    public Object doInConnection(Connection con) throws SQLException, DataAccessException {
        DatabaseMetaData metadata = con.getMetaData();
        if (!hasTable(metadata)) {
            template.update(
                format("CREATE TABLE %s (table_name VARCHAR(255))", PK_RECORD_TABLE_NAME));
        }
        Collection<String> keyColumns = existingKeyColumns(metadata);
        for (int i = 0 ; i < minKeyCount ; i++) {
            String name = "key" + i;
            if (!keyColumns.contains(name)) {
                template.update(
                    format("ALTER TABLE %s ADD %s VARCHAR(255)", PK_RECORD_TABLE_NAME, name));
            }
        }
        return null;
    }

    private boolean hasTable(DatabaseMetaData metadata) throws SQLException {
        ResultSet tables = metadata.getTables(null, null, null, new String[] { "TABLE" });
        while (tables.next()) {
            if (PK_RECORD_TABLE_NAME.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                return true;
            }
        }
        return false;
    }

    private Collection<String> existingKeyColumns(DatabaseMetaData metadata) throws SQLException {
        Collection<String> keyColumns = new LinkedList<String>();
        ResultSet allColumns = metadata.getColumns(null, null, PK_RECORD_TABLE_NAME, null);
        while (allColumns.next()) {
            String name = allColumns.getString("COLUMN_NAME").toLowerCase();
            if (name.startsWith("key")) {
                keyColumns.add(name);
            }
        }
        return keyColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowPreservationTableCreator that = (RowPreservationTableCreator) o;

        return minKeyCount == that.minKeyCount;
    }

    @Override
    public int hashCode() {
        return minKeyCount;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[minKeyCount=").append(minKeyCount).append(']').toString();
    }
}
