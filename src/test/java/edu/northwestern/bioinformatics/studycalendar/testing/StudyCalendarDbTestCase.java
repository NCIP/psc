package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.nwu.bioinformatics.commons.testing.DbTestCase;
import edu.nwu.bioinformatics.commons.testing.HsqlDataTypeFactory;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

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
        super.tearDown();
    }

    @Override
    protected DataSource getDataSource() {
        return (DataSource) getApplicationContext().getBean("dataSource");
    }

    public static ApplicationContext getApplicationContext() {
        return StudyCalendarTestCase.getDeployedApplicationContext();
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
}
