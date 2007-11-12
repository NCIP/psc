package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;

import java.util.Map;
import java.util.HashMap;

public class DeleteFromPeriodCommand extends EditPeriodEventsCommand{

    private static final Logger log = LoggerFactory.getLogger(DeleteFromPeriodCommand.class.getName());

    protected PlannedActivity performEdit() {
        Integer id = getEventIds().get(getColumnNumber());
        PlannedActivity plannedActivity = plannedActivityDao.getById(id);
        Remove remove = Remove.create(plannedActivity);
        amendmentService.updateDevelopmentAmendment(getPeriod(), remove);
        return null;
    }

    public String getRelativeViewName() {
        return "removePlannedActivity";
    }

    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", -1);
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }
}
