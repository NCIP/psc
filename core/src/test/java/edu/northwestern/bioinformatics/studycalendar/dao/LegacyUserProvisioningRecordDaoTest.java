package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.LegacyUserProvisioningRecord;
import junit.framework.Assert;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class LegacyUserProvisioningRecordDaoTest extends DaoTestCase {
    private LegacyUserProvisioningRecordDao dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        dao = new LegacyUserProvisioningRecordDao(getDataSource());
    }

    public void testCorrectNumberOfRecordsSelected() {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();
        assertEquals("Wrong number of records", 5, actual.size());
    }

    public void testUsersWithoutRolesSelected() throws Exception {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();

        LegacyUserProvisioningRecord last = actual.get(actual.size() - 1);
        Assert.assertEquals("Wrong name", "Captain", last.getUserName());
        assertTrue("Site should be blank", isBlank(last.getSiteName()));
        assertTrue("Study should be blank", isBlank(last.getStudyName()));
    }

    public void testUsersWithSiteRolesSelected() throws Exception {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();

        LegacyUserProvisioningRecord first = actual.get(0);
        Assert.assertEquals("Wrong name", "Al", first.getUserName());
        Assert.assertEquals("Wrong Role", "STUDY_ADMIN", first.getRole());
        assertTrue("Site should be blank", isBlank(first.getSiteName()));
        assertTrue("Study should be blank", isBlank(first.getStudyName()));

        LegacyUserProvisioningRecord second = actual.get(1);
        Assert.assertEquals("Wrong name", "Al", second.getUserName());
        Assert.assertEquals("Wrong role", "SUBJECT_COORDINATOR", second.getRole());
        Assert.assertEquals("Wrong site", "Alpha Centauri", second.getSiteName());
        Assert.assertEquals("Wrong study", "Artichoke Study", second.getStudyName());

        LegacyUserProvisioningRecord third = actual.get(2);
        Assert.assertEquals("Wrong name", "Al", third.getUserName());
        Assert.assertEquals("Wrong role", "SUBJECT_COORDINATOR", third.getRole());
        Assert.assertEquals("Wrong site", "Alpha Centauri", third.getSiteName());
        Assert.assertEquals("Wrong study", "Cartwheel Study", third.getStudyName());

        LegacyUserProvisioningRecord fourth = actual.get(3);
        Assert.assertEquals("Wrong name", "Al", fourth.getUserName());
        Assert.assertEquals("Wrong role", "SUBJECT_COORDINATOR", fourth.getRole());
        Assert.assertEquals("Wrong site", "Canopus", fourth.getSiteName());
        assertTrue("Study should be blank", isBlank(fourth.getStudyName()));
    }
}
