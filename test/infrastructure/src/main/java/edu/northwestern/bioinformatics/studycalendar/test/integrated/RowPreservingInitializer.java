/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link SchemaInitializer} which ensures that any rows in the designated
 * table which exist {@link #beforeAll} are retained {@link #afterEach}.  All
 * other rows are deleted {@link #afterEach}.
 *
 * @author Rhett Sutphin
 */
public class RowPreservingInitializer extends EmptySchemaInitializer {
    public static final String PK_RECORD_TABLE_NAME = "__integration_preserve_keys";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_PRIMARY_KEY_NAME = "id";

    private String tableName;
    private List<String> primaryKeyNames;

    public RowPreservingInitializer(String tableName) {
        this(tableName, DEFAULT_PRIMARY_KEY_NAME);
    }

    public RowPreservingInitializer(String tableName, String primaryKeyName) {
        this(tableName, Arrays.asList(primaryKeyName));
    }

    public RowPreservingInitializer(String tableName, List<String> primaryKeyNames) {
        this.tableName = tableName;
        this.primaryKeyNames = primaryKeyNames;
    }

    @Override
    public void oneTimeSetup(ConnectionSource connectionSource) {
        super.oneTimeSetup(connectionSource);
        connectionSource.currentJdbcTemplate().
            execute(new RowPreservationTableCreator(
                primaryKeyNames.size(), connectionSource.currentJdbcTemplate()));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void beforeAll(ConnectionSource connectionSource) {
        String clearSql = String.format(
            "DELETE FROM %s WHERE table_name='%s'", PK_RECORD_TABLE_NAME, getTableName());
        log.debug("Purging old record of saved PKs: {}", clearSql);
        int ct = connectionSource.currentJdbcTemplate().update(clearSql);
        log.debug(" - {} records removed", ct);

        String copySql = String.format(
            "INSERT INTO %s (table_name, %s) SELECT '%s', %s FROM %s",
            PK_RECORD_TABLE_NAME,
            StringUtils.join(keyColumns(), ", "),
            getTableName(),
            StringUtils.join(getPrimaryKeyNames(), ", "),
            getTableName()
        );
        log.debug("Noting PK values to save: {}", copySql);
        ct = connectionSource.currentJdbcTemplate().update(copySql);
        log.debug(" - {} found to preserve", ct);
    }

    private List<String> keyColumns() {
        List<String> keys = new ArrayList<String>(getPrimaryKeyNames().size());
        for (int i = 0 ; i < getPrimaryKeyNames().size() ; i++) {
            keys.add("key" + i);
        }
        return keys;
    }

    @Override
    public void afterEach(ConnectionSource connectionSource) {
        String sql = String.format(
            "DELETE FROM %s WHERE (%s) NOT IN (SELECT %s FROM %s WHERE table_name='%s')",
            getTableName(),
            "CAST (" + StringUtils.join(getPrimaryKeyNames(), " AS VARCHAR(32)), CAST (") + " AS VARCHAR(32))",
            StringUtils.join(keyColumns(), ", "),
            PK_RECORD_TABLE_NAME,
            getTableName()
        );
        log.debug("Clearing added rows using SQL: {}", sql);
        int ct = connectionSource.currentJdbcTemplate().update(sql);
        log.debug(" - {} cleared", ct);
    }

    ////// BEAN PROPERTIES

    public String getTableName() {
        return tableName;
    }

    public List<String> getPrimaryKeyNames() {
        return primaryKeyNames;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[tableName=").append(getTableName()).
            append("; PKs=").append(getPrimaryKeyNames()).
            append(']').toString();
    }
}
