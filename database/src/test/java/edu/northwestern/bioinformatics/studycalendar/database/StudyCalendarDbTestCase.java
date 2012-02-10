package edu.northwestern.bioinformatics.studycalendar.database;

import edu.nwu.bioinformatics.commons.StringUtils;
import edu.nwu.bioinformatics.commons.testing.DbTestCase;
import edu.nwu.bioinformatics.commons.testing.HsqlDataTypeFactory;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarDbTestCase extends DbTestCase {
    // protected final Log log = LogFactory.getLog(getClass());
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DataAuditInfo.setLocal(new DataAuditInfo("jo", "127.0.0.8", new Date(),
            "/the/url"));
    }

    @Override
    protected void tearDown() throws Exception {
        DataAuditInfo.setLocal(null);
        emptyAuditTables();
        super.tearDown();
    }

     /** Empty all the audit tables from the database */
    private void emptyAuditTables() throws Exception
    {
        getJdbcTemplate().execute("DELETE FROM AUDIT_EVENTS");
        getJdbcTemplate().execute("DELETE FROM AUDIT_EVENT_VALUES");
    }

    @Override
    protected IDataTypeFactory createDataTypeFactory() {
        String productName = ((String) getJdbcTemplate().execute(new ConnectionCallback() {
            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                return con.getMetaData().getDatabaseProductName();
            }
        })).toLowerCase();
        if (productName.contains("oracle")) {
            return new OracleDataTypeFactory();
        }
        else if (productName.contains("hsql")) {
            return new HsqlDataTypeFactory();
        }
        else {
            return new DefaultDataTypeFactory();
        }
    }

    /**
     * Prints the (tabular) result of executing the given SELECT at the point
     * where it is invoked.  Useful for debugging apparently data-related problems
     * with your tests, since dbunit wipes out data post execution.
     * @param sql
     */
    @SuppressWarnings({ "unchecked" })
    protected final void dumpResults(String sql) {
        List<Map<String, Object>> rows = new JdbcTemplate(getDataSource()).query(
            sql, new ColumnMapRowMapper() {
            @Override
            protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
                Object value = super.getColumnValue(rs, index);
                return value == null ? "null" : value.toString();
            }
        }
        );
        StringBuffer dump = new StringBuffer(sql).append('\n');
        if (rows.size() > 0) {
            Map<String, Integer> colWidths = new HashMap<String, Integer>();
            for (String colName : rows.get(0).keySet()) {
                colWidths.put(colName, colName.length());
                for (Map<String, Object> row : rows) {
                    colWidths.put(colName, Math.max(colWidths.get(colName), row.get(colName).toString().length()));
                }
            }

            for (String colName : rows.get(0).keySet()) {
                StringUtils.appendWithPadding(colName, colWidths.get(colName), false, dump);
                dump.append("   ");
            }
            dump.append('\n');

            for (Map<String, Object> row : rows) {
                for (String colName : row.keySet()) {
                    StringUtils.appendWithPadding(row.get(colName).toString(), colWidths.get(colName), false, dump);
                    dump.append(" | ");
                }
                dump.append('\n');
            }
        }
        dump.append("  ").append(rows.size()).append(" row").append(rows.size() != 1 ? "s\n" : "\n");

        System.out.print(dump);
    }
}
