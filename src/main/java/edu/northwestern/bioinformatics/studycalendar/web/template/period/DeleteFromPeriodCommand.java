package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.web.template.period.EditPeriodEventsCommand;

import java.util.HashMap;
import java.util.Map;

public class DeleteFromPeriodCommand extends EditPeriodEventsCommand {
    @Override
    protected void performEdit() {
        Integer id = getPlannedActivities().get(getColumnNumber());
        PlannedActivity plannedActivity = plannedActivityDao.getById(id);
        Remove remove = Remove.create(plannedActivity);
        amendmentService.updateDevelopmentAmendment(getPeriod(), remove);
    }

    @Override
    public String getRelativeViewName() {
        return "removePlannedActivity";
    }

    @Override
    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", -1);
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }
}
