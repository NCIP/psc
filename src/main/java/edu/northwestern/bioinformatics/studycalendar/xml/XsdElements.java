package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;

/**
 * @author Rhett Sutphin
 */
public enum XsdElements {
    ACTIVITY,
    ACTIVITY_SOURCES("sources"),
    ACTIVITY_SOURCE("source")
    ;

    private String elementName;

    XsdElements() {
        this(null);
    }

    XsdElements(String elementName) {
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
