package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.LegacyUserProvisioningRecordDao;
import edu.northwestern.bioinformatics.studycalendar.domain.LegacyUserProvisioningRecord;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

public class LegacyUserProvisioningRecordExporter implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Boolean exporterEnabled = Boolean.valueOf(servletContextEvent.getServletContext().getInitParameter("legacyUserProvisioningExporterSwitch"));
        if (!exporterEnabled) { return; }

        String catalinaHome = System.getProperty("catalina.home");

        if (isNotEmpty(catalinaHome)) {

            File logDir = new File(catalinaHome + File.separator + "logs");

            logDir.mkdirs();

            if (logDir.exists()) {
                LegacyUserProvisioningRecordFile f =
                        new LegacyUserProvisioningRecordFile(logDir.getPath());

                for (LegacyUserProvisioningRecord r : getDao(servletContextEvent).getAll()) {
                    f.write(r.csv());
                }

                f.close();

            } else {
                log.debug("Problem creating the log directory: " + logDir.getPath());
            }

        } else {
            String msg = "Problem exporting the legacy user provisioning data. $CATALINA_HOME is not set.";
            log.debug(msg);
            throw new StudyCalendarSystemException(msg);
        }

    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {}

    protected LegacyUserProvisioningRecordDao getDao(ServletContextEvent sce) {
        WebApplicationContext ctx = getRequiredWebApplicationContext(sce.getServletContext());
        DataSource dataSource = (DataSource) ctx.getBean("dataSource");
        return new LegacyUserProvisioningRecordDao(dataSource);
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
