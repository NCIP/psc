/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class PlannedActivityLabelXmlSerializer
    extends AbstractStudyCalendarXmlSerializer<PlannedActivityLabel>
{

    @Override
    public Element createElement(PlannedActivityLabel paLabel) {
        Element paLabelElt = XsdElement.PLANNED_ACTIVITY_LABEL.create();
        XsdAttribute.LABEL_ID.addTo(paLabelElt, paLabel.getGridId());
        XsdAttribute.LABEL_NAME.addTo(paLabelElt, paLabel.getLabel());
        if (paLabel.getRepetitionNumber() != null) {
            XsdAttribute.LABEL_REP_NUM.addTo(paLabelElt, paLabel.getRepetitionNumber());
        }
        return paLabelElt;
    }

    @Override
    public PlannedActivityLabel readElement(Element element) {
        PlannedActivityLabel label = new PlannedActivityLabel();
        label.setLabel(XsdAttribute.LABEL_NAME.from(element));
        if (XsdAttribute.LABEL_REP_NUM.from(element) != null) {
            label.setRepetitionNumber(Integer.parseInt(XsdAttribute.LABEL_REP_NUM.from(element)));
        }
        label.setGridId(XsdAttribute.LABEL_ID.from(element));
        return label;
    }

    public boolean validateElement(PlannedActivityLabel paLabel, Element element) {
        boolean valid = true;
        if (element == null && paLabel == null) {
            return true;
        } else if ((element != null && paLabel == null) || (paLabel != null && element == null)) {
            return false;
        } else if (!StringUtils.equals(paLabel.getLabel(), XsdAttribute.LABEL_NAME.from(element))) {
            valid = false;
        } else if (!StringUtils.equals(String.valueOf(paLabel.getRepetitionNumber()), XsdAttribute.LABEL_REP_NUM.from(element))) {
            valid = false;
        }
        return valid;
    }
}
