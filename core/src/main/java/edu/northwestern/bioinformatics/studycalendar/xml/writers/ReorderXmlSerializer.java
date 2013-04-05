/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

public class ReorderXmlSerializer extends AbstractChildrenChangeXmlSerializer {
    public static final String REORDER = "reorder";
    private static final String OLD_INDEX = "old-index";
    private static final String NEW_INDEX = "new-index";

    protected Change changeInstance() {
        return new Reorder();
    }

    protected String elementName() {
        return REORDER;
    }


    protected void addAdditionalAttributes(final Change change, Element element) {
        super.addAdditionalAttributes(change, element);
        element.addAttribute(OLD_INDEX, ((Reorder) change).getOldIndex().toString());
        element.addAttribute(NEW_INDEX, ((Reorder) change).getNewIndex().toString());
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        super.setAdditionalProperties(element, change);
        ((Reorder) change).setOldIndex(new Integer(element.attributeValue(OLD_INDEX)));
        ((Reorder) change).setNewIndex(new Integer(element.attributeValue(NEW_INDEX)));
    }

    @Override
    public StringBuffer validateElement(Change change, Element eChange) {
        if (change == null && eChange == null) {
            return new StringBuffer("");
        } else if ((change == null && eChange != null) || (change != null && eChange == null)) {
            return new StringBuffer("either change or element is null");
        }
        StringBuffer errorMessageStringBuffer = super.validateElement(change, eChange);


        String expectedOldIndex = StringTools.valueOf(((Reorder) change).getOldIndex());
        String expectedNewIndex = StringTools.valueOf(((Reorder) change).getNewIndex());

        if (!StringUtils.equals(expectedOldIndex, eChange.attributeValue(OLD_INDEX))) {
            errorMessageStringBuffer.append(String.format("old index  is different. expected:%s , found (in imported document) :%s \n", expectedOldIndex,
                    eChange.attributeValue(OLD_INDEX)));
        } else if (!StringUtils.equals(expectedNewIndex, eChange.attributeValue(NEW_INDEX))) {
            errorMessageStringBuffer.append(String.format("new index  is different. expected:%s , found (in imported document) :%s \n", expectedNewIndex,
                    eChange.attributeValue(NEW_INDEX)));
        }

        return errorMessageStringBuffer;
    }
}
