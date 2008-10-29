package edu.northwestern.bioinformatics.studycalendar.test;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A {@link SchemaInitializer} which ensures that any rows in the designated
 * table which exist {@link #beforeAll} are retained {@link #afterEach}.  All
 * other rows are deleted {@link #afterEach}.
 *
 * @author Rhett Sutphin
 */
public class RowPreservingInitializer extends EmptySchemaInitializer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_PRIMARY_KEY_NAME = "id";

    private String tableName;
    private List<String> primaryKeyNames;
    private List<Map<String, Object>> idsToPreserve;

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

    @SuppressWarnings({ "unchecked" })
    public void beforeAll(ConnectionSource connectionSource) {
        String sql = String.format(
            "SELECT %s FROM %s", StringUtils.join(getPrimaryKeyNames().iterator(), ", "), getTableName());
        log.debug("Identifying rows to preserve using SQL: {}", sql);
        idsToPreserve = connectionSource.currentJdbcTemplate().queryForList(sql);
        log.debug("Found {} rows to preserve", idsToPreserve.size());
        log.trace("  - {}", idsToPreserve);
    }

    public void afterEach(ConnectionSource connectionSource) {
        String sql;
        Object[] params;
        if (idsToPreserve == null || idsToPreserve.isEmpty()) {
            sql = String.format("DELETE FROM %s", getTableName());
            params = null;
        } else {
            List<String> expressions = primaryKeyExpressions(idsToPreserve.size());
            sql = String.format("DELETE FROM %s WHERE NOT (%s)",
                getTableName(), StringUtils.join(expressions.iterator(), ") AND NOT ("));
            params = primaryKeyExpressionParams();
        }
        connectionSource.currentJdbcTemplate().update(sql, params);
    }

    private List<String> primaryKeyExpressions(int count) {
        StringBuilder expression = new StringBuilder();
        for (Iterator<String> pkIt = primaryKeyNames.iterator(); pkIt.hasNext();) {
            String pkName = pkIt.next();
            expression.append(pkName).append("=?");
            if (pkIt.hasNext()) expression.append(" AND ");
        }
        List<String> expressions = new ArrayList<String>(count);
        while (expressions.size() < count) {
            expressions.add(expression.toString());
        }
        return expressions;
    }

    private Object[] primaryKeyExpressionParams() {
        List<Object> params = new ArrayList<Object>(idsToPreserve.size() * primaryKeyNames.size());
        for (Map<String, Object> ids : idsToPreserve) {
            for (String pkName : primaryKeyNames) {
                params.add(ids.get(pkName));
            }
        }
        return params.toArray();
    }

    ////// BEAN PROPERTIES

    public String getTableName() {
        return tableName;
    }

    public List<String> getPrimaryKeyNames() {
        return primaryKeyNames;
    }

    ////// OBJECT METHODS

    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[tableName=").append(getTableName()).
            append("; PKs=").append(getPrimaryKeyNames()).
            append("]").toString();
    }
}
