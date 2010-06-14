package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.text.DateFormat;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Nataliya Shurupova
 */
public class FormatToolsTest extends TestCase {
    private FormatTools tools;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tools = new FormatTools("MM/dd/yyyy");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FormatTools.clearLocalInstance();
    }

    public void testUsesConfiguredDateFormat() {
        assertEquals("Not formatted correctly", 
                "12/01/2003", tools.formatDate(DateTools.createDate(2003, Calendar.DECEMBER, 1)));
    }

    public void testUsesAlternativeFormatIfConfigured() throws Exception {
        tools.setDateFormatString("dd/MM/yyyy");
        assertEquals("Not formatted correctly",
                "01/12/2003", tools.formatDate(DateTools.createDate(2003, Calendar.DECEMBER, 1)));
    }

    public void testGetDateFormat() throws Exception {
        DateFormat actual = tools.getDateFormat();
        assertEquals("Not formatted correctly",
                "03/30/2008", actual.format(DateTools.createDate(2008, Calendar.MARCH, 30)));
    }

    public void testGetMonthDayFormatString() throws Exception {
        assertEquals("Wrong format", "MM/dd", tools.getMonthDayFormatString());
    }

    public void testGetMonthDayFormatStringForAlternativeFormat() throws Exception {
        tools.setDateFormatString("dd/MM/yyyy");
        assertEquals("Wrong format", "dd/MM", tools.getMonthDayFormatString());
    }

    public void testGetMonthDayFormatStringForUnconfiguredFormat() throws Exception {
        tools.setDateFormatString(FormatTools.UNCONFIGURED_FORMAT);
        assertEquals("Wrong format", "MM-dd", tools.getMonthDayFormatString());
    }

    public void testGetMonthDayFailsForUnknownDateFormat() throws Exception {
        tools.setDateFormatString("yyyy/MM/dd");
        try {
            tools.getMonthDayFormatString();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("Wrong message", "Unsupported base date format for month-day: yyyy/MM/dd", scse.getMessage());
        }
    }
    
    public void testGetMonthDayFormat() throws Exception {
        DateFormat actual = tools.getMonthDayFormat();
        assertEquals("Not formatted correctly",
                "05/31", actual.format(DateTools.createDate(2009, Calendar.MAY, 31)));
    }

    public void testUsesAlternativeFormatGetMonthDay() throws Exception {
        tools.setDateFormatString("dd/MM/yyyy");
        DateFormat actual = tools.getMonthDayFormat();
        assertEquals("Not formatted correctly",
                "31/05", actual.format(DateTools.createDate(2009, Calendar.MAY, 31)));
    }

    public void testGetReturnsDefaultInstanceIfNoneSet() throws Exception {
        FormatTools actual = FormatTools.getLocal();
        assertEquals("Wrong default format", FormatTools.UNCONFIGURED_FORMAT, actual.getDateFormatString());
    }

    public void testGetReturnsConfiguredInstanceIfSet() throws Exception {
        FormatTools instance = new FormatTools("dd/MM/yyyy");
        FormatTools.setLocal(instance);

        assertEquals("Wrong returned format", "dd/MM/yyyy", FormatTools.getLocal().getDateFormatString());
    }

    public void testClear() throws Exception {
        FormatTools.setLocal(new FormatTools("dd/MM/yyyy"));
        FormatTools.clearLocalInstance();
        assertFalse("Not cleared", FormatTools.hasLocalInstance());
    }
}
