package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ActivityReferenceXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
    @Override
    public Element createElement(Activity a) {
        Element aElt = XsdElement.ACTIVITY_REFERENCE.create();
        XsdAttribute.ACTIVITY_CODE.addTo(aElt, a.getCode());
        XsdAttribute.ACTIVITY_SOURCE.addTo(aElt, a.getSource().getNaturalKey());
        return aElt;
    }

    @Override
    public Activity readElement(Element element) {
        Activity activity = new Activity();

        String code = XsdAttribute.ACTIVITY_CODE.from(element);
        if (isBlank(code)) {
            throw new StudyCalendarValidationException(String.format("Activity code is required for ", XsdElement.ACTIVITY_REFERENCE));
        } else {
            activity.setCode(code);
        }

        String sourceName = XsdAttribute.ACTIVITY_SOURCE.from(element);
        if (isBlank(sourceName)) {
           throw new StudyCalendarValidationException(String.format("Source is required for ", XsdElement.ACTIVITY_REFERENCE));
        } else {
           Source source = new Source();
           source.setName(sourceName);
           activity.setSource(source);
        }

        return activity;
    }

    public boolean validateElement(Activity activity, Element element) {
        return true;
    }
}
