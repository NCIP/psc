package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Jalpa Patel
 */
public class ActivityPropertyXmlSerializer  extends AbstractStudyCalendarXmlSerializer<ActivityProperty> {

    @Override
    public Element createElement(ActivityProperty ap) {
        Element apElt = XsdElement.ACTIVITY_PROPERTY.create();
        XsdAttribute.ACTIVITY_PROPERTY_NAMESPACE.addTo(apElt,ap.getNamespace());
        XsdAttribute.ACTIVITY_PROPERTY_NAME.addTo(apElt,ap.getName());
        if(ap.getValue()!=null)
            XsdAttribute.ACTIVITY_PROPERTY_VALUE.addTo(apElt,ap.getValue());
        return apElt;
    }

    @Override
    public ActivityProperty readElement(Element element) {
        ActivityProperty activityProperty = new ActivityProperty();
        activityProperty.setNamespace(XsdAttribute.ACTIVITY_PROPERTY_NAMESPACE.from(element));
        activityProperty.setName(XsdAttribute.ACTIVITY_PROPERTY_NAME.from(element));
        if(XsdAttribute.ACTIVITY_PROPERTY_VALUE.from(element)!=null)
            activityProperty.setValue(XsdAttribute.ACTIVITY_PROPERTY_VALUE.from(element));
        return activityProperty;
    }

    public boolean validateElement(ActivityProperty ap, Element element) {
        boolean valid = true;
        if (element == null && ap == null) {
            return true;
        } else if ((element != null && ap == null) || (ap != null && element == null)) {
            return false;
        } else if (!StringUtils.equals(ap.getNamespace(),XsdAttribute.ACTIVITY_PROPERTY_NAMESPACE.from(element))) {
            valid = false;
        }  else if (!StringUtils.equals(ap.getName(),XsdAttribute.ACTIVITY_PROPERTY_NAME.from(element))) {
            valid = false;
        }  else if (!StringUtils.equals(ap.getValue(),XsdAttribute.ACTIVITY_PROPERTY_VALUE.from(element))) {
            valid = false;
        }
        return valid;
    }

}
