/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public static <N extends Changeable> Collection<N> findAllNodes(Study study, Class<N> nodeClass) {
        if (study == null) { return Collections.emptyList(); }

        Collection<N> nodes = new LinkedList<N>();
        for (Changeable c : findAddedNodes(study)) {
            if (nodeClass.isAssignableFrom(c.getClass())) {
                nodes.add((N) c);
            } else if (Child.class.isAssignableFrom(nodeClass) && c instanceof Parent) {
                nodes.addAll(
                    (Collection<? extends N>) findChildren((Parent) c, (Class<? extends Child>) nodeClass));
            }
        }
        return nodes;
    }

    /**
     * Returns all the nodes in the study that have an {@link Add} of their own,
     * plus the PlannedCalendar itself.
     */
    @SuppressWarnings("unchecked")
    public static Collection<Changeable> findAddedNodes(Study study) {
        if (study == null) { return Collections.EMPTY_LIST; }

        Collection<Changeable> result = new ArrayList<Changeable>();
        result.add(study.getPlannedCalendar());
        result.addAll(findAddedNodes(study.getDevelopmentAmendment()));
        for (Amendment a : study.getAmendmentsList()) {
            result.addAll(findAddedNodes(a));
        }
        return result;
    }

    private static Collection<Changeable> findAddedNodes(Revision rev) {
        if (rev == null) return Collections.emptySet();

        Collection<Changeable> result = new LinkedList<Changeable>();
        for (Delta<?> delta : rev.getDeltas()) {
            for (Change change : delta.getChanges()) {
                if (change instanceof Add) {
                    result.add(((Add) change).getChild());
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Parent> findRootParentNodes(Revision amendment) {
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
