package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Element;

/**
 * Enum for the names of attributes defined in psc.xsd.  Enum values should
 * be added for distinct attributes -- i.e., there should be two separate enum
 * values for two attributes named "name" if they are defined separately in the
 * XSD.
 *
 * @author Rhett Sutphin
 */
public enum XsdAttribute {
    ACTIVITY_NAME("name"),
    ACTIVITY_CODE("code"),
    ACTIVITY_DESC("description"),
    ACTIVITY_TYPE("type-id"),
    ACTIVITY_SOURCE("source"),

    ACTIVITY_SOURCE_NAME("name"),

    REGISTRATION_FIRST_STUDY_SEGMENT("first-study-segment"),
    REGISTRATION_DATE("date")
    ;

    private String attributeName;

    private XsdAttribute(String attrname) {
        this.attributeName = attrname;
    }

    public String from(Element elt) {
        return elt.attributeValue(attributeName);
    }

    public void addTo(Element elt, Object value) {
        elt.addAttribute(attributeName, value == null ? null : value.toString());
    }
}
