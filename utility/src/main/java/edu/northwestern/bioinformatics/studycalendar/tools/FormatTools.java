package edu.northwestern.bioinformatics.studycalendar.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class FormatTools {
    private static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>();
    
    // TODO: make date format externally configurable
    public static DateFormat createDateFormat() {
        return new SimpleDateFormat("MM/dd/yyyy");
    }

    public static String formatDate(Date date) {
        if (dateFormat.get() == null) {
            dateFormat.set(createDateFormat());
        }
        return dateFormat.get().format(date);
    }

    private FormatTools() { }
}
