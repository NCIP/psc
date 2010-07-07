package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;

import javax.sql.DataSource;
import java.util.List;

public class LegacyUserProvisioningRecordDaoTest extends DaoTestCase {
    private DataSource dataSource;
    private LegacyUserProvisioningRecordDao dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        dataSource = (DataSource) getApplicationContext().getBean("dataSource");

        dao = new LegacyUserProvisioningRecordDao(dataSource);
    }

    public void testRecordsAreSelected() throws Exception {
        List<LegacyUserProvisioningRecord> actual = dao.getAll();
        assertEquals("Wrong number of records", 2, actual.size());

        assertEquals("Wrong name", "Zena", actual.get(0).getUserName());
        assertEquals("Wrong name", "Zorro", actual.get(1).getUserName());
    }
}
