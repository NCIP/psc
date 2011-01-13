package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;

import java.util.*;

/*
* @author John Dzak
*/
public class TemplateTraversalHelper {
    private static final TemplateTraversalHelper instance = new TemplateTraversalHelper();

    public static TemplateTraversalHelper getInstance() {
        return instance;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public <T extends Child> Collection<T> findChildren(Parent node, Class<T> childClass) {
        List<T> children = new LinkedList<T>();
        findChildren(node, childClass, children);
        return children;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    private <T extends Child> void findChildren(Parent node, Class<T> childClass, Collection<T> target) {
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

    public static Collection<Parent> findRootParentNodes(Study study) {
        TemplateTraversal t = new TemplateTraversal(study);
        return t.findRootParentNodes();
    }

    protected static class TemplateTraversal {
        private Study study;

        protected TemplateTraversal(Study study) {
            this.study = study;
        }

        @SuppressWarnings("unchecked")
        protected Collection<Parent> findRootParentNodes() {
            if (study == null) { return Collections.EMPTY_LIST; }

            Collection<Parent> result = new ArrayList<Parent>();
            result.add(study.getPlannedCalendar());
            result.addAll(findRootParentNodes(study.getDevelopmentAmendment()));
            for (Amendment a : study.getAmendmentsList()) {
                result.addAll(findRootParentNodes(a));
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        protected Collection<Parent> findRootParentNodes(Amendment amendment) {
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
//
//        protected Collection<Parent> getRootPlanTreeNodesFromDeltas(Collection<Amendment> amendments) {
//            Collection<Parent> result = new ArrayList<Parent>();
//            for (Amendment a : amendments) {
//                for (Delta d : a.getDeltas()) {
//                    for (Object c : d.getChanges()) {
//                        if (c instanceof ChildrenChange) {
//                            Changeable node = ((ChildrenChange) c).getChild();
//                            if (node instanceof Parent) {
//                                result.add((Parent) node);
//                            }
//                        }
//                    }
//                }
//            }
//            return result;
//        }
    }

}
