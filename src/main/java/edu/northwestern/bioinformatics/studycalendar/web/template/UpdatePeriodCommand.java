package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.HashMap;
import java.util.Map;

public class UpdatePeriodCommand extends EditPeriodEventsCommand {
    @Override
    protected void performEdit() {
        if (isDetailsUpdated()) {
            updateDetails(getDetails());
        } else if (isConditionalUpdated()) {
            log.debug("updating conditional details");
            updateConditionalParameters();
            setColumnNumber(-1);
        }
    }

    @Override
    public String getRelativeViewName() {
        return "updateDetails";
    }

    @Override
    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        if (isDetailsUpdated()) {
            map.put("details", getDetails());
        } else if (isConditionalUpdated()) {
            map.put("conditionalDetails", getConditionalDetails());
        }
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }
    
    private void updateConditionalParameters() {
        for (Integer id: getEventIds()) {
            if (id != null && id>-1) {
                PlannedActivity event = plannedActivityDao.getById(id);
                amendmentService.updateDevelopmentAmendment(event,
                    PropertyChange.create("condition", event.getCondition(),
                        getConditionalDetails()));
            }
        }
    }

     private void updateDetails(String details){
        for (Integer id: getEventIds()) {
            if (id != null && id >-1) {
                PlannedActivity event = plannedActivityDao.getById(id);
                amendmentService.updateDevelopmentAmendment(event, PropertyChange.create("details", event.getDetails(), details));
            }
        }
    }
}
