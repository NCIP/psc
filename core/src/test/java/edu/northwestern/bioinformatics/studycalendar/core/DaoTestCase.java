package edu.northwestern.bioinformatics.studycalendar.core;

import edu.nwu.bioinformatics.commons.StringUtils;
import edu.northwestern.bioinformatics.studycalendar.database.StudyCalendarDbTestCase;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class DaoTestCase extends StudyCalendarDbTestCase {

    protected MockHttpServletRequest request = new MockHttpServletRequest();
    protected MockHttpServletResponse response = new MockHttpServletResponse();
    protected WebRequest webRequest = new ServletWebRequest(request);
    private boolean shouldFlush = true;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        beginSession();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            endSession();
        } finally {
            super.tearDown();
        }
    }

    @Override
    public void runBare() throws Throwable {
        setUp();
        try {
            runTest();
        }
        catch (Throwable throwable) {
            shouldFlush = false;
            throw throwable;
        }
        finally {
            tearDown();
        }
    }

    private void beginSession() {
        log.info("-- beginning DaoTestCase interceptor session --");
        for (OpenSessionInViewInterceptor interceptor : interceptors()) {
            interceptor.preHandle(webRequest);
        }
    }

    private void endSession() {
        log.info("--    ending DaoTestCase interceptor session --");
        for (OpenSessionInViewInterceptor interceptor : reverseInterceptors()) {
            if (shouldFlush) {
                interceptor.postHandle(webRequest, null);
            }
            interceptor.afterCompletion(webRequest, null);
        }
    }

    protected void interruptSession() {
        endSession();
        log.info("-- interrupted DaoTestCase session --");
        beginSession();
    }

    private List<OpenSessionInViewInterceptor> interceptors() {
        return Arrays.asList((OpenSessionInViewInterceptor) getApplicationContext().getBean(
            "auditOpenSessionInViewInterceptor"), (OpenSessionInViewInterceptor) getApplicationContext().getBean(
            "openSessionInViewInterceptor"));
    }

    private List<OpenSessionInViewInterceptor> reverseInterceptors() {
        List<OpenSessionInViewInterceptor> interceptors = interceptors();
        Collections.reverse(interceptors);
        return interceptors;
    }

    @SuppressWarnings({ "unchecked" })
    protected final void dumpResults(String sql) {
        List<Map<String, String>> rows = new JdbcTemplate(getDataSource()).query(
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

    @Override
    protected DataSource getDataSource() {
        return (DataSource) getApplicationContext().getBean("dataSource");
    }

    public static ApplicationContext getApplicationContext() {
        return StudyCalendarTestCase.getDeployedApplicationContext();
    }
}
