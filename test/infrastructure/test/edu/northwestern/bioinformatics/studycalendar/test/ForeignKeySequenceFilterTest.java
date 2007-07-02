package edu.northwestern.bioinformatics.studycalendar.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.SQLWarning;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Ref;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Array;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.math.BigDecimal;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import static org.easymock.classextension.EasyMock.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ForeignKeySequenceFilterTest extends StudyCalendarTestCase {
    private MockDbMetadata metadata;
    private ForeignKeySequenceFilter filter;
    private IDataSet dataSet;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        metadata = new MockDbMetadata();
        dataSet = new ReflexiveDataSet();
    }

    public void testBasicReordering() throws Exception {
        metadata.link("studies", "planned_calendars");
        metadata.link("planned_calendars", "epochs", "scheduled_calendars");
        metadata.link("epochs", "arms");

        String[] actualOrder = doReorder(
            "epochs", "arms", "scheduled_calendars", "studies", "planned_calendars"
        );

        assertPartialOrder(actualOrder, "studies", "planned_calendars", "scheduled_calendars", "epochs", "arms");
        assertPartialOrder(actualOrder, "planned_calendars", "scheduled_calendars", "epochs", "arms");
        assertPartialOrder(actualOrder, "scheduled_calendars", "arms");
        assertPartialOrder(actualOrder, "epochs", "arms");
    }

    public void testReorderingRetainsLoneTables() throws Exception {
        metadata.solo("loner");
        metadata.link("alpha", "beta", "gamma");

        String[] actualOrder = doReorder(
            "gamma", "alpha", "loner", "beta"
        );

        assertPartialOrder(actualOrder, "alpha", "beta", "gamma");
        assertPresent(actualOrder, "loner");
    }

    private String[] doReorder(String... tables) throws SQLException, DataSetException {
        filter = new ForeignKeySequenceFilter(conn(), tables);
        return filter.getTableNames(dataSet);
    }

    private static void assertPresent(String[] actualOrder, String expected) {
        assertNonnegative("Missing " + expected + " from " + Arrays.asList(actualOrder),
            search(expected, actualOrder));
    }

    private static void assertPartialOrder(String[] actualOrder, String expectedEarlier, String... expectedLater) {
        for (String later : expectedLater) {
            assertOrder(actualOrder, expectedEarlier, later);
        }
    }

    private static void assertOrder(String[] actualOrder, String expectedEarlier, String expectedLater) {
        int earlierIndex = search(expectedEarlier, actualOrder);
        int laterIndex = search(expectedLater, actualOrder);
        assertNonnegative("Earlier string - " + expectedEarlier + " - not present", earlierIndex);
        assertNonnegative("Later string - " + expectedLater + " - not present", laterIndex);
        assertTrue(expectedEarlier + " is after " + expectedLater, earlierIndex < laterIndex);
    }

    private static int search(String target, String[] order) {
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(target)) return i;
        }
        return -1;
    }

    private IDatabaseConnection conn() throws SQLException {
        Connection jdbc = createNiceMock(Connection.class);
        expect(jdbc.getMetaData()).andReturn(metadata);
        replay(jdbc);

        return new DatabaseConnection(jdbc);
    }

    private static class MockDbMetadata implements DatabaseMetaData {
        private Map<String, Set<String>> tree;

        public MockDbMetadata() {
            tree = new HashMap<String, Set<String>>();
        }

        public void solo(String table) {
            link(table);
        }

        public void link(String parent, String... children) {
            if (!tree.containsKey(parent)) tree.put(parent, new HashSet<String>());
            for (String child : children) {
                tree.get(parent).add(child);
            }
        }

        public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
            return new MockFkResultSet(tree.get(table));
        }

        //////
        ////// None of the rest of the methods are used
        //////

        public boolean allProceduresAreCallable() throws SQLException {
            throw new UnsupportedOperationException("allProceduresAreCallable not implemented");
        }

        public boolean allTablesAreSelectable() throws SQLException {
            throw new UnsupportedOperationException("allTablesAreSelectable not implemented");
        }

        public String getURL() throws SQLException {
            throw new UnsupportedOperationException("getURL not implemented");
        }

        public String getUserName() throws SQLException {
            throw new UnsupportedOperationException("getUserName not implemented");
        }

        public boolean isReadOnly() throws SQLException {
            throw new UnsupportedOperationException("isReadOnly not implemented");
        }

        public boolean nullsAreSortedHigh() throws SQLException {
            throw new UnsupportedOperationException("nullsAreSortedHigh not implemented");
        }

        public boolean nullsAreSortedLow() throws SQLException {
            throw new UnsupportedOperationException("nullsAreSortedLow not implemented");
        }

        public boolean nullsAreSortedAtStart() throws SQLException {
            throw new UnsupportedOperationException("nullsAreSortedAtStart not implemented");
        }

        public boolean nullsAreSortedAtEnd() throws SQLException {
            throw new UnsupportedOperationException("nullsAreSortedAtEnd not implemented");
        }

        public String getDatabaseProductName() throws SQLException {
            throw new UnsupportedOperationException("getDatabaseProductName not implemented");
        }

        public String getDatabaseProductVersion() throws SQLException {
            throw new UnsupportedOperationException("getDatabaseProductVersion not implemented");
        }

        public String getDriverName() throws SQLException {
            throw new UnsupportedOperationException("getDriverName not implemented");
        }

        public String getDriverVersion() throws SQLException {
            throw new UnsupportedOperationException("getDriverVersion not implemented");
        }

        public int getDriverMajorVersion() {
            throw new UnsupportedOperationException("getDriverMajorVersion not implemented");
        }

        public int getDriverMinorVersion() {
            throw new UnsupportedOperationException("getDriverMinorVersion not implemented");
        }

        public boolean usesLocalFiles() throws SQLException {
            throw new UnsupportedOperationException("usesLocalFiles not implemented");
        }

        public boolean usesLocalFilePerTable() throws SQLException {
            throw new UnsupportedOperationException("usesLocalFilePerTable not implemented");
        }

        public boolean supportsMixedCaseIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("supportsMixedCaseIdentifiers not implemented");
        }

        public boolean storesUpperCaseIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("storesUpperCaseIdentifiers not implemented");
        }

        public boolean storesLowerCaseIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("storesLowerCaseIdentifiers not implemented");
        }

        public boolean storesMixedCaseIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("storesMixedCaseIdentifiers not implemented");
        }

        public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("supportsMixedCaseQuotedIdentifiers not implemented");
        }

        public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("storesUpperCaseQuotedIdentifiers not implemented");
        }

        public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("storesLowerCaseQuotedIdentifiers not implemented");
        }

        public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
            throw new UnsupportedOperationException("storesMixedCaseQuotedIdentifiers not implemented");
        }

        public String getIdentifierQuoteString() throws SQLException {
            throw new UnsupportedOperationException("getIdentifierQuoteString not implemented");
        }

        public String getSQLKeywords() throws SQLException {
            throw new UnsupportedOperationException("getSQLKeywords not implemented");
        }

        public String getNumericFunctions() throws SQLException {
            throw new UnsupportedOperationException("getNumericFunctions not implemented");
        }

        public String getStringFunctions() throws SQLException {
            throw new UnsupportedOperationException("getStringFunctions not implemented");
        }

        public String getSystemFunctions() throws SQLException {
            throw new UnsupportedOperationException("getSystemFunctions not implemented");
        }

        public String getTimeDateFunctions() throws SQLException {
            throw new UnsupportedOperationException("getTimeDateFunctions not implemented");
        }

        public String getSearchStringEscape() throws SQLException {
            throw new UnsupportedOperationException("getSearchStringEscape not implemented");
        }

        public String getExtraNameCharacters() throws SQLException {
            throw new UnsupportedOperationException("getExtraNameCharacters not implemented");
        }

        public boolean supportsAlterTableWithAddColumn() throws SQLException {
            throw new UnsupportedOperationException("supportsAlterTableWithAddColumn not implemented");
        }

        public boolean supportsAlterTableWithDropColumn() throws SQLException {
            throw new UnsupportedOperationException("supportsAlterTableWithDropColumn not implemented");
        }

        public boolean supportsColumnAliasing() throws SQLException {
            throw new UnsupportedOperationException("supportsColumnAliasing not implemented");
        }

        public boolean nullPlusNonNullIsNull() throws SQLException {
            throw new UnsupportedOperationException("nullPlusNonNullIsNull not implemented");
        }

        public boolean supportsConvert() throws SQLException {
            throw new UnsupportedOperationException("supportsConvert not implemented");
        }

        public boolean supportsConvert(int fromType, int toType) throws SQLException {
            throw new UnsupportedOperationException("supportsConvert not implemented");
        }

        public boolean supportsTableCorrelationNames() throws SQLException {
            throw new UnsupportedOperationException("supportsTableCorrelationNames not implemented");
        }

        public boolean supportsDifferentTableCorrelationNames() throws SQLException {
            throw new UnsupportedOperationException("supportsDifferentTableCorrelationNames not implemented");
        }

        public boolean supportsExpressionsInOrderBy() throws SQLException {
            throw new UnsupportedOperationException("supportsExpressionsInOrderBy not implemented");
        }

        public boolean supportsOrderByUnrelated() throws SQLException {
            throw new UnsupportedOperationException("supportsOrderByUnrelated not implemented");
        }

        public boolean supportsGroupBy() throws SQLException {
            throw new UnsupportedOperationException("supportsGroupBy not implemented");
        }

        public boolean supportsGroupByUnrelated() throws SQLException {
            throw new UnsupportedOperationException("supportsGroupByUnrelated not implemented");
        }

        public boolean supportsGroupByBeyondSelect() throws SQLException {
            throw new UnsupportedOperationException("supportsGroupByBeyondSelect not implemented");
        }

        public boolean supportsLikeEscapeClause() throws SQLException {
            throw new UnsupportedOperationException("supportsLikeEscapeClause not implemented");
        }

        public boolean supportsMultipleResultSets() throws SQLException {
            throw new UnsupportedOperationException("supportsMultipleResultSets not implemented");
        }

        public boolean supportsMultipleTransactions() throws SQLException {
            throw new UnsupportedOperationException("supportsMultipleTransactions not implemented");
        }

        public boolean supportsNonNullableColumns() throws SQLException {
            throw new UnsupportedOperationException("supportsNonNullableColumns not implemented");
        }

        public boolean supportsMinimumSQLGrammar() throws SQLException {
            throw new UnsupportedOperationException("supportsMinimumSQLGrammar not implemented");
        }

        public boolean supportsCoreSQLGrammar() throws SQLException {
            throw new UnsupportedOperationException("supportsCoreSQLGrammar not implemented");
        }

        public boolean supportsExtendedSQLGrammar() throws SQLException {
            throw new UnsupportedOperationException("supportsExtendedSQLGrammar not implemented");
        }

        public boolean supportsANSI92EntryLevelSQL() throws SQLException {
            throw new UnsupportedOperationException("supportsANSI92EntryLevelSQL not implemented");
        }

        public boolean supportsANSI92IntermediateSQL() throws SQLException {
            throw new UnsupportedOperationException("supportsANSI92IntermediateSQL not implemented");
        }

        public boolean supportsANSI92FullSQL() throws SQLException {
            throw new UnsupportedOperationException("supportsANSI92FullSQL not implemented");
        }

        public boolean supportsIntegrityEnhancementFacility() throws SQLException {
            throw new UnsupportedOperationException("supportsIntegrityEnhancementFacility not implemented");
        }

        public boolean supportsOuterJoins() throws SQLException {
            throw new UnsupportedOperationException("supportsOuterJoins not implemented");
        }

        public boolean supportsFullOuterJoins() throws SQLException {
            throw new UnsupportedOperationException("supportsFullOuterJoins not implemented");
        }

        public boolean supportsLimitedOuterJoins() throws SQLException {
            throw new UnsupportedOperationException("supportsLimitedOuterJoins not implemented");
        }

        public String getSchemaTerm() throws SQLException {
            throw new UnsupportedOperationException("getSchemaTerm not implemented");
        }

        public String getProcedureTerm() throws SQLException {
            throw new UnsupportedOperationException("getProcedureTerm not implemented");
        }

        public String getCatalogTerm() throws SQLException {
            throw new UnsupportedOperationException("getCatalogTerm not implemented");
        }

        public boolean isCatalogAtStart() throws SQLException {
            throw new UnsupportedOperationException("isCatalogAtStart not implemented");
        }

        public String getCatalogSeparator() throws SQLException {
            throw new UnsupportedOperationException("getCatalogSeparator not implemented");
        }

        public boolean supportsSchemasInDataManipulation() throws SQLException {
            throw new UnsupportedOperationException("supportsSchemasInDataManipulation not implemented");
        }

        public boolean supportsSchemasInProcedureCalls() throws SQLException {
            throw new UnsupportedOperationException("supportsSchemasInProcedureCalls not implemented");
        }

        public boolean supportsSchemasInTableDefinitions() throws SQLException {
            throw new UnsupportedOperationException("supportsSchemasInTableDefinitions not implemented");
        }

        public boolean supportsSchemasInIndexDefinitions() throws SQLException {
            throw new UnsupportedOperationException("supportsSchemasInIndexDefinitions not implemented");
        }

        public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
            throw new UnsupportedOperationException("supportsSchemasInPrivilegeDefinitions not implemented");
        }

        public boolean supportsCatalogsInDataManipulation() throws SQLException {
            throw new UnsupportedOperationException("supportsCatalogsInDataManipulation not implemented");
        }

        public boolean supportsCatalogsInProcedureCalls() throws SQLException {
            throw new UnsupportedOperationException("supportsCatalogsInProcedureCalls not implemented");
        }

        public boolean supportsCatalogsInTableDefinitions() throws SQLException {
            throw new UnsupportedOperationException("supportsCatalogsInTableDefinitions not implemented");
        }

        public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
            throw new UnsupportedOperationException("supportsCatalogsInIndexDefinitions not implemented");
        }

        public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
            throw new UnsupportedOperationException("supportsCatalogsInPrivilegeDefinitions not implemented");
        }

        public boolean supportsPositionedDelete() throws SQLException {
            throw new UnsupportedOperationException("supportsPositionedDelete not implemented");
        }

        public boolean supportsPositionedUpdate() throws SQLException {
            throw new UnsupportedOperationException("supportsPositionedUpdate not implemented");
        }

        public boolean supportsSelectForUpdate() throws SQLException {
            throw new UnsupportedOperationException("supportsSelectForUpdate not implemented");
        }

        public boolean supportsStoredProcedures() throws SQLException {
            throw new UnsupportedOperationException("supportsStoredProcedures not implemented");
        }

        public boolean supportsSubqueriesInComparisons() throws SQLException {
            throw new UnsupportedOperationException("supportsSubqueriesInComparisons not implemented");
        }

        public boolean supportsSubqueriesInExists() throws SQLException {
            throw new UnsupportedOperationException("supportsSubqueriesInExists not implemented");
        }

        public boolean supportsSubqueriesInIns() throws SQLException {
            throw new UnsupportedOperationException("supportsSubqueriesInIns not implemented");
        }

        public boolean supportsSubqueriesInQuantifieds() throws SQLException {
            throw new UnsupportedOperationException("supportsSubqueriesInQuantifieds not implemented");
        }

        public boolean supportsCorrelatedSubqueries() throws SQLException {
            throw new UnsupportedOperationException("supportsCorrelatedSubqueries not implemented");
        }

        public boolean supportsUnion() throws SQLException {
            throw new UnsupportedOperationException("supportsUnion not implemented");
        }

        public boolean supportsUnionAll() throws SQLException {
            throw new UnsupportedOperationException("supportsUnionAll not implemented");
        }

        public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
            throw new UnsupportedOperationException("supportsOpenCursorsAcrossCommit not implemented");
        }

        public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
            throw new UnsupportedOperationException("supportsOpenCursorsAcrossRollback not implemented");
        }

        public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
            throw new UnsupportedOperationException("supportsOpenStatementsAcrossCommit not implemented");
        }

        public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
            throw new UnsupportedOperationException("supportsOpenStatementsAcrossRollback not implemented");
        }

        public int getMaxBinaryLiteralLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxBinaryLiteralLength not implemented");
        }

        public int getMaxCharLiteralLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxCharLiteralLength not implemented");
        }

        public int getMaxColumnNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxColumnNameLength not implemented");
        }

        public int getMaxColumnsInGroupBy() throws SQLException {
            throw new UnsupportedOperationException("getMaxColumnsInGroupBy not implemented");
        }

        public int getMaxColumnsInIndex() throws SQLException {
            throw new UnsupportedOperationException("getMaxColumnsInIndex not implemented");
        }

        public int getMaxColumnsInOrderBy() throws SQLException {
            throw new UnsupportedOperationException("getMaxColumnsInOrderBy not implemented");
        }

        public int getMaxColumnsInSelect() throws SQLException {
            throw new UnsupportedOperationException("getMaxColumnsInSelect not implemented");
        }

        public int getMaxColumnsInTable() throws SQLException {
            throw new UnsupportedOperationException("getMaxColumnsInTable not implemented");
        }

        public int getMaxConnections() throws SQLException {
            throw new UnsupportedOperationException("getMaxConnections not implemented");
        }

        public int getMaxCursorNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxCursorNameLength not implemented");
        }

        public int getMaxIndexLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxIndexLength not implemented");
        }

        public int getMaxSchemaNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxSchemaNameLength not implemented");
        }

        public int getMaxProcedureNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxProcedureNameLength not implemented");
        }

        public int getMaxCatalogNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxCatalogNameLength not implemented");
        }

        public int getMaxRowSize() throws SQLException {
            throw new UnsupportedOperationException("getMaxRowSize not implemented");
        }

        public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
            throw new UnsupportedOperationException("doesMaxRowSizeIncludeBlobs not implemented");
        }

        public int getMaxStatementLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxStatementLength not implemented");
        }

        public int getMaxStatements() throws SQLException {
            throw new UnsupportedOperationException("getMaxStatements not implemented");
        }

        public int getMaxTableNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxTableNameLength not implemented");
        }

        public int getMaxTablesInSelect() throws SQLException {
            throw new UnsupportedOperationException("getMaxTablesInSelect not implemented");
        }

        public int getMaxUserNameLength() throws SQLException {
            throw new UnsupportedOperationException("getMaxUserNameLength not implemented");
        }

        public int getDefaultTransactionIsolation() throws SQLException {
            throw new UnsupportedOperationException("getDefaultTransactionIsolation not implemented");
        }

        public boolean supportsTransactions() throws SQLException {
            throw new UnsupportedOperationException("supportsTransactions not implemented");
        }

        public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
            throw new UnsupportedOperationException("supportsTransactionIsolationLevel not implemented");
        }

        public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
            throw new UnsupportedOperationException("supportsDataDefinitionAndDataManipulationTransactions not implemented");
        }

        public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
            throw new UnsupportedOperationException("supportsDataManipulationTransactionsOnly not implemented");
        }

        public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
            throw new UnsupportedOperationException("dataDefinitionCausesTransactionCommit not implemented");
        }

        public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
            throw new UnsupportedOperationException("dataDefinitionIgnoredInTransactions not implemented");
        }

        public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getProcedures not implemented");
        }

        public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getProcedureColumns not implemented");
        }

        public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException {
            throw new UnsupportedOperationException("getTables not implemented");
        }

        public ResultSet getSchemas() throws SQLException {
            throw new UnsupportedOperationException("getSchemas not implemented");
        }

        public ResultSet getCatalogs() throws SQLException {
            throw new UnsupportedOperationException("getCatalogs not implemented");
        }

        public ResultSet getTableTypes() throws SQLException {
            throw new UnsupportedOperationException("getTableTypes not implemented");
        }

        public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getColumns not implemented");
        }

        public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getColumnPrivileges not implemented");
        }

        public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getTablePrivileges not implemented");
        }

        public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
            throw new UnsupportedOperationException("getBestRowIdentifier not implemented");
        }

        public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
            throw new UnsupportedOperationException("getVersionColumns not implemented");
        }

        public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
            throw new UnsupportedOperationException("getPrimaryKeys not implemented");
        }

        public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
            throw new UnsupportedOperationException("getImportedKeys not implemented");
        }

        public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
            throw new UnsupportedOperationException("getCrossReference not implemented");
        }

        public ResultSet getTypeInfo() throws SQLException {
            throw new UnsupportedOperationException("getTypeInfo not implemented");
        }

        public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
            throw new UnsupportedOperationException("getIndexInfo not implemented");
        }

        public boolean supportsResultSetType(int type) throws SQLException {
            throw new UnsupportedOperationException("supportsResultSetType not implemented");
        }

        public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
            throw new UnsupportedOperationException("supportsResultSetConcurrency not implemented");
        }

        public boolean ownUpdatesAreVisible(int type) throws SQLException {
            throw new UnsupportedOperationException("ownUpdatesAreVisible not implemented");
        }

        public boolean ownDeletesAreVisible(int type) throws SQLException {
            throw new UnsupportedOperationException("ownDeletesAreVisible not implemented");
        }

        public boolean ownInsertsAreVisible(int type) throws SQLException {
            throw new UnsupportedOperationException("ownInsertsAreVisible not implemented");
        }

        public boolean othersUpdatesAreVisible(int type) throws SQLException {
            throw new UnsupportedOperationException("othersUpdatesAreVisible not implemented");
        }

        public boolean othersDeletesAreVisible(int type) throws SQLException {
            throw new UnsupportedOperationException("othersDeletesAreVisible not implemented");
        }

        public boolean othersInsertsAreVisible(int type) throws SQLException {
            throw new UnsupportedOperationException("othersInsertsAreVisible not implemented");
        }

        public boolean updatesAreDetected(int type) throws SQLException {
            throw new UnsupportedOperationException("updatesAreDetected not implemented");
        }

        public boolean deletesAreDetected(int type) throws SQLException {
            throw new UnsupportedOperationException("deletesAreDetected not implemented");
        }

        public boolean insertsAreDetected(int type) throws SQLException {
            throw new UnsupportedOperationException("insertsAreDetected not implemented");
        }

        public boolean supportsBatchUpdates() throws SQLException {
            throw new UnsupportedOperationException("supportsBatchUpdates not implemented");
        }

        public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
            throw new UnsupportedOperationException("getUDTs not implemented");
        }

        public Connection getConnection() throws SQLException {
            throw new UnsupportedOperationException("getConnection not implemented");
        }

        public boolean supportsSavepoints() throws SQLException {
            throw new UnsupportedOperationException("supportsSavepoints not implemented");
        }

        public boolean supportsNamedParameters() throws SQLException {
            throw new UnsupportedOperationException("supportsNamedParameters not implemented");
        }

        public boolean supportsMultipleOpenResults() throws SQLException {
            throw new UnsupportedOperationException("supportsMultipleOpenResults not implemented");
        }

        public boolean supportsGetGeneratedKeys() throws SQLException {
            throw new UnsupportedOperationException("supportsGetGeneratedKeys not implemented");
        }

        public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getSuperTypes not implemented");
        }

        public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getSuperTables not implemented");
        }

        public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
            throw new UnsupportedOperationException("getAttributes not implemented");
        }

        public boolean supportsResultSetHoldability(int holdability) throws SQLException {
            throw new UnsupportedOperationException("supportsResultSetHoldability not implemented");
        }

        public int getResultSetHoldability() throws SQLException {
            throw new UnsupportedOperationException("getResultSetHoldability not implemented");
        }

        public int getDatabaseMajorVersion() throws SQLException {
            throw new UnsupportedOperationException("getDatabaseMajorVersion not implemented");
        }

        public int getDatabaseMinorVersion() throws SQLException {
            throw new UnsupportedOperationException("getDatabaseMinorVersion not implemented");
        }

        public int getJDBCMajorVersion() throws SQLException {
            throw new UnsupportedOperationException("getJDBCMajorVersion not implemented");
        }

        public int getJDBCMinorVersion() throws SQLException {
            throw new UnsupportedOperationException("getJDBCMinorVersion not implemented");
        }

        public int getSQLStateType() throws SQLException {
            throw new UnsupportedOperationException("getSQLStateType not implemented");
        }

        public boolean locatorsUpdateCopy() throws SQLException {
            throw new UnsupportedOperationException("locatorsUpdateCopy not implemented");
        }

        public boolean supportsStatementPooling() throws SQLException {
            throw new UnsupportedOperationException("supportsStatementPooling not implemented");
        }
    }

    private static class MockFkResultSet implements ResultSet {
        private Iterator<String> children;

        public MockFkResultSet(Collection<String> children) {
            this.children = children == null
                ? Collections.<String>emptySet().iterator()
                : children.iterator();
        }

        public String getString(int columnIndex) throws SQLException {
            if (columnIndex == 7) return children.next();
            else throw new IllegalArgumentException("Unexpected column requested: " + columnIndex);
        }

        public boolean next() throws SQLException {
            return children.hasNext();
        }

        public void close() throws SQLException {
            children = null;
        }

        //////
        ////// None of the rest of the methods are used
        //////

        public boolean wasNull() throws SQLException {
            throw new UnsupportedOperationException("wasNull not implemented");
        }

        public boolean getBoolean(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getBoolean not implemented");
        }

        public byte getByte(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getByte not implemented");
        }

        public short getShort(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getShort not implemented");
        }

        public int getInt(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getInt not implemented");
        }

        public long getLong(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getLong not implemented");
        }

        public float getFloat(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getFloat not implemented");
        }

        public double getDouble(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getDouble not implemented");
        }

        @Deprecated
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
            throw new UnsupportedOperationException("getBigDecimal not implemented");
        }

        public byte[] getBytes(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getBytes not implemented");
        }

        public Date getDate(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getDate not implemented");
        }

        public Time getTime(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getTime not implemented");
        }

        public Timestamp getTimestamp(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getTimestamp not implemented");
        }

        public InputStream getAsciiStream(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getAsciiStream not implemented");
        }

        @Deprecated
        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getUnicodeStream not implemented");
        }

        public InputStream getBinaryStream(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getBinaryStream not implemented");
        }

        public String getString(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getString not implemented");
        }

        public boolean getBoolean(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getBoolean not implemented");
        }

        public byte getByte(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getByte not implemented");
        }

        public short getShort(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getShort not implemented");
        }

        public int getInt(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getInt not implemented");
        }

        public long getLong(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getLong not implemented");
        }

        public float getFloat(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getFloat not implemented");
        }

        public double getDouble(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getDouble not implemented");
        }

        @Deprecated
        public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
            throw new UnsupportedOperationException("getBigDecimal not implemented");
        }

        public byte[] getBytes(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getBytes not implemented");
        }

        public Date getDate(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getDate not implemented");
        }

        public Time getTime(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getTime not implemented");
        }

        public Timestamp getTimestamp(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getTimestamp not implemented");
        }

        public InputStream getAsciiStream(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getAsciiStream not implemented");
        }

        @Deprecated
        public InputStream getUnicodeStream(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getUnicodeStream not implemented");
        }

        public InputStream getBinaryStream(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getBinaryStream not implemented");
        }

        public SQLWarning getWarnings() throws SQLException {
            throw new UnsupportedOperationException("getWarnings not implemented");
        }

        public void clearWarnings() throws SQLException {
            throw new UnsupportedOperationException("clearWarnings not implemented");
        }

        public String getCursorName() throws SQLException {
            throw new UnsupportedOperationException("getCursorName not implemented");
        }

        public ResultSetMetaData getMetaData() throws SQLException {
            throw new UnsupportedOperationException("getMetaData not implemented");
        }

        public Object getObject(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getObject not implemented");
        }

        public Object getObject(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getObject not implemented");
        }

        public int findColumn(String columnName) throws SQLException {
            throw new UnsupportedOperationException("findColumn not implemented");
        }

        public Reader getCharacterStream(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getCharacterStream not implemented");
        }

        public Reader getCharacterStream(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getCharacterStream not implemented");
        }

        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getBigDecimal not implemented");
        }

        public BigDecimal getBigDecimal(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getBigDecimal not implemented");
        }

        public boolean isBeforeFirst() throws SQLException {
            throw new UnsupportedOperationException("isBeforeFirst not implemented");
        }

        public boolean isAfterLast() throws SQLException {
            throw new UnsupportedOperationException("isAfterLast not implemented");
        }

        public boolean isFirst() throws SQLException {
            throw new UnsupportedOperationException("isFirst not implemented");
        }

        public boolean isLast() throws SQLException {
            throw new UnsupportedOperationException("isLast not implemented");
        }

        public void beforeFirst() throws SQLException {
            throw new UnsupportedOperationException("beforeFirst not implemented");
        }

        public void afterLast() throws SQLException {
            throw new UnsupportedOperationException("afterLast not implemented");
        }

        public boolean first() throws SQLException {
            throw new UnsupportedOperationException("first not implemented");
        }

        public boolean last() throws SQLException {
            throw new UnsupportedOperationException("last not implemented");
        }

        public int getRow() throws SQLException {
            throw new UnsupportedOperationException("getRow not implemented");
        }

        public boolean absolute(int row) throws SQLException {
            throw new UnsupportedOperationException("absolute not implemented");
        }

        public boolean relative(int rows) throws SQLException {
            throw new UnsupportedOperationException("relative not implemented");
        }

        public boolean previous() throws SQLException {
            throw new UnsupportedOperationException("previous not implemented");
        }

        public void setFetchDirection(int direction) throws SQLException {
            throw new UnsupportedOperationException("setFetchDirection not implemented");
        }

        public int getFetchDirection() throws SQLException {
            throw new UnsupportedOperationException("getFetchDirection not implemented");
        }

        public void setFetchSize(int rows) throws SQLException {
            throw new UnsupportedOperationException("setFetchSize not implemented");
        }

        public int getFetchSize() throws SQLException {
            throw new UnsupportedOperationException("getFetchSize not implemented");
        }

        public int getType() throws SQLException {
            throw new UnsupportedOperationException("getType not implemented");
        }

        public int getConcurrency() throws SQLException {
            throw new UnsupportedOperationException("getConcurrency not implemented");
        }

        public boolean rowUpdated() throws SQLException {
            throw new UnsupportedOperationException("rowUpdated not implemented");
        }

        public boolean rowInserted() throws SQLException {
            throw new UnsupportedOperationException("rowInserted not implemented");
        }

        public boolean rowDeleted() throws SQLException {
            throw new UnsupportedOperationException("rowDeleted not implemented");
        }

        public void updateNull(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("updateNull not implemented");
        }

        public void updateBoolean(int columnIndex, boolean x) throws SQLException {
            throw new UnsupportedOperationException("updateBoolean not implemented");

        }

        public void updateByte(int columnIndex, byte x) throws SQLException {
            throw new UnsupportedOperationException("updateByte not implemented");

        }

        public void updateShort(int columnIndex, short x) throws SQLException {
            throw new UnsupportedOperationException("updateShort not implemented");

        }

        public void updateInt(int columnIndex, int x) throws SQLException {
            throw new UnsupportedOperationException("updateInt not implemented");

        }

        public void updateLong(int columnIndex, long x) throws SQLException {
            throw new UnsupportedOperationException("updateLong not implemented");

        }

        public void updateFloat(int columnIndex, float x) throws SQLException {
            throw new UnsupportedOperationException("updateFloat not implemented");

        }

        public void updateDouble(int columnIndex, double x) throws SQLException {
            throw new UnsupportedOperationException("updateDouble not implemented");

        }

        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
            throw new UnsupportedOperationException("updateBigDecimal not implemented");

        }

        public void updateString(int columnIndex, String x) throws SQLException {
            throw new UnsupportedOperationException("updateString not implemented");

        }

        public void updateBytes(int columnIndex, byte x[]) throws SQLException {
            throw new UnsupportedOperationException("updateBytes not implemented");

        }

        public void updateDate(int columnIndex, Date x) throws SQLException {
            throw new UnsupportedOperationException("updateDate not implemented");

        }

        public void updateTime(int columnIndex, Time x) throws SQLException {
            throw new UnsupportedOperationException("updateTime not implemented");

        }

        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
            throw new UnsupportedOperationException("updateTimestamp not implemented");

        }

        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
            throw new UnsupportedOperationException("updateAsciiStream not implemented");

        }

        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
            throw new UnsupportedOperationException("updateBinaryStream not implemented");

        }

        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
            throw new UnsupportedOperationException("updateCharacterStream not implemented");

        }

        public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
            throw new UnsupportedOperationException("updateObject not implemented");

        }

        public void updateObject(int columnIndex, Object x) throws SQLException {
            throw new UnsupportedOperationException("updateObject not implemented");

        }

        public void updateNull(String columnName) throws SQLException {
            throw new UnsupportedOperationException("updateNull not implemented");

        }

        public void updateBoolean(String columnName, boolean x) throws SQLException {
            throw new UnsupportedOperationException("updateBoolean not implemented");

        }

        public void updateByte(String columnName, byte x) throws SQLException {
            throw new UnsupportedOperationException("updateByte not implemented");

        }

        public void updateShort(String columnName, short x) throws SQLException {
            throw new UnsupportedOperationException("updateShort not implemented");

        }

        public void updateInt(String columnName, int x) throws SQLException {
            throw new UnsupportedOperationException("updateInt not implemented");

        }

        public void updateLong(String columnName, long x) throws SQLException {
            throw new UnsupportedOperationException("updateLong not implemented");

        }

        public void updateFloat(String columnName, float x) throws SQLException {
            throw new UnsupportedOperationException("updateFloat not implemented");

        }

        public void updateDouble(String columnName, double x) throws SQLException {
            throw new UnsupportedOperationException("updateDouble not implemented");

        }

        public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
            throw new UnsupportedOperationException("updateBigDecimal not implemented");

        }

        public void updateString(String columnName, String x) throws SQLException {
            throw new UnsupportedOperationException("updateString not implemented");

        }

        public void updateBytes(String columnName, byte x[]) throws SQLException {
            throw new UnsupportedOperationException("updateBytes not implemented");

        }

        public void updateDate(String columnName, Date x) throws SQLException {
            throw new UnsupportedOperationException("updateDate not implemented");

        }

        public void updateTime(String columnName, Time x) throws SQLException {
            throw new UnsupportedOperationException("updateTime not implemented");

        }

        public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
            throw new UnsupportedOperationException("updateTimestamp not implemented");

        }

        public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
            throw new UnsupportedOperationException("updateAsciiStream not implemented");

        }

        public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
            throw new UnsupportedOperationException("updateBinaryStream not implemented");

        }

        public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
            throw new UnsupportedOperationException("updateCharacterStream not implemented");

        }

        public void updateObject(String columnName, Object x, int scale) throws SQLException {
            throw new UnsupportedOperationException("updateObject not implemented");

        }

        public void updateObject(String columnName, Object x) throws SQLException {
            throw new UnsupportedOperationException("updateObject not implemented");

        }

        public void insertRow() throws SQLException {
            throw new UnsupportedOperationException("insertRow not implemented");

        }

        public void updateRow() throws SQLException {
            throw new UnsupportedOperationException("updateRow not implemented");

        }

        public void deleteRow() throws SQLException {
            throw new UnsupportedOperationException("deleteRow not implemented");

        }

        public void refreshRow() throws SQLException {
            throw new UnsupportedOperationException("refreshRow not implemented");

        }

        public void cancelRowUpdates() throws SQLException {
            throw new UnsupportedOperationException("cancelRowUpdates not implemented");

        }

        public void moveToInsertRow() throws SQLException {
            throw new UnsupportedOperationException("moveToInsertRow not implemented");

        }

        public void moveToCurrentRow() throws SQLException {
            throw new UnsupportedOperationException("moveToCurrentRow not implemented");

        }

        public Statement getStatement() throws SQLException {
            throw new UnsupportedOperationException("getStatement not implemented");
        }

        public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
            throw new UnsupportedOperationException("getObject not implemented");
        }

        public Ref getRef(int i) throws SQLException {
            throw new UnsupportedOperationException("getRef not implemented");
        }

        public Blob getBlob(int i) throws SQLException {
            throw new UnsupportedOperationException("getBlob not implemented");
        }

        public Clob getClob(int i) throws SQLException {
            throw new UnsupportedOperationException("getClob not implemented");
        }

        public Array getArray(int i) throws SQLException {
            throw new UnsupportedOperationException("getArray not implemented");
        }

        public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
            throw new UnsupportedOperationException("getObject not implemented");
        }

        public Ref getRef(String colName) throws SQLException {
            throw new UnsupportedOperationException("getRef not implemented");
        }

        public Blob getBlob(String colName) throws SQLException {
            throw new UnsupportedOperationException("getBlob not implemented");
        }

        public Clob getClob(String colName) throws SQLException {
            throw new UnsupportedOperationException("getClob not implemented");
        }

        public Array getArray(String colName) throws SQLException {
            throw new UnsupportedOperationException("getArray not implemented");
        }

        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            throw new UnsupportedOperationException("getDate not implemented");
        }

        public Date getDate(String columnName, Calendar cal) throws SQLException {
            throw new UnsupportedOperationException("getDate not implemented");
        }

        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            throw new UnsupportedOperationException("getTime not implemented");
        }

        public Time getTime(String columnName, Calendar cal) throws SQLException {
            throw new UnsupportedOperationException("getTime not implemented");
        }

        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            throw new UnsupportedOperationException("getTimestamp not implemented");
        }

        public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
            throw new UnsupportedOperationException("getTimestamp not implemented");
        }

        public URL getURL(int columnIndex) throws SQLException {
            throw new UnsupportedOperationException("getURL not implemented");
        }

        public URL getURL(String columnName) throws SQLException {
            throw new UnsupportedOperationException("getURL not implemented");
        }

        public void updateRef(int columnIndex, Ref x) throws SQLException {
            throw new UnsupportedOperationException("updateRef not implemented");

        }

        public void updateRef(String columnName, Ref x) throws SQLException {
            throw new UnsupportedOperationException("updateRef not implemented");

        }

        public void updateBlob(int columnIndex, Blob x) throws SQLException {
            throw new UnsupportedOperationException("updateBlob not implemented");

        }

        public void updateBlob(String columnName, Blob x) throws SQLException {
            throw new UnsupportedOperationException("updateBlob not implemented");

        }

        public void updateClob(int columnIndex, Clob x) throws SQLException {
            throw new UnsupportedOperationException("updateClob not implemented");

        }

        public void updateClob(String columnName, Clob x) throws SQLException {
            throw new UnsupportedOperationException("updateClob not implemented");
        }

        public void updateArray(int columnIndex, Array x) throws SQLException {
            throw new UnsupportedOperationException("updateArray not implemented");
        }

        public void updateArray(String columnName, Array x) throws SQLException {
            throw new UnsupportedOperationException("updateArray not implemented");
        }
    }

    private static class ReflexiveDataSet implements IDataSet {
        public ITableMetaData getTableMetaData(String tableName) throws DataSetException {
            ITableMetaData table = createNiceMock(ITableMetaData.class);
            expect(table.getTableName()).andReturn(tableName);
            replay(table);

            return table;
        }

        //////
        ////// None of the rest of the methods are used
        //////

        public String[] getTableNames() throws DataSetException {
            throw new UnsupportedOperationException("getTableNames not implemented");
            // return new java.lang.String[0];
        }

        public ITable getTable(String tableName) throws DataSetException {
            throw new UnsupportedOperationException("getTable not implemented");
            // return null;
        }

        public ITable[] getTables() throws DataSetException {
            throw new UnsupportedOperationException("getTables not implemented");
            // return new org.dbunit.dataset.ITable[0];
        }

        public ITableIterator iterator() throws DataSetException {
            throw new UnsupportedOperationException("iterator not implemented");
            // return null;
        }

        public ITableIterator reverseIterator() throws DataSetException {
            throw new UnsupportedOperationException("reverseIterator not implemented");
            // return null;
        }
    }
}
