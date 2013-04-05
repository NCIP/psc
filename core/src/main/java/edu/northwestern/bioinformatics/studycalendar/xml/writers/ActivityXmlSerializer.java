/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ActivityXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
    private boolean embeddedInSource;
    private ActivityPropertyXmlSerializer activityPropertyXmlSerializer = new ActivityPropertyXmlSerializer();
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
        XsdAttribute.ACTIVITY_TYPE.addTo(aElt, a.getType().getName());
        if (!embeddedInSource && a.getSource() != null) {
            XsdAttribute.ACTIVITY_SOURCE.addTo(aElt, a.getSource().getNaturalKey());
        }
        if (embeddedInSource) {
            for(ActivityProperty ap : a.getProperties()) {
                aElt.add(activityPropertyXmlSerializer.createElement(ap));
            }
        }
        return aElt;
    }

    @Override
    public Activity readElement(Element element) {
        Activity activity = new Activity();
        String nameAttr = XsdAttribute.ACTIVITY_NAME.from(element);
        if (nameAttr.length()==0) {
            throw new StudyCalendarValidationException("Activity name can not be null for activities");
        } else {
            activity.setName(nameAttr);
        }

        String codeAttr = XsdAttribute.ACTIVITY_CODE.from(element);
        if (codeAttr.length()==0) {
            throw new StudyCalendarValidationException("Activity code can not be null for activities");
        } else {
            activity.setCode(codeAttr);
        }
        activity.setDescription(XsdAttribute.ACTIVITY_DESC.from(element));
        String typeAttr = XsdAttribute.ACTIVITY_TYPE.from(element);
        ActivityType type;
        if (typeAttr ==null) {
            throw new StudyCalendarValidationException("Type is required for activities");
        } else {
            type = new ActivityType();
            type.setName(typeAttr);
            activity.setType(type);
        }

        if (!embeddedInSource) {
            String sourceName = XsdAttribute.ACTIVITY_SOURCE.from(element);
            Source source;
            if (sourceName ==  null) {
               throw new StudyCalendarValidationException("Source is required for activities");
            } else {
               source = new Source();
               source.setName(sourceName);
               activity.setSource(source);
            }
        }

        if (embeddedInSource) {
            for(Element apElt:(List<Element>) element.elements("property")) {
                activity.addProperty(activityPropertyXmlSerializer.readElement(apElt));
            }
        }
        return activity;
    }
    public boolean validateElement(Activity activity, Element element) {
        boolean valid = true;
        if (element == null && activity == null) {
            return true;
        } else if ((element != null && activity == null) || (activity != null && element == null)) {
            return false;
        } else if (!StringUtils.equals(activity.getName(), XsdAttribute.ACTIVITY_NAME.from(element))) {
            valid = false;
        } else if (!StringUtils.equals(activity.getCode(), XsdAttribute.ACTIVITY_CODE.from(element))) {
            valid = false;
        } else if (!StringUtils.equals(activity.getDescription(), XsdAttribute.ACTIVITY_DESC.from(element))) {
            valid = false;
        } else if (!StringUtils.equals(String.valueOf(activity.getType().getName()), XsdAttribute.ACTIVITY_TYPE.from(element))) {
            valid = false;
        }

        if (!embeddedInSource &&!(activity.getSource()==null && XsdAttribute.ACTIVITY_SOURCE.from(element)==null)) {

            if ((activity.getSource() == null && XsdAttribute.ACTIVITY_SOURCE.from(element) != null)
                    || (activity.getSource() != null && XsdAttribute.ACTIVITY_SOURCE.from(element) == null)
                    || (!StringUtils.equals(activity.getSource().getName(), XsdAttribute.ACTIVITY_SOURCE.from(element)))) {
                valid = false;
            }

        }

        return valid;
    }

    ////// Bean setters

    @Required
    public void setActivityPropertyXmlSerializer(ActivityPropertyXmlSerializer activityPropertyXmlSerializer) {
        this.activityPropertyXmlSerializer = activityPropertyXmlSerializer;
    }
}
