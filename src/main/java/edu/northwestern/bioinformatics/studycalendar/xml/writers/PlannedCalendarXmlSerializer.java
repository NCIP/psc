package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class PlannedCalendarXmlSerializer extends AbstractStudyCalendarXmlSerializer <PlannedCalendar> {
    public static final String PLANNED_CALENDAR = "planned-calendar";
    private PlannedCalendarDao plannedCalendarDao;
    private boolean serializeEpoch;
    private Study study;

    public PlannedCalendarXmlSerializer() {
        serializeEpoch = false;
    }

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedCalendar();
    }


    public Element createElement(PlannedCalendar cal) {
        Element element = element(PLANNED_CALENDAR);
        element.addAttribute(ID, cal.getGridId());

        if (serializeEpoch) {
            EpochXmlSerializer serializer = getEpochSerializer();

            for (Epoch epoch : cal.getEpochs()) {
                Element childElement = serializer.createElement(epoch);
                element.add(childElement);
            }
        }

        return element;
    }

    @SuppressWarnings({"unchecked"})
    public PlannedCalendar readElement(Element element) {
        if (!element.getName().equals(PLANNED_CALENDAR)) {
            return null;
        }

        String key = element.attributeValue(ID);
        PlannedCalendar cal = plannedCalendarDao.getByGridId(key);
        if (cal == null) {
            cal = new PlannedCalendar();
            cal.setGridId(key);

            EpochXmlSerializer childSerializer = getEpochSerializer();
            if (childSerializer != null) {
                for (Element elt : (List<Element>) element.elements()) {
                    Epoch childNode = (Epoch) childSerializer.readElement(elt);
                    cal.addEpoch(childNode);
                }
            }
        }
        return cal;
    }

    protected EpochXmlSerializer getEpochSerializer() {
        EpochXmlSerializer serializer = (EpochXmlSerializer) getBeanFactory().getBean("epochXmlSerializer");
        serializer.setStudy(study);
        return serializer;
    }

    public void setSerializeEpoch(boolean serializeEpoch) {
        this.serializeEpoch = serializeEpoch;
    }

    ////// Bean Setters
    @Required
    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
