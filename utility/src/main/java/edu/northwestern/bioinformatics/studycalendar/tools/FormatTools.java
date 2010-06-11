package edu.northwestern.bioinformatics.studycalendar.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class FormatTools {
    private static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>();
    private final static String DAY_MONTH = "dd/MM";
    private final static String MONTH_DAY = "MM/dd";


    // TODO: make date format externally configurable
    public static DateFormat createDateFormat() {
        return new SimpleDateFormat("MM/dd/yyyy");
    }

    public static DateFormat createDateFormat(String dateFormatFromConfiguration) {
        if (dateFormatFromConfiguration.equals("MM/DD/YYYY")) {
            return new SimpleDateFormat("MM/dd/yyyy");
        } else if (dateFormatFromConfiguration.equals("DD/MM/YYYY")) {
            return new SimpleDateFormat("dd/MM/yyyy");
        } else {
            //todo should throw unsupported exception
//            throw new Exception("Date format is not supported. Should be MM/dd/yyyy or dd/MM/yyyy");
            return new SimpleDateFormat("MM/dd/yyyy");
        }
    }

    public static DateFormat createDateFormatAsDayOrMonth(String dateFormat) {
        if (dateFormat.startsWith("DD/MM")) {
            return new SimpleDateFormat(DAY_MONTH);
        } else {
            return new SimpleDateFormat(MONTH_DAY);
        }
    }

   //todo - this has to be changed!
    public static String formatDate(Date date) {
        if (dateFormat.get() == null) {
            dateFormat.set(createDateFormat());
        }
        return dateFormat.get().format(date);
    }

    private FormatTools() { }
}
