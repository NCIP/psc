package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ManagePeriodEventsCommand {
    private Period period;

    private List<GridRow> grid;

    public ManagePeriodEventsCommand(Period period) {
        this.period = period;
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
        for (PlannedActivity event : src.getPlannedActivities()) {
            String activityAndDetails = GridRow.key(event);
            if (counts.get(activityAndDetails) == null) {
                counts.put(activityAndDetails, new int[dayCount]);
            }
            int countI = event.getDay() - 1;
            String binKey = activityAndDetails + counts.get(activityAndDetails)[countI];
            counts.get(activityAndDetails)[countI]++;
            if (!bins.containsKey(binKey)) {
                bins.put(binKey, new GridRow(event.getActivity(), event.getDetails(), dayCount, event.getCondition()));
            }
            bins.get(binKey).addPlannedActivity(event);
        }
        return new ExpandingList<GridRow>(
            new GridFiller(dayCount),
            new LinkedList<GridRow>(bins.values()));
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
        // TODO: rename
        private List<PlannedActivity> eventIds;

        private String details;
        private int rowNumber;
        private int columnNumber;

        private String conditionalDetails;

        public GridRow(int length) {
            eventIds = new ArrayList<PlannedActivity>(length);
            while (eventIds.size() < length) {
                eventIds.add(null);
            }
        }
        
        public GridRow(Activity activity, String details, int length, String conditionalDetails) {
            this(length);
            setActivity(activity);
            setDetails(details);
            setConditionalDetails(conditionalDetails);
        }

        ////// LOGIC

        private static String key(Activity activity, String details, String conditionalDetails) {
            return activity.getId() + details + conditionalDetails;
        }

        public static String key(PlannedActivity event) {
            return key(event.getActivity(), event.getDetails(), event.getCondition());
        }

        public void addPlannedActivity(PlannedActivity event) {
            int day = event.getDay();
            this.getEventIds().set(day-1, event);
        }

        ////// BOUND PROPERTIES

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public List<PlannedActivity> getEventIds() {
            return eventIds;
        }

        public void setEventIds(List<PlannedActivity> eventIds) {
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

        public String getConditionalDetails() {
            return conditionalDetails;
        }

        public void setConditionalDetails(String conditionalDetails) {
            this.conditionalDetails = conditionalDetails;
        }
    }
}
