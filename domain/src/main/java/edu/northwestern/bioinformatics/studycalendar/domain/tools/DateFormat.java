package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Jalpa Patel
 */
public class DateFormat {
    static DecimalFormat nft = new DecimalFormat("00");

    public static SimpleDateFormat getISO8601Format() {
       return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    }

    public static SimpleDateFormat getUTCFormat() {
       return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String generateTimeFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new StringBuffer().append(calendar.get(Calendar.HOUR_OF_DAY))
                .append(":").append(nft.format(calendar.get(Calendar.MINUTE))).toString();
    }

    public static String generateAmPmTimeFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String format = "AM" ;
        if (calendar.get(Calendar.AM_PM) == 1) {
           format = "PM";
        }
        return new StringBuffer().append(calendar.get(Calendar.HOUR))
                .append(":").append(nft.format(calendar.get(Calendar.MINUTE)))
                .append(" ").append(format).toString();
    }

    public static Date generateDateTime(Date date, String time) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat sdf  =  new SimpleDateFormat("HH:mm");
        Date dateTime = sdf.parse(time);
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(dateTime);

        calendar.set(Calendar.HOUR_OF_DAY, newCal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, newCal.get(Calendar.MINUTE));
        return calendar.getTime();
    }

    public static Date generateAmPmDateTime (Date date, String time) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat sdf  =  new SimpleDateFormat("hh:mm a");
        Date dateTime = sdf.parse(time);
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(dateTime);

        calendar.set(Calendar.HOUR, newCal.get(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, newCal.get(Calendar.MINUTE));
        calendar.set(Calendar.AM_PM, newCal.get(Calendar.AM_PM));
        return calendar.getTime();
    }

}
