package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import org.dom4j.Element;

public class PropertyChangeXmlSerializer extends AbstractChangeXmlSerializer {
    private static final String PROPERTY_CHANGE = "property-change";
    private static final String OLD_VALUE = "old-value";
    private static final String NEW_VALUE = "new-value";
    private static final String PROPERTY_NAME="property-name";

    public PropertyChangeXmlSerializer(Study study) {
        super(study);
    }

    protected Change changeInstance() {
        return new PropertyChange();
    }

    protected String elementName() {
        return PROPERTY_CHANGE;
    }


    protected void addAdditionalAttributes(final Change change, Element element) {
        element.addAttribute(PROPERTY_NAME, ((PropertyChange)change).getPropertyName());
        element.addAttribute(OLD_VALUE, ((PropertyChange) change).getOldValue());
        element.addAttribute(NEW_VALUE, ((PropertyChange) change).getNewValue());
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        ((PropertyChange)change).setPropertyName(element.attributeValue(PROPERTY_NAME));
        ((PropertyChange)change).setOldValue(element.attributeValue(OLD_VALUE));
        ((PropertyChange)change).setNewValue(element.attributeValue(NEW_VALUE));
    }
}
