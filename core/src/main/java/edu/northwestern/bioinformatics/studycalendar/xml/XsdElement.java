/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PeriodDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedActivityDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PopulationDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudyDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudySegmentDelta;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
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
    ACTIVITY(Activity.class),
    ACTIVITY_SOURCES("sources"),
    ACTIVITY_SOURCE("source", Source.class),
    ACTIVITY_PROPERTY("property"),
    
    REGISTRATIONS,
    REGISTRATION(Registration.class),
    SUBJECT_ASSIGNMENTS,
    SUBJECT_ASSIGNMENT(StudySubjectAssignment.class),
    SUBJECT(Subject.class),
    SUBJECT_PROPERTY("property"),

    SITES,
    SITE(Site.class),
    BLACKOUT_DATE(BlackoutDate.class),
    BLACKOUT_DATES,

    STUDY_SITE_LINK(StudySite.class),
    AMENDMENT_APPROVALS,
    AMENDMENT_APPROVAL(AmendmentApproval.class),

    STUDIES,
    STUDY(Study.class),
    STUDY_SNAPSHOT,
    DEVELOPMENT_AMENDMENT,
    AMENDMENT,
    PLANNED_CALENDAR(PlannedCalendar.class),
    EPOCH(Epoch.class),
    STUDY_SEGMENT(StudySegment.class),
    PERIOD(Period.class),
    PLANNED_ACTIVITY(PlannedActivity.class),
    POPULATION(Population.class),
    SECONDARY_IDENTIFIER(StudySecondaryIdentifier.class),
    LONG_TITLE,
    SCHEDULED_CALENDARS,
    SCHEDULED_CALENDAR(ScheduledCalendar.class),
    SCHEDULED_STUDY_SEGMENT(ScheduledStudySegment.class),
    SCHEDULED_ACTIVITY(ScheduledActivity.class),
    CURRENT_SCHEDULED_ACTIVITY_STATE,
    PREVIOUS_SCHEDULED_ACTIVITY_STATE,
    SCHEDULED_ACTIVITIES,
    NEXT_SCHEDULED_STUDY_SEGMENT,
    NOTIFICATION, NOTIFICATIONS,
    PLANNED_ACTIVITY_LABELS("labels"),
    PLANNED_ACTIVITY_LABEL("label", PlannedActivityLabel.class),

    USER_ROLES("roles"),
    USER_ROLE("role"),
    ROLE_SITES,
    ROLE_STUDIES,
    ACTIVITY_REFERENCE,

    PLANNED_CALENDAR_DELTA(PlannedCalendarDelta.class),
    EPOCH_DELTA(EpochDelta.class),
    STUDY_SEGMENT_DELTA(StudySegmentDelta.class),
    PERIOD_DELTA(PeriodDelta.class),
    PLANNED_ACTIVITY_DELTA(PlannedActivityDelta.class),
    PLANNED_ACTIVITY_LABEL_DELTA("label-delta", PlannedActivityDelta.class),
    POPULATION_DELTA(PopulationDelta.class),
    STUDY_DELTA(StudyDelta.class)
    ;

    private String elementName;
    private Class<?> correspondingClass;

    XsdElement() {
        this(null, null);
    }

    XsdElement(String elementName) {
        this(elementName, null);
    }

    XsdElement(Class<?> correspondingClass) {
        this(null, correspondingClass);
    }

    XsdElement(String elementName, Class<?> correspondingClass) {
        this.elementName = elementName == null ? name().replaceAll("_", "-").toLowerCase() : elementName;
        this.correspondingClass = correspondingClass;
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

    public static XsdElement forElement(Element e) {
        for (XsdElement x : values()) {
            if (e.getName().equals(x.xmlName())) {
                return x;
            }
        }
        throw new IllegalArgumentException(
            String.format("No XsdElement for element %s.", e.getName()));
    }

    public boolean mapsToOneClass() {
        return correspondingClass() != null;
    }

    public Class<?> correspondingClass() {
        return correspondingClass;
    }

    public static XsdElement forCorrespondingClass(Class<?> correspondingClass) {
        for (XsdElement x : values()) {
            if (x.mapsToOneClass() && x.correspondingClass().isAssignableFrom(correspondingClass)) {
                return x;
            }
        }
        throw new IllegalArgumentException(
            String.format("No XsdElement corresponds to %s.", correspondingClass.getName()));
    }
}
