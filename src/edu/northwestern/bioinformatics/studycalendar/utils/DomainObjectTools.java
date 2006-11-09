package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class DomainObjectTools {
    private static final List<Class<? extends AbstractDomainObject>> DETAIL_ORDER = Arrays.asList(
        Study.class,
        PlannedCalendar.class,
        Epoch.class,
        Arm.class,
        Period.class,
        PlannedEvent.class,

        ScheduledCalendar.class,
        ScheduledArm.class,
        ScheduledEvent.class
    );


    public static <T extends AbstractDomainObject> Map<Integer, T> byId(List<T> objs) {
        Map<Integer, T> map = new LinkedHashMap<Integer, T>();
        for (T t : objs) {
            map.put(t.getId(), t);
        }
        return map;
    }
    
    

    public static String createExternalObjectId(DomainObject domainObject) {
        if (domainObject == null) {
            return "null";
        } else if (domainObject.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot create an external object ID for a transient instance of "
                    + domainObject.getClass().getName());
        } else {
            return new StringBuilder(domainObject.getClass().getName()).append('.')
                .append(domainObject.getId()).toString();
        }
    }
    
    public static int parseExternalObjectId(String objectId) {
    	if (objectId == null) {
    		return 0;
    	} else {
    		String[] objectIdStrings = objectId.split("\\.");
    		return  Integer.parseInt(objectIdStrings[objectIdStrings.length - 1]);
    	}
    }

    public static boolean isMoreSpecific(Class<? extends DomainObject> more, Class<? extends DomainObject> less) {
        int diff = DETAIL_ORDER.indexOf(more) - DETAIL_ORDER.indexOf(less);
        return diff > 0;
    }

    private DomainObjectTools() { }
}
