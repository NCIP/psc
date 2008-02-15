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

    REGISTRATION_FIRST_STUDY_SEGMENT_ID("first-study-segment-id"),
    REGISTRATION_DATE("date"),
    REGISTRATION_SUBJECT_COORDINATOR_NAME("subject-coordinator-name"),
    REGISTRATION_DESIRED_ASSIGNMENT_ID("desired-assignment-id"),
    ASSIGNMENT_STUDY_NAME("study-name"),
    ASSIGNMENT_SITE_NAME("site-name"),
    SUBJECT_FIRST_NM("first-name"),
    SUBJECT_LAST_NM("last-name"),
    SUBJECT_BIRTH_DATE("birth-date"),
    SUBJECT_PERSON_ID("person-id"),
    ASSIGNMENT_CURRENT_AMENDMENT("current-amendment-key"),
    ASSIGNMENT_SUBJECT_COORD("subject-coordinator-name"),
    ASSIGNMENT_START_DATE("start-date"),
    ASSIGNMENT_END_DATE("end-date"),
    STUDY_SITE_STUDY_NM("study-name"),
    STUDY_SITE_SITE_NM("site-name"),
    SITE_SITE_NM("site-name"),
    SITE_ASSIGNED_IDENTIFIER("assigned-identifier"),
    SUBJECT_GENDER("gender"),
    ASSIGNMENT_ID("id"),
    SCHEDULED_CALENDAR_ASSIGNMENT_ID("assignment-id"),
    SCHEDULED_CALENDAR_ID("id"),
    BLACKOUT_DATE_DESC("description"),
    BLACKOUT_DATE_DAY("day"),
    BLACKOUT_DATE_MONTH("month"),
    BLACKOUT_DATE_SITE_ID("site_id"),
    BLACKOUT_DATE_YEAR("year"),
    BLACKOUT_DATE_DAY_OF_WEEK("day-of-the-week"),
    BLACKOUT_DATE_WEEK_NUMBER("week-number"),
    BLACKOUT_DATE_ID("id"),
    SCHEDULED_STUDY_SEGMENT_START_DATE("start-date"),
    SCHEDULED_STUDY_SEGMENT_START_DAY("start-day"),
    SCHEDULED_ACTIVITY_IDEAL_DATE("ideal-date"),
    SCHEDULED_ACTIVITY_NOTES("notes"),
    SCHEDULED_ACTIVITY_DETAILS("details"),
    SCHEDULED_ACTIVITY_REPITITION_NUMBER("repitition-number"),
    SCHEDULED_ACTIVITY_PLANNED_ACITIVITY_ID("planned-activity-id"),
    SCHEDULED_ACTIVITY_STATE_REASON("reason"),
    SCHEDULED_ACTIVITY_STATE_DATE("date");

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
        if (dateString == null) { return null; }

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
