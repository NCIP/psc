package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

/**
 * @author Padmaja Vedula
 */

public class LocalUser {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static String getInstance() {
        return threadLocal.get();
    }

    public static void init(String userName) {
        threadLocal.set(userName);
    }

    public static void release() {
        threadLocal.set(null);
    }

} 
