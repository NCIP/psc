package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Jalpa Patel
 */
public class DateFormatTest extends TestCase {
    private Calendar calendar = Calendar.getInstance();

    public void setUp() throws Exception {
        super.setUp();
        calendar.set(2011, 10, 2, 15, 25);
    }

    public void testGenerateTimeFromDate() throws Exception {
        String time = DateFormat.generateTimeFromDate(calendar.getTime());
        assertEquals("Time does not match", "15:25", time);
    }

    public void testGenerateAmPmTimeFromDate() throws Exception {
        String time = DateFormat.generateAmPmTimeFromDate(calendar.getTime());
        assertEquals("Time does not match", "3:25 PM", time);
    }

    public void testGenerateDateTime() throws Exception {
        String time = "15:25";
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 10, 2);
        Date date = DateFormat.generateDateTime(calendar.getTime(), time);
        assertEquals("DateTime does not match", calendar.getTime(), date);
    }

    public void testGenerateDateTimeWithInvalidTime() throws Exception {
        String time = "15 25";
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 10, 2);
        try {
            DateFormat.generateDateTime(calendar.getTime(), time);
            fail("Exception not thrown");
        } catch (ParseException e) {
            assertEquals("Unparseable date: \"15 25\"", e.getMessage());
        }
    }

    public void testGenerateAmPmDateTime() throws Exception {
        String time = "3:25 PM";
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 10, 2);
        Date date = DateFormat.generateAmPmDateTime(calendar.getTime(), time);
        assertEquals("DateTime does not match", calendar.getTime(), date);
    }

    public void testGenerateAmPmDateTimeInvalid() throws Exception {
        String time = "325 PM";
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 10, 2);
        try {
            DateFormat.generateAmPmDateTime(calendar.getTime(), time);
            fail("Exception not thrown");
        } catch (ParseException e) {
            assertEquals("Unparseable date: \"325 PM\"", e.getMessage());
        }
    }
}
