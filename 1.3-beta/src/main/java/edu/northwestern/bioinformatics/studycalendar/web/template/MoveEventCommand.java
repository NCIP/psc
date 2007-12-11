package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.Map;
import java.util.HashMap;

public class MoveEventCommand extends EditPeriodEventsCommand {

    private static final Logger log = LoggerFactory.getLogger(UpdatePeriodCommand.class.getName());

    PlannedActivity newEvent = null;

    protected PlannedActivity performEdit() {
        for (Integer id: getEventIds()) {
            if (id != null && id>-1) {
                PlannedActivity event = plannedActivityDao.getById(id);
                event.setDay(getColumnNumber()+1);
                setNewEvent(event);
                amendmentService.updateDevelopmentAmendment(event, PropertyChange.create("day", getMoveFrom()+1, getMoveTo()+1));
            }
        }
        return null;
    }

    public String getRelativeViewName() {
        return "moveEvent";
    }

    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getNewEvent() != null) {
            map.put("id", getNewEvent().getId());
        }
        map.put("moveFrom", getMoveFrom());
        map.put("moveTo", getMoveTo());
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }


    public PlannedActivity getNewEvent() {
        return newEvent;
    }

    public void setNewEvent(PlannedActivity newEvent) {
        this.newEvent = newEvent;
    }
}