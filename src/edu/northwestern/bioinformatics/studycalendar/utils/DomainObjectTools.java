package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * @author Rhett Sutphin
 */
public class DomainObjectTools {
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
    
    public static String parseExternalObjectId(String objectId) {
    	if (objectId == null) {
    		return "null";
    	} else {
    		String[] objectIdStrings = objectId.split("\\.");
    		return  objectIdStrings[objectIdStrings.length - 1];
    	}
    }

    private DomainObjectTools() { }
}
