package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public enum XsdAttributes {
    ACTIVITY_NAME("name"),
    ACTIVITY_CODE("code"),
    ACTIVITY_DESC("description"),
    ACTIVITY_TYPE("type-id"),
    ACTIVITY_SOURCE("source"),

    ACTIVITY_SOURCE_NAME("name")
    ;

    private String attributeName;

    private XsdAttributes(String attrname) {
        this.attributeName = attrname;
    }

    public String from(Element elt) {
        return elt.attributeValue(attributeName);
    }

    public void addTo(Element elt, Object value) {
        elt.addAttribute(attributeName, value == null ? null : value.toString());
    }
}
