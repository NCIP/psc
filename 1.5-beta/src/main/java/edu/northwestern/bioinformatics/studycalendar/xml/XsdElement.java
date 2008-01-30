package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;

/**
 * Enum for the names of elements defined in psc.xsd.
 *
 * @author Rhett Sutphin
 */
public enum XsdElement {
    ACTIVITY,
    ACTIVITY_SOURCES("sources"),
    ACTIVITY_SOURCE("source")
    ;

    private String elementName;

    XsdElement() {
        this(null);
    }

    XsdElement(String elementName) {
        this.elementName = elementName == null ? name().toLowerCase() : elementName;
    }

    public String xmlName() {
        return elementName;
    }

    public Element create() {
        QName qNode = QName.get(xmlName(), AbstractStudyCalendarXmlSerializer.PSC_NS);
        return DocumentHelper.createElement(qNode);
    }
}
