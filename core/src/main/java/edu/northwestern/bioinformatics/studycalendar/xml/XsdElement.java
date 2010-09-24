package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import java.util.List;

/**
 * Enum for the names of elements defined in psc.xsd.
 *
 * @author Rhett Sutphin
 */
public enum XsdElement {
    ACTIVITY,
    ACTIVITY_SOURCES("sources"),
    ACTIVITY_SOURCE("source"),
    ACTIVITY_PROPERTY("property"),
    
    REGISTRATIONS,
    REGISTRATION,
    SUBJECT_ASSIGNMENTS,
    SUBJECT_ASSIGNMENT,
    SUBJECT,

    SITES,
    SITE,
    BLACKOUT_DATE,
    BLACKOUT_DATES,

    STUDY_SITE_LINK,
    AMENDMENT_APPROVALS,
    AMENDMENT_APPROVAL,

    STUDIES,
    STUDY,
    STUDY_SNAPSHOT,
    DEVELOPMENT_AMENDMENT,
    AMENDMENT,
    PLANNED_CALENDAR,
    EPOCH,
    STUDY_SEGMENT,
    PERIOD,
    PLANNED_ACTIVITY,
    POPULATION,
    SECONDARY_IDENTIFIER,
    LONG_TITLE,
    SCHEDULED_CALENDARS,
    SCHEDULED_CALENDAR,
    SCHEDULED_STUDY_SEGMENT,
    SCHEDULED_ACTIVITY,
    CURRENT_SCHEDULED_ACTIVITY_STATE,
    PREVIOUS_SCHEDULED_ACTIVITY_STATE,
    SCHEDULED_ACTIVITIES,
    NEXT_SCHEDULED_STUDY_SEGMENT,
    NOTIFICATION, NOTIFICATIONS,
    SOURCES,SOURCE,

    PLANNED_ACTIVITY_LABELS("labels"),
    PLANNED_ACTIVITY_LABEL("label"),

    USER_ROLES("roles"),
    USER_ROLE("role"),
    ROLE_SITES,
    ROLE_STUDIES;

    private String elementName;

    XsdElement() {
        this(null);
    }

    XsdElement(String elementName) {
        this.elementName = elementName == null ? name().replaceAll("_", "-").toLowerCase() : elementName;
    }

    public String xmlName() {
        return elementName;
    }

    public Element create() {
        QName qNode = QName.get(xmlName(), AbstractStudyCalendarXmlSerializer.PSC_NS);
        return DocumentHelper.createElement(qNode);
    }

    public Element from(Element parent) {
        return parent.element(xmlName());
    }

    @SuppressWarnings({"unchecked"})
    public List<Element> allFrom(Element parent) {
        return parent.elements(xmlName());
    }
}
