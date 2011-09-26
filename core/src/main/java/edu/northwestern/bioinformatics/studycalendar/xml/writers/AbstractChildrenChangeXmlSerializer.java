package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.BeanFactoryAware;

public abstract class AbstractChildrenChangeXmlSerializer extends AbstractChangeXmlSerializer implements BeanFactoryAware {
    private static final String CHILD_ID = "child-id";

    protected void addAdditionalAttributes(final Change change, Element element) {
        Child child = ((ChildrenChange) change).getChild();
        if (child != null) {
           element.addAttribute(CHILD_ID, child.getGridId());
        }
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        String childId = element.attributeValue(CHILD_ID);
        Element child = getElementById(element, childId);
        StudyCalendarXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createXmlSerializer(child);
        Child node = (Child)serializer.readElement(child);
        ((ChildrenChange) change).setChild(node);
    }

    @Override
    public StringBuffer validateElement(Change change, Element eChange) {
        if (change == null && eChange == null) {
            return new StringBuffer("");
        } else if ((change == null && eChange != null) || (change != null && eChange == null)) {
            return new StringBuffer("either change or element is null");
        }

        StringBuffer errorMessageStringBuffer = super.validateElement(change, eChange);
        ChildrenChange childrenChange = (ChildrenChange) change;

        if (eChange.attributeValue(CHILD_ID) != null) {
            String childId = childrenChange.getChild() != null ? childrenChange.getChild().getGridId() : null;
            if (!StringUtils.equals(childId, eChange.attributeValue(CHILD_ID))) {
                errorMessageStringBuffer.append(String.format("childId  is different. expected:%s , found (in imported document) :%s \n", childId, eChange.attributeValue(CHILD_ID)));
            }
        }

        return errorMessageStringBuffer;
    }

}
