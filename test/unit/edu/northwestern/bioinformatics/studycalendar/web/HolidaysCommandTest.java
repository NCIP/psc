package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;
import java.util.Calendar;

/**
 * @author Nataliya Shurupova
 */

public class HolidaysCommandTest extends StudyCalendarTestCase {
    private HolidaysCommand command;
    private SiteDao siteDao;
    private Site site;


    protected void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        command = new HolidaysCommand(siteDao);

        site = new Site();
        site.getHolidaysAndWeekends().add(Fixtures.setId(1, new MonthDayHoliday()));
        site.getHolidaysAndWeekends().add(Fixtures.setId(2, new DayOfTheWeek()));
        site.getHolidaysAndWeekends().add(Fixtures.setId(3, new RelativeRecurringHoliday()));
        command.setSite(site);
    }

    public void testRemove() {
        command.setAction("Remove");
        command.setSelectedHoliday(new Integer("1"));

        siteDao.save(same(site));

        replayMocks();
        command.execute();
        verifyMocks();

        assertEquals("Holiday not removed", 2, site.getHolidaysAndWeekends().size());
        assertEquals("Wrong holiday remains", 2, (int) site.getHolidaysAndWeekends().get(0).getId());
    }

    public void testParseNonRecurringDate() throws Exception {
        String date= "12/23/2009";
        command.parse(date);
        assertEquals("Wrong day", new Integer(23), command.getDay());
        assertEquals("Wrong month", Calendar.DECEMBER, (int) command.getMonth());
        assertEquals("Wrong year", new Integer(2009), command.getYear());
    }

    public void testParseRecurringDate() throws Exception {
        String date = "3/11";
        command.parse(date);
        assertEquals("Wrong day ", 11, (int) command.getDay());
        assertEquals("Wrong month", Calendar.MARCH, (int) command.getMonth());
        assertEquals("Wrong year", null, command.getYear());
    }

    public void testAddNonRecurringDate() throws Exception {
        command.setAction("Add");
        command.setHolidayDate("12/1/2009");
        String expectedDescription = "sdsdfs sfs fjsd";
        command.setHolidayDescription(expectedDescription);

        siteDao.save(same(site));

        replayMocks();
        command.execute();
        verifyMocks();

        assertEquals("Didn't add a non recur. date", 4, site.getHolidaysAndWeekends().size());
        assertTrue(site.getHolidaysAndWeekends().get(3) instanceof MonthDayHoliday);
        MonthDayHoliday holiday = (MonthDayHoliday)site.getHolidaysAndWeekends().get(3);
        assertEquals("day doesn't match ", 1, (int) holiday.getDay());
        assertEquals("month doesn't match ", Calendar.DECEMBER, (int) holiday.getMonth());
        assertEquals("year is wrong ", 2009, (int) holiday.getYear());
        assertEquals("description doesn't match ", expectedDescription, holiday.getDescription());
    }

    public void testDayOfTheWeek() throws Exception {
        command.setAction("Add");
        String dayOfTheWeek = "Tuesday";
        command.setDayOfTheWeek(dayOfTheWeek);
        command.setHolidayDescription("off");

        siteDao.save(same(site));
        replayMocks();
        command.execute();
        verifyMocks();

        assertEquals("didn't add the day", 4, site.getHolidaysAndWeekends().size());
        assertEquals("Wrong day of the week", dayOfTheWeek,
                ((DayOfTheWeek)site.getHolidaysAndWeekends().get(3)).getDayOfTheWeek());
        assertEquals("wrong description ", "off", site.getHolidaysAndWeekends().get(3).getDescription());
    }

    public void testAddRelativeRecurringHoliday() throws Exception {
        command.setAction("Add");
        command.setWeek(1);
        command.setDayOfTheWeek("Monday");
        command.setMonth(Calendar.SEPTEMBER);
        String expectedDescription = "Memorial Day";
        command.setHolidayDescription(expectedDescription);

        siteDao.save(same(site));

        replayMocks();
        command.execute();
        verifyMocks();

        assertEquals("Didn't add a relative recurring holiday", 4, site.getHolidaysAndWeekends().size());
        assertTrue(site.getHolidaysAndWeekends().get(3) instanceof RelativeRecurringHoliday);
        RelativeRecurringHoliday relativeHoliday =
                (RelativeRecurringHoliday)site.getHolidaysAndWeekends().get(3);
        assertEquals("day of the week doesn't match ", "Monday", relativeHoliday.getDayOfTheWeek());
        assertEquals("month doesn't match ", Calendar.SEPTEMBER, (int) relativeHoliday.getMonth());
        assertEquals("description doesn't match ", expectedDescription, relativeHoliday.getDescription());

    }

    public void testUniqueDayOfTheWeek() throws Exception {
        DayOfTheWeek oneDayOfTheWeek = new DayOfTheWeek();
        oneDayOfTheWeek.setDayOfTheWeek("Monday");
        oneDayOfTheWeek.setDescription("Closed");

        DayOfTheWeek anotherDayOfTheWeek = new DayOfTheWeek();
        anotherDayOfTheWeek.setDayOfTheWeek("Monday");
        anotherDayOfTheWeek.setDescription("And definitely Closed");

        DayOfTheWeek thirdDayOfTheWeek = new DayOfTheWeek();
        thirdDayOfTheWeek.setDayOfTheWeek("Tuesday");
        thirdDayOfTheWeek.setDescription("whatever");

        List<BlackoutDate> list = site.getHolidaysAndWeekends();
        list.add(oneDayOfTheWeek);
        list.add(thirdDayOfTheWeek);

        assertEquals("objects are not equals ", true, command.isElementInTheList(list, oneDayOfTheWeek));
        assertEquals("objects are not equals ", true, command.isElementInTheList(list, anotherDayOfTheWeek));
        assertEquals("objects are equals ", true, command.isElementInTheList(list, thirdDayOfTheWeek));
    }
}
