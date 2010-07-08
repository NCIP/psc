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
//        List<LegacyUserProvisioningRecord> actual = dao.getAll();
//
//        String[] query = new String[] {
//            "select distinct u.name, first_name, last_name, csm_group_name, s.name as site_name, st.assigned_identifier as study_name, active_flag",
//            "from Users u",
//            "left join User_Roles ur on u.id = ur.user_id",
//            "left join User_Role_Sites urs on ur.id = urs.user_role_id",
//            "left join User_Role_Study_Sites urss ON ur.id = urss.user_role_id",
//            "left join Study_Sites ss ON urss.study_site_id = ss.id AND urs.site_id = ss.site_id",
//            "left join Studies st ON ss.study_id = st.id",
//            "left join Sites s on ss.site_id = s.id",
//            "order by name, csm_group_name, site_name, study_name"
//        };
//
//
//        dumpResults(StringUtils.join(query, ' '));
//
//        LegacyUserProvisioningRecord first = actual.get(0);
//        assertEquals("Wrong name", "Al", first.getUserName());
//        assertEquals("Wrong Role", "STUDY_ADMIN", first.getRole());
//        assertTrue("Site should be blank", isBlank(first.getSiteName()));
//        assertTrue("Study should be blank", isBlank(first.getStudyName()));
//
//        LegacyUserProvisioningRecord second = actual.get(1);
//        assertEquals("Wrong name", "Al", second.getUserName());
//        assertEquals("Wrong role", "SUBJECT_COORDINATOR", second.getRole());
//        assertEquals("Wrong site", "Alpha Centauri", second.getSiteName());
//        assertEquals("Wrong study", "Artichoke Study", second.getStudyName());
//
//        LegacyUserProvisioningRecord third = actual.get(2);
//        assertEquals("Wrong name", "Al", third.getUserName());
//        assertEquals("Wrong role", "SUBJECT_COORDINATOR", third.getRole());
//        assertEquals("Wrong site", "Alpha Centauri", third.getSiteName());
//        assertEquals("Wrong study", "Cartwheel Study", third.getStudyName());
//
//        LegacyUserProvisioningRecord fourth = actual.get(3);
//        assertEquals("Wrong name", "Al", fourth.getUserName());
//        assertEquals("Wrong role", "SUBJECT_COORDINATOR", fourth.getRole());
//        assertEquals("Wrong site", "Canopus", fourth.getSiteName());
//        assertTrue("Study should be blank", isBlank(fourth.getStudyName()));
    }
}
