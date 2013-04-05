/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.jdbcmock;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * @author Rhett Sutphin
 */
public class NoOperationsDatabaseMetaData implements DatabaseMetaData {
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

    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
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

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        throw new UnsupportedOperationException("getExportedKeys not implemented");
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
