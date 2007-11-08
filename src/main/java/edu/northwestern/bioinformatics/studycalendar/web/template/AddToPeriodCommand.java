package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;


public class AddToPeriodCommand extends EditPeriodEventsCommand{

    private static final Logger log = LoggerFactory.getLogger(AddToPeriodCommand.class.getName());

    PlannedActivity newEvent = null;

    protected PlannedActivity performEdit() {
        newEvent = new PlannedActivity();
        newEvent.setDay(getColumnNumber()+1);
        newEvent.setActivity(getActivity());
        newEvent.setDetails(getDetails());
        newEvent.setCondition(getConditionalDetails());
        Add add = Add.create(newEvent);
        amendmentService.updateDevelopmentAmendment(getPeriod(), add);
        setPlannedActivity(newEvent);
        return newEvent;
    }

    public String getRelativeViewName() {
        return "addPlannedEvent";
    }

    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getEvent() != null) {
            map.put("id", getEvent().getId());
        }
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }

    private PlannedActivity getEvent() {
        return newEvent;
    }

    private void setPlannedActivity(PlannedActivity event) {
        this.newEvent = event;
    }
}
