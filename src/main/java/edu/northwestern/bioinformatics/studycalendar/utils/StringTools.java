package edu.northwestern.bioinformatics.studycalendar.utils;

/**
 * @author Rhett Sutphin
 */
public class StringTools {
    public static String humanizeClassName(String className) {
        if (className == null) return null;
        return className.replaceAll("([A-Z])", " $1").trim().toLowerCase();
    }

    public static String valueOf(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }

    // static class
    private StringTools() {
    }
}
