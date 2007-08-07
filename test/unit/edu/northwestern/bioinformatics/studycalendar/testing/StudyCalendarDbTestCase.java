package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.nwu.bioinformatics.commons.testing.DbTestCase;
import edu.nwu.bioinformatics.commons.testing.HsqlDataTypeFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarDbTestCase extends DbTestCase {
//    protected final Log log = LogFactory.getLog(getClass());
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DataAuditInfo.setLocal(new DataAuditInfo("jo", "127.0.0.8", new Date(), "/the/url"));
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
    // XXX: This is sort of a hack, but it works.  (A more declarative solution would be better.)
    protected IDataTypeFactory createDataTypeFactory() {
        Properties hibProps = (Properties) getApplicationContext().getBean("hibernateProperties");
        String dialectName = hibProps.getProperty("hibernate.dialect").toLowerCase();
        if (dialectName.contains("oracle")) {
            return new OracleDataTypeFactory();
        } else if (dialectName.contains("hsql")) {
            return new HsqlDataTypeFactory();
        } else {
            return new DefaultDataTypeFactory();
        }
    }
}
