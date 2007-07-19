package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;

import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Rhett Sutphin
 */
public class ManagePeriodEventsCommand {
    private static final Logger log = Logger.getLogger(ManagePeriodEventsCommand.class.getName());
    private Period period;

    /** Representation of the activity/day grid.
     * Map keys are activity IDs,
     * values are lists of counts of the number of times that activity occurs on that day
     * (list index is day number, zero-based).
     */
    private List<GridRow> grid;

    public ManagePeriodEventsCommand(Period period) {
        this.period = period;
        grid = createGrid(period);
    }

    /**
     * Set up the initial grid
     */
    private List<GridRow> createGrid(Period src) {
        Map<String, GridRow> bins = new LinkedHashMap<String, GridRow>();
        Integer dayCount = src.getDuration().getDays();
        for (PlannedEvent event : src.getPlannedEvents()) {
            String binKey = GridRow.key(event);
            if (!bins.containsKey(binKey)) {
                bins.put(binKey, new GridRow(event.getActivity(), event.getDetails(), dayCount));
            }
            bins.get(binKey).incrementDay(event.getDay());
        }
        return new ExpandingList<GridRow>(
            new GridFiller(dayCount),
            new LinkedList<GridRow>(bins.values()));
    }

    /**
     * Apply any changes in the grid to the period in the command.
     */
    public void apply() {
        List<GridRow> existingGrid = createGrid(period);
        Map<String, GridRow> difference = new LinkedHashMap<String, GridRow>();
        // Initialize difference from existing events
        for (GridRow diffRow : existingGrid) {
            difference.put(diffRow.key(), diffRow);
        }
        // calculate difference from bound grid
         for (GridRow bound : getGrid()) {
            //just need to update details on the row
            if (bound.getColumnNumber() <0 && bound.isUpdated()) {
                GridRow existing = existingGrid.get(bound.getRowNumber());
                updateDetails(existing, bound.getDetails());
            } else {
                int day = bound.getColumnNumber()+1;
                if (difference.containsKey(bound.key())) {
                    // if this is an update to an existing row calculate the difference
                    if (bound.isUpdated()) {
                        if (bound.getStatus()) {
                            addEvent(bound, day);
                        } else {
                            removeEvent(bound, day);
                        }
                    }
                 } else {
                   // otherwise, it is all new
                    difference.put(bound.key(), bound);
                    addEvent(bound, day);
                }
            }
        }
    }

    private void addEvent(GridRow row, int day) {
        PlannedEvent newEvent = new PlannedEvent();
        newEvent.setDay(day);
        newEvent.setActivity(row.getActivity());
        newEvent.setDetails(row.getDetails());
        period.addPlannedEvent(newEvent);
    }

    private void removeEvent(GridRow row, int day) {
         for (Iterator<PlannedEvent> it = period.getPlannedEvents().iterator(); it.hasNext();) {
             PlannedEvent event = it.next();
             if (row.matchesEvent(event) && day == event.getDay()) {
                 it.remove();
                break;  // only remove one
            }
        }
    }

    private void updateDetails(GridRow row, String details){
        for (Iterator<PlannedEvent> it = period.getPlannedEvents().iterator(); it.hasNext();) {
            PlannedEvent event = it.next();
              if(row.matchesEvent(event)) {
                event.setDetails(details);
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
        private List<Boolean> counts;
        private String details;
        private int rowNumber;
        private int columnNumber;
        private boolean status;
        private boolean updated;

        public GridRow(int length) {
            counts = new ArrayList<Boolean>(length);
            while (counts.size() < length) {
                counts.add(false);
            }
        }
        
        public GridRow(Activity activity, String details, int length) {
            this(length);
            setActivity(activity);
            setDetails(details);
        }

        ////// LOGIC

        public void incrementDay(Integer day) {
            getCounts().set(day-1, true);
        }

        public void decrementDay(Integer day) {
            getCounts().set(day-1, false);
        }

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

        ////// BOUND PROPERTIES

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public List<Boolean> getCounts() {
            return counts;
        }

        public void setCounts(List<Boolean> counts) {
            this.counts = counts;
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

        public boolean getStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public boolean isUpdated() {
            return updated;
        }

        public void setUpdated(boolean updated) {
            this.updated = updated;
        }        
    }
}
