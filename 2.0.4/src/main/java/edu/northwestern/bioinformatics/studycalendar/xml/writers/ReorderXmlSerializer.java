package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
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
        element.addAttribute(OLD_INDEX, ((Reorder)change).getOldIndex().toString());
        element.addAttribute(NEW_INDEX, ((Reorder)change).getNewIndex().toString());
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        super.setAdditionalProperties(element, change);
        ((Reorder)change).setOldIndex(new Integer(element.attributeValue(OLD_INDEX)));
        ((Reorder)change).setNewIndex(new Integer(element.attributeValue(NEW_INDEX)));
    }
}
