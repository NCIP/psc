package edu.northwestern.bioinformatics.studycalendar.core.editors;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Jalpa Patel
 */
public class EditorUtils {
    private static final String CHAR_ENCODE = "UTF-8";
    public static final String SEPARATOR = " ";

    public static String getEncodedString(String str) {
        try {
            return URLEncoder.encode(str, CHAR_ENCODE);
        } catch (UnsupportedEncodingException e) {
            throw new StudyCalendarError("Unsupported character encoding", e);
        }
    }

    public static String getDecodedString(String str) {
        try {
            return URLDecoder.decode(str, CHAR_ENCODE);
        } catch (UnsupportedEncodingException e) {
            throw new StudyCalendarError("Unsupported character encoding", e);
        }
    }

    public static String[] splitValue(String str) {
        return StringUtils.split(str, SEPARATOR);
    }

}
