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

    public MockDbMetadata() {
        tree = new LinkedHashMap<String, Set<String>>();
        tableSchemas = new HashMap<String, String>();
    }

    public void solo(String... tables) {
        for (String t : tables) link(t);
    }

    public void link(String parent, String... children) {
        if (!tree.containsKey(parent)) tree.put(parent, new HashSet<String>());
        tree.get(parent).addAll(Arrays.asList(children));
    }

    public void schema(String schema, String... tables) {
        for (String table : tables) {
            tableSchemas.put(table, schema);
        }
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

    private class MockTablesResultSet extends NoOperationsResultSet {
        private MockTableMetadata current;
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
        public String getString(String columnName) throws SQLException {
            if ("TABLE_NAME".equals(columnName)) return current.getTableName();
            if ("TABLE_SCHEM".equals(columnName)) return current.getSchemaName();
            else throw new IllegalArgumentException("Unexpected column requested: " + columnName);
        }

        @Override
        public boolean next() throws SQLException {
            if (tables.hasNext()) {
                current = tables.next();
                return true;
            } else {
                current = null;
                return false;
            }
        }

        @Override
        public void close() throws SQLException {
            tables = null;
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
    }
}
