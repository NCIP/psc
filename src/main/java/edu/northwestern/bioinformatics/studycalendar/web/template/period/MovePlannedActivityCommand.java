package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.web.template.period.EditPeriodEventsCommand;

import java.util.HashMap;
import java.util.Map;

public class MovePlannedActivityCommand extends EditPeriodEventsCommand {
    private PlannedActivity movedPlannedActivity = null;

    private Integer moveFrom;
    private Integer moveTo;

    @Override
    protected void performEdit() {
        Integer targetId = getPlannedActivities().get(getMoveFrom());
        if (targetId == null || targetId < 0) {
            throw new StudyCalendarValidationException("No event ID for the requested move");
        }
        PlannedActivity event = plannedActivityDao.getById(targetId);
        setMovedPlannedActivity(event);
        amendmentService.updateDevelopmentAmendment(event,
            PropertyChange.create("day", getMoveFrom()+1, getMoveTo()+1));
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
        map.put("columnNumber", getMoveTo());
        return map;
    }

    public PlannedActivity getMovedPlannedActivity() {
        return movedPlannedActivity;
    }

    public void setMovedPlannedActivity(PlannedActivity movedPlannedActivity) {
        this.movedPlannedActivity = movedPlannedActivity;
    }

    ////// BOUND PROPERTIES

    public Integer getMoveFrom() {
        return moveFrom;
    }

    public void setMoveFrom(Integer moveFrom) {
        this.moveFrom = moveFrom;
    }

    public Integer getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(Integer moveTo) {
        this.moveTo = moveTo;
    }
}