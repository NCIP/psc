package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingMap;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Rhett Sutphin
 */
public class ManagePeriodEventsCommand {
    private Period period;

    /** Representation of the activity/day grid.
     * Map keys are activity IDs,
     * values are lists of counts of the number of times that activity occurs on that day
     * (list index is day number, zero-based).
     */
    private Map<Integer, List<Integer>> grid;
    private ActivityDao activityDao;

    public ManagePeriodEventsCommand(Period period, ActivityDao activityDao) {
        this.period = period;
        this.activityDao = activityDao;
        grid = createGrid(period);
    }

    /**
     * Set up the initial grid
     */
    private Map<Integer, List<Integer>> createGrid(Period src) {
        Map<Integer, List<Integer>> newGrid = new ExpandingMap<Integer, List<Integer>>(
            new GridFiller(src.getDuration().getDays()),
            new TreeMap<Integer, List<Integer>>());
        for (PlannedEvent event : src.getPlannedEvents()) {
            int index = event.getDay() - 1;
            List<Integer> listForActivity = newGrid.get(event.getActivity().getId());
            Integer count = listForActivity.get(index);
            listForActivity.set(index, count + 1);
        }
        return newGrid;
    }

    /**
     * Apply any changes in the grid to the period in the command.
     */
    public void apply() {
        Map<Integer, List<Integer>> difference = createGrid(period);
        Collection<Integer> activities
            = CollectionUtils.union(getGrid().keySet(), difference.keySet());
        for (Integer activityId : activities) {
            List<Integer> diffList = difference.get(activityId);
            List<Integer> newList = getGrid().get(activityId);
            for (int i = 0 ; i < newList.size() ; i++) {
                diffList.set(i, newList.get(i) - diffList.get(i));
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : difference.entrySet()) {
            Integer activityId = entry.getKey();
            for (int i = 0; i < entry.getValue().size(); i++) {
                int diff = entry.getValue().get(i);
                int day = i + 1;
                while (diff < 0) {
                    removeEvent(activityId, day);
                    diff++;
                }
                while (diff > 0) {
                    addEvent(activityId, day);
                    diff--;
                }
            }
        }
    }

    private void addEvent(Integer activityId, int day) {
        PlannedEvent newEvent = new PlannedEvent();
        newEvent.setDay(day);
        newEvent.setActivity(activityDao.getById(activityId));
        period.addPlannedEvent(newEvent);
    }

    private void removeEvent(Integer activityId, int day) {
        for (Iterator<PlannedEvent> it = period.getPlannedEvents().iterator(); it.hasNext();) {
            PlannedEvent event = it.next();
            if (event.getActivity().getId() == activityId && event.getDay() == day) {
                it.remove();
                break;  // only remove one
            }
        }
    }

    public Map<Integer, List<Integer>> getGrid() {
        return grid;
    }

    public Period getPeriod() {
        return period;
    }

    private static class GridFiller implements ExpandingMap.Filler<List<Integer>> {
        private int initialLength;

        public GridFiller(int initialLength) {
            this.initialLength = initialLength;
        }

        public List<Integer> createNew(Object key) {
            ExpandingList<Integer> list
                = new ExpandingList<Integer>(new ExpandingList.StaticFiller<Integer>(0));
            list.get(initialLength - 1);  // force it to fill to initialLength
            return list;
        }
    }
}
