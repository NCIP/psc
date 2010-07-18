package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class SiteDaoTest extends DaoTestCase {
    private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
    private BlackoutDateDao blackoutDateDao = (BlackoutDateDao) getApplicationContext().getBean("blackoutDateDao");

    public void testGetById() throws Exception {
        Site actual = siteDao.getById(-4);
        assertNotNull("Site not found", actual);
        assertEquals("Wrong id", -4, (int) actual.getId());
        assertEquals("Wrong name", "default", actual.getName());
        assertEquals("Wrong provider", "coppa-direct", actual.getProvider());
        assertDayOfDate("Wrong last refresh", 2007, Calendar.MARCH, 23, actual.getLastRefresh());
        assertTimeOfDate("Wrong last refresh", 13, 45, 7, 0, actual.getLastRefresh());
        assertEquals("Wrong number of managed studies", 1, actual.getManagedStudies().size());
        Study actualManagedStudy = actual.getManagedStudies().iterator().next();
        assertEquals("Wrong managed study", "EL 5203", actualManagedStudy.getAssignedIdentifier());
        assertSame("Birdirectional relationship not loaded for managed study", actual, actualManagedStudy.getManagingSites().iterator().next());
    }

    public void testGetByAssignedIdentifier() throws Exception {
        Site actual = siteDao.getByAssignedIdentifier("assignedId");
        assertNotNull("site not found", actual);
        assertEquals("Wrong assignedIdentifier", "assignedId", actual.getAssignedIdentifier());
        //check for name also
        actual = siteDao.getByAssignedIdentifier("default");
        assertNull("site must not exists because identifier is not null", actual);

        actual = siteDao.getByAssignedIdentifier("anotherSite");
        assertNotNull("site not found", actual);
        assertEquals("Wrong assignedIdentifier", "anotherSite", actual.getAssignedIdentifier());

        actual = siteDao.getByAssignedIdentifier("Mayo Clinic");
        assertEquals("Wrong assigned identifier", "Mayo Clinic", actual.getAssignedIdentifier());
        assertEquals("Wrong name", "Mayo Clinic", actual.getName());
    }

    public void testGetByAssignedIdentifiers() throws Exception {
        List<Site> actual = siteDao.getByAssignedIdentifiers(Arrays.asList("IL036", "TN008"));
        assertEquals("Wrong number of sites found", 2, actual.size());
        assertEquals("Wrong first site", new Integer(-10), actual.get(0).getId());
        assertEquals("Wrong second site", new Integer(-11), actual.get(1).getId());
    }

    public void testGetByAssignedIdentifiersReturnEmptyForUnknown() throws Exception {
        List<Site> actual = siteDao.getByAssignedIdentifiers(Arrays.asList("IL037"));
        assertEquals("Wrong number of sites found", 0, actual.size());
    }

    public void testDeleteHoliday() throws Exception {
        Site actual = siteDao.getById(-4);
        actual.getBlackoutDates().remove(1);
        siteDao.save(actual);

        interruptSession();

        Site reloaded = siteDao.getById(-4);
        assertEquals("BlackoutDate not removed", 1, reloaded.getBlackoutDates().size());
        assertEquals("Wrong holiday removed", -2,
                (int) reloaded.getBlackoutDates().get(0).getId());
    }

    public void testAddHoliday() throws Exception {
        Site actual = siteDao.getById(-4);

        RelativeRecurringBlackout holidayToAdd = new RelativeRecurringBlackout();
        holidayToAdd.setWeekNumber(1);
        holidayToAdd.setDayOfTheWeek("Monday");
        holidayToAdd.setMonth(Calendar.SEPTEMBER);
        holidayToAdd.setId(-3);
        holidayToAdd.setDescription("Closed");
        holidayToAdd.setSite(actual);
        List<BlackoutDate> list = actual.getBlackoutDates();
        int size = list.size();
        list.add(holidayToAdd);
        actual.setBlackoutDates(list);

        blackoutDateDao.save(holidayToAdd);
        siteDao.save(actual);

        interruptSession();

        Site reloaded = siteDao.getById(-4);
        assertEquals("BlackoutDate is not added", size + 1, reloaded.getBlackoutDates().size());
        assertEquals("Wrong holiday added", holidayToAdd,
                reloaded.getBlackoutDates().get(2));
    }

    public void testCount() throws Exception {
        assertEquals("Should be five sites, to start", 5, siteDao.getCount());

        Site newSite = new Site();
        newSite.setName("Hampshire");
        siteDao.save(newSite);
        interruptSession();
        assertEquals("Should be six sites after saving", 6, siteDao.getCount());

        siteDao.delete(siteDao.getById(-4));
        interruptSession();
        assertEquals("And now there should be five again", 5, siteDao.getCount());
    }

    public void testDeleteWithManagedStudies() throws Exception {
        siteDao.delete(siteDao.getById(-4));
        interruptSession();
        assertNull(siteDao.getById(-4));
    }
}
