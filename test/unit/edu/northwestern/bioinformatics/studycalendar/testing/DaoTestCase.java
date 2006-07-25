package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.nwu.bioinformatics.commons.testing.DbTestCase;
import edu.nwu.bioinformatics.commons.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * @author Rhett Sutphin
 */
public abstract class DaoTestCase extends DbTestCase {
    private static ApplicationContext applicationContext = null;
    protected final Log log = LogFactory.getLog(getClass());

    protected MockHttpServletRequest request = new MockHttpServletRequest();
    protected MockHttpServletResponse response = new MockHttpServletResponse();
    private boolean shouldFlush = true;

    protected void setUp() throws Exception {
        super.setUp();
        beginSession();
    }

    protected void tearDown() throws Exception {
        endSession();
        super.tearDown();
    }

    public void runBare() throws Throwable {
        setUp();
        try {
            runTest();
        } catch (Throwable throwable) {
            shouldFlush = false;
            throw throwable;
        } finally {
            tearDown();
        }
    }

    private void beginSession() {
        log.info("-- beginning DaoTestCase interceptor session --");
        findOpenSessionInViewInterceptor().preHandle(request, response, null);
    }

    private void endSession() {
        log.info("--    ending DaoTestCase interceptor session --");
        OpenSessionInViewInterceptor interceptor = findOpenSessionInViewInterceptor();
        if (shouldFlush) {
            interceptor.postHandle(request, response, null, null);
        }
        interceptor.afterCompletion(request, response, null, null);
    }

    protected void interruptSession() {
        endSession();
        log.info("-- interrupted DaoTestCase session --");
        beginSession();
    }

    private OpenSessionInViewInterceptor findOpenSessionInViewInterceptor() {
        return (OpenSessionInViewInterceptor) getApplicationContext().getBean("openSessionInViewInterceptor");
    }


    protected DataSource getDataSource() {
        return (DataSource) getApplicationContext().getBean("dataSource");
    }

    public ApplicationContext getApplicationContext() {
        synchronized (DaoTestCase.class) {
            if (applicationContext == null) {
                applicationContext = ContextTools.createDeployedApplicationContext();
            }
            return applicationContext;
        }
    }

    protected final void dumpResults(String sql) {
        List<Map<String, String>> rows = new JdbcTemplate(getDataSource()).query(
            sql,
            new ColumnMapRowMapper() {
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
                for (Map<String, String> row : rows) {
                    colWidths.put(colName, Math.max(colWidths.get(colName), row.get(colName).length()));
                }
            }

            for (String colName : rows.get(0).keySet()) {
                StringUtils.appendWithPadding(colName, colWidths.get(colName), false, dump);
                dump.append("   ");
            }
            dump.append('\n');

            for (Map<String, String> row : rows) {
                for (String colName : row.keySet()) {
                    StringUtils.appendWithPadding(row.get(colName), colWidths.get(colName), false, dump);
                    dump.append(" | ");
                }
                dump.append('\n');
            }
        }
        dump.append("  ").append(rows.size()).append(" row").append(rows.size() != 1 ? "s\n" : "\n");

        System.out.print(dump);
    }
}
