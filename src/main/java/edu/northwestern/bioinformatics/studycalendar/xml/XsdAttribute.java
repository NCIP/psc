package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    REGISTRATION_DATE("date"),
    REGISTRATION_SUBJECT_COORDINATOR_NAME("subject-coordinator-name"),
    REGISTRATION_DESIRED_ASSIGNMENT_ID("desired-assignment-id"),
    ASSIGNMENT_STUDY_NAME("study-name"),
    ASSIGNMENT_SITE_NAME("site-name"),
    SUBJECT_FIRST_NM("first-name"),
    SUBJECT_LAST_NM("last-name"),
    SUBJECT_DATE_OF_BIRTH("date-of-birth"),
    SUBJECT_PERSON_ID("person-id"),
    ASSIGNMENT_CURRENT_AMENDMENT("current-amendment-key"),
    ASSIGNMENT_SUBJECT_COORD("subject-coordinator-name"),
    ASSIGNMENT_START_DATE("start-date"),
    ASSIGNMENT_END_DATE("end-date"),
    STUDY_SITE_STUDY_NM("study-name"),
    STUDY_SITE_SITE_NM("site-name"),
    SITE_SITE_NM("site-name"),
    SITE_ASSIGNED_IDENTIFIER("assigned-identifier");

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private String attributeName;

    private XsdAttribute(String attrname) {
        this.attributeName = attrname;
    }

    public String from(Element elt) {
        return elt.attributeValue(attributeName);
    }

    public Date fromDate(Element elt) {
        String dateString = from(elt);
        try {
            return formatter.parse(dateString);
        } catch(ParseException pe) {
            throw new StudyCalendarValidationException("Problem parsing date %s", dateString);
        }
    }

    public void addTo(Element elt, Object value) {
        elt.addAttribute(attributeName, value == null ? null : value.toString());
    }

    public void addTo(Element elt, Date value) {
        elt.addAttribute(attributeName, value == null ? null : formatter.format(value));
    }
}
