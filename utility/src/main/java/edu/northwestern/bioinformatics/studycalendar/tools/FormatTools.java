/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Encapsulates PSC's configured date format.  Instances are immutable but,
 * because they include a {@link SimpleDateFormat}, are not thread-safe.
 * However, the class provides accessors for a {@link ThreadLocal} instance.
 *
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
        this.dateFormatString = format;
    }

    ////// INSTANCE ACCESS

    public static FormatTools getLocal() {
        if (!hasLocalInstance()) {
            setLocal(createDefaultInstance());
        }
        return instance.get();
    }

    // package level for testing
    static FormatTools createDefaultInstance() {
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

    public DateFormat getDateFormat() {
        return new SimpleDateFormat(getDateFormatString());
    }

    public DateFormat getMonthDayFormat() {
        return new SimpleDateFormat(getMonthDayFormatString());
    }
}
