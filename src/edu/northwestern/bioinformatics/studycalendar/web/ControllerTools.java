package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ControllerTools {

    public static void addHierarchyToModel(PlannedEvent event, Map<String, Object> model) {
        model.put("plannedEvent", event);
        addHierarchyToModel(event.getPeriod(), model);
    }

    public static void addHierarchyToModel(Period period, Map<String, Object> model) {
        model.put("period", period);
        addHierarchyToModel(period.getArm(), model);
    }

    public static void addHierarchyToModel(Arm arm, Map<String, Object> model) {
        model.put("arm", arm);
        addHierarchyToModel(arm.getEpoch(), model);
    }

    public static void addHierarchyToModel(Epoch epoch, Map<String, Object> model) {
        model.put("epoch", epoch);
        addHierarchyToModel(epoch.getPlannedCalendar(), model);
    }

    public static void addHierarchyToModel(PlannedCalendar plannedCalendar, Map<String, Object> model) {
        model.put("plannedCalendar", plannedCalendar);
        model.put("study", plannedCalendar.getStudy());
    }

    private ControllerTools() { }
}
