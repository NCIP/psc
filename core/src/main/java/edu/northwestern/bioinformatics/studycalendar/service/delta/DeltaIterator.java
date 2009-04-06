package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nataliya Shurupova
 */
public class DeltaIterator implements Iterator<Delta> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Iterator<List<Delta<?>>> lists;
    private Iterator<Delta<?>> currentList;
    private Study study;
    private TemplateService templateService;

    public DeltaIterator(List<Delta<?>> deltas) {
        this.lists = getDeltaLists(deltas).iterator();
        this.currentList = null;
    }


    public DeltaIterator(List<Delta<?>> deltas, Study study, TemplateService templateService) {
        this.lists = getDeltaLists(deltas).iterator();
        this.currentList = null;
        this.study = study;
        this.templateService = templateService;
    }

    public boolean hasNext() {
        while ((currentList == null || !currentList.hasNext()) && lists.hasNext()) {
            List<Delta<?>> deltaList = lists.next();
            for (Iterator<Delta<?>> iterator = deltaList.iterator(); iterator.hasNext();) {
                Delta<?> delta = iterator.next();
                Changeable originalChild = templateService.findEquivalentChild(study, delta.getNode());
                if (originalChild == null || originalChild.isDetached()) {
                    iterator.remove();
                }
            }
            currentList = deltaList.iterator();
        }
        return currentList != null && currentList.hasNext();
    }

    private Collection<List<Delta<?>>> getDeltaLists(List<Delta<?>> deltas) {
        Map<Class, List<Delta<?>>> map = new TreeMap<Class, List<Delta<?>>>(DomainObjectTools.DETAIL_ORDER_COMPARATOR);

        for (Delta<?> delta : deltas) {
            addToMap(delta, map);
        }

        return new LinkedList<List<Delta<?>>>(map.values());
     }

     private void addToMap(Delta<?> delta, Map<Class, List<Delta<?>>>map) {
         Class key = delta.getNode().getClass();
         if (!map.containsKey(key)) {
             List<Delta<?>> deltaType = new ArrayList<Delta<?>>();
             map.put(key, deltaType);
         }
         List<Delta<?>> listOfDeltas = map.get(key);
         listOfDeltas.add(delta);
     }

    public Delta next() {
        if (hasNext()) {
            return currentList.next();
        } else {
            throw new NoSuchElementException("No more elements");
        }
    }


    public void remove() {
        throw new UnsupportedOperationException("we don't need to implement this yet ");
    }
}
