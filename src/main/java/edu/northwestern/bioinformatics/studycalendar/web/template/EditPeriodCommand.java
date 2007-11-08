package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;
import edu.northwestern.bioinformatics.studycalendar.utils.DefaultDayRange;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

/**
 * @author Rhett Sutphin
 */
public class EditPeriodCommand implements PeriodCommand {
    private static final Collection<String> PROPERTIES_TO_UPDATE
        = Arrays.asList("name", "startDay", "duration.quantity", "duration.unit", "repetitions");

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Period period;
    private Period originalPeriod;
    private AmendmentService amendmentService;
    private TemplateService templateService;

    public EditPeriodCommand(Period period, AmendmentService amendmentService, TemplateService templateService) {
        this.originalPeriod = period;
        this.period = (Period) period.transientClone();
        this.amendmentService = amendmentService;
        this.templateService = templateService;
    }

    public Period getPeriod() {
        return period;
    }

    public Arm getArm() {
        return templateService.findParent(getPeriod());
    }

    public void apply() {
        updateRevWithChangedProperties();
        removeInvalidPlannedEvents();
    }

    private void updateRevWithChangedProperties() {
        BeanWrapper originalWrapped = new BeanWrapperImpl(originalPeriod);
        BeanWrapper newWrapped = new BeanWrapperImpl(period);
        for (String prop : PROPERTIES_TO_UPDATE) {
            Object oldV = originalWrapped.getPropertyValue(prop);
            Object newV = newWrapped.getPropertyValue(prop);
            if (!ComparisonTools.nullSafeEquals(oldV, newV)) {
                amendmentService.updateDevelopmentAmendment(originalPeriod,
                    PropertyChange.create(prop, oldV, newV));
            }
        }
    }

    private void removeInvalidPlannedEvents() {
        // look for PlannedEvents that are now invalid
        DayRange peDayRange = new DefaultDayRange(1, getPeriod().getDuration().getDays());
        for (PlannedActivity event : originalPeriod.getPlannedEvents()) {
            if (!peDayRange.containsDay(event.getDay())) {
                amendmentService.updateDevelopmentAmendment(originalPeriod, Remove.create(event));
            }
        }
    }
}
