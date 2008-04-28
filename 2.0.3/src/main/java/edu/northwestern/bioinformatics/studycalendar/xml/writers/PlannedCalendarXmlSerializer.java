package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class PlannedCalendarXmlSerializer extends AbstractStudyCalendarXmlSerializer<PlannedCalendar> {
    public static final String PLANNED_CALENDAR = "planned-calendar";
    private PlannedCalendarDao plannedCalendarDao;
    private boolean serializeEpoch;
    private Study study;

    public PlannedCalendarXmlSerializer() {
        serializeEpoch = false;
    }

    @Override
    public Element createElement(PlannedCalendar cal) {
        Element element = element(PLANNED_CALENDAR);
        element.addAttribute(ID, cal.getGridId());

        if (serializeEpoch) {
            EpochXmlSerializer serializer = getEpochSerializer();

            for (Epoch epoch : cal.getEpochs()) {
                element.add(serializer.createElement(epoch));
            }
        }

        return element;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public PlannedCalendar readElement(Element element) {
        if (!element.getName().equals(PLANNED_CALENDAR)) {
            return null;
        }

        String key = element.attributeValue(ID);
        PlannedCalendar cal = key == null ? null : plannedCalendarDao.getByGridId(key);
        if (cal == null) {
            cal = new PlannedCalendar();
            cal.setGridId(key);

            EpochXmlSerializer serializer = getEpochSerializer();
            
            if (serializer != null) {
                for (Element elt : (List<Element>) element.elements()) {
                    cal.addEpoch((Epoch) serializer.readElement(elt));
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
