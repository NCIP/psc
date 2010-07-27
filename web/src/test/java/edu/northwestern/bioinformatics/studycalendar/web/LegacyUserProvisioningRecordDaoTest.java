package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import freemarker.template.utility.StringUtil;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class LegacyUserProvisioningRecordDaoTest extends DaoTestCase {
    private DataSource dataSource;
    private LegacyUserProvisioningRecordDao dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        dataSource = (DataSource) getApplicationContext().getBean("dataSource");

        dao = new LegacyUserProvisioningRecordDao(dataSource);
    }

    public void testCorrectNumberOfRecordsSelected() {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();
        assertEquals("Wrong number of records", 5, actual.size());
    }

    public void testUsersWithoutRolesSelected() throws Exception {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();

        LegacyUserProvisioningRecord last = actual.get(actual.size() - 1);
        assertEquals("Wrong name", "Captain", last.getUserName());
        assertTrue("Site should be blank", isBlank(last.getSiteName()));
        assertTrue("Study should be blank", isBlank(last.getStudyName()));
    }

    public void testUsersWithSiteRolesSelected() throws Exception {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();

        LegacyUserProvisioningRecord first = actual.get(0);
        assertEquals("Wrong name", "Al", first.getUserName());
        assertEquals("Wrong Role", "STUDY_ADMIN", first.getRole());
        assertTrue("Site should be blank", isBlank(first.getSiteName()));
        assertTrue("Study should be blank", isBlank(first.getStudyName()));

        LegacyUserProvisioningRecord second = actual.get(1);
        assertEquals("Wrong name", "Al", second.getUserName());
        assertEquals("Wrong role", "SUBJECT_COORDINATOR", second.getRole());
        assertEquals("Wrong site", "Alpha Centauri", second.getSiteName());
        assertEquals("Wrong study", "Artichoke Study", second.getStudyName());

        LegacyUserProvisioningRecord third = actual.get(2);
        assertEquals("Wrong name", "Al", third.getUserName());
        assertEquals("Wrong role", "SUBJECT_COORDINATOR", third.getRole());
        assertEquals("Wrong site", "Alpha Centauri", third.getSiteName());
        assertEquals("Wrong study", "Cartwheel Study", third.getStudyName());

        LegacyUserProvisioningRecord fourth = actual.get(3);
        assertEquals("Wrong name", "Al", fourth.getUserName());
        assertEquals("Wrong role", "SUBJECT_COORDINATOR", fourth.getRole());
        assertEquals("Wrong site", "Canopus", fourth.getSiteName());
        assertTrue("Study should be blank", isBlank(fourth.getStudyName()));
    }
}
