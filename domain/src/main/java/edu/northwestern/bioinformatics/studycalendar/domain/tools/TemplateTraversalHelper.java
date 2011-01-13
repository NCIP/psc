package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

}
