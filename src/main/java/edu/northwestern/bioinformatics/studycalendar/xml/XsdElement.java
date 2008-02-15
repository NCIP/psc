package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

/**
 * Enum for the names of elements defined in psc.xsd.
 *
 * @author Rhett Sutphin
 */
public enum XsdElement {
    ACTIVITY,
    ACTIVITY_SOURCES("sources"),
    ACTIVITY_SOURCE("source"),
    REGISTRATION,
    REGISTRATIONS,
    SUBJECT_ASSIGNMENTS("subject-assignments"),
    SUBJECT_ASSIGNMENT("subject-assignment"),
    SUBJECT,
    SITE,
    SITES,
    STUDY_SITE_LINK("study-site-link"),
    STUDIES,
    STUDY,
    DEVELOPMENT_AMENDMENT("development-amendment"),
    AMENDMENT,
    BLACKOUT_DATE("blackout-date"),
    BLACKOUT_DATES("blackout-dates"),
    SCHEDULED_CALENDARS("scheduled-calendars"),
    SCHEDULED_CALENDAR("scheduled-calendar"),
    SCHEDULED_STUDY_SEGMENT("scheduled-study-segment"),
    SCHEDULED_ACTIVITY("scheduled-activity"),
    SCHEDULED_ACTIVITY_STATE("scheduled-activity-state");

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
