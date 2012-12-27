/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class DomainObjectTools {
    private static final List<Class<? extends DomainObject>> DETAIL_ORDER = Arrays.<Class<? extends DomainObject>>asList(
        Site.class,
        StudySite.class,

        Study.class,
        PlannedCalendar.class,
        Epoch.class,
        StudySegment.class,
        Period.class,
        PlannedActivity.class,
        PlannedActivityLabel.class,

        Subject.class,
        StudySubjectAssignment.class,
        ScheduledCalendar.class,
        ScheduledStudySegment.class,
        ScheduledActivity.class
    );
    public static final Comparator<? super Class> DETAIL_ORDER_COMPARATOR = new ByDetailOrder();
    public static final Comparator<? super Class> DETAIL_ORDER_REVERSED_COMPARATOR = new ByDetailOrderReversed();

    public static <T extends DomainObject> Map<Integer, T> byId(List<T> objs) {
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

    public static <T extends DomainObject> T loadFromExternalObjectId(String objectId, DomainObjectDao<T> dao) {
        return dao.getById(parseExternalObjectId(objectId));
    }

    public static boolean isMoreSpecific(Class<? extends DomainObject> more, Class<? extends DomainObject> less) {
        int diff = detailOf(more) - detailOf(less);
        return diff > 0;
    }

    private static int detailOf(Class<? extends DomainObject> target) {
        int direct = DETAIL_ORDER.indexOf(target);
        if (direct >= 0) return direct;
        for (int i = 0; i < DETAIL_ORDER.size(); i++) {
            Class<? extends DomainObject> klass = DETAIL_ORDER.get(i);
            if (klass.isAssignableFrom(target)) return i;
        }
        return -1;
    }

    public static Collection<Integer> collectIds(Collection<? extends DomainObject> objs) {
        Set<Integer> ids = new LinkedHashSet<Integer>();
        for (DomainObject obj : objs) ids.add(obj.getId());
        return ids;
    }

    private DomainObjectTools() { }

    public static class ById<T extends DomainObject> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return ComparisonTools.nullSafeCompare(o1.getId(), o2.getId());
        }
    }

    private static class ByDetailOrder implements Comparator<Class> {
        @SuppressWarnings({"unchecked"})
        public int compare(Class o1, Class o2) {
            return detailOf(o1) - detailOf(o2);
        }
    }

    private static class ByDetailOrderReversed implements Comparator<Class> {
        @SuppressWarnings({"unchecked"})
        public int compare(Class o1, Class o2) {
            return detailOf(o2) - detailOf(o1);
        }
    }
}
