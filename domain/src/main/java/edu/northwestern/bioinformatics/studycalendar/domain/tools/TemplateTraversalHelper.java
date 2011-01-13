package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;

import java.util.*;

/*
* @author John Dzak
*/
public class TemplateTraversalHelper {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public static <T extends Child> Collection<T> findChildren(Parent node, Class<T> childClass) {
        List<T> children = new LinkedList<T>();
        findChildren(node, childClass, children);
        return children;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    private static <T extends Child> void findChildren(Parent node, Class<T> childClass, Collection<T> target) {
        if (childClass.isAssignableFrom(node.childClass())) {
            target.addAll(node.getChildren());
        } else {
            for (Object o : node.getChildren()) {
                if (o instanceof Parent) {
                    findChildren((Parent) o, childClass, target);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<Parent> findRootParentNodes(Study study) {
        if (study == null) { return Collections.EMPTY_LIST; }

        Collection<Parent> result = new ArrayList<Parent>();
        result.add(study.getPlannedCalendar());
        result.addAll(findRootParentNodes((Revision) study.getDevelopmentAmendment()));
        for (Amendment a : study.getAmendmentsList()) {
            result.addAll(findRootParentNodes(a));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Parent> findRootParentNodes(Revision amendment) {
        if (amendment == null) { return Collections.EMPTY_LIST; }

        Collection<Parent> result = new ArrayList<Parent>();
        for (Delta d : amendment.getDeltas()) {
            for (Object c : d.getChanges()) {
                if (c instanceof ChildrenChange) {
                    Changeable node = ((ChildrenChange) c).getChild();
                    if (node instanceof Parent) {
                        result.add((Parent) node);
                    }
                }
            }
        }
        return result;
    }
}
