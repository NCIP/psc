package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

public class ActivityReferenceXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
    @Override
    public Element createElement(Activity a) {
        Element aElt = XsdElement.ACTIVITY_REF.create();
        XsdAttribute.ACTIVITY_CODE.addTo(aElt, a.getCode());
        XsdAttribute.ACTIVITY_SOURCE.addTo(aElt, a.getSource().getNaturalKey());
        return aElt;
    }

    @Override
    public Activity readElement(Element element) {
        return null;
    }
}
