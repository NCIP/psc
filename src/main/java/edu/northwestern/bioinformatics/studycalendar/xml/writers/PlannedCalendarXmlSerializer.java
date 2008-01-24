package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

public class PlannedCalendarXmlSerializer extends PlanTreeNodeXmlSerializer {
    public static final String PLANNED_CALENDAR = "planned-calendar";
    private PlannedCalendarDao plannedCalendarDao;

    public PlannedCalendarXmlSerializer(Study study) {
        super(study);
    }

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedCalendar();
    }

    protected String elementName() {
        return PLANNED_CALENDAR;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return plannedCalendarDao.getByGridId(id);
    }

    protected PlanTreeNodeXmlSerializer getChildSerializer() {
        return new EpochXmlSerializer(study);
    }

    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }
}
