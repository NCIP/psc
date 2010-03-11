package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.util.List;

public class PlannedCalendarXmlSerializer extends AbstractStudyCalendarXmlSerializer <PlannedCalendar> {
    public static final String PLANNED_CALENDAR = "planned-calendar";
    private boolean serializeEpoch;
    private Study study;

    public PlannedCalendarXmlSerializer() {
        serializeEpoch = false;
    }

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

    @SuppressWarnings({"unchecked"})
    public PlannedCalendar readElement(Element element) {
        if (!element.getName().equals(PLANNED_CALENDAR)) {
            return null;
        }

        String key = element.attributeValue(ID);
        PlannedCalendar cal = new PlannedCalendar();
        cal.setGridId(key);

        EpochXmlSerializer serializer = getEpochSerializer();
            
        if (serializer != null) {
            for (Element elt : (List<Element>) element.elements()) {
                cal.addEpoch((Epoch) serializer.readElement(elt));
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

    public void setStudy(Study study) {
        this.study = study;
    }
}
