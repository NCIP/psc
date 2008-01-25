package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

public abstract class AbstractChangeXmlSerializer extends AbstractStudyCalendarXmlSerializer<Change> {

    protected abstract Change changeInstance();
    protected abstract String elementName();
    protected abstract void addAdditionalAttributes(final Change change, Element element);

    public Element createElement(Change change) {
        Element element = element(elementName());
        element.addAttribute(ID, change.getGridId());
        addAdditionalAttributes(change, element);
        return element;
    }

    public Change readElement(Element element) {
        throw new UnsupportedOperationException();
    }
}
