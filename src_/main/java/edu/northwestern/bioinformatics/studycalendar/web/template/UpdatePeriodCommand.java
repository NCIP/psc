package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.Map;
import java.util.HashMap;


public class UpdatePeriodCommand extends EditPeriodEventsCommand {

    private static final Logger log = LoggerFactory.getLogger(UpdatePeriodCommand.class.getName());

    PlannedEvent newEvent = null;

    protected PlannedEvent performEdit() {
        if (isDetailsUpdated()) {
            updateDetails(getDetails());
        } else if (isConditionalUpdated()) {
            log.info("updating conditional details");
            updateConditionalParameters();
            setColumnNumber(-1);
        }
        return null;
    }

    public String getRelativeViewName() {
        return "updateDetails";
    }

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
                PlannedEvent event = plannedEventDao.getById(id);
                amendmentService.updateDevelopmentAmendment(event,
                    PropertyChange.create("conditionalDetails", event.getConditionalDetails(),
                        getConditionalDetails()));
            }
        }
    }

     private void updateDetails(String details){
        for (Integer id: getEventIds()) {
            if (id != null && id >-1) {
                PlannedEvent event = plannedEventDao.getById(id);
                amendmentService.updateDevelopmentAmendment(event, PropertyChange.create("details", event.getDetails(), details));
            }
        }
    }
}
