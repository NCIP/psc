package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.HashMap;
import java.util.Map;

public class UpdatePeriodCommand extends EditPeriodEventsCommand {
    @Override
    protected void performEdit() {
        updateDetails();
        log.debug("updating conditional details");
        updateCondition();
    }

    @Override
    public String getRelativeViewName() {
        return "updateDetails";
    }

    @Override
    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }
    
    private void updateCondition() {
        for (Integer id: getPlannedActivities()) {
            if (id != null && id>-1) {
                PlannedActivity event = plannedActivityDao.getById(id);
                amendmentService.updateDevelopmentAmendment(event,
                    PropertyChange.create("condition", event.getCondition(),
                        getConditionalDetails()));
            }
        }
    }

     private void updateDetails() {
        for (Integer id: getPlannedActivities()) {
            if (id != null && id >-1) {
                PlannedActivity event = plannedActivityDao.getById(id);
                amendmentService.updateDevelopmentAmendment(event,
                    PropertyChange.create("details", event.getDetails(), getDetails()));
            }
        }
    }
}
