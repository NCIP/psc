package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Methods to ease the creation of domain objects for testing.
 *
 * @see edu.northwestern.bioinformatics.studycalendar.domain.test.ServicedFixtures
 * @author Rhett Sutphin
 */
public class Fixtures {
    private static final Logger log = LoggerFactory.getLogger(Fixtures.class);
    public static final ActivityType DEFAULT_ACTIVITY_TYPE = createActivityType("LAB_TEST");
    public static final Source DEFAULT_ACTIVITY_SOURCE = createNamedInstance("Fixtures Source", Source.class);

    public static <T extends DomainObject> T setId(Integer id, T target) {
        target.setId(id);
        return target;
    }

    public static Study assignIds(Study study) {
        return assignIds(study, 0);
    }

    public static Study assignIds(Study study, int start) {
        setIds(start, study);
        assignIds(study.getPlannedCalendar(), start + 1);
        return study;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public static <T extends MutableDomainObject> T assignIds(T node, int start) {
        int i = start;
        setIds(i, node);
        if (node instanceof Parent) {
            for (Object child : ((Parent) node).getChildren()) {
                i++;
                assignIds((MutableDomainObject) child, i * 50);
            }
        }
        return node;
    }

    public static <T extends MutableDomainObject> T setIds(int base, T target) {
        target.setId(base);
        target.setGridId("GRID-" + base);
        return target;
    }

    public static <T extends GridIdentifiable> T setGridId(String gridId, T target) {
        target.setGridId(gridId);
        return target;
    }

    /**
     * Creates a template similar to the default one PSC builds when "new template" is selected,
     * except that it has already been released.
     */
    public static Study createReleasedTemplate() {
        Study study = createInDevelopmentTemplate();
        study.setAmendment(study.getDevelopmentAmendment());
        study.setDevelopmentAmendment(null);
        for (Change change : study.getAmendment().getDeltas().get(0).getChanges()) {
            study.getPlannedCalendar().addEpoch(
                (Epoch) ((Add) change).getChild()
            );
        }
        return study;
    }

    /**
     * Creates a template similar to the default one PSC builds when "new template" is selected in the GUI.
     */
    public static Study createInDevelopmentTemplate(String ident) {
        Study study = new Study();
        study.setAssignedIdentifier(ident);
        PlannedCalendar cal = new PlannedCalendar();
        study.setPlannedCalendar(cal);
        study.setDevelopmentAmendment(new Amendment());
        study.getDevelopmentAmendment().addDelta(
            Delta.createDeltaFor(cal,
                Add.create(Epoch.create("Treatment", "A", "B", "C"), 0),
                Add.create(Epoch.create("Follow up"), 1)
            )
        );
        return study;
    }

    public static Study createInDevelopmentTemplate() {
        return createInDevelopmentTemplate(null);
    }

    public static Period createPeriod(int startDay, int dayCount, int repetitions) {
        return createPeriod(null, startDay, dayCount, repetitions);
    }

    public static Period createPeriod(String name, int startDay, int dayCount, int repetitions) {
        return createPeriod(name, startDay, Duration.Unit.day, dayCount, repetitions);
    }

    public static Period createPeriod(String name, int startDay, Duration.Unit dUnit, int dQuantity, int repetitions) {
        Period p = new Period();
        p.setName(name);
        p.setStartDay(startDay);
        p.getDuration().setUnit(dUnit);
        p.getDuration().setQuantity(dQuantity);
        p.setRepetitions(repetitions);
        return p;
    }

    public static PlannedActivity createPlannedActivity(Activity activity, int day) {
        PlannedActivity event = new PlannedActivity();
        event.setActivity(activity);
        event.setDay(day);
        return event;
    }

    public static PlannedActivity createPlannedActivity(Activity activity, int day, int weight) {
        PlannedActivity event = createPlannedActivity(activity, day);
        event.setWeight(weight);
        return event;
    }


    public static PlannedActivity createPlannedActivity(String activityName, int day) {
        Activity activity = createActivity(activityName);
        return createPlannedActivity(activity, day);
    }

    public static PlannedActivity createPlannedActivity(String activityName, int day, String details) {
        PlannedActivity event = createPlannedActivity(activityName, day);
        event.setDetails(details);
        return event;
    }

    public static PlannedActivity createPlannedActivity(String activityName, int day, String details, String condition) {
        PlannedActivity event = createPlannedActivity(activityName, day, details);
        event.setCondition(condition);
        return event;
    }

    public static PlannedActivity createPlannedActivity(String activityName, int day, int weight) {
        Activity activity = createActivity(activityName);
        return createPlannedActivity(activity, day, weight);
    }

    public static void labelPlannedActivity(PlannedActivity pa, String... labels) {
        labelPlannedActivity(pa, null, labels);
    }

    public static void labelPlannedActivity(PlannedActivity pa, Integer repetitionNumber, String... labels) {
        for (String label : labels) {
            PlannedActivityLabel paLabel = new PlannedActivityLabel();
            paLabel.setLabel(label);
            paLabel.setRepetitionNumber(repetitionNumber);
            pa.addPlannedActivityLabel(paLabel);
        }
    }

    public static PlannedActivityLabel createPlannedActivityLabel(String label) {
        PlannedActivityLabel pal = new PlannedActivityLabel();
        pal.setLabel(label);
        return pal;
    }

    public static PlannedActivityLabel createPlannedActivityLabel(String label, Integer repetitionNumber) {
        PlannedActivityLabel pal = createPlannedActivityLabel(label);
        pal.setRepetitionNumber(repetitionNumber);
        return pal;
    }

    public static PlannedActivityLabel createPlannedActivityLabel(PlannedActivity activity, String label, Integer repetitionNumber) {
        PlannedActivityLabel paLabel = createPlannedActivityLabel(label, repetitionNumber);
        paLabel.setPlannedActivity(activity);
        return paLabel;
    }

    public static Study createSingleEpochStudy(String studyName, String epochName, String... studySegmentNames) {
        Study study = createNamedInstance(studyName, Study.class);
        study.setPlannedCalendar(new PlannedCalendar());
        study.getPlannedCalendar().addEpoch(Epoch.create(epochName, studySegmentNames));
        return study;
    }

    public static StudySite createStudySite(Study study, Site site) {
        StudySite studySite = new StudySite();
        if (study != null) study.addStudySite(studySite);
        if (site != null) site.addStudySite(studySite);
        return studySite;
    }

    public static Subject createSubject(String firstName, String lastName) {
        Subject p = new Subject();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        return p;
    }

    public static Subject createSubject(String personId, String firstName, String lastName, Date birthDate) {
        return createSubject(personId, firstName, lastName, birthDate, Gender.MALE);
    }

    public static Subject createSubject(String personId, String firstName, String lastName, Date birthDate, Gender gender) {
        Subject subject = createSubject(firstName, lastName);
        subject.setPersonId(personId);
        subject.setDateOfBirth(birthDate);
        subject.setGender(gender);
        return subject;
    }

    public static StudySubjectAssignment createAssignment(Study study, Site site, Subject subject) {
        return createAssignment(createStudySite(study, site), subject);
    }

    public static StudySubjectAssignment createAssignment(StudySite ss, Subject subject) {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setScheduledCalendar(new ScheduledCalendar());
        if (subject != null) subject.addAssignment(assignment);
        ss.addStudySubjectAssignment(assignment);
        return assignment;
    }

    public static ScheduledStudySegment createScheduledStudySegment(StudySegment studySegment) {
        ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();
        scheduledStudySegment.setStudySegment(studySegment);
        scheduledStudySegment.setStartDay(1);
        scheduledStudySegment.setStartDate(DateTools.createDate(1997, Calendar.JANUARY, 12));
        return scheduledStudySegment;
    }

    public static ScheduledStudySegment createScheduledStudySegment(Date startDate, int length) {
        Period period = createPeriod(1, length, 1);
        StudySegment segment = createNamedInstance("Fixture", StudySegment.class);
        segment.addPeriod(period);
        ScheduledStudySegment scheduled = createScheduledStudySegment(segment);
        scheduled.setStartDate(startDate);
        new ScheduledCalendar().addStudySegment(scheduled);
        return scheduled;
    }

    public static ScheduledActivity createScheduledActivity(
        String activityName, int year, int month, int day, ScheduledActivityState state
    ) {
        ScheduledActivity scheduled = createScheduledActivity(activityName, year, month, day);
        scheduled.changeState(state);
        return scheduled;
    }

    public static ScheduledActivity createScheduledActivity(
        PlannedActivity planned, int year, int month, int day, ScheduledActivityState state
    ) {
        ScheduledActivity scheduledActivity = createScheduledActivity(planned, year, month, day);
        scheduledActivity.changeState(state);
        return scheduledActivity;
    }

    public static ScheduledActivity createScheduledActivity(String activityName, int year, int month, int day) {
        PlannedActivity baseEvent = createPlannedActivity(activityName, 0);
        return createScheduledActivity(baseEvent, year, month, day);
    }

    public static ScheduledActivity createScheduledActivity(PlannedActivity planned, int year, int month, int day) {
        ScheduledActivity event = new ScheduledActivity();
        event.setPlannedActivity(planned);
        event.setActivity(planned.getActivity());
        event.setIdealDate(DateUtils.createDate(year, month, day - 2));
        event.changeState(new Scheduled(null, DateUtils.createDate(year, month, day)));
        return event;
    }

    public static ScheduledActivity createScheduledActivityWithLabels(PlannedActivity planned, int year, int month, int day) {
        ScheduledActivity event = new ScheduledActivity();
        event.setPlannedActivity(planned);
        event.setActivity(planned.getActivity());
        event.setRepetitionNumber(0);
        event.setLabels(planned.getLabels());
        event.setIdealDate(DateUtils.createDate(year, month, day - 2));
        event.changeState(new Scheduled(null, DateUtils.createDate(year, month, day)));
        return event;
    }

    public static ScheduledActivity createConditionalEvent(String activityName, int year, int month, int day) {
        PlannedActivity baseEvent = createPlannedActivity(activityName, 0);
        baseEvent.setCondition("Details");
        return createScheduledActivity(baseEvent, year, month, day, new Conditional());
    }

    public static Source createSource(final String name) {
        return createNamedInstance(name, Source.class);
    }

    public static Activity createActivity(String name) {
        return createActivity(name, DEFAULT_ACTIVITY_TYPE);
    }

    public static ActivityType createActivityType(final String name) {
        return createNamedInstance(name, ActivityType.class);
    }

    public static Activity createActivity(String name, ActivityType type) {
        return createActivity(name, name, DEFAULT_ACTIVITY_SOURCE, type);
    }

    public static Activity createActivity(String name, String code, Source source, ActivityType type) {
        Activity activity = createNamedInstance(name, Activity.class);
        activity.setId(name.hashCode());
        activity.setCode(code);
        activity.setType(type);
        if (source != null) {
            source.addActivity(activity);
        }
        return activity;
    }

    public static List<ActivityProperty> createActivityProperty(Activity activity, String namespace, String templateName, String templateValue, String textName, String textValue) {
        List<ActivityProperty> properties = new ArrayList<ActivityProperty>();
        ActivityProperty activityProperty = new ActivityProperty();
        ActivityProperty activityProperty1 = new ActivityProperty();
        activityProperty.setNamespace(namespace);
        activityProperty.setName(templateName);
        activityProperty.setValue(templateValue);
        activityProperty1.setNamespace(namespace);
        activityProperty1.setName(textName);
        activityProperty1.setValue(textValue);
        activityProperty.setActivity(activity);
        activityProperty1.setActivity(activity);
        properties.add(activityProperty);
        properties.add(activityProperty1);
        return properties;
    }

    public static List<ActivityProperty> createActivityProperty(Activity activity, String namespace, String templateName, String templateValue) {
        List<ActivityProperty> properties = new ArrayList<ActivityProperty>();
        ActivityProperty activityProperty = new ActivityProperty();
        activityProperty.setNamespace(namespace);
        activityProperty.setName(templateName);
        activityProperty.setValue(templateValue);
        activityProperty.setActivity(activity);
        properties.add(activityProperty);
        return properties;
    }

    public static ActivityProperty createSingleActivityProperty(Activity activity, String namespace, String templateName, String templateValue) {
        ActivityProperty activityProperty = new ActivityProperty();
        activityProperty.setNamespace(namespace);
        activityProperty.setName(templateName);
        activityProperty.setValue(templateValue);
        activityProperty.setActivity(activity);
        return activityProperty;
    }

    public static Activity createActivity(String name, String code, Source source, ActivityType type, String description) {
        Activity activity = createActivity(name, code, source, type);
        activity.setDescription(description);
        return activity;
    }

    public static void addEvents(ScheduledStudySegment scheduledStudySegment, ScheduledActivity... events) {
        for (ScheduledActivity event : events) {
            scheduledStudySegment.addEvent(event);
        }
    }

    public static Site createSite(String name) {
        return createSite(name, null);
    }

    public static Site createSite(String name, String assignedIdentifier) {
        Site site = createNamedInstance(name, Site.class);
        site.setAssignedIdentifier(assignedIdentifier);
        return site;
    }

    public static void setUserRoles(User user, Role... roles) {
        user.clearUserRoles();
        for (Role role : roles) {
            UserRole userRole = new UserRole(user, role);
            user.getUserRoles().add(userRole);
        }
    }

    public static User createUser(String name, Role... roles) {
        return createUser(null, name, null, true, roles);
    }

    public static User createUser(Integer id, String name, Long csmUserId, boolean activeFlag, Role... roles) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setCsmUserId(csmUserId);
        user.setActiveFlag(activeFlag);
        setUserRoles(user, roles);
        return user;
    }

    public static UserRole createUserRole(User user, Role role, Site... sites) {
        UserRole userRole = new UserRole(user, role, sites);
        user.addUserRole(userRole);
        return userRole;
    }

    public static ProtectionGroup createProtectionGroup(Long aId, String aName) {
        ProtectionGroup myProtectionGroup = new ProtectionGroup();
        myProtectionGroup.setProtectionGroupName(aName);
        myProtectionGroup.setProtectionGroupId(aId);
        return myProtectionGroup;
    }

    public static <T extends Named> T createNamedInstance(String name, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            instance.setName(name);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException("Creating domain obj of class " + clazz.getName() + " failed", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Creating domain obj of class " + clazz.getName() + " failed", e);
        }
    }

    public static Population createPopulation() {
        return createPopulation("P", "People");
    }

    public static Population createPopulation(String abbreviation, String name) {
        Population population = new Population();
        population.setAbbreviation(abbreviation);
        population.setName(name);
        return population;
    }

    public static Amendment createAmendment(String name, Date date) {
        return createAmendment(name, date, true);
    }

    public static Amendment createAmendment(String name, Date date, boolean mandatory) {
        Amendment amendment = new Amendment();
        amendment.setMandatory(mandatory);
        amendment.setName(name);
        amendment.setDate(date);
        amendment.setReleasedDate(date);
        return amendment;
    }

    /**
     * Creates a chain of amendments with the given names, returning the one at the end of the
     * chain (the most recent one).
     */
    public static Amendment createAmendments(String... nameHistory) {
        Amendment current = new Amendment();
        Calendar cal = Calendar.getInstance();
        cal.set(2001, Calendar.JANUARY, 1);
        for (int i = 0; i < nameHistory.length - 1; i++) {
            String name = nameHistory[i];
            current.setName(name);
            current.setDate(cal.getTime());
            Amendment next = new Amendment();
            next.setPreviousAmendment(current);
            current = next;
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        current.setName(nameHistory[nameHistory.length - 1]);
        return current;
    }

    /**
     * Creates a chain of amendments with the given dates, returning the one at the end of the
     * chain (the most recent one).
     */
    public static Amendment createAmendments(Date... dateHistory) {
        Amendment current = new Amendment();
        for (int i = 0; i < dateHistory.length - 1; i++) {
            Date date = dateHistory[i];
            current.setDate(date);
            Amendment next = new Amendment();
            next.setPreviousAmendment(current);
            current = next;
        }
        current.setDate(dateHistory[dateHistory.length - 1]);
        return current;
    }

    public static Add createAddChange(Integer newChildId, Integer index) {
        Add add = new Add();
        add.setChildId(newChildId);
        add.setIndex(index);
        return add;
    }

    public static BlackoutDate createBlackoutDate(int year, int month, int day, String description, Site site) {
        SpecificDateBlackout blackoutDate = new SpecificDateBlackout();
        blackoutDate.setYear(year);
        blackoutDate.setMonth(month);
        blackoutDate.setDay(day);
        blackoutDate.setDescription(description);
        blackoutDate.setSite(site);
        return blackoutDate;
    }

    // static class
    protected Fixtures() { }
}
