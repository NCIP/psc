package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.LegacyUserProvisioningRecordDao;
import edu.northwestern.bioinformatics.studycalendar.domain.LegacyUserProvisioningRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.Boolean.valueOf;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

public class LegacyUserProvisioningRecordExporter implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ServletContextEvent servletContextEvent;

    private List<String> LEGACY_PROVISIOINING_TABLES = asList("user_role_study_sites", "user_role_sites", "user_roles", "users");

    public void contextInitialized(ServletContextEvent sce) {
        servletContextEvent = sce;

        if (!isExporterEnabled() || !isStructurePresent()) { return; }

        exportRecords();

        dropTables();

    }

    private void dropTables() {
        try {
            Statement sta = getDataSource().getConnection().createStatement();
            for (String table : LEGACY_PROVISIOINING_TABLES) {
                sta.executeUpdate("DROP TABLE " + table);
            }
        } catch (SQLException e) {
            String msg = "Problem dropping the legacy provisioning tables: " + StringUtils.join(LEGACY_PROVISIOINING_TABLES, ", ");
            log.debug(msg);
            throw new StudyCalendarSystemException(msg, e);
        }
    }

    private void exportRecords() {
        String catalinaHome = System.getProperty("catalina.home");

        if (isNotEmpty(catalinaHome)) {

            File logDir = new File(catalinaHome + File.separator + "logs");

            logDir.mkdirs();

            if (logDir.exists()) {
                LegacyUserProvisioningRecordFile f =
                        new LegacyUserProvisioningRecordFile(logDir.getPath());

                for (LegacyUserProvisioningRecord r : getDao().getAll()) {
                    f.write(r.csv());
                }

                f.close();

            } else {
                String msg = "Problem creating the log directory: " + logDir.getPath();
                log.debug(msg);
                throw new StudyCalendarSystemException(msg);
            }

        } else {
            String msg = "Problem exporting the legacy user provisioning data. $CATALINA_HOME is not set.";
            log.debug(msg);
            throw new StudyCalendarSystemException(msg);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {}

    protected WebApplicationContext getWebApplicationContext() {
        return getRequiredWebApplicationContext(servletContextEvent.getServletContext());
    }

    protected DataSource getDataSource() {
        return (DataSource) getWebApplicationContext().getBean("dataSource");
    }

    protected LegacyUserProvisioningRecordDao getDao() {
        return new LegacyUserProvisioningRecordDao(getDataSource());
    }

    protected boolean isExporterEnabled() {
        return valueOf(servletContextEvent.getServletContext().getInitParameter("legacyUserProvisioningExporterSwitch"));
    }

    protected boolean isStructurePresent() {
        List<String> tables = legacyUserProvisioningTables();

        return tables.containsAll(LEGACY_PROVISIOINING_TABLES);
    }

    private List<String> legacyUserProvisioningTables() {
        List<String> tables = new ArrayList<String>();
        try {
            DatabaseMetaData md = getDataSource().getConnection().getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);

            while (rs.next()) {
              tables.add(rs.getString(3).toLowerCase());
            }
        } catch (SQLException e) {
            String msg = "Problem querying the database table structure to see if the necessary legacy user provisioning tables exist.";
            log.debug(msg);
            throw new StudyCalendarSystemException(msg);
        }
        return tables;
    }

    class LegacyUserProvisioningRecordFile {
        private File file;
        private String directoryPath;
        private BufferedWriter writer;

        LegacyUserProvisioningRecordFile(String directoryPath) {
            this.directoryPath = directoryPath;
        }

        private String getFileName() {
            String timestamp = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
            return "studycalendar-pre28-users-" + timestamp + ".csv";
        }

        protected String getPath() {
            return directoryPath + File.separator + getFileName();
        }

        public void write(String line) {
            if (open()) {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    String msg = "Problem writing out the legacy user provisioning information: " + file.getPath();
                    log.debug(msg, e);
                    throw new StudyCalendarSystemException(msg, e);
                }
            }
        }

        protected boolean open() {
            if (file == null || writer == null) {
                file = new File(getPath());

                try {
                    file.createNewFile();
                } catch (IOException e) {
                    String msg = "Problem creating the legacy user provisioning information file: " + file.getPath();
                    log.debug(msg, e);
                    throw new StudyCalendarSystemException(msg, e);
                }

                try {
                    writer = new BufferedWriter(new FileWriter(file));
                } catch (IOException e) {
                    String msg = "Problem opening the legacy user provisioning information file: " + file.getPath();
                    log.debug(msg, e);
                    throw new StudyCalendarSystemException(msg, e);
                }
            }

            return true;
        }

        public void close() {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    String msg = "Problem closing the legacy user provisioning information file: " + file.getPath();
                    log.debug(msg, e);
                    throw new StudyCalendarSystemException(msg, e);
                }
            }
        }
    }
}
