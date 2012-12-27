/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test;

import edu.northwestern.bioinformatics.studycalendar.jdbcmock.NoOperationsDatabaseMetaData;
import edu.northwestern.bioinformatics.studycalendar.jdbcmock.NoOperationsResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a fake set of database metadata for testing {@link TableOrderer}.
 *
 * @author Rhett Sutphin
 */
public class MockDbMetadata extends NoOperationsDatabaseMetaData {
    private Map<String, Set<String>> tree;
    private Map<String, String> tableSchemas;
    private Map<String, List<Column>> tableColumns;

    public MockDbMetadata() {
        tree = new LinkedHashMap<String, Set<String>>();
        tableSchemas = new HashMap<String, String>();
        tableColumns = new HashMap<String, List<Column>>();
    }

    public MockDbMetadata solo(String... tables) {
        for (String t : tables) link(t);
        return this;
    }

    public MockDbMetadata link(String parent, String... children) {
        if (!tree.containsKey(parent)) tree.put(parent, new HashSet<String>());
        tree.get(parent).addAll(Arrays.asList(children));
        return this;
    }

    public MockDbMetadata schema(String schema, String... tables) {
        for (String table : tables) {
            tableSchemas.put(table, schema);
        }
        return this;
    }

    public MockDbMetadata column(String table, String column, int sqlType) {
        if (!tableColumns.containsKey(table)) tableColumns.put(table, new LinkedList<Column>());
        tableColumns.get(table).add(new Column(column, sqlType));
        return this;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return new MockFkResultSet(tree.get(table));
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException {
        Set<String> allTables = new HashSet<String>();
        for (Map.Entry<String, Set<String>> entry : tree.entrySet()) {
            allTables.add(entry.getKey());
            allTables.addAll(entry.getValue());
        }
        return new MockTablesResultSet(allTables);
    }

    @Override
    public ResultSet getColumns(
        String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern
    ) throws SQLException {
        List<Column> columns = tableColumns.get(tableNamePattern);
        return new MockColumnsResultSet(
            columns == null ? Collections.<Column>emptyList().iterator() : columns.iterator());
    }

    private class MockFkResultSet extends NoOperationsResultSet {
        private Iterator<String> children;

        public MockFkResultSet(Collection<String> children) {
            this.children = children == null
                ? Collections.<String>emptySet().iterator()
                : children.iterator();
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            if (columnIndex == 7) return children.next();
            else throw new IllegalArgumentException("Unexpected column requested: " + columnIndex);
        }

        @Override
        public boolean next() throws SQLException {
            return children.hasNext();
        }

        @Override
        public void close() throws SQLException {
            children = null;
        }
    }

    private abstract class AbstractIteratorResultSet<I> extends NoOperationsResultSet {
        private I current = null;

        protected abstract Iterator<I> iterator();

        @Override
        public boolean next() throws SQLException {
            if (iterator().hasNext()) {
                current = iterator().next();
                return true;
            } else {
                current = null;
                return false;
            }
        }

        protected I current() {
            return current;
        }
    }

    private class MockTablesResultSet extends AbstractIteratorResultSet<MockTableMetadata> {
        private Iterator<MockTableMetadata> tables;

        private MockTablesResultSet(Set<String> tables) {
            if (tables == null) {
                this.tables = Collections.<MockTableMetadata>emptySet().iterator();
            } else {
                List<MockTableMetadata> tableRows = new ArrayList<MockTableMetadata>(tables.size());
                for (String table : tables) {
                    tableRows.add(new MockTableMetadata(table));
                }
                this.tables = tableRows.iterator();
            }
        }

        @Override
        protected Iterator<MockTableMetadata> iterator() {
            return tables;
        }

        @Override
        public String getString(String columnName) throws SQLException {
            if ("TABLE_NAME".equals(columnName)) return current().getTableName();
            if ("TABLE_SCHEM".equals(columnName)) return current().getSchemaName();
            else throw new IllegalArgumentException("Unexpected column requested: " + columnName);
        }

        @Override
        public void close() throws SQLException {
            tables = null;
        }
    }

    private class MockTableMetadata {
        private String schema;
        private String tableName;

        private MockTableMetadata(String tableName) {
            this.tableName = tableName;
            if (tableSchemas.containsKey(tableName)) {
                this.schema = tableSchemas.get(tableName);
            } else {
                this.schema = "public";
            }
        }

        public String getSchemaName() {
            return schema;
        }

        public String getTableName() {
            return tableName;
        }
    }

    public class MockColumnsResultSet extends AbstractIteratorResultSet<Column> {
        private Iterator<Column> columns;

        public MockColumnsResultSet(Iterator<Column> columns) {
            this.columns = columns;
        }

        @Override
        protected Iterator<Column> iterator() {
            return columns;
        }

        @Override
        public String getString(String columnName) throws SQLException {
            if ("COLUMN_NAME".equals(columnName)) return current().getName();
            else throw new IllegalArgumentException("Unexpected column requested: " + columnName);
        }

        @Override
        public int getInt(String columnName) throws SQLException {
            if ("DATA_TYPE".equals(columnName)) return current().getSqlType();
            else throw new IllegalArgumentException("Unexpected column requested: " + columnName);
        }
    }

    private static class Column {
        private final String name;
        private final int sqlType;

        private Column(String columnName, int sqlType) {
            this.name = columnName;
            this.sqlType = sqlType;
        }

        public String getName() {
            return name;
        }

        public int getSqlType() {
            return sqlType;
        }
    }
}
