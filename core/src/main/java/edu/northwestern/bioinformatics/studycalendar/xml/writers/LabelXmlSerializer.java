/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import org.dom4j.Element;


/**
 * @author Nataliya Shurupova
 */
public class LabelXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<String> {

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.PLANNED_ACTIVITY_LABELS;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.PLANNED_ACTIVITY_LABEL;
    }

    @Override
    protected Element createElement(String label, boolean inCollection) {
        Element labelElement = rootElement().create();
        XsdAttribute.PLANNED_ACTIVITY_LABEL_NAME.addTo(labelElement, label);
        return labelElement;
    }

    @Override
    //keep it so far, but we are not using it
    public String readElement(Element element) {
        return null;
    }

}
