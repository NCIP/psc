package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

public class ActivityReferenceXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
    @Override
    public Element createElement(Activity object) {
        return null;
    }

    @Override
    public Activity readElement(Element element) {
        return null;
    }
}
