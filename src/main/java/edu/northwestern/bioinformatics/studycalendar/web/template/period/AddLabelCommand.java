package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Label;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.web.template.period.EditPlannedActivityCommand;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class AddLabelCommand extends EditPlannedActivityCommand {

    Label label;

    @Override
    protected void performEdit() {
        addLabel();
    }

    @Override
    public String getRelativeViewName() {
        return "addLabelToTheGrid";
    }

    @Override
    public Map<String, Object> getLocalModel() {
        List<Integer> activityIds = getPlannedActivities();
        List<PlannedActivity> plannedActivities = new ArrayList<PlannedActivity>();
        for(Integer id : activityIds) {
            if (id!=null) {
                plannedActivities.add(plannedActivityDao.getById(id));
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("rowNumber", getRowNumber());
        map.put("label", label);
        map.put("activity", getActivity());
        map.put("details", getDetails());
        map.put("conditionalDetails", getConditionalDetails());
        map.put("plannedActivities", plannedActivities);
        map.put("period", getPeriod());
        return map;
    }

    private void addLabel() {
        label = new Label();
        if (labelDao.getByName(getLabel()) == null) {
            label.setName(getLabel());
            labelDao.save(label);
        } else {
            label= labelDao.getByName(getLabel());
        }
        for (Integer id: getPlannedActivities()) {
            if (id != null && id >-1) {
                PlannedActivity event = plannedActivityDao.getById(id);

                for (int repetition = 0; repetition < period.getRepetitions(); repetition++) {
                    PlannedActivityLabel plannedActivityLabel = new PlannedActivityLabel();
                    plannedActivityLabel.setLabel(label);
                    plannedActivityLabel.setPlannedActivity(event);

                    plannedActivityLabel.setRepetitionNumber(repetition+1);
                    plannedActivityLabelDao.save(plannedActivityLabel);
                }
            }
        }
    }
}
