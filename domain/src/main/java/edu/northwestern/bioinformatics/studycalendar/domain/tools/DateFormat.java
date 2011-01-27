package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import java.text.SimpleDateFormat;

/**
 * @author Jalpa Patel
 */
public class DateFormat {
    public static SimpleDateFormat getISO8601Format() {
       return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    }

    public static SimpleDateFormat getUTCFormat() {
       return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
}
