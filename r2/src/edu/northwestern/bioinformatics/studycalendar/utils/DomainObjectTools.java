package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

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
}
