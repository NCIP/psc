package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class ActivityXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
    private boolean embeddedInSource;
    private ActivityTypeDao activityTypeDao;

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
            ActivityType type;
            String typeIdAttr = null;

            if (typeAttr == null) {
                typeIdAttr = XsdAttribute.ACTIVITY_TYPE_ID.from(element);
                if (typeIdAttr == null) {
                    throw new StudyCalendarValidationException("Type or typeId are required for activities");
                }
                int typeId = Integer.parseInt(typeIdAttr);
                type = activityTypeDao.getById(typeId);
                if (type == null) {
                    throw new StudyCalendarValidationException("Type Name is required for activities with unknown id");
                }
            } else {
                type = activityTypeDao.getByName(typeAttr);
                if (type == null) {
                    //means it's a new activity type
                    type = new ActivityType();
                    type.setName(typeAttr);
                    activityTypeDao.save(type);
                }

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
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
