package edu.northwestern.bioinformatics.studycalendar.tools;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rhett Sutphin
 * @author Nataliya Shurupova
 */
public class FormatTools {
    public static final String UNCONFIGURED_FORMAT = "yyyy-MM-dd";
    private static final String UNCONFIGURED_MONTH_DAY_FORMAT = "MM-dd";
    private static final String EXPECTED_YEAR_SUFFIX = "/yyyy";

    private String dateFormatString;

    private static ThreadLocal<FormatTools> instance = new ThreadLocal<FormatTools>();

    public FormatTools(String format) {
        setDateFormatString(format);
    }

    ////// INSTANCE ACCESS

    public static FormatTools getLocal() {
        if (!hasLocalInstance()) {
            setLocal(createDefaultInstance());
        }
        return instance.get();
    }

    private static FormatTools createDefaultInstance() {
        return new FormatTools(UNCONFIGURED_FORMAT);
    }

    public static void setLocal(FormatTools tools) {
        instance.set(tools);
    }

    public static void clearLocalInstance() {
        instance.set(null);
    }

    public static boolean hasLocalInstance() {
        return instance.get() != null;
    }

    ////// LOGIC

    public String getMonthDayFormatString() {
        if (UNCONFIGURED_FORMAT.equals(getDateFormatString())) {
            return UNCONFIGURED_MONTH_DAY_FORMAT;
        } else if (getDateFormatString().endsWith(EXPECTED_YEAR_SUFFIX))  {
            return getDateFormatString().substring(0, getDateFormatString().length() - EXPECTED_YEAR_SUFFIX.length());
        } else {
            throw new StudyCalendarSystemException("Unsupported base date format for month-day: %s", getDateFormatString());
        }
    }

    public String formatDate(Date date) {
        return getDateFormat().format(date);
    }

    ////// BEAN PROPERTIES

    public String getDateFormatString() {
        return dateFormatString;
    }

    public void setDateFormatString(String dateFormat) {
        this.dateFormatString = dateFormat;
    }

    public DateFormat getDateFormat() {
        return new SimpleDateFormat(dateFormatString);
    }

    public DateFormat getMonthDayFormat() {
        return new SimpleDateFormat(getMonthDayFormatString());
//        throw new UnsupportedOperationException("getMonthDayFormat not implemented yet.");
    }
}
