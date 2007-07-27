package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;

import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Rhett Sutphin
 */
public class ManagePeriodEventsCommand {
    private static final Logger log = Logger.getLogger(ManagePeriodEventsCommand.class.getName());
    private Period period;
    private PlannedEventDao plannedEventDao;


    /** Representation of the activity/day grid.
     * Map keys are activity IDs,
     * values are lists of counts of the number of times that activity occurs on that day
     * (list index is day number, zero-based).
     */
    private List<GridRow> grid;

    public ManagePeriodEventsCommand(Period period, PlannedEventDao plannedEventDao) {
        this.period = period;
        this.plannedEventDao = plannedEventDao;
        grid = createGrid();
    }

    protected List<GridRow> createGrid() {
        return createGrid(period);
    }

    /**
     * Set up the initial grid
     */
    private List<GridRow> createGrid(Period src) {
        Map<String, GridRow> bins = new LinkedHashMap<String, GridRow>();
        Integer dayCount = src.getDuration().getDays();
        Map<String, int[]> counts = new HashMap<String, int[]>();
        for (PlannedEvent event : src.getPlannedEvents()) {
            String activityAndDetails = GridRow.key(event);
            if (counts.get(activityAndDetails) == null) {
                counts.put(activityAndDetails, new int[dayCount]);
            }
            int countI = event.getDay() - 1;
            String binKey = activityAndDetails + counts.get(activityAndDetails)[countI];
            counts.get(activityAndDetails)[countI]++;
            if (!bins.containsKey(binKey)) {
                bins.put(binKey, new GridRow(event.getActivity(), event.getDetails(), dayCount,
                        event.getConditional(), event.getConditionalDetails()));
            }
            bins.get(binKey).addId(event);
        }
        return new ExpandingList<GridRow>(
            new GridFiller(dayCount),
            new LinkedList<GridRow>(bins.values()));
    }

    /**
     * Apply any changes in the grid to the period in the command.
     */
    public GridRow apply() {
        List<GridRow> existingGrid = createGrid(period);
        Map<String, GridRow> rowMap = new LinkedHashMap<String, GridRow>();
        // Maps the existing Grid
        for (GridRow diffRow : existingGrid) {
            rowMap.put(diffRow.key(), diffRow);
        }
        // calculate difference from bound grid
         for (GridRow bound : getGrid()) {
            //just need to update details on the row
            if (!bound.isUpdated()) continue;
            if (bound.isDetailsUpdated()) {
                GridRow existing = existingGrid.get(bound.getRowNumber());
                updateDetails(existing, bound.getDetails());
            } else if (bound.isConditionalUpdated()) {
                GridRow existing = existingGrid.get(bound.getRowNumber());
                updateConditionalParameters(existing, bound.isConditionalCheckbox(), bound.getConditionalDetails());
                bound.setColumnNumber(-1);
            }
            else {
                log.debug("Processing grid " + bound.getRowNumber() + ", " + bound.getColumnNumber());
                if (rowMap.containsKey(bound.key())) {
                    // if this is an update to an existing row calculate the difference
                    if (!bound.isAddition()) {
                        log.debug("Action is remove from an existing row");
                        Integer id = bound.getEventIds().get(bound.getColumnNumber());
                        if (id != null) {
                            removeEvent(id);
                        } else {
                            log.debug("Attempted to remove event twice: " + bound.getRowNumber() + ", " + bound.getColumnNumber() + " (r, c)");
                        }
                    } else {
                        log.debug("Action is add to existing row");
                        addEvent(bound);
                    }
                 } else {
                   // otherwise, it is all new
                    log.debug("Action is add into new row");
                    rowMap.put(bound.key(), bound);
                    addEvent(bound);
                }
            }
            return bound;
        }
        return null;
    }

    private void updateConditionalParameters(GridRow row, boolean conditional, String conditionalDetails) {
        for (Integer id: row.getEventIds()) {
            if (id != null) {
                PlannedEvent event = plannedEventDao.getById(id);
                event.setConditional(conditional);
                if (conditional) {
                    event.setConditionalDetails(conditionalDetails);
                } else {
                    event.setConditionalDetails(null);
                }
                plannedEventDao.save(event);
            }
        }
    }

    private void addEvent(GridRow row) {
        PlannedEvent newEvent = new PlannedEvent();
        newEvent.setDay(row.getColumnNumber()+1);
        newEvent.setActivity(row.getActivity());
        newEvent.setDetails(row.getDetails());
        newEvent.setConditional(row.isConditionalCheckbox());
        newEvent.setConditionalDetails(row.getConditionalDetails());
        period.addPlannedEvent(newEvent);
        plannedEventDao.save(newEvent);
    }

    private void removeEvent(Integer eventId) {
        for (Iterator<PlannedEvent> iterator = period.getPlannedEvents().iterator(); iterator.hasNext();) {
            PlannedEvent event = iterator.next();
            if(eventId.equals(event.getId())) {
                iterator.remove();
                plannedEventDao.delete(event);
            }
        }
    }

    private void updateDetails(GridRow row, String details){
        for (Integer id: row.getEventIds()) {
            if (id != null) {
                PlannedEvent event = plannedEventDao.getById(id);
                event.setDetails(details);
                plannedEventDao.save(event);
            }
        }
    }

    public List<GridRow> getGrid() {
        return grid;
    }

    public Period getPeriod() {
        return period;
    }

    private static class GridFiller implements ExpandingList.Filler<GridRow> {
        private int length;

        public GridFiller(int length) {
            this.length = length;
        }

        public GridRow createNew(int index) {
            return new GridRow(length);
        }
    }

    public static class GridRow {
        private Activity activity;
        private List<Integer> eventIds;

        private String details;
        private int rowNumber;
        private int columnNumber;
        private boolean addition;
        private boolean updated;

        private boolean conditionalCheckbox;
        private String conditionalDetails;
        private boolean conditionalUpdated;

        public GridRow(int length) {
            eventIds = new ArrayList<Integer>(length);
            while (eventIds.size() < length) {
                eventIds.add(null);
            }
        }
        
        public GridRow(Activity activity, String details, int length, Boolean conditional, String conditionalDetails) {
            this(length);
            setActivity(activity);
            setDetails(details);
            setConditionalDetails(conditionalDetails);
            setConditionalCheckbox(conditional);
        }

        ////// LOGIC

        public String key() {
            return key(getActivity(), getDetails());
        }

        private static String key(Activity activity, String details) {
            return activity.getId() + details;
        }

        public static String key(PlannedEvent event) {
            return key(event.getActivity(), event.getDetails());
        }

        public boolean matchesEvent(PlannedEvent event) {
            return event.getActivity().equals(getActivity())
                && ComparisonUtils.nullSafeEquals(event.getDetails(), getDetails());
        }


        public void addId(PlannedEvent event) {
            int id = event.getId();
            int day = event.getDay();
            this.getEventIds().set(day-1, id);
            //To change body of created methods use File | Settings | File Templates.
        }

        public boolean isDetailsUpdated() {
            return getColumnNumber() <0;
        }

        ////// BOUND PROPERTIES

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public List<Integer> getEventIds() {
            return eventIds;
        }

        public void setEventIds(List<Integer> eventIds) {
            this.eventIds = eventIds;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public void setColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
        }

        public boolean isAddition() {
            return addition;
        }

        public void setAddition(boolean addition) {
            this.addition = addition;
        }

        public boolean isUpdated() {
            return updated;
        }

        public void setUpdated(boolean updated) {
            this.updated = updated;
        }

        public boolean isConditionalCheckbox() {
            return conditionalCheckbox;
        }

        public void setConditionalCheckbox(boolean conditionalCheckbox) {
            this.conditionalCheckbox = conditionalCheckbox;
        }

        public String getConditionalDetails() {
            return conditionalDetails;
        }

        public void setConditionalDetails(String conditionalDetails) {
            this.conditionalDetails = conditionalDetails;
        }


        public boolean isConditionalUpdated() {
            return conditionalUpdated;
        }

        public void setConditionalUpdated(boolean conditionalUpdated) {
            this.conditionalUpdated = conditionalUpdated;
        }
    }
}
