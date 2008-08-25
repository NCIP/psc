package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public class ActivityXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
    private boolean embeddedInSource;

    public ActivityXmlSerializer() {
        this(false);
    }

    public ActivityXmlSerializer(boolean embeddedInSource) {
        this.embeddedInSource = embeddedInSource;
    }

    @Override
    public Element createElement(Activity a) {
        Element aElt = XsdElement.ACTIVITY.create();
        XsdAttribute.ACTIVITY_NAME.addTo(aElt, a.getName());
        XsdAttribute.ACTIVITY_CODE.addTo(aElt, a.getCode());
        XsdAttribute.ACTIVITY_DESC.addTo(aElt, a.getDescription());
        XsdAttribute.ACTIVITY_TYPE.addTo(aElt, a.getType().getId());
        if (!embeddedInSource && a.getSource() != null) {
            XsdAttribute.ACTIVITY_SOURCE.addTo(aElt, a.getSource().getNaturalKey());
        }
        return aElt;
    }

    @Override
    public Activity readElement(Element element) {
        Activity activity = new Activity();
        activity.setName(XsdAttribute.ACTIVITY_NAME.from(element));
        activity.setCode(XsdAttribute.ACTIVITY_CODE.from(element));
        activity.setDescription(XsdAttribute.ACTIVITY_DESC.from(element));
        try {
            String typeAttr = XsdAttribute.ACTIVITY_TYPE.from(element);
            if (typeAttr == null) {
                throw new StudyCalendarValidationException("Type is required for activities");
            }
            int typeId = Integer.parseInt(typeAttr);
            ActivityType type = ActivityType.getById(typeId);
            if (typeAttr == null) {
                throw new StudyCalendarValidationException("Type id %d is not valid", typeId);
            }
            activity.setType(type);
        } catch (NumberFormatException nfe) {
            throw new StudyCalendarValidationException("Type attribute must be an integer", nfe);
        }
        if (!embeddedInSource) {
            Source sourceTemplate = new Source();
            sourceTemplate.setName(XsdAttribute.ACTIVITY_SOURCE.from(element));
            activity.setSource(sourceTemplate);
        }

        return activity;
    }
}
