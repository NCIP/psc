package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.DaoBasedPropertyEditor;

import java.util.Map;
import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * @author Rhett Sutphin
 */
public class ControllerTools {

    // TODO: make date format externally configurable
    public static PropertyEditor getDateEditor(boolean required) {
        // note that date formats are not threadsafe, so we have to create a new one each time
        return new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), !required);
    }

    public static void registerDomainObjectEditor(ServletRequestDataBinder binder, String field, StudyCalendarDao dao) {
        binder.registerCustomEditor(dao.domainClass(), field, new DaoBasedPropertyEditor(dao));
    }

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
