package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
@Deprecated // will be replaced with period.ManagePeriodActivities* when all behaviors are ported
public class ManagePeriodEventsCommand {
    protected final Logger log = LoggerFactory.getLogger(getClass());

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

        //filtering labels by label name per grid
        Collection<GridRow> rows = bins.values();
        for (GridRow row: rows) {
            List<PlannedActivityLabel> labels = new ArrayList<PlannedActivityLabel>();
            List<PlannedActivity> events= row.getPlannedActivities();
            if (!events.isEmpty()){
                for(PlannedActivity event: events){
                    if (event!=null) {
                        List<PlannedActivityLabel> labelsPerEvent = event.getPlannedActivityLabels();
                        if (labelsPerEvent!=null){
                            for (PlannedActivityLabel aLabelsPerEvent : labelsPerEvent) {
                                labels.add(aLabelsPerEvent);
                            }
                        }
                    }
                }
            }
            List<PlannedActivityLabel> labelsAfterFiltering = filteringListOfLabels(labels);
            row.setLabels(labelsAfterFiltering);
        }


        return new ExpandingList<GridRow>(
            new GridFiller(dayCount),
            new LinkedList<GridRow>(bins.values()));
    }

    private List<PlannedActivityLabel> filteringListOfLabels(List<PlannedActivityLabel> labelsToFilter) {
        List<PlannedActivityLabel> result = new ArrayList<PlannedActivityLabel>();
        for (int i=0; i< labelsToFilter.size(); i++){
            PlannedActivityLabel pal = labelsToFilter.get(i);
            if (result.size() ==0) {
                result.add(pal);
            }
            boolean found = false;
            for (int j = 0; j< result.size(); j++) {
                PlannedActivityLabel pal2 = result.get(j);
                if(pal.getLabel().getName().equals(pal2.getLabel().getName())){
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(pal);
            }
        }
        return result;
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
        private List<PlannedActivity> plannedActivities;

        private String details;
        private int rowNumber;
        private int columnNumber;

        private String conditionalDetails;

        private List<PlannedActivityLabel> labels;

        public GridRow(int length) {
            plannedActivities = new ArrayList<PlannedActivity>(length);
            while (plannedActivities.size() < length) {
                plannedActivities.add(null);
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
            this.getPlannedActivities().set(day-1, event);
        }

        ////// BOUND PROPERTIES

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public List<PlannedActivity> getPlannedActivities() {
            return plannedActivities;
        }

        public void setPlannedActivities(List<PlannedActivity> plannedActivities) {
            this.plannedActivities = plannedActivities;
        }


        public List<PlannedActivityLabel> getLabels() {
            return labels;
        }

        public void setLabels(List<PlannedActivityLabel> labels) {
            this.labels = labels;
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
