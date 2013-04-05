/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class ActivityPropertyXmlSerializer  extends AbstractStudyCalendarXmlSerializer<ActivityProperty> {

    @Override
    public Element createElement(ActivityProperty ap) {
        Element apElt = XsdElement.ACTIVITY_PROPERTY.create();
        XsdAttribute.ACTIVITY_PROPERTY_NAMESPACE.addTo(apElt,ap.getNamespace());
        XsdAttribute.PROPERTY_NAME.addTo(apElt,ap.getName());
        if(ap.getValue()!=null)
            XsdAttribute.PROPERTY_VALUE.addTo(apElt,ap.getValue());
        return apElt;
    }

    @Override
    public ActivityProperty readElement(Element element) {
        ActivityProperty activityProperty = new ActivityProperty();
        activityProperty.setNamespace(XsdAttribute.ACTIVITY_PROPERTY_NAMESPACE.from(element));
        activityProperty.setName(XsdAttribute.PROPERTY_NAME.from(element));
        if(XsdAttribute.PROPERTY_VALUE.from(element)!=null)
            activityProperty.setValue(XsdAttribute.PROPERTY_VALUE.from(element));
        return activityProperty;
    }
}
