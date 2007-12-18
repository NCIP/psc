package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.HashMap;
import java.util.Map;

public class MovePlannedActivityCommand extends EditPeriodEventsCommand {
    private PlannedActivity movedPlannedActivity = null;

    @Override
    protected void performEdit() {
        for (Integer id: getEventIds()) {
            if (id != null && id>-1) {
                PlannedActivity event = plannedActivityDao.getById(id);
                event.setDay(getColumnNumber()+1);
                setMovedPlannedActivity(event);
                amendmentService.updateDevelopmentAmendment(event,
                    PropertyChange.create("day", getMoveFrom()+1, getMoveTo()+1));
            }
        }
    }

    @Override
    public String getRelativeViewName() {
        return "movePlannedActivity";
    }

    @Override
    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("movedEvent", getMovedPlannedActivity());
        map.put("moveFrom", getMoveFrom());
        map.put("moveTo", getMoveTo());
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }

    public PlannedActivity getMovedPlannedActivity() {
        return movedPlannedActivity;
    }

    public void setMovedPlannedActivity(PlannedActivity movedPlannedActivity) {
        this.movedPlannedActivity = movedPlannedActivity;
    }
}